package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Requirement
import eu.dcotta.confis.model.Clause.RequirementWithCircumstances
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.PrecedentSentence
import eu.dcotta.confis.model.generalises
import eu.dcotta.confis.util.plus

internal fun asComplianceRules(c: Clause): List<ComplianceRule> = when (c) {
    is Text -> emptyList()
    is Requirement -> asComplianceRules(c)
    is RequirementWithCircumstances -> TODO()
    is Rule -> TODO()
    is SentenceWithCircumstances -> TODO()
}

private fun asComplianceRules(c: Requirement): List<ComplianceRule> = listOf(
    // if requirement not already present in current circumstances
    ComplianceRule(
        case = { !(PrecedentSentence(c.sentence) generalises q.cs) },
        then = { required += PrecedentSentence(c.sentence) },
    )
)
