
// import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

plugins {
    id("ru.vyarus.mkdocs") version "2.3.0"
}

/*
val generatedDocs = file("src/doc/docs/generated")

tasks.clean {
    delete(generatedDocs)
}

val copyFromDokka by tasks.registering(Copy::class) {
    val dokkaTask = rootProject.tasks.named<DokkaMultiModuleTask>("dokkaHtmlMultiModule")
    dependsOn(dokkaTask)

    from(dokkaTask.get().outputDirectory)
    into(generatedDocs)
}

tasks.withType<MkdocsTask> {
    dependsOn(copyFromDokka)
}
*/

python {
    // modules.removeIf { "mkdocs-material" in it }
    // modules = modules + "mkdocs-material:7.1.6"
}
