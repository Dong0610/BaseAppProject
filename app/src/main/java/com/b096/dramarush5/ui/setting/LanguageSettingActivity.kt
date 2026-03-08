package com.b096.dramarush5.ui.setting

import android.content.Intent
import com.b096.dramarush5.databinding.ActivitySettingLanguageBinding
import com.b096.dramarush5.ui.language.LanguageItem
import com.b096.dramarush5.ui.language.listLanguage
import com.b096.dramarush5.ui.main.MainActivity
import com.dong.baselib.base.BaseActivity
import com.dong.baselib.base.LocateManager
import com.dong.baselib.extensions.click

class LanguageSettingActivity :
    BaseActivity<ActivitySettingLanguageBinding>(ActivitySettingLanguageBinding::inflate) {
    var currentLang :LanguageItem = listLanguage.toMutableList()[0]
    private val languageAdapter by lazy {
        LanguageSettingAdapter {
            currentLang = it
        }
    }

    override fun backPressed() {
        finish()
    }

    override fun initialize() {
    }

    override fun ActivitySettingLanguageBinding.setData() {
        val listLangCopy = listLanguage.toMutableList()
        currentLang = listLangCopy.find {
            it.code == LocateManager.getPreLanguage(this@LanguageSettingActivity)
        } ?: listLanguage.toMutableList()[0]
        val post =
            listLangCopy.find { it.code == LocateManager.getPreLanguage(this@LanguageSettingActivity) }
                ?.let {
                    listLangCopy.indexOf(it)
                }
        languageAdapter.currentPosition.value = post
        rcvLanguage.adapter = languageAdapter
        languageAdapter.submitList(listLangCopy)
        rcvLanguage.scrollToPosition(post ?: 0)
    }

    override fun ActivitySettingLanguageBinding.onClick() {
        icSaveLang.click {
            restartApp()
        }
        icBack.click {
            backPressed()
        }
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finishAffinity()
    }
}
