package com.b096.dramarush5.ui.setting

import android.annotation.SuppressLint
import androidx.activity.result.ActivityResult
import androidx.core.view.isVisible
import com.ads.control.admob.AppOpenManager
import com.ads.control.billing.AppPurchase
import com.b096.dramarush5.app.Aso
import com.b096.dramarush5.app.PreferenceData
import com.b096.dramarush5.app.openUrl
import com.b096.dramarush5.app.remoteConfig
import com.b096.dramarush5.app.shareApp
import com.b096.dramarush5.databinding.ActivitySettingBinding
import com.b096.dramarush5.dialog.RatingDialog
import com.b096.dramarush5.ui.language.listLanguage
import com.dong.baselib.base.BaseActivity
import com.dong.baselib.base.LocateManager
import com.dong.baselib.extensions.click
import com.dong.baselib.extensions.gone

class SettingActivity : BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {
    override fun backPressed() {
        finish()
    }

    override fun initialize() = Unit

    override fun ActivitySettingBinding.onClick() {
        llLanguage.click {
            launchActivity<LanguageSettingActivity>()
        }
        llRateApp.click {
            RatingDialog(
                this@SettingActivity,
                onFinishRate = {
                    PreferenceData.isRatedApp.value = true
                    llRateApp.gone()
                }).show()
        }
        llShareApp.click {
            this@SettingActivity.shareApp()
        }
        llPolicy.click {
            AppOpenManager.getInstance().disableAdResumeByClickAction()
            this@SettingActivity.openUrl(Aso.POLICY_LINK)
        }
        llTeamOfService.click {
            AppOpenManager.getInstance().disableAdResumeByClickAction()

            openUrl(Aso.TEAM_SERVICE)
        }
        icBack.click {
            backPressed()
        }
    }


    @SuppressLint("SetTextI18n")
    override fun ActivitySettingBinding.setData() {
        PreferenceData.isRatedApp.liveData.observe(this@SettingActivity) {
            llRateApp.isVisible = !it
        }
        LocateManager.getPreLanguage(this@SettingActivity)?.let { code ->
            binding.currentLang.text =
                listLanguage.find { it.code == code }?.name?.ifBlank { "English" } ?: "English"
        }
    }
}