package com.b096.dramarush5.app

import android.app.*
import android.content.*
import android.graphics.Color
import android.net.*
import android.util.*
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.*
import com.google.android.play.core.review.ReviewManagerFactory
import androidx.core.net.toUri
import com.b096.dramarush5.R
import com.b096.dramarush5.app.Aso.MAIL_SUPPORT
import com.google.android.material.snackbar.Snackbar
import kotlin.also
import kotlin.runCatching
import kotlin.toString

object Aso {
    const val POLICY_LINK ="https://docs.google.com/document/d/e/2PACX-1vR9cdqgB4xiXr7y5RdGtZ3kJEqaTvZX0vGVm4VDSwE6Q1HYmVYL-8hO81nF9TtUvZCUgoQdsL4a6MfI/pub"

    const val TEAM_SERVICE ="https://docs.google.com/document/d/e/2PACX-1vQtOv_9xkgTpoXNFwMRnp5OO50I7fqzy-6F1opsnqnNf3f2GQbj95nr5l5VICWtSTRnGIihxekqf9rR/pub"

    const val MAIL_SUPPORT = "fwashlaundry@gmail.com"
}

fun Context.openUrl(url: String) {
    runCatching {
        Intent(Intent.ACTION_VIEW, url.toUri()).also {
            this.startActivity(it)
        }
    }
}

fun Context.toastShort(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.toastTop(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).apply {
        setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
        show()
    }
}

fun View.showTopSnackbar(message: String) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    val snackbarView = snackbar.view
    val params = snackbarView.layoutParams
    when (params) {
        is CoordinatorLayout.LayoutParams -> {
            params.gravity = Gravity.TOP
            params.topMargin = 100
            snackbarView.layoutParams = params
        }
        is FrameLayout.LayoutParams -> {
            params.gravity = Gravity.TOP
            params.topMargin = 100
            snackbarView.layoutParams = params
        }
    }
    snackbarView.setBackgroundColor(Color.TRANSPARENT)
    val textView =
        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

    textView.setTextColor(Color.BLACK)                        // text màu đen
    textView.textAlignment = View.TEXT_ALIGNMENT_CENTER       // align center
    textView.gravity = Gravity.CENTER                         // đề phòng
    textView.maxLines = 5                                     // tuỳ chọn
    textView.layoutParams =
        (textView.layoutParams as ViewGroup.LayoutParams).apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }

    snackbar.show()
}

fun Context.toastLong(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Activity.rateApp(onFinish: () -> Unit) {
    val manager = ReviewManagerFactory.create(this)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task: Task<ReviewInfo> ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            val flow =
                manager.launchReviewFlow(this, reviewInfo)
            flow.addOnCompleteListener {
                Log.d("RateApp", "Rate complete")
                onFinish()
            }
        } else {
            Log.e("RateApp", "error: " + task.exception.toString())
            onFinish()
        }
    }
}

fun Activity.sendFeedBack() {
    val emailIntent = Intent(Intent.ACTION_SENDTO)
    emailIntent.data = Uri.parse("mailto:")
    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(MAIL_SUPPORT))
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")

    try {
        startActivity(Intent.createChooser(emailIntent, "Feedback"))
    }
    catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Context.shareApp() {
    val applicationID = this.packageName
    val appPlayStoreUrl = "https://play.google.com/store/apps/details?id=$applicationID"
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name))
    shareIntent.putExtra(
        Intent.EXTRA_TEXT,
        getString(R.string.check_out_this_amazing_app) + appPlayStoreUrl
    )
    val chooserIntent = Intent.createChooser(shareIntent, getString(R.string.share_via))
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(chooserIntent)
}

fun Context.shareValue(value: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name))
    shareIntent.putExtra(
        Intent.EXTRA_TEXT,
        value
    )
    val chooserIntent = Intent.createChooser(shareIntent, getString(R.string.share_via))
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(chooserIntent)
}
