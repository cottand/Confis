import org.jlleitschuh.gradle.ktlint.KtlintPlugin

plugins {
    kotlin("jvm") version "1.6.20"
    idea
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1" apply false
     id("org.jetbrains.kotlinx.kover") version "0.5.0"
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

        val kotestVersion = "5.2.3"
        testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")

        val mockkVerison ="1.12.3"
        testImplementation("io.mockk:mockk:$mockkVerison")


    }
    tasks.test {
        useJUnitPlatform()
        extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
            isDisabled = false
            //binaryReportFile.set(file("$buildDir/custom/result.bin"))
            //includes = listOf("com.example.*")
            excludes = listOf("eu.dcotta.confis.plugin*")
        }
    }
    //if ("plugin" !in name) {
    //    apply<JacocoPlugin>()
    //    tasks {
    //        jacocoTestReport {
    //            reports {
    //                xml.required.set(false)
    //                html.required.set(false)
    //            }
    //            dependsOn(test)
    //        }
    //        test {
    //            useJUnitPlatform()
    //        }
    //    }
    //}
}
//val rootCoverageReport by tasks.registering(JacocoReport::class) {
//    executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))
//
//    subprojects { sourceSets(sourceSets.main.get()) }
//
//    reports {
//        xml.required.set(true)
//        xml.outputLocation.set(file("${buildDir}/reports/jacoco/report.xml"))
//        html.required.set(true)
//        csv.required.set(false)
//    }
//}
//tasks.check { dependsOn(rootCoverageReport) }

//afterEvaluate {
//    rootCoverageReport {
//        val testingTasks = subprojects.flatMap { it.tasks.withType<JacocoReport>() }
//        dependsOn(testingTasks)
//    }
//}
//tasks.koverMergedXmlReport {
//    isEnabled = true                        // false to disable report generation
//    xmlReportFile.set(layout.buildDirectory.file("my-merged-report/result.xml"))
//
//    includes = listOf("com.example.*")            // inclusion rules for classes
//    excludes = listOf("com.example.subpackage.*") // exclusion rules for classes
//}
