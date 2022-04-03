@file:Suppress("SuspiciousCollectionReassignment")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.4.0"

}

repositories {
    mavenCentral()
}
fun properties(key: String) = project.findProperty(key).toString()

intellij {
    pluginName.set("Confis")
    version.set("2021.3")
    type.set("IC")

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(
        listOf(
            //"com.intellij.modules.platform",
            "org.jetbrains.kotlin",
        )
    )
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjvm-default=enable"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":script"))
}
