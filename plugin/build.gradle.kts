@file:Suppress("SuspiciousCollectionReassignment")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.4.0"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":script"))
    compileOnly(project(":scriptHost"))
    api(project(":lib"))
}

intellij {
    pluginName.set("Confis")
    //version.set("2021.3")
    version.set("IC-213.5744.223")
    type.set("IC")

    plugins.set(
        listOf(
            "Kotlin",
            "java",
            "org.intellij.plugins.markdown"
        )
    )
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjvm-default=all"
    }
}

configurations.all {
    resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
}
