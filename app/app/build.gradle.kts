plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}


android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 36
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
    // AndroidX / Compose (giữ cái bạn đang dùng từ libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Nếu đã có dòng trên từ version catalog thì BỎ dòng này để tránh trùng:
    // implementation("androidx.activity:activity-compose:1.8.2")

    // CameraX (đồng bộ version)
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("androidx.camera:camera-video:1.3.4")

    // TensorFlow Lite (dùng Task Vision là đủ – đã kéo support)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    // (Tuỳ chọn) Nếu không chạy GPU thì BỎ dòng gpu để đỡ kéo thêm phụ thuộc:
    // implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    // BỎ dòng support riêng vì task-vision đã gồm:
    // implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Guava for ListenableFuture
    implementation("com.google.guava:guava:32.1.3-android")
    
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore")
    // Cloudinary for image upload
    implementation("com.cloudinary:cloudinary-android:2.3.1")
    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")
    
    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
