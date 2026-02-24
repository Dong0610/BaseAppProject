package com.b096.dramarush5.utils

import android.content.Context
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import androidx.annotation.Keep
import androidx.core.net.toUri
import com.b096.dramarush5.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.yield
import kotlinx.parcelize.Parcelize
import java.io.File
import java.text.DecimalFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

interface IFile
@Keep
@Parcelize
data class MediaModel(
    var mediaId: Int = 0,
    var path: String = "",
    var fileSize: Long = 0L,
    var lastModify: Long = 0L
) : IFile, Parcelable

data class FolderSection(
    var header: FolderInfo = FolderInfo(),
    var dataList: List<IFile?> = mutableListOf()
) : IFile
@Parcelize
data class FolderInfo(
    val folderPath: String = "",
    val folderName: String = "",
) : Parcelable

fun MediaModel.folderInfo(): FolderInfo {
    val p = path
    return if (p.startsWith("content://")) {
        FolderInfo(folderPath = "content://", folderName = "Content")
    } else {
        val file = File(p)
        val parent = file.parent ?: ""
        val name = file.parentFile?.name ?: parent.substringAfterLast('/').ifEmpty { "Root" }
        FolderInfo(folderPath = parent, folderName = name)
    }
}

private fun String.isContentUri(): Boolean = startsWith("content://")
private fun String.isFilePath(): Boolean = startsWith("/") || startsWith("file:/")
private fun String.existsOnDevice(context: Context): Boolean {
    return when {
        isFilePath() -> File(this.removePrefix("file://")).exists()
        isContentUri() -> {
            runCatching {
                context.contentResolver.openAssetFileDescriptor(this.toUri(), "r")?.use { true }
            }.getOrDefault(false)
        }
        else -> File(this).exists()
    } == true
}

private data class MediaWrap(
    val media: MediaModel,
    val folder: FolderInfo
)

fun List<MediaModel>.toSectionedByMediaFolder(
    context: Context,
    sortFolderByNameAsc: Boolean = true
): List<FolderSection> {
    if (isEmpty()) return emptyList()
    val existing = asSequence()
        .filter { it.path.existsOnDevice(context) }
        .map { MediaWrap(it, it.folderInfo()) }
        .toList()

    if (existing.isEmpty()) {
        return listOf(
            FolderSection(
                header = FolderInfo("all", context.getString(R.string.all)),
                dataList = listOf<IFile?>(null) // camera sentinel
            )
        )
    }
    val grouped = existing.groupBy { it.folder.folderName.lowercase(Locale.getDefault()) }
    val buckets = if (sortFolderByNameAsc)
        grouped.toList().sortedBy { (key, _) -> key }
    else grouped.toList()
    val out = ArrayList<FolderSection>(buckets.size + 1)
    val allWithNull: List<IFile?> = buildList {
        add(null)
        existing.forEach { add(it.media) } // MediaModel : IFile
    }
    out += FolderSection(
        header = FolderInfo(folderPath = "all", folderName = context.getString(R.string.all)),
        dataList = allWithNull
    )
    for ((_, wraps) in buckets) {
        val info = wraps.first().folder
        out += FolderSection(
            header = FolderInfo(folderPath = info.folderPath, folderName = info.folderName),
            dataList = wraps.map { it.media as IFile } // explicit widen to IFile
        )
    }
    return out
}

object AppUtils {
    val photoExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    private const val YIELD_EVERY = 200

    fun getSdStorageDirectories(context: Context): List<String> {
        val results = mutableListOf<String>()

        context.getExternalFilesDirs(null)?.forEach { file ->
            file?.let {
                val path = file.path.substringBefore("/Android")
                if (Environment.isExternalStorageRemovable(file)) {
                    results.add(path)
                }
            }
        }
        if (results.isEmpty()) {
            val output = runCatching {
                ProcessBuilder()
                    .command("mount | grep /dev/block/vold")
                    .redirectErrorStream(true)
                    .start()
                    .inputStream
                    .bufferedReader()
                    .use { it.readText() }
            }.getOrElse {
                it.printStackTrace()
                ""
            }

            if (output.isNotBlank()) {
                output.lines().forEach { line ->
                    runCatching {
                        val mountPoint = line.split(" ")[2]
                        results.add(mountPoint)
                    }.onFailure { it.printStackTrace() }
                }
            }
        }
        return results
    }

    fun scanMediaImageByMediaStore(
        context: Context,
        minFileSize: Long = 1L,
        maxFileSize: Long = 20L * 1024 * 1024
    ): Flow<MediaModel> = flow {
        val cr = context.contentResolver
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE
        )
        val selection = "${MediaStore.Images.Media.SIZE} BETWEEN ? AND ?"
        val selectionArgs = arrayOf(
            minFileSize.toString(),
            maxFileSize.toString()
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        var visited = 0

        cr.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idxId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val idxData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val idxDateModified =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val idxSize = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                currentCoroutineContext().ensureActive()
                val id = cursor.getLong(idxId)
                val path = cursor.getString(idxData) ?: continue
                val lastModifiedSec = cursor.getLong(idxDateModified)
                val size = cursor.getLong(idxSize)
                // Check extension
                val file = File(path)
                val ext = file.extension.lowercase(Locale.ROOT)
                if (ext !in photoExtensions) continue

                if (!file.exists() || !file.isFile || !file.canRead()) {
                    continue
                }

                emit(
                    MediaModel(
                        mediaId = id.toInt(),
                        path = path,
                        lastModify = lastModifiedSec * 1000L,
                        fileSize = size
                    )
                )

                visited++
                if (visited % YIELD_EVERY == 0) {
                    yield()
                }
            }
        }
    }.onEach { delay(1) }.flowOn(Dispatchers.IO)

    private fun shouldSkipDir(dir: File): Boolean {
        val name = dir.name
        if (name.startsWith(".")) return true
        val p = dir.absolutePath.replace('\\', '/')
        if (p.contains("/Android/", ignoreCase = true)) return true
        return false
    }

    private fun File.isValidPhoto(): Boolean {
        val ext = extension.lowercase(Locale.ROOT)
        return ext in photoExtensions && length() > 0
    }

    private fun isSymlink(file: File): Boolean = try {
        file.canonicalPath != file.absolutePath
    } catch (_: Throwable) {
        false
    }

    fun formatSize(size: Long): String {
        if (size <= 0) return ""
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / 1024.toDouble().pow(digitGroups.toDouble())
        ) + " " + units[digitGroups]
    }

    fun formatMilliseconds(milliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun formatSeconds(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
