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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    //etc
    // Kotlin lang
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.23")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")

// App compat and UI things
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.android.material:material:1.0.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.0.0")

// Navigation library
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

// CameraX core library
    implementation("androidx.camera:camera-core:1.1.0-beta03")

// CameraX Camera2 extensions
    implementation("androidx.camera:camera-camera2:1.1.0-beta03")

// CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:1.1.0-beta03")

// CameraX View class
    implementation("androidx.camera:camera-view:1.1.0-beta03")

// WindowManager
    implementation("androidx.window:window:1.0.0-alpha09")

// Unit testing
    testImplementation("androidx.test.ext:junit:1.1.3")
    testImplementation("androidx.test:rules:1.4.0")
    testImplementation("androidx.test:runner:1.4.0")
    testImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("org.robolectric:robolectric:4.4")

// Instrumented testing
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.0")

// Import the GPU delegate plugin Library for GPU inference
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.9.0")

}