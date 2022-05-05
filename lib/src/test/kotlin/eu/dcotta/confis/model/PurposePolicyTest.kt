package eu.dcotta.confis.model

import eu.dcotta.confis.model.circumstance.Purpose.Commercial
import eu.dcotta.confis.model.circumstance.Purpose.Research
import eu.dcotta.confis.model.circumstance.PurposePolicy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PurposePolicyTest : StringSpec({

    "a large list of purposes is less general than its sublist" {
        val query = PurposePolicy(Commercial, Research)
        val clause = PurposePolicy(Commercial)

        (clause generalises query) shouldBe true
    }
})
