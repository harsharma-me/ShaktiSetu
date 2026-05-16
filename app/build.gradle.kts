plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.shaktisetu"

    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.shaktisetu"
        minSdk = 29
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps3d)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    ksp(libs.room.compiler)
    // Firebase BOM
    implementation(
        platform(
            "com.google.firebase:firebase-bom:33.1.2"
        )
    )

    // Firebase
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // CameraX
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation(libs.guava)

    // Image Loading
    implementation("com.squareup.picasso:picasso:2.8")

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("io.coil-kt:coil:2.7.0")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

}