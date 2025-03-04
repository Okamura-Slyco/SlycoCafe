import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
//    id("kotlin-kapt")
}

android {
    namespace = "br.com.slyco.slycocafe"
    compileSdk = 34


    defaultConfig {
        applicationId = "br.com.slyco.slycocafe"
        minSdk = 26
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 29
        versionCode = 19
        versionName = "1.19"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    var buildTimestamp = "\"${
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }\""

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "SLYCO_API_URL","\"${System.getenv("SLYCO_API_PRODUCTION")}\"")
            buildConfigField("String", "SLYCO_API_SECRET", "\"${System.getenv("SLYCO_API_SECRET_PRODUCTION")}\"")
            buildConfigField("String", "SLYCO_API_ENVIRONMENT","\"p\"")
            buildConfigField("String", "SLYCO_APP_BUILD_TIMESTAMP",buildTimestamp)
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "SLYCO_API_URL","\"${System.getenv("SLYCO_API_DEVELOPMENT")}\"")
            buildConfigField("String", "SLYCO_API_SECRET","\"${System.getenv("SLYCO_API_SECRET_DEVELOPMENT")}\"")
            buildConfigField("String", "SLYCO_API_ENVIRONMENT","\"s\"")
            buildConfigField("String", "SLYCO_APP_BUILD_TIMESTAMP",buildTimestamp)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.usb.android)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation ("com.google.zxing:core:3.4.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("androidx.core:core-ktx:1.6.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("com.airbnb.android:lottie:6.6.2")
    implementation ("com.amazonaws:aws-android-sdk-iot:2.78.0")
    //implementation(com.clover.sdk:clover-android-sdk:228.3)
    //implementation("androidx.room:room-runtime:2.6.1")
    //kapt("androidx.room:room-compiler:2.6.1")
    //implementation("androidx.room:room-ktx:2.6.1")
}


