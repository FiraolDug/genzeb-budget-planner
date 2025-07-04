plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.genzeb"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.genzeb"
        minSdk = 26
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
    implementation(libs.appcompat)  // Includes androidx.appcompat
    implementation(libs.material)   // Includes com.google.android.material
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.compiler)

    // Exclude listenablefuture from guava if it's included as a transitive dependency
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
