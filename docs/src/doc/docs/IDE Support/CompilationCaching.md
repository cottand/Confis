# Compilation Caching

In order to speed up intelligent editing and [rendering Document previews](IDEAPlugin.md#confis-document-previews) in the plugin, scripts are cached in the filesystem and in memory.

By default, the cache is in your operating system's temporary directory (aka, `/tmp` in Linux) if set through the JVM proeprty `java.io.tmpdir`.
In order to choose where scripts are cached, you can set the environment variable `COMPILED_CONFIS_SCRIPTS_CACHE_DIR`.

To disable caching entirely, you can set `COMPILED_CONFIS_SCRIPTS_CACHE_DIR` to an empty string `""`.

