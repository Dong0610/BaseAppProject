package com.b096.dramarush5.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import com.ads.control.admob.AppOpenManager
import com.b096.dramarush5.R
import com.b096.dramarush5.ads.wrapper.banner.BannerPlacement
import com.b096.dramarush5.ads.wrapper.interstitial.InterstitialAdManager
import com.b096.dramarush5.app.PreferenceData
import com.b096.dramarush5.app.remoteConfig
import com.b096.dramarush5.base.BaseAppActivity
import com.b096.dramarush5.databinding.ActivityMainBinding
import com.b096.dramarush5.dialog.DialogLoading
import com.b096.dramarush5.dialog.DialogNotiFullScreen
import com.b096.dramarush5.dialog.QuitAppDialog
import com.b096.dramarush5.firebase.Analytics
import com.b096.dramarush5.notilock.ReminderUtils.Companion.reminderUtils
import com.az.inappupdate.AppUpdate
import com.az.inappupdate.AppUpdateManager
import com.dong.baselib.api.getData
import com.dong.baselib.base.BaseActivity
import com.dong.baselib.base.animateNavigate
import org.koin.android.ext.android.inject
import kotlin.getValue

class MainActivity :
    BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate, fullStatus = true) {
    companion object {
        const val IS_OPEN_RECORD = "isOpenRecord"
    }

    override fun backPressed() {
        QuitAppDialog(this@MainActivity) { finishAffinity() }.show()
    }

    private var isShowRequestNotiInSession = false

    private val loadingDialog by lazy {
        DialogLoading(this@MainActivity)
    }

    private val launcherNotification =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                if (!isUseFullScreenIntent()) {
                    showFullScreenDialog()
                } else {
                    reminderUtils.createScheduleLockScreenReminder(this@MainActivity)
                }
            }

        }

    @SuppressLint("ScheduleExactAlarm")
    private val launcherFullScreenIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                reminderUtils.createScheduleLockScreenReminder(this@MainActivity)
            }
        }

    private fun Context.isUseFullScreenIntent(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getNotificationManager().canUseFullScreenIntent()
        } else {
            true
        }
    }

    private fun showFullScreenDialog() {
        DialogNotiFullScreen(this@MainActivity) {
            AppOpenManager.getInstance().disableAdResumeByClickAction()
            requestFullScreenIntent()
        }.show()
    }

    private fun requestFullScreenIntent() {
        AppOpenManager.getInstance().disableAdResumeByClickAction()
        val intent = Intent().apply {
            action = Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
            data = Uri.fromParts("package", packageName, null)
        }
        launcherFullScreenIntent.launch(intent)
    }

    fun Context.getNotificationManager() =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun isGrantedPostNotification(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun checkPostNotification() {
        if (isShowRequestNotiInSession) return
        if (isGrantedPostNotification()) {
            if (!isUseFullScreenIntent()) {
                showFullScreenDialog()
            } else {
                reminderUtils.createScheduleLockScreenReminder(this@MainActivity)
            }
        } else {
            launcherNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun showLoading() {
        loadingDialog.show()
    }

    fun hideLoading() {
        loadingDialog.dismiss()
    }

    override fun initialize() {
        PreferenceData.isFinishFirstFlow = true
        checkPostNotification()
        checkUpdate()
        setupOpenAds()
        InterstitialAdManager.loadInterAll(this@MainActivity)
        Analytics.track("HomeScr_Show")
    }

    private fun setupOpenAds() {
        if (remoteConfig.appOpenAdConfig.enable.not()) return
        val listEnableAds =
            remoteConfig.appOpenAdConfig.listAds.filter { it.enableAd }.map { it.adUnit }
        AppOpenManager.getInstance().init(this.application, listEnableAds)
    }


    override fun ActivityMainBinding.setData() {
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        bottomNav.setOnItemSelectedListener { item, _ ->
            val currentDestinationId = navController.currentDestination?.id
            if (currentDestinationId == item.id) {
                return@setOnItemSelectedListener
            }
            InterstitialAdManager.showInterAll(this@MainActivity) {
                navController.animateNavigate(item.id)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible = when (destination.id) {
                R.id.homeFragment,-> true
                else -> false
            }
            binding.bottomNav.selectItemById(destination.id, triggerListener = false)
        }
        bannerAd.attach(this@MainActivity, BannerPlacement.BANNER_ALL).setAutoReload(
            true,
            remoteConfig.timeReloadBannerMs
        ).requestAd()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun ActivityMainBinding.onClick() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppUpdate.REQ_CODE_VERSION_UPDATE) {
            if (resultCode == RESULT_OK) {
                disableAdsResume()
            } else {
                if (AppUpdateManager.getInstance(this)
                        .getStyleUpdate() == AppUpdateManager.STYLE_FORCE_UPDATE
                ) {
                    disableAdsResume()
                } else {
                    enableAdsResume()
                }
            }
            AppUpdateManager.getInstance(this).onCheckResultUpdate(requestCode, resultCode) {
                if (it) {
                    disableAdsResume()
                }
            }
        }
    }

    private fun checkUpdate() {
        if (!isCheckedUpdate) {
            isCheckedUpdate = true
            AppUpdateManager.getInstance(this).checkUpdateApp(this) {
                if (it) {
                    disableAdsResume()
                }
            }
        }
    }

    private var isCheckedUpdate = false
    private fun enableAdsResume() {
          AppOpenManager.getInstance().enableAppResume()
    }

    private fun disableAdsResume() {
          AppOpenManager.getInstance().disableAppResume()
    }
}
