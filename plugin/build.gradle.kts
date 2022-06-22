@file:Suppress("SuspiciousCollectionReassignment")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.6.0"
}

repositories {
    mavenCentral()
}
val kotlinVersion: String by rootProject.extra

dependencies {
    api(project(":script"))
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")
    api(project(":lib"))
}

intellij {
    pluginName.set("Confis")
    version.set("2022.1")
    type.set("IC")

    plugins.set(
        listOf(
            "Kotlin",
            "java",
            "org.intellij.plugins.markdown"
        )
    )
    downloadSources.set(true)
}

tasks.runIde {
    autoReloadPlugins.set(true)
    jvmArgs("-Xmx2048m")
}

tasks.buildSearchableOptions {
    enabled = false
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjvm-default=all"
        jvmTarget = "11"
    }
}
configurations {
    all {
        // Allows using project dependencies instead of IDE dependencies during compilation and test running
        resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
    }
}
