plugins {
    kotlin("jvm")
    // id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    `java-library`
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jeasy:easy-rules-core:4.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
}
