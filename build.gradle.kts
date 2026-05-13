import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootEnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootEnvSpec

plugins {
    kotlin("multiplatform") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    id("com.android.kotlin.multiplatform.library") version "9.2.1"
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

    sourceSets.all {
        languageSettings.optIn("kotlin.time.ExperimentalTime")
        languageSettings.optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
    }

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
    linuxArm64()
    mingwX64()
    iosArm64 {
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
    iosX64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    tvosArm64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    tvosSimulatorArm64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    watchosArm32 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    watchosArm64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    watchosDeviceArm64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    watchosSimulatorArm64 {
        binaries.framework {
            baseName = "Crossterm"
            xcf.add(this)
        }
    }
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    js {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    swiftExport {
        moduleName = "Crossterm"
        flattenPackage = "io.github.kotlinmania.crossterm"
    }

    android {
        namespace = "io.github.kotlinmania.crossterm"
        compileSdk = 34
        minSdk = 24
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
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

tasks.withType<AbstractTestTask>().configureEach {
    testLogging {
        events(
            TestLogEvent.STARTED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.FAILED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR,
        )
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
    }
}

rootProject.extensions.configure<NodeJsEnvSpec>("kotlinNodeJsSpec") {
    version.set("22.22.2")
}

rootProject.extensions.configure<WasmNodeJsEnvSpec>("kotlinWasmNodeJsSpec") {
    version.set("22.22.2")
}

rootProject.extensions.configure<YarnRootEnvSpec>("kotlinYarnSpec") {
    version.set("1.22.22")
}

rootProject.extensions.configure<WasmYarnRootEnvSpec>("kotlinWasmYarnSpec") {
    version.set("1.22.22")
}

rootProject.extensions.configure<YarnRootExtension>("kotlinYarn") {
    resolution("diff", "8.0.3")
    resolution("**/diff", "8.0.3")
    resolution("serialize-javascript", "7.0.5")
    resolution("**/serialize-javascript", "7.0.5")
    resolution("webpack", "5.106.2")
    resolution("**/webpack", "5.106.2")
    resolution("follow-redirects", "1.16.0")
    resolution("**/follow-redirects", "1.16.0")
    resolution("lodash", "4.18.1")
    resolution("**/lodash", "4.18.1")
    resolution("ajv", "8.20.0")
    resolution("**/ajv", "8.20.0")
    resolution("brace-expansion", "5.0.5")
    resolution("**/brace-expansion", "5.0.5")
    resolution("flatted", "3.4.2")
    resolution("**/flatted", "3.4.2")
    resolution("minimatch", "10.2.5")
    resolution("**/minimatch", "10.2.5")
    resolution("picomatch", "4.0.4")
    resolution("**/picomatch", "4.0.4")
    resolution("qs", "6.15.1")
    resolution("**/qs", "6.15.1")
    resolution("socket.io-parser", "4.2.6")
    resolution("**/socket.io-parser", "4.2.6")
}


val patchedKarmaWebpackPackage = rootProject.layout.projectDirectory.dir("gradle/npm/karma-webpack").asFile.absolutePath.replace("\\", "/")

rootProject.extensions.configure<NodeJsRootExtension>("kotlinNodeJs") {
    versions.webpack.version = "5.106.2"
    versions.webpackCli.version = "7.0.2"
    versions.karma.version = "npm:karma-maintained@6.4.7"
    versions.karmaWebpack.version = "file:$patchedKarmaWebpackPackage"
    versions.mocha.version = "12.0.0-beta-10"
    versions.kotlinWebHelpers.version = "3.1.0"
}

val codeqlCompileJvm = tasks.register("codeqlCompileJvm") {
    description = "Run the MPP Android compile task so CodeQL can trace a Kotlin/JVM compiler invocation."
    group = "verification"
    dependsOn("compileAndroidMain")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), "crossterm-kotlin", version.toString())

    pom {
        name.set("crossterm-kotlin")
        description.set("Kotlin Multiplatform port of crossterm-rs/crossterm - A crossplatform terminal library for manipulating terminals")
        inceptionYear.set("2026")
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

tasks.register("test") {
    group = "verification"
    description =
        "Runs a portable test suite (macOS + JS + WasmJS). Android and non-host native targets are intentionally excluded."

    val defaultTestTasks = listOf(
        "macosArm64Test",
        "jsNodeTest",
        "wasmJsNodeTest",
    )

    dependsOn(defaultTestTasks.mapNotNull { taskName -> tasks.findByName(taskName) })
}
