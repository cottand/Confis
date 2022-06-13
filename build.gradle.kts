import kotlinx.kover.api.KoverTaskExtension
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

plugins {
    kotlin("jvm") version "1.6.20"
    idea
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1" apply false
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
}

allprojects {
    group = "eu.dcotta.confis"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply<KtlintPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        //"implementation"("io.github.microutils:kotlin-logging-jvm:2.1.20")

        val kotestVersion = "5.2.3"
        testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")

        val mockkVerison = "1.12.3"
        testImplementation("io.mockk:mockk:$mockkVerison")

    }
    tasks.test {
        useJUnitPlatform()
        extensions.configure(KoverTaskExtension::class) {
            isDisabled = false
            excludes = listOf("eu.dcotta.confis.plugin.*")
        }
    }
}
