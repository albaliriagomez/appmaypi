import com.android.build.gradle.internal.cxx.configure.createNativeBuildSystemVariantConfig

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.torrezpillcokevin.nuna"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.torrezpillcokevin.nuna"
        minSdk = 21
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
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation("com.mapbox.maps:android:11.7.1") //libreria para el uso del mapa

    //01 - 11 - 2024
    implementation ("com.squareup.picasso:picasso:2.8") //permite cargar y gestionar imágenes de manera sencilla y eficiente
    implementation ("com.squareup.retrofit2:retrofit:2.11.0") // biblioteca de cliente HTTP para Android para el uso de apis
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0") //convertidor de JSON para Retrofit
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0") // para manejar operaciones asíncronas,


    implementation("androidx.cardview:cardview:1.0.0")

    implementation("androidx.security:security-crypto:1.1.0-alpha06") //para guardar de forma segura





}