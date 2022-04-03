plugins {
    kotlin("jvm")
    `java-library`
    idea
}

val kotlinVersion: String by rootProject.extra

dependencies {
    api(project(":lib"))

    implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")
}
