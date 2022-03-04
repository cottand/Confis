import org.gradle.kotlin.dsl.support.kotlinScriptTypeFor

plugins {
    kotlin("jvm") version "1.6.10" apply false
    idea
}

subprojects {

    repositories {
        mavenCentral()
    }

    dependencies {
        //"implementation"("io.github.microutils:kotlin-logging-jvm:2.1.20")

    }

}

