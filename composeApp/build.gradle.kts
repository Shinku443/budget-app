import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val voyagerVersion = "1.1.0-beta02"
        val koinVersion = "3.5.6"

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("io.ktor:ktor-client-okhttp:2.3.7")
            implementation("androidx.compose.material:material-icons-core:1.7.5")
            implementation("androidx.compose.material:material-icons-extended:1.7.5")
            implementation("io.insert-koin:koin-android:$koinVersion")
            implementation("io.insert-koin:koin-androidx-compose:$koinVersion")
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.android.driver)
            implementation("androidx.work:work-runtime-ktx:2.11.0")
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(projects.shared)

            implementation("io.ktor:ktor-client-core:2.3.7")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

            implementation("io.insert-koin:koin-core:$koinVersion")

            implementation("co.touchlab:kermit:2.0.4")
            implementation("io.github.koalaplot:koalaplot-core:0.10.2")

            implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-koin:$voyagerVersion")

            implementation("androidx.datastore:datastore:1.1.7")
            implementation("androidx.datastore:datastore-preferences:1.1.7")
        }


        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.7")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.projects.shinku443.budgetapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.projects.shinku443.budgetapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
