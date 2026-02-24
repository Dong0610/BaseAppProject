package com.b096.dramarush5.app


import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object PreferenceData : KoinComponent {
    private val sharedPreference: SharedPreference by inject()
    var languageCode by sharedPreference.string("languageCode", "en")
        private set
    var isFinishFirstFlow by sharedPreference.boolean("isFinishFirstFlow", false)
    var isRatedApp = sharedPreference.observable("isRatedApp", false)
    var countSessionApp by sharedPreference.int("countSessionApp", 0)
    var countSaveSession by sharedPreference.int("countSaveSession", 0)
    var countHomeSession by sharedPreference.int("countHomeSession", 0)
    var countPlayChannelSession by sharedPreference.int("countPlayChannelSession", 0)
    var countAddPlaylistSession by sharedPreference.int("countAddPlaylistSession", 0)

    fun isUfo(): Boolean {
        return countSessionApp == 1
    }
}