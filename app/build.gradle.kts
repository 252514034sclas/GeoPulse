import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.geopulse.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.geopulse.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystorePropsFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties()
    if (keystorePropsFile.exists()) {
        FileInputStream(keystorePropsFile).use { stream ->
            keystoreProps.load(stream)
        }
    }

    val releaseStoreFilePath = System.getenv("GEOPULSE_KEYSTORE_FILE")
        ?: keystoreProps.getProperty("storeFile")
        ?: "geopulse-release.jks"
    val releaseStorePassword = System.getenv("GEOPULSE_KEYSTORE_PASSWORD")
        ?: keystoreProps.getProperty("storePassword")
    val releaseKeyAlias = System.getenv("GEOPULSE_KEY_ALIAS")
        ?: keystoreProps.getProperty("keyAlias")
        ?: "geopulse_release_prod"
    val releaseKeyPassword = System.getenv("GEOPULSE_KEY_PASSWORD")
        ?: keystoreProps.getProperty("keyPassword")

    signingConfigs {
        create("release") {
            val isReleaseTask = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
            if (isReleaseTask && (releaseStorePassword.isNullOrBlank() || releaseKeyPassword.isNullOrBlank())) {
                throw GradleException("Release build failed: Release signing credentials are not configured. Provide GEOPULSE_KEYSTORE_PASSWORD / GEOPULSE_KEY_PASSWORD in environment variables or keystore.properties.")
            }
            if (!releaseStorePassword.isNullOrBlank() && !releaseKeyPassword.isNullOrBlank()) {
                storeFile = file(releaseStoreFilePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.maplibre.gl:android-sdk:11.11.0")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
