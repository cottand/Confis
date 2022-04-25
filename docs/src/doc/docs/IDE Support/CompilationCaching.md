# Compilation Caching

In order to speed up intelligent editing and [rendering Document previews](IDEAPlugin.md#confis-document-previews) in the plugin, scripts are cached in the filesystem and in memory.

By default, the cache is in your operating system's temporary directory (aka, `/tmp` in Linux).
In order to choose where scripts are cached, you can set the environment variable `COMPILED_CONFIS_SCRIPTS_CACHE_DIR`.

To disable filesystem caching, you can set `COMPILED_CONFIS_SCRIPTS_CACHE_DIR` to an empty string `""`.

