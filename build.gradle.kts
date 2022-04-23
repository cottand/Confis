import org.jlleitschuh.gradle.ktlint.KtlintPlugin

plugins {
    kotlin("jvm") version "1.6.20"
    idea
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1" apply false
    jacoco
}

allprojects {
    group = "eu.dcotta.confis"

    repositories {
        mavenCentral()
    }
    //jacoco {
    //    toolVersion = "0.8.7"
    //}
}

subprojects {
    apply<KtlintPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        //"implementation"("io.github.microutils:kotlin-logging-jvm:2.1.20")

        val kotestVersion = "5.1.0"
        testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")

        val mockkVerison ="1.12.3"
        testImplementation("io.mockk:mockk:$mockkVerison")


    }
    if ("plugin" !in name) {
        apply<JacocoPlugin>()
        tasks {
            jacocoTestReport {
                reports {
                    xml.required.set(false)
                    html.required.set(false)
                }
                dependsOn(test)
            }
        }
    }
    testing {
        suites {
            val test by getting(JvmTestSuite::class) {
                useJUnitJupiter()
            }
        }
    }

}

val rootCoverageReport by tasks.registering(JacocoReport::class) {
    executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

    subprojects { sourceSets(sourceSets.main.get()) }

    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${buildDir}/reports/jacoco/report.xml"))
        html.required.set(true)
        csv.required.set(false)
    }
}
tasks.check { dependsOn(rootCoverageReport) }

afterEvaluate {
    rootCoverageReport {
        val testingTasks = subprojects.flatMap { it.tasks.withType<JacocoReport>() }
        dependsOn(testingTasks)
    }
}
