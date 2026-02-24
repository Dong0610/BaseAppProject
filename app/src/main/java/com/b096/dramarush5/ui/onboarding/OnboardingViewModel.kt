package com.b096.dramarush5.ui.onboarding

import androidx.lifecycle.ViewModel
import com.b096.dramarush5.app.PreferenceData
import com.b096.dramarush5.firebase.Analytics
import kotlin.collections.set

class OnboardingViewModel: ViewModel() {
    var isPreloadNativeFull = false
    var isPreloadNativeFeature = false
    var listTrackingScreen = HashMap<Int, Boolean>()
    var listTrackingFullScreen = HashMap<Int, Boolean>()
    var isFirstAutoSkip = false
    fun trackScreenView(isFullScreen: Boolean, position: Int) {
        if (!PreferenceData.isUfo()) return

        val trackingMap = if (isFullScreen) listTrackingFullScreen else listTrackingScreen
        if (trackingMap[position] == true) return
        trackingMap[position] = true
        val eventName = if (isFullScreen) {
            "ufo_native_full_screen_${position + 1}"
        } else {
            "ufo_onboarding_${position + 1}"
        }
        Analytics.track(eventName)
    }
}
