plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.21"
    // id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    `java-library`
}

dependencies {

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jeasy:easy-rules-core:4.1.0") {
        exclude(group = "org.slf4j")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    testImplementation("org.slf4j:slf4j-log4j12:1.7.29")
}
