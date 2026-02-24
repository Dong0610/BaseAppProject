package com.b096.dramarush5.ui.language

import com.b096.dramarush5.R
import com.b096.dramarush5.databinding.ActivityLangApplyBinding
import com.b096.dramarush5.firebase.Analytics
import com.dong.baselib.base.BaseActivity
import com.b096.dramarush5.ui.onboarding.OnboardingActivity
import com.dong.baselib.base.LocateManager
import com.dong.baselib.extensions.click
import com.dong.baselib.extensions.gone
import com.dong.baselib.extensions.load
import com.dong.baselib.extensions.rotate
import com.dong.baselib.extensions.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LangApplyActivity :
    BaseActivity<ActivityLangApplyBinding>(ActivityLangApplyBinding::inflate) {
    override fun backPressed() {
        LocateManager.setLocale(
            this@LangApplyActivity, "en"
        )
        finish()
    }

    override fun initialize() {
        LanguageOpenActivity.currentLang.observe(this) {
            binding.tvItemName.text = it?.name ?: "English"
            binding.imgItemFlag.load(it?.flagId ?: R.drawable.ic_flag_uk)
        }
    }

    private val jobLauncher = CoroutineScope(Dispatchers.Main)

    override fun ActivityLangApplyBinding.onClick() {
        selectLanguage.click {
            if (jobLauncher.isActive) {
                jobLauncher.cancel()
            }
            LocateManager.setLocale(
                this@LangApplyActivity,
                LanguageOpenActivity.currentLang.value?.code ?: "en"
            )
            launchActivity<OnboardingActivity>()
            finish()
        }
        Analytics.track("LFOScr4_Show")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (jobLauncher.isActive) {
            jobLauncher.cancel()
        }
    }

    override fun ActivityLangApplyBinding.setData() {
        jobLauncher.launch {
            ivImgLoad.rotate(duration = 1500, loop = true)
            delay(2000)
            llLoading.gone()
            llApplySuccess.visible()
            delay(1000)
            runCatching {
                LocateManager.setLocale(
                    this@LangApplyActivity,
                    LanguageOpenActivity.currentLang.value?.code ?: "en"
                )
                launchActivity<OnboardingActivity>()

                finish()
            }
        }
    }
}