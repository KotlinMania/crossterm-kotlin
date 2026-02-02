import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.3.0"
    id("com.android.kotlin.multiplatform.library") version "8.6.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kotlinmania"
version = "0.1.0"

// Setup Android SDK location and licenses automatically
val sdkDir = file(".android-sdk")
val licensesDir = sdkDir.resolve("licenses")
if (!licensesDir.exists()) licensesDir.mkdirs()
val licenseFile = licensesDir.resolve("android-sdk-license")
if (!licenseFile.exists()) {
    licenseFile.writeText(
        """
        8933bad161af4178b1185d1a37fbf41ea5269c55
        d56f5187479451eabf01fb74abc367c344559d7b
        24333f8a63b6825ea9c5514f83c2829b004d1fee
        """.trimIndent()
    )
}
val localProperties: File? = rootProject.file("local.properties")
if (!localProperties?.exists()!!) {
    localProperties.writeText("sdk.dir=${sdkDir.absolutePath}")
}

repositories {
    mavenCentral()
    google()
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

        // nativeMain is now empty - shared native code would go in nativeMain/src
        // but platform-specific implementations go in posixMain or mingwMain
        val nativeMain by getting {
            kotlin.srcDir("nativeMain/src")
        }

        // desktopPosixMain contains terminal-specific implementations for desktop POSIX (macOS, Linux)
        // This is separate from iOS which doesn't have traditional terminal access
        val desktopPosixMain by creating {
            dependsOn(nativeMain)
            kotlin.srcDir("posixMain/src")
        }

        // macOS and Linux use desktop POSIX terminal code
        val macosMain by getting {
            dependsOn(desktopPosixMain)
            kotlin.srcDir("macosMain/src")
        }
        val linuxMain by getting {
            dependsOn(desktopPosixMain)
            kotlin.srcDir("linuxMain/src")
        }

        // iOS needs its own stubs since it doesn't have terminal access
        val iosMain by getting {
            dependsOn(nativeMain)
            kotlin.srcDir("iosMain/src")
        }

        // mingwMain contains Windows-specific implementations
        val mingwMain by getting {
            kotlin.srcDir("mingwMain/src")
        }

        val jsMain by getting {
            kotlin.srcDir("jsMain/src")
        }

        val wasmJsMain by getting {
            kotlin.srcDir("wasmJsMain/src")
        }

        val androidMain by getting {
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
