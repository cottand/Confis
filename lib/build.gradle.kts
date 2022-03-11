plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    `java-library`
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jeasy:easy-rules-core:4.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")

    implementation("org.slf4j:slf4j-log4j12:1.7.36")

    val kotestVersion = "5.1.0"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}
