import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    kotlin("multiplatform") version "2.3.21"
    id("com.android.kotlin.multiplatform.library") version "9.2.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.kotlinmania"
version = "0.1.4"

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

    compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    val xcf = XCFramework("Crossterm")

    macosArm64 {
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

    swiftExport {
        moduleName = "Crossterm"
        flattenPackage = "io.github.kotlinmania.crossterm"
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("commonMain/src")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("org.jetbrains.kotlinx:atomicfu:0.27.0")
            }
        }

        val commonTest by getting {
            kotlin.srcDir("commonTest/kotlin")
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
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

rootProject.extensions.configure<YarnRootExtension>("kotlinYarn") {
    resolution("diff", "8.0.3")
    resolution("serialize-javascript", "7.0.5")
    resolution("webpack", "5.106.2")
    resolution("follow-redirects", "1.16.0")
    resolution("lodash", "4.18.1")
    resolution("ajv", "8.20.0")
    resolution("brace-expansion", "5.0.5")
    resolution("flatted", "3.4.2")
    resolution("minimatch", "10.2.5")
    resolution("picomatch", "4.0.4")
    resolution("qs", "6.15.1")
    resolution("socket.io-parser", "4.2.6")
}

kotlin {
    android {
        namespace = "io.github.kotlinmania.crossterm"
        compileSdk = 34
        minSdk = 24
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
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
    publishToMavenCentral()
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

// CodeQL's Gradle autobuild invokes `./gradlew testClasses`, which is a
// JVM-convention task that Kotlin Multiplatform projects without a JVM
// target do not provide. Without it, CodeQL aborts with
// `Task 'testClasses' not found in root project` and skips the scan.
// Register an aggregate task that depends on every per-target
// test-compile task (jsTestClasses, wasmJsTestClasses, and the
// compileTestKotlin<Target> tasks for native targets) so the convention
// call resolves.
tasks.register("testClasses") {
    description = "Aggregate test-compile task for CodeQL and other JVM-convention callers."
    group = "verification"
    dependsOn(tasks.matching { other ->
        val n = other.name
        n != "testClasses" &&
            (n.endsWith("TestClasses") || n.startsWith("compileTestKotlin"))
    })
}
