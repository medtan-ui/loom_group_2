plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.loom_group_2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.loom_group_2"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.firebase.ui.auth)

    // AI
    implementation(libs.firebase.ai)
    implementation(libs.guava)

    // Glide
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    
    // Room
    implementation(libs.room.runtime)
    implementation(libs.firebase.firestore)
    annotationProcessor(libs.room.compiler)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
