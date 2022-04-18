package eu.dcotta.confis.render

import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Purpose.Commercial
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain

@Suppress("unused_variable")
class RenderKtTest : StringSpec({

    "agreement contains parties header and lists parties" {
        val a = Agreement {
            val alice by party(named = "Alice Liddell", description = "Alice Pleasance Hargreaves of Westminster")
            val bob by party(description = "Bob the Builder")
            val Charlie by party
        }

        val md = a.renderMarkdown()

        md shouldContain "## 1 - Parties"

        md shouldContain "- 1.1 Alice Pleasance Hargreaves of Westminster (**Alice Liddell**)"
        md shouldContain "- 1.2 Bob the Builder (**bob**)"
        md shouldContain "- 1.3 **Charlie**"
    }

    "agreement contains actions and lists them" {
        val md = Agreement {
            val alice by party
            val pay by action
            val notify by action(description = "Notify by email")

            alice may pay(alice)
            alice may notify(alice) asLongAs {
                with purpose Commercial
                after { alice did pay(alice) }
            }
        }.renderMarkdown()

        md shouldContain """- 2.2 _"pay"_"""
        md shouldContain """- 2.1 _"notify"_ : Notify by email"""
    }
})
