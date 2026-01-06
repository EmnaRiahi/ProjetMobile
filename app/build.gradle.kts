plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.mama"
    compileSdk = 34 // On reste sur la version STABLE (Android 14)

    defaultConfig {
        applicationId = "com.example.mama"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
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

    @Suppress("UnstableApiUsage")
    aaptOptions {
        noCompress("tflite")
    }
}

dependencies {
    // --- BASIQUES (VERSIONS STABILISÉES POUR API 34) ---
    // On force la version 1.6.1 pour éviter le bug de l'API 36
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // On force une version d'activity compatible
    implementation("androidx.activity:activity:1.8.0")

    // --- ROOM (Base de données) ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // --- RETROFIT (API & Chatbot) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // --- LOCALISATION (Urgences) ---
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // --- GRAPHIQUES (Santé - Nessim) ---
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // --- CAMÉRA & SCAN (Nutrition - Rayen) ---
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("com.google.zxing:core:3.5.2")

    // --- MACHINE LEARNING (IA) ---
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // --- TESTS ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}