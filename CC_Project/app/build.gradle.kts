plugins {
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.android.application")
    id("com.google.gms.google-services")

    id ("kotlin-kapt")
}

android {
    namespace = "com.example.cc_project"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.example.cc_project"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }


    buildFeatures {
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
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.database.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.google.android.material:material:1.6.1")
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
    implementation ("com.jakewharton.threetenabp:threetenabp:1.3.0")
    implementation("com.google.android.gms:play-services-auth:20.3.0")

    implementation("com.google.firebase:firebase-firestore")

    implementation ("com.google.android.gms:play-services-auth:20.3.0")
    implementation ("androidx.multidex:multidex:2.0.1")
    implementation ("com.google.firebase:firebase-auth-ktx:21.0.8")

    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-storage-ktx:20.0.2")

    implementation ("androidx.core:core-ktx:1.7.0")
    implementation ("com.google.android.material:material:1.4.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

}