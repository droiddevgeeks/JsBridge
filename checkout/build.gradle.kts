plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.android)
}

extra.apply {
    set("PUBLISH_GROUP_ID", "com.cashfree.pg")
    set("PUBLISH_ARTIFACT_ID", "checkout-bridge")
    set("PUBLISH_VERSION", "1.0.0")
}

apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

android {
    namespace = "com.checkout.bridge"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
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

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.playService)
    implementation(libs.playServiceAuthApiPhone)
}