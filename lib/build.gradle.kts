plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    `java-library`
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("com.deliveredtechnologies:rulebook-core:0.12")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")

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
