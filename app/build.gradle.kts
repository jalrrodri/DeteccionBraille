plugins {
//    alias(libs.plugins.androidApplication)
//    alias(libs.plugins.jetbrainsKotlinAndroid)

    //etc
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("de.undercouch.download")
}

android {
    namespace = "com.example.deteccionbraille"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.deteccionbraille"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //etc
        vectorDrawables.useSupportLibrary = true
    }

    //etc
    dataBinding {
        enable = true
    }
    viewBinding {
        enable = true
    }
    buildFeatures{
        dataBinding = true
        viewBinding = true
    }
    androidResources{
        noCompress("tflite")
    }
    namespace = "com.example.deteccionbraille"

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
}

dependencies {

    //root
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    //etc
    implementation(libs.androidx.appcompat)
    // Kotlin lang
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.coroutines.android)

// App compat and UI things
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.localbroadcastmanager)

// Navigation library
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

// CameraX core library
    implementation(libs.camera.core)

// CameraX Camera2 extensions
    implementation(libs.camera.camera2)

// CameraX Lifecycle library
    implementation(libs.camera.lifecycle)

// CameraX View class
    implementation(libs.androidx.camera.view)

// WindowManager
    implementation(libs.androidx.window)

// Unit testing
    testImplementation(libs.androidx.junit)
    testImplementation(libs.rules)
    testImplementation(libs.runner)
    testImplementation(libs.androidx.espresso.core)
    testImplementation(libs.robolectric)

// Instrumented testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.core)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.tensorflow.tensorflow.lite.task.vision)


// Import the GPU delegate plugin Library for GPU inference
    implementation(libs.tensorflow.lite.gpu.delegate.plugin)
    implementation(libs.tensorflow.lite.gpu)

    //Select TensorFlow op(s)
    implementation(libs.tensorflow.lite.select.tf.ops)
    implementation(libs.tensorflow.lite)
}