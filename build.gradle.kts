import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    kotlin("multiplatform") version "2.3.20"
    id("com.android.kotlin.multiplatform.library") version "8.6.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kotlinmania"
version = "0.1.3"

val androidSdkDir: String? =
    providers.environmentVariable("ANDROID_SDK_ROOT").orNull
        ?: providers.environmentVariable("ANDROID_HOME").orNull

if (androidSdkDir != null && file(androidSdkDir).exists()) {
    val localProperties = rootProject.file("local.properties")
    if (!localProperties.exists()) {
        val sdkDirPropertyValue = file(androidSdkDir).absolutePath.replace("\\", "/")
        localProperties.writeText("sdk.dir=$sdkDirPropertyValue")
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    val xcf = XCFramework("Crossterm")

    macosArm64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    macosX64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    linuxX64()
    mingwX64()
    iosArm64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    iosX64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    js {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("commonMain/src")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
                implementation("org.jetbrains.kotlinx:atomicfu:0.27.0")
            }
        }

        val commonTest by getting {
            kotlin.srcDir("commonTest/kotlin")
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
            }
        }

        // Shared implementation for platforms that behave like "Other" in Rust cfg blocks.
        // This avoids per-target "stub" actuals.
        val otherMain by creating {
            dependsOn(commonMain)
            kotlin.srcDir("otherMain/src")
        }

        // nativeMain is now empty - shared native code would go in nativeMain/src
        // but platform-specific implementations go in posixMain or mingwMain
        val nativeMain by getting {
            kotlin.srcDir("nativeMain/src")
        }

        // desktopPosixMain: shared desktop code that must compile as metadata.
        // IMPORTANT: do not put any `platform.*` (e.g. `platform.posix`) references here.
        val desktopPosixMain by creating {
            dependsOn(nativeMain)
            kotlin.srcDir("desktopPosixMain/src")
        }

        // IMPORTANT: `macosMain` / `linuxMain` are *shared* between multiple native targets and
        // compile as metadata. Keep them free of `platform.*` references.
        val macosMain by getting {
            dependsOn(desktopPosixMain)
            kotlin.srcDir("macosMain/src")
        }
        val linuxMain by getting {
            dependsOn(desktopPosixMain)
            dependsOn(otherMain)
        }

        // Leaf native targets can contain `platform.posix` code.
        val linuxX64Main by getting {
            kotlin.srcDir("posixMain/src")
            kotlin.srcDir("linuxMain/src")
        }
        val macosArm64Main by getting {
            kotlin.srcDir("posixMain/src")
            kotlin.srcDir("macosArm64Main/src")
        }
        val macosX64Main by getting {
            kotlin.srcDir("posixMain/src")
            kotlin.srcDir("macosX64Main/src")
        }

        val iosMain by getting {
            dependsOn(otherMain)
            dependsOn(desktopPosixMain)
            kotlin.srcDir("iosMain/src")
        }

        // Leaf iOS targets can contain `platform.posix` code via posixMain/src.
        val iosArm64Main by getting {
            kotlin.srcDir("posixMain/src")
            kotlin.srcDir("iosArm64Main/src")
        }
        val iosX64Main by getting {
            kotlin.srcDir("posixMain/src")
            kotlin.srcDir("iosX64Main/src")
        }
        val iosSimulatorArm64Main by getting {
            kotlin.srcDir("posixMain/src")
            kotlin.srcDir("iosSimulatorArm64Main/src")
        }

        // mingwMain contains Windows-specific implementations
        val mingwMain by getting {
            kotlin.srcDir("mingwMain/src")
        }

        val jsMain by getting {
            dependsOn(otherMain)
            kotlin.srcDir("jsMain/src")
        }

        val wasmJsMain by getting {
            dependsOn(otherMain)
            kotlin.srcDir("wasmJsMain/src")
        }

        val androidMain by getting {
            dependsOn(otherMain)
            kotlin.srcDir("androidMain/src")
        }
    }

    jvmToolchain(21)
}

kotlin {
    androidLibrary {
        namespace = "io.github.kotlinmania.crossterm"
        compileSdk = 34
        minSdk = 24
    }
}

val enableIosSimulatorTests =
    providers.gradleProperty("enableIosSimulatorTests").map { it.toBoolean() }.orElse(false)

tasks.withType<KotlinNativeTest>().configureEach {
    if (!enableIosSimulatorTests.get() && (name == "iosX64Test" || name == "iosSimulatorArm64Test")) {
        enabled = false
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "crossterm-kotlin", version.toString())

    pom {
        name.set("crossterm-kotlin")
        description.set("Kotlin Multiplatform terminal manipulation library - port of Rust crossterm")
        inceptionYear.set("2025")
        url.set("https://github.com/KotlinMania/crossterm-kotlin")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("sydneyrenee")
                name.set("Sydney Renee")
                email.set("sydney@solace.ofharmony.ai")
                url.set("https://github.com/sydneyrenee")
            }
        }

        scm {
            url.set("https://github.com/KotlinMania/crossterm-kotlin")
            connection.set("scm:git:git://github.com/KotlinMania/crossterm-kotlin.git")
            developerConnection.set("scm:git:ssh://github.com/KotlinMania/crossterm-kotlin.git")
        }
    }
}
