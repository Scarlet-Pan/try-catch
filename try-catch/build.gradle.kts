import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    signing
}

group = "io.github.scarlet-pan"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
    jvmToolchain(11)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "try-catch", version.toString())

    pom {
        name = "try-catch"
        description = "A coroutine-friendly, type-safe try-catch-style wrapper for handling exception."
        inceptionYear = "2025"
        url = "https://github.com/scarlet-pan/try-catch"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "Scarlet-Pan"
                name = "Scarlet Pan"
                email = "scarletpan@qq.com"
            }
        }
        scm {
            url = "https://github.com/scarlet-pan/try-catch"
            connection = "scm:git:https://github.com/scarlet-pan/try-catch.git"
            developerConnection = "scm:git:ssh://git@github.com/scarlet-pan/try-catch.git"
        }
    }
}

signing {
    useInMemoryPgpKeys(
        providers.gradleProperty("signingInMemoryKeyId").orNull,
        providers.gradleProperty("signingInMemoryKey").orNull,
        providers.gradleProperty("signingInMemoryKeyPassword").orNull
    )
    sign(publishing.publications)
}