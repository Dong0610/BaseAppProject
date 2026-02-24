import org.gradle.internal.impldep.com.amazonaws.util.IOUtils.release
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.gmsGoogleServices)
    alias(libs.plugins.firebaseCrashlytics)
    id("kotlin-parcelize")
    id(id = "com.google.devtools.ksp")
    alias(libs.plugins.tripletPlay)
}

android {
    namespace = "com.b096.dramarush5"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.b096.dramarush5"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val formattedDate = SimpleDateFormat("MMM.dd.yyyy").format(Date())
        base.archivesName = "IPTV-v$versionName($versionCode)_${formattedDate}"
    }
    signingConfigs {
        create("release") {
            keyAlias = "as069"
            keyPassword = "vohathi"
            storeFile = rootProject.file("keystore/vohathi.jks")
            storePassword = "vohathi"
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    flavorDimensions.add("version")
    productFlavors {
        create("dev") {
            applicationId = "com.b096.dramarush5"
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("boolean", "build_debug", "true")
        }

        create("product") {
            applicationId = "com.iptv.livetv.smartersplayerlite"
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-5417263955398589~7622527348"
            buildConfigField("boolean", "build_debug", "false")
        }
    }

    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.app.update.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.glide)

    ksp(libs.ksp)
    implementation(libs.gson)
    implementation(libs.koin.android)
    implementation(libs.koin.android.compat)
    implementation(libs.koin.core)
    implementation(libs.lottie)
    implementation(libs.material)
    implementation(libs.sdp.android)
    implementation(libs.shimmer)
    implementation(libs.ssp.android)
    implementation(libs.review.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(project(":lib"))

    implementation(libs.androidx.multidex)
    implementation(libs.inappupdate)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)

    implementation(libs.play.services.ads)
    implementation(libs.azmoduleads)
    implementation(libs.mediation.facebook)
    implementation(libs.mediation.mintegral)
    implementation(libs.mediation.pangle)
    implementation(libs.joda.time)
}
