val latexClean by tasks.registering(Exec::class) {
    commandLine("latexmk", "-c")
}

tasks.clean {
    delete(fileTree("out/"))
    dependsOn(latexClean)
}
