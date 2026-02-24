package com.b096.dramarush5.dialog

import androidx.appcompat.app.AppCompatActivity
import com.b096.dramarush5.app.PreferenceData

object RatePopupManager {
    var isShownInSession = false
        private set

    fun showRateIfNeeded(activity: AppCompatActivity, sessionList: List<Int>?, screenCount: Int) {
        if (PreferenceData.isRatedApp.value) return
        if (isShownInSession) return
        if (sessionList.isNullOrEmpty()) return
        if (!sessionList.contains(screenCount)) return
        isShownInSession = true
        RatingDialog(activity, onFinishRate = {
            PreferenceData.isRatedApp.value = true
        }).show()
    }

    fun reset() {
        isShownInSession = false
    }
}
