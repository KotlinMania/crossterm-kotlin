import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("multiplatform") version "2.1.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kotlinmania"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()

    // Native targets - crossterm requires actual terminal I/O
    macosArm64()
    macosX64()
    linuxX64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("commonMain/src")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }

        val commonTest by getting {
            kotlin.srcDir("commonTest/kotlin")
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
            }
        }

        val nativeMain by getting {
            kotlin.srcDir("nativeMain/src")
        }
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
