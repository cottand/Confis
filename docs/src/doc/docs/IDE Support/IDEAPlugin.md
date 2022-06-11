# IDEA Plugin

If you wish to write contracts in your computer directly, you can get syntax highlighting support inside IntelliJ IDEA thanks to the Confis IDEA plugin.

## Intelligent Editing

The plugin brings the usual commodities of developing inside an IDE, like syntax highlighting, reporting of errors in-the-fly, and autocompletion for contract syntax (`unless`, `asLongAs`) and for contract variables (circumstances, parties, and action names, for example)

![Syntax highlighting in action](../pics/syntax.png)

## Confis Document Previews

Confis code can be converted to a better-looking [Document](../AgreementsAsDocuments.md). The Document that results from the contract you are writing can be previewed by the Confis Editor provided by the plugin:

![Document Preview](../pics/documentPreviewZoomed.png)
![Confis Editor](../pics/contractRenderedIdea.png)

## Installing the plugin from sources

The IDEA plugin is not currently published to any repositories in binary form.
In order to use it, you must compile it and install it manually.
You only require Java 11 or later installed in your machine.

- Clone the Confis repository
- Run `./gradlew assemble`
- Open your installation of IntelliJ IDEA and select _Settings_ > _Plugins_ > _Install plugin From Disk_
- Select the compiled zip file
`<repositoryRoot>/plugin/build/distributions/Confis-*.zip`
- Restart IntelliJ
- Open any `*.confis.kts` and you can start writing contracts
