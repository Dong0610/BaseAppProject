package com.b096.dramarush5.app

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.AzAds
import com.ads.control.application.AdsMultiDexApplication
import com.ads.control.config.AdjustConfig
import com.ads.control.config.AppsflyerConfig
import com.ads.control.config.AzAdConfig
import com.ads.control.config.TaichiConfig
import com.ads.control.util.AppLogger
import com.b096.dramarush5.BuildConfig
import com.b096.dramarush5.ads.HeadUpNotification
import com.b096.dramarush5.ui.splash.SplashActivity
import com.b096.dramarush5.ads.HeadUpNotification.Companion.BACKGROUND_NOTIFICATION_ID
import com.b096.dramarush5.ads.HeadUpNotification.Companion.FOREGROUND_NOTIFICATION_ID
import com.b096.dramarush5.ui.main.MainActivity
import com.b096.dramarush5.ui.splash.NativeSplashActivity
import com.dong.baselib.base.LocateManager
import com.google.android.gms.ads.AdActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.LocalCacheSettings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

abstract class ActivityLifecycleCallbacksImpl : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
}

class ProjectApplication : AdsMultiDexApplication(), LocalCacheSettings {
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val headUpNotification: HeadUpNotification by lazy {
        HeadUpNotification.getInstance()
    }

    companion object {
        @Volatile
        var isAppForeground = false

        var isMainActivityActive = false
        var isFirstOpenMainActivity = true
    }

    override fun onCreate() {
        super.onCreate()
        LocateManager.initDeviceLocate()
        FirebaseApp.initializeApp(this)
        startKoin {
            androidLogger()
            androidContext(this@ProjectApplication)
            modules(listOf(dataModule, databaseModule, viewModelModule))
        }

        registerLifecycleCallback()
        FirebaseApp.initializeApp(this)
        headUpNotification.createHeadUpNotificationChannel(this)
        FirebaseApp.initializeApp(this)
        initAds()
    }

    private fun initAds() {
        val environment = if (BuildConfig.build_debug) {
            AzAdConfig.ENVIRONMENT_DEVELOP
        } else {
            AzAdConfig.ENVIRONMENT_PRODUCTION
        }
        azAdConfig = AzAdConfig(this, environment)
        azAdConfig.adjustConfig = AdjustConfig(true, "d3o3lgi9o7wg")
        azAdConfig.appsflyerConfig = AppsflyerConfig(false, "")
        azAdConfig.listDeviceTest =
            listOf("D73E67518D24524879CFA9AABAAE46CC", "BF14C2D3AC4405FA8D900C423C915020")
        azAdConfig.taichiConfig = TaichiConfig(true)
        AzAds.getInstance().init(this, azAdConfig)
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
        AppOpenManager.getInstance().disableAppResumeWithActivity(NativeSplashActivity::class.java)
        Admob.getInstance().setOpenActivityAfterShowInterAds(true)
        AppLogger.isEnabled = BuildConfig.build_debug
    }

    private fun registerLifecycleCallback() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacksImpl() {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity::class.java == SplashActivity::class.java) {
                    isFirstOpenMainActivity = true
                } else if (activity::class.java != SplashActivity::class.java && activity::class.java != AdActivity::class.java) {
                    isMainActivityActive = true
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                super.onActivityDestroyed(activity)
                if (activity::class.java != SplashActivity::class.java && activity::class.java != AdActivity::class.java) {
                    isMainActivityActive = false
                }
            }

            override fun onActivityResumed(activity: Activity) {
                if (activity::class.java == MainActivity::class.java) {
                    if (isFirstOpenMainActivity && NotificationManagerCompat.from(this@ProjectApplication)
                            .areNotificationsEnabled()
                    ) {
                        isFirstOpenMainActivity = false
                        showForegroundNotification()
                    }
                }
            }
        })

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                cancelNotification()
            }

            override fun onStop(owner: LifecycleOwner) {
                if (isMainActivityActive) {
                    showBackgroundNotification()
                    isFirstOpenMainActivity = true
                }
            }
        })
    }

    private fun showBackgroundNotification() {
        if (NotificationManagerCompat.from(this).areNotificationsEnabled() && !BuildConfig.build_debug) {
            notificationManager.notify(
                BACKGROUND_NOTIFICATION_ID,
                headUpNotification.onQuitNotiOpen(this)
            )
        }
    }

    private fun showForegroundNotification() {
        if (!BuildConfig.build_debug) {
            notificationManager.notify(
                FOREGROUND_NOTIFICATION_ID,
                headUpNotification.onShowHomeOpen(this)
            )
        }

    }

    private fun cancelNotification() {
        headUpNotification.clearNotification(this, BACKGROUND_NOTIFICATION_ID)
    }

}


