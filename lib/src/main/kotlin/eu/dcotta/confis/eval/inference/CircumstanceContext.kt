package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.model.Clause
import kotlinx.collections.immutable.PersistentSet

interface CircumstanceContext {
    val q: CircumstanceQuestion
    var circumstances: CircumstancesToClauses
    var contradictions: PersistentSet<List<Clause>>

    var unless: CircumstancesToClauses
}
