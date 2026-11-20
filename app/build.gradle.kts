plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.manish.liquidlauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.manish.liquidlauncher"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "3.2.1"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    sourceSets.getByName("main").java.apply {
        when ((project.findProperty("partial") as String?)?.trim()) {
            "1" -> {
                exclude("**/ui/screens/**")
                exclude("**/MainActivity.kt")
            }
            "2" -> {
                exclude("**/ui/screens/HomeScreen.kt")
            }
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    implementation("androidx.activity:activity-compose:1.9.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("io.coil-kt:coil-compose:2.6.0")

    // Explicit AndroidX runtime dependencies
    implementation("androidx.collection:collection-ktx:1.4.0")
    implementation("androidx.arch.core:core-runtime:2.2.0")
    implementation("androidx.concurrent:concurrent-futures:1.1.0")
    implementation("androidx.emoji2:emoji2:1.4.0")
}