package eu.dcotta.confis.scripting

import eu.dcotta.confis.scripting.eu.dcotta.confis.scripting.ConfisSourceCode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.jvm.impl.KJvmCompiledModuleInMemory
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript

class InMemAndFileSystemCacheTest : StringSpec({

    // only changes source code!
    fun source(i: Int) =
        ConfisSourceCode("here", "dummy1.confis.kts", """ -"hi $i!" """)

    val config = CompilationConfig

    val compiled = List(4) { i ->
        mockk<KJvmCompiledScript>("compiled_$i", relaxed = true) {
            every { compilationConfiguration } returns config
            every { sourceLocationId } returns i.toString()
            coEvery { getClass(any()) } returns ResultWithDiagnostics.Failure()
            every { getCompiledModule() } returns object : KJvmCompiledModuleInMemory {
                override val compilerOutputFiles: Map<String, ByteArray> = emptyMap()
                override fun createClassLoader(baseClassLoader: ClassLoader?): ClassLoader = baseClassLoader
                    ?: this::class.java.classLoader
            }
            every { scriptClassFQName } returns "eu.dcotta.confis.test"
        }
    }

    val temp = System.getProperty("java.io.tmpdir")
        ?: error("Expected a temp fir to be available")
    val confisCacheFolder = "confis.kts.cache"
    val tempDir = File(temp, confisCacheFolder)
        .also(File::mkdirs)

    beforeTest {
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }
        tempDir.mkdirs()
    }

    "stores in fs even when in mem has room" {
        val cache = InMemAndFileSystemCache(tempDir, 2)

        cache.store(compiled[0], source(0), config)

        tempDir.list() shouldHaveSize 1
    }

    "fetch from fs after evicting from fs and keep in mem afterwards" {
        val cache = InMemAndFileSystemCache(tempDir, 1)

        cache.store(compiled[1], source(1), config)
        cache.store(compiled[2], source(2), config)
        cache.store(compiled[3], source(3), config)

        tempDir.list() shouldHaveSize 3

        val loaded = cache.get(source(1), config)

        loaded shouldNotBe compiled[1]
        loaded shouldNotBe null

        // we deleted the cache fs entry but we should still be able to fetch from memory
        tempDir.listFiles().forEach { it.deleteRecursively() }
        cache.get(source(1), config) shouldBe loaded
    }
})
