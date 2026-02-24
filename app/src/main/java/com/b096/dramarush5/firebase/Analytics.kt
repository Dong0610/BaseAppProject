package com.b096.dramarush5.firebase

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

object Analytics {
    private val firebaseAnalytics = Firebase.analytics

    fun track(event: String) {
        firebaseAnalytics.logEvent(event, null)
    }

    fun track(event: String, bundle: Bundle) {
        firebaseAnalytics.logEvent(event, bundle)
    }
}
