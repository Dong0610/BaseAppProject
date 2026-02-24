package com.dong.baselib.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dong.baselib.utils.PermissionUtils.isGranted

/**
 * Permission utility class for checking and requesting permissions
 *
 * Usage in Activity:
 * ```kotlin
 * class MyActivity : BaseActivity<...>(...) {
 *     private val permissionHelper = PermissionHelper(this)
 *
 *     override fun initialize() {
 *         permissionHelper.init()
 *     }
 *
 *     fun requestMicPermission() {
 *         permissionHelper.requestPermission(
 *             permission = Manifest.permission.RECORD_AUDIO,
 *             onGranted = { startRecording() },
 *             onDenied = { showPermissionDeniedMessage() }
 *         )
 *     }
 * }
 * ```
 */
object PermissionUtils {

   
    // region Check Single Permission
   

    /**
     * Check if a permission is granted
     */
    fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if all permissions are granted
     */
    fun areAllGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { isGranted(context, it) }
    }

    /**
     * Check if any permission is granted
     */
    fun isAnyGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.any { isGranted(context, it) }
    }

    // endregion

   
    // region Common Permission Checks
   

    /**
     * Check if notification permission is granted (API 33+)
     * Returns true for API < 33 (no permission required)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isGranted(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
    }

    /**
     * Check if record audio permission is granted
     */
    fun hasRecordAudioPermission(context: Context): Boolean {
        return isGranted(context, Manifest.permission.RECORD_AUDIO)
    }

    /**
     * Check if camera permission is granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        return isGranted(context, Manifest.permission.CAMERA)
    }

    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(context: Context): Boolean {
        return isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
              isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    /**
     * Check if storage read permission is granted
     * Handles different API levels appropriately
     */
    fun hasReadStoragePermission(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // API 33+ uses media permissions
                isGranted(context, Manifest.permission.READ_MEDIA_IMAGES) ||
                      isGranted(context, Manifest.permission.READ_MEDIA_VIDEO) ||
                      isGranted(context, Manifest.permission.READ_MEDIA_AUDIO)
            }
            else -> {
                isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * Check if audio read permission is granted (API 33+)
     */
    fun hasReadAudioPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isGranted(context, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Check if images read permission is granted (API 33+)
     */
    fun hasReadImagesPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Check if video read permission is granted (API 33+)
     */
    fun hasReadVideoPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isGranted(context, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // endregion

   
    // region Get Permission Strings
   

    /**
     * Get notification permission string for current API level
     * Returns null for API < 33
     */
    fun getNotificationPermission(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }
    }

    /**
     * Get audio read permission string for current API level
     */
    fun getReadAudioPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    /**
     * Get images read permission string for current API level
     */
    fun getReadImagesPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    /**
     * Get video read permission string for current API level
     */
    fun getReadVideoPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    // endregion

   
    // region Rationale Check
   

    /**
     * Check if should show permission rationale
     */

    // endregion

   
    // region Open Settings
   

    /**
     * Open app settings page
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Open notification settings (API 26+)
     */
    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // endregion
}

/**
 * Permission helper class for requesting permissions with callbacks
 * Must be initialized in onCreate before use
 *
 * Usage:
 * ```kotlin
 * class MyActivity : ComponentActivity() {
 *     private val permissionHelper = PermissionHelper(this)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         permissionHelper.init()
 *     }
 *
 *     fun requestCamera() {
 *         permissionHelper.requestPermission(
 *             Manifest.permission.CAMERA,
 *             onGranted = { openCamera() },
 *             onDenied = { showDeniedMessage() }
 *         )
 *     }
 * }
 * ```
 */
class PermissionHelper(private val activity: ComponentActivity) {

    private var singlePermissionLauncher: ActivityResultLauncher<String>? = null
    private var multiplePermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    private var onGrantedCallback: (() -> Unit)? = null
    private var onDeniedCallback: (() -> Unit)? = null
    private var onMultipleResultCallback: ((Map<String, Boolean>) -> Unit)? = null

    /**
     * Initialize the permission launchers
     * MUST be called in onCreate before requesting permissions
     */
    fun init() {
        singlePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onGrantedCallback?.invoke()
            } else {
                onDeniedCallback?.invoke()
            }
            clearCallbacks()
        }

        multiplePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            onMultipleResultCallback?.invoke(results)
            clearCallbacks()
        }
    }

    /**
     * Request a single permission
     */
    fun requestPermission(
          permission: String,
          onGranted: () -> Unit = {},
          onDenied: () -> Unit = {}
    ) {
        if (isGranted(activity, permission)) {
            onGranted()
            return
        }

        onGrantedCallback = onGranted
        onDeniedCallback = onDenied
        singlePermissionLauncher?.launch(permission)
    }

    /**
     * Request multiple permissions
     */
    fun requestPermissions(
          permissions: Array<String>,
          onResult: (Map<String, Boolean>) -> Unit
    ) {
        // Filter out already granted permissions
        val notGranted = permissions.filter { !PermissionUtils.isGranted(activity, it) }

        if (notGranted.isEmpty()) {
            // All permissions already granted
            onResult(permissions.associateWith { true })
            return
        }

        onMultipleResultCallback = onResult
        multiplePermissionLauncher?.launch(notGranted.toTypedArray())
    }

    /**
     * Request permission with rationale handling
     */
    fun requestPermissionWithRationale(
          permission: String,
          onGranted: () -> Unit = {},
          onDenied: () -> Unit = {},
          onShowRationale: (() -> Unit)? = null,
          onPermanentlyDenied: (() -> Unit)? = null
    ) {
        when {
            isGranted(activity, permission) -> {
                onGranted()
            }
            activity.shouldShowRationale(permission) -> {
                onShowRationale?.invoke() ?: requestPermission(permission, onGranted, onDenied)
            }
            activity.isPermanentlyDenied(permission) -> {
                onPermanentlyDenied?.invoke() ?: onDenied()
            }
            else -> {
                requestPermission(permission, onGranted, onDenied)
            }
        }
    }

    private fun clearCallbacks() {
        onGrantedCallback = null
        onDeniedCallback = null
        onMultipleResultCallback = null
    }
}

/**
 * Permission helper for Fragments
 */
class FragmentPermissionHelper(private val fragment: Fragment) {

    private var singlePermissionLauncher: ActivityResultLauncher<String>? = null
    private var multiplePermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    private var onGrantedCallback: (() -> Unit)? = null
    private var onDeniedCallback: (() -> Unit)? = null
    private var onMultipleResultCallback: ((Map<String, Boolean>) -> Unit)? = null

    /**
     * Initialize the permission launchers
     * MUST be called in onCreate before requesting permissions
     */
    fun init() {
        singlePermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onGrantedCallback?.invoke()
            } else {
                onDeniedCallback?.invoke()
            }
            clearCallbacks()
        }

        multiplePermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            onMultipleResultCallback?.invoke(results)
            clearCallbacks()
        }
    }

    /**
     * Request a single permission
     */
    fun requestPermission(
          permission: String,
          onGranted: () -> Unit = {},
          onDenied: () -> Unit = {}
    ) {
        val context = fragment.context ?: return

        if (PermissionUtils.isGranted(context, permission)) {
            onGranted()
            return
        }

        onGrantedCallback = onGranted
        onDeniedCallback = onDenied
        singlePermissionLauncher?.launch(permission)
    }

    /**
     * Request multiple permissions
     */
    fun requestPermissions(
          permissions: Array<String>,
          onResult: (Map<String, Boolean>) -> Unit
    ) {
        val context = fragment.context ?: return

        val notGranted = permissions.filter { !PermissionUtils.isGranted(context, it) }

        if (notGranted.isEmpty()) {
            onResult(permissions.associateWith { true })
            return
        }

        onMultipleResultCallback = onResult
        multiplePermissionLauncher?.launch(notGranted.toTypedArray())
    }

    private fun clearCallbacks() {
        onGrantedCallback = null
        onDeniedCallback = null
        onMultipleResultCallback = null
    }
}

// ============================================================================
// Extension Functions
// ============================================================================

/**
 * Check if permission is granted
 */

fun Activity.shouldShowRationale(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}

/**
 * Check if permission is permanently denied (user selected "Don't ask again")
 * Returns true if permission was denied and rationale should not be shown
 */
fun Activity.isPermanentlyDenied(permission: String): Boolean {
    return !isGranted(this, permission) &&
          !shouldShowRationale(permission)
}

fun Context.hasPermission(permission: String): Boolean {
    return PermissionUtils.isGranted(this, permission)
}

/**
 * Check if all permissions are granted
 */
fun Context.hasPermissions(vararg permissions: String): Boolean {
    return PermissionUtils.areAllGranted(this, permissions.toList().toTypedArray())
}

/**
 * Check if notification permission is granted
 */
fun Context.hasNotificationPermission(): Boolean {
    return PermissionUtils.hasNotificationPermission(this)
}

/**
 * Check if record audio permission is granted
 */
fun Context.hasRecordAudioPermission(): Boolean {
    return PermissionUtils.hasRecordAudioPermission(this)
}

/**
 * Check if camera permission is granted
 */
fun Context.hasCameraPermission(): Boolean {
    return PermissionUtils.hasCameraPermission(this)
}

/**
 * Open app settings
 */
fun Context.openAppSettings() {
    PermissionUtils.openAppSettings(this)
}
