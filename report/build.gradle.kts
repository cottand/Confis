val latexClean by tasks.registering(Exec::class) {
    commandLine("latexmk", "-c")
}

tasks.clean {
    val others = fileTree(".").apply {
        include(
            "*.pdf",
            "*.lol",
            "*.synctex.gz",
            "*.markdown.lua",
            "*.markdown.out",
            "*.markdown.in",
            "*.bbl",
            "*-blx.bib",
        )
    }
    delete(fileTree("out/"), fileTree("_minted-report/"), others)
    dependsOn(latexClean)
}
