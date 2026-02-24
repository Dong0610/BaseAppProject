package com.b096.dramarush5.dialog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.admob.AppOpenManager
import com.b096.dramarush5.R
import com.b096.dramarush5.app.Aso
import com.b096.dramarush5.app.PreferenceData
import com.b096.dramarush5.app.rateApp
import com.b096.dramarush5.app.toastShort
import com.dong.baselib.base.BaseDialog
import com.b096.dramarush5.databinding.DiallogRateAppBinding
import com.dong.baselib.extensions.click
import com.dong.baselib.widget.view.RatingBar
import com.dong.baselib.extensions.gone
import com.dong.baselib.extensions.visible
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

class RatingDialog(val activity: AppCompatActivity, private val onFinishRate: () -> Unit = {}) :
    BaseDialog<DiallogRateAppBinding>(activity, DiallogRateAppBinding::inflate, true) {
    private lateinit var reviewManagerInstance: ReviewManager
    private var reviewInfoInstance: ReviewInfo? = null
    override fun DiallogRateAppBinding.initView() {
        binding.tvRate.setOnClickListener {
            if (binding.ratingBar.getRating() == 0) {
                Toast.makeText(
                    context,
                    context.getText(R.string.please_rate_us),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (binding.ratingBar.getRating() >= 4) {
                requestReview()
                dismiss()
                PreferenceData.isRatedApp.value = true
            } else {
                PreferenceData.isRatedApp.value = true
                AppOpenManager.getInstance().disableAdResumeByClickAction()
                composeEmail()
            }
        }

        reviewManagerInstance = ReviewManagerFactory.create(context)
        reviewManagerInstance.requestReviewFlow().addOnCompleteListener {
            if (it.isSuccessful)
                reviewInfoInstance = it.result
        }
        icCloseApp.click {
            dismiss()
        }
        binding.imgRate.setImageResource(R.drawable.ic_rate_00)
        binding.tvTitle.text = activity.getString(R.string.rating_title_0)
        binding.ratingBar.setRatingChangeListener(object : RatingBar.RatingChangeListener {
            override fun onRatingChanged(rating: Int) {
                when (rating) {
                    0 -> {
                        binding.llFocusItem.visible()
                        binding.imgRate.setImageResource(R.drawable.ic_rate_00)
                        binding.tvTitle.text = activity.getString(R.string.rating_title_0)
                        binding.tvDescription.text =
                            activity.getString(R.string.rating_description_0)
                        binding.tvRate.text = activity.getString(R.string.rate_us)
                    }
                    1 -> {
                        binding.llFocusItem.visible()
                        binding.tvTitle.text = activity.getString(R.string.rating_title_1_2)
                        binding.imgRate.setImageResource(R.drawable.ic_rate_01)
                        binding.tvDescription.text =
                            activity.getString(R.string.rating_description_1_2)
                        binding.tvRate.text = activity.getString(R.string.rate_us)
                    }
                    2 -> {
                        binding.llFocusItem.visible()
                        binding.tvTitle.text = activity.getString(R.string.rating_title_1_2)
                        binding.imgRate.setImageResource(R.drawable.ic_rate_02)
                        binding.tvDescription.text =
                            activity.getString(R.string.rating_description_1_2)
                        binding.tvRate.text = activity.getString(R.string.rate_us)
                    }
                    3 -> {
                        binding.llFocusItem.visible()
                        binding.tvTitle.text = activity.getString(R.string.rating_title_3)
                        binding.imgRate.setImageResource(R.drawable.ic_rate_03)
                        binding.tvDescription.text =
                            activity.getString(R.string.rating_description_3)
                        binding.tvRate.text = activity.getString(R.string.rate_us)
                    }
                    4 -> {
                        binding.llFocusItem.gone()
                        binding.imgRate.setImageResource(R.drawable.ic_rate_04)
                        binding.tvTitle.text = activity.getString(R.string.rating_title_4)
                        binding.tvDescription.text =
                            activity.getString(R.string.rating_description_4)
                        binding.tvRate.text = activity.getString(R.string.rate_on_google_play)
                    }
                    else -> {
                        binding.llFocusItem.gone()
                        binding.tvTitle.text = activity.getString(R.string.rating_title_5)
                        binding.imgRate.setImageResource(R.drawable.ic_rate_05)
                        binding.tvDescription.text =
                            activity.getString(R.string.rating_description_5)
                        binding.tvRate.text = activity.getString(R.string.rate_on_google_play)
                    }
                }
            }
        })
    }

    private fun rateApp() {
        activity.rateApp {
            activity.toastShort(activity.getString(R.string.thanks_for_your_rating))
            onFinishRate()
            dismiss()
        }
    }

    private var onDismiss: () -> Unit = {}

    private fun requestReview() {
        reviewInfoInstance?.let {
            val flow = reviewManagerInstance.launchReviewFlow(activity, it)
            flow.addOnCompleteListener { _ ->
                rateApp()
            }
        } ?: run {
            rateApp()
        }
    }

    fun setDismissRate(onDismiss: () -> Unit) = apply {
        this.onDismiss = onDismiss
    }

    override fun dismiss() {
        super.dismiss()
        onDismiss.invoke()
    }

    private fun composeEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Aso.MAIL_SUPPORT))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Feedback"))
        }
        catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}


