plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.bracketcove.android"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.bracketcove.android"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "1.8" // data objects
    }

}

dependencies {
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.google.firebase:firebase-storage-ktx:20.1.0")

    val simplestack_version = "2.2.5"

    implementation("com.github.Zhuinden.simple-stack-extensions:core-ktx:$simplestack_version")
    implementation("com.github.Zhuinden.simple-stack-extensions:fragments:$simplestack_version")
    implementation("com.github.Zhuinden.simple-stack-extensions:fragments-ktx:$simplestack_version")
    implementation("com.github.Zhuinden.simple-stack-extensions:navigator-ktx:$simplestack_version")
    implementation("com.github.Zhuinden.simple-stack-extensions:services:$simplestack_version")
    implementation("com.github.Zhuinden.simple-stack-extensions:services-ktx:$simplestack_version")

    implementation("com.github.skydoves:landscapist-glide:2.1.2")
    implementation("com.github.bumptech.glide:glide:4.14.2")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("androidx.compose.ui:ui:1.3.3")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha06")
    implementation("androidx.compose.ui:ui-tooling:1.3.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.3")
    implementation("androidx.compose.foundation:foundation:1.3.1")
    implementation("androidx.compose.material:material:1.3.1")
    implementation("androidx.compose.material:material-icons-core:1.3.1")
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    implementation("androidx.activity:activity-compose:1.6.1")

    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha06")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("androidx.core:core:1.9.0")
    implementation("androidx.activity:activity:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.5.5")

    val stream_version = "5.11.10"
    implementation("io.getstream:stream-chat-android-client:$stream_version")
    implementation("io.getstream:stream-chat-android-ui-components:$stream_version")

    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.libraries.places:places:3.0.0")
    implementation("com.google.maps:google-maps-services:2.1.2")
    implementation("com.google.maps.android:android-maps-utils:3.3.0")
    implementation("org.slf4j:slf4j-simple:1.7.25")

    implementation("com.google.firebase:firebase-auth-ktx:21.1.0")
    implementation("com.google.firebase:firebase-database-ktx:20.1.0")
}