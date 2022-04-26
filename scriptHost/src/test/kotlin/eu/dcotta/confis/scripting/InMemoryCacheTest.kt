package eu.dcotta.confis.scripting

import eu.dcotta.confis.scripting.CompilationConfig
import eu.dcotta.confis.scripting.InMemoryCache
import eu.dcotta.confis.scripting.eu.dcotta.confis.scripting.ConfisSourceCode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.script.experimental.api.CompiledScript

class InMemoryCacheTest : StringSpec({

    // only changes source code!
    fun source(i: Int) =
        ConfisSourceCode("here", "dummy1.confis.kts", """ -"hi $i!" """)

    val config = CompilationConfig

    val compiled = List(4) { i ->
        mockk<CompiledScript>("compiled_$i") {
            every { compilationConfiguration } returns config
            every { sourceLocationId } returns i.toString()
        }
    }

    val source = source(0)

    "can put and remove stuff form cache" {

        val cache = InMemoryCache(2)

        cache.store(
            compiled[0],
            source,
            config,
        )

        cache.get(source, config) shouldBe compiled[0]
    }

    "latest access entry gets evicted when full" {
        val cache = InMemoryCache(3)

        for (i in 0..2) {
            cache.store(compiled[i], source(i), config)
        }

        for (i in 0..2) {
            cache.get(source(i), config) shouldBe compiled[i]
        }

        // access the first script
        cache.get(source(0), config) shouldBe compiled[0]

        // 4th entry: 2nd script should get evicted, not the 1st
        cache.store(compiled[3], source(3), config)

        cache.get(source(1), config) shouldBe null
    }

    "can dispose" {
        val cache = InMemoryCache(3)

        for (i in 0..2) {
            cache.store(compiled[i], source(i), config)
        }

        cache.dispose()

        for (i in 0..2) {
            cache.get(source(i), config) shouldBe null
        }
    }
})
