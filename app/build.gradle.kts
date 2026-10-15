import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

// Machine-local overrides (git-ignored), e.g. to point the dev flavor at your laptop's LAN IP:
//   echno.dev.apiBaseUrl=http://192.168.1.20:8080/
//   echno.dev.authIssuer=http://192.168.1.20:8180/realms/echno-realm
val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use(::load)
    }

fun local(key: String, default: String): String = localProperties.getProperty(key, default)

android {
    namespace = "org.echno"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "org.echno"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // TODO(Q3): replace once the Android public client is registered in Keycloak.
        buildConfigField("String", "AUTH_CLIENT_ID", "\"echno-android\"")
        buildConfigField("String", "AUTH_REDIRECT_URI", "\"org.echno:/oauth2redirect\"")
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            // 10.0.2.2 is how the Android emulator reaches localhost on your computer.
            buildConfigField("String", "API_BASE_URL", "\"${local("echno.dev.apiBaseUrl", "http://10.0.2.2:8080/")}\"")
            buildConfigField(
                "String",
                "AUTH_ISSUER",
                "\"${local("echno.dev.authIssuer", "http://10.0.2.2:8180/realms/echno-realm")}\""
            )
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            // TODO: set real staging URLs.
            buildConfigField("String", "API_BASE_URL", "\"${local("echno.staging.apiBaseUrl", "https://staging.echno.invalid/")}\"")
            buildConfigField(
                "String",
                "AUTH_ISSUER",
                "\"${local("echno.staging.authIssuer", "https://staging-auth.echno.invalid/realms/echno-realm")}\""
            )
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://backend.echno.xyz/\"")
            buildConfigField("String", "AUTH_ISSUER", "\"https://auth.echno.in/realms/echno-realm\"")
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ktlint {
    android.set(true)
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
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
