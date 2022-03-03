import org.gradle.kotlin.dsl.support.kotlinScriptTypeFor

plugins {
    kotlin("jvm") version "1.6.10" apply false
    idea
}

allprojects {

    repositories {
        mavenCentral()
    }

}

