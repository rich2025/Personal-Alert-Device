plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.personalalertdevice"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.personalalertdevice"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
    }
}

dependencies {
    // Firebase platform for consistent versions across Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:20.2.0")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    // Firestore
    implementation ("com.google.firebase:firebase-firestore-ktx:24.4.0")

    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    //using this for debugging
    implementation ("com.jakewharton.timber:timber:5.0.1")


    // AndroidX core libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.credentials)
    implementation(libs.googleid)

    // Navigation
    val nav_version = "2.8.3"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Additional Compose dependencies
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.foundation:foundation:1.4.3")
    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("androidx.activity:activity-compose:1.6.1")

    // Google dependencies
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")
    implementation("com.google.accompanist:accompanist-coil:0.15.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.maps.android:maps-compose:2.9.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-compose:2.0.0")
    implementation ("com.github.yalantis:ucrop:2.2.6")

    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
}

// Apply Google services plugin for Firebase
apply(plugin = "com.google.gms.google-services")
