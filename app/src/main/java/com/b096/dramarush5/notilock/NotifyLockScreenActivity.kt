package com.b096.dramarush5.notilock

import android.app.KeyguardManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.b096.dramarush5.R
import com.b096.dramarush5.databinding.ActivityNotifyLockBinding
import com.b096.dramarush5.notilock.ReminderUtils.Companion.reminderUtils
import com.b096.dramarush5.ui.splash.SplashActivity
import com.dong.baselib.base.BaseActivity
import kotlin.random.Random

class NotifyLockScreenActivity :
    BaseActivity<ActivityNotifyLockBinding>(ActivityNotifyLockBinding::inflate, true) {
    val listNotiContent = mutableListOf(
        Triple(R.drawable.img_noti_1, R.string.des_1, R.string.cta_1),
        Triple(R.drawable.img_noti_2, R.string.des_2, R.string.cta_2),
        Triple(R.drawable.img_noti_3, R.string.des_3, R.string.cta_3),
        Triple(R.drawable.img_noti_4, R.string.des_4, R.string.cta_4),
        Triple(R.drawable.img_noti_5, R.string.des_5, R.string.cta_5),
    )

    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                      or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
        with(getSystemService(KEYGUARD_SERVICE) as KeyguardManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestDismissKeyguard(this@NotifyLockScreenActivity, null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.run {
            ivNotifyClose.setOnClickListener {
                finish()
            }
            constNotifyLockHalfRoot.setOnClickListener {
                openApp()
            }
            btnOpenApp.setOnClickListener {
                openApp()
            }
            binding.root.setOnClickListener {
                openApp()
            }
        }

        hideNavigationBar()
    }

    private fun openApp() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            null,
            this,
            SplashActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        finish()
        startActivity(intent)
    }

    private fun hideNavigationBar() {
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else @Suppress("DEPRECATION") {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                  or View.SYSTEM_UI_FLAG_FULLSCREEN
                  or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.decorView.systemUiVisibility = flags
            window.decorView.setOnSystemUiVisibilityChangeListener {
                if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    window.decorView.systemUiVisibility = flags
                }
            }
        }
    }

    private fun turnScreenOffAndKeyguardOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        } else {
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                      or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }

    override fun backPressed() {
        finish()
    }

    override fun initialize() = Unit
    override fun ActivityNotifyLockBinding.setData() {
        val radomContent = listNotiContent[Random.nextInt(0, listNotiContent.size - 1)]
        binding.ivTitle.setImageResource(radomContent.first)
        binding.tvTitle.text = getString(radomContent.second)
        binding.btnOpenApp.text = getString(radomContent.third)
        reminderUtils.createScheduleLockScreenReminder(this@NotifyLockScreenActivity)
        turnScreenOnAndKeyguardOff()
    }

    override fun ActivityNotifyLockBinding.onClick() {

    }

    override fun onDestroy() {
        super.onDestroy()
        reminderUtils.isShow = false
        turnScreenOffAndKeyguardOn()
    }

}
