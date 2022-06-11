package eu.dcotta.confis.render

import eu.dcotta.confis.Sentence
import eu.dcotta.confis.dsl.of
import eu.dcotta.confis.dsl.rangeTo
import eu.dcotta.confis.dsl.year
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.circumstance.Month.May
import eu.dcotta.confis.model.circumstance.Purpose.Commercial
import eu.dcotta.confis.model.circumstance.WorldState
import eu.dcotta.confis.model.circumstance.render
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.collections.immutable.persistentMapOf

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

        md shouldContain "1. Alice Pleasance Hargreaves of Westminster (**Alice Liddell**)"
        md shouldContain "2. Bob the Builder (**bob**)"
        md shouldContain "3. **Charlie**"
    }

    "agreement contains actions and lists them" {
        val md = Agreement {
            val alice by party
            val pay by action
            val notify by action(description = "Notify by email")
            val message by thing
            val message2 by thing(description = "messaging2")

            alice may pay(alice)
            alice may notify(alice) asLongAs {
                with purpose Commercial
                after { alice did pay(alice) }
            }
            alice may notify(message) asLongAs {
                after { alice did notify(message2) }
            }

            -"""
                Some useless text clause
            """
        }.renderMarkdown()

        md shouldContain """_"pay"_"""
        md shouldContain """_"message"_"""
        md shouldContain """_"notify"_: Notify by email"""
        md shouldContain """_"message2"_: messaging2"""
    }

    "render title and intro" {
        val md = Agreement {
            title = "My Agreement"
            introduction = "For specifying the terms and conditions of life"
        }.renderMarkdown()

        md shouldContain "# My Agreement"
        md shouldContain "terms and conditions of life"
    }

    "render simple permission clause" {
        val md = Agreement {
            val alice by party
            val pay by action

            alice may pay(alice)
        }

        md.clauses.first().renderMd(1) shouldContain "1. alice may pay alice"
    }

    "render simple permission clause with time circumstances" {
        val a = Agreement {
            val alice by party
            val pay by action

            alice may pay(alice) asLongAs {
                within { (1 of May)..(3 of May) year 2022 }
            }
        }

        val rendered = a.clauses.first().renderMd(1)
        rendered shouldContain "1. alice may pay alice under the following circumstances"
    }

    "render simple requirement clause with time circumstances" {
        val a = Agreement {
            val alice by party
            val pay by action

            alice must pay(alice) underCircumstances {
                within { (1 of May)..(3 of May) year 2022 }
            }
        }

        val rendered = a.clauses.first().renderMd(1)
        rendered shouldContain "1. alice must pay alice:"
    }

    "world state rendering" {
             Sentence { "alice"("take", Obj("cookie"))} to CircumstanceMap.empty       val empty: WorldState = persistentMapOf(
            Sentence { "alice"("take", Obj("cookie")) } to CircumstanceMap.empty
        )
        empty.render() should {
            it shouldContain "alice take cookie"
            it shouldNotContain "circumstance"
        }

        val nonEmpty: WorldState = persistentMapOf(
            Sentence { "alice"("take", Obj("cookie")) } to CircumstanceMap.of(1 of May year 2022)
        )

        nonEmpty.render() should {
            it shouldContain "alice take cookie"
            it shouldContain "circumstance"
        }
    }
})
