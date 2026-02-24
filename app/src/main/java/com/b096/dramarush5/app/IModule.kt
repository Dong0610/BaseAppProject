package com.b096.dramarush5.app

import com.b096.dramarush5.ui.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
}

val dataModule = module {
    singleOf(::SharedPreference)
}

val databaseModule = module {
}
