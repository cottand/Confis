package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Permission
import eu.dcotta.confis.model.Clause.PermissionWithCircumstances
import eu.dcotta.confis.model.Clause.Requirement
import eu.dcotta.confis.model.Clause.RequirementWithCircumstances
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Obj.Named
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.util.oneTimeProperty
import eu.dcotta.confis.util.removeLastOccurrence

// @ConfisDsl
open class AgreementBuilder {

    // metadata
    var title: String? = null
    var introduction: String? = null

    // internal
    @Suppress("unused", "PropertyName")
    var `$$questionCircumstances$$`: CircumstanceBuilder.() -> Unit = {}

    private val parties = mutableListOf<Party>()

    private val clauses = mutableListOf<Clause>()

    operator fun String.unaryMinus() {
        clauses += Text(this.trimIndent())
    }

    // permission
    /**
     * Specifies that [Subject] may perform [sentence]
     */
    @CircumstanceDsl
    infix fun Subject.may(sentence: SentenceBuilderWithSubject.() -> Sentence): Permission {
        val permission = Permission(Allow, sentence(SentenceBuilderWithSubject(this)))
        clauses += permission
        return permission
    }

    /**
     * Specifies that [Subject] may not perform [sentence]
     */
    @CircumstanceDsl
    infix fun Subject.mayNot(sentence: SentenceBuilderWithSubject.() -> Sentence): Permission {
        val permission = Permission(Forbid, sentence(SentenceBuilderWithSubject(this)))
        clauses += permission
        return permission
    }

    operator fun Action.invoke(obj: Obj): ActionObject = ActionObject(this, obj)

    // overloads for less braces
    @CircumstanceDsl
    infix fun Subject.may(s: ActionObject) = may { s.action(s.obj) }

    @CircumstanceDsl
    infix fun Subject.mayNot(s: ActionObject) = mayNot { s.action(s.obj) }

    /**
     * Constrains the previous [Permission] to [init]
     *
     */
    @CircumstanceDsl
    infix fun Permission.asLongAs(init: CircumstanceBuilder.() -> Unit) {
        val cs = CircumstanceBuilder(sentence).also(init).`$$build$$`()
        val s = PermissionWithCircumstances(this, Allow, cs)
        clauses.removeLastOccurrence(this)
        clauses += s
    }

    @CircumstanceDsl
    infix fun Permission.unless(init: CircumstanceBuilder.() -> Unit) {
        val cs = CircumstanceBuilder(sentence).also(init).`$$build$$`()
        val s = PermissionWithCircumstances(this, Forbid, cs)
        clauses.removeLastOccurrence(this)
        clauses += s
    }
    // return when (rule) {
    //    is Requirement -> RequirementWithCircumstances(rule.sentence, circumstances)
    //    is Rule -> SentenceWithCircumstances(rule, circumstanceAllowance, circumstances)
    // }

    // requirement

    @CircumstanceDsl
    infix fun Subject.must(sentence: SentenceBuilderWithSubject.() -> Sentence): Requirement {
        val req = Requirement(sentence(SentenceBuilderWithSubject(this)))
        clauses += req
        return req
    }

    @CircumstanceDsl
    infix fun Subject.must(s: ActionObject) = must { s.action(s.obj) }

    infix fun Requirement.underCircumstances(init: CircumstanceBuilder.() -> Unit) {
        val cs = CircumstanceBuilder(sentence).also(init).`$$build$$`()
        clauses.removeLastOccurrence(this)
        clauses += RequirementWithCircumstances(sentence, cs)
    }

    private fun build(): Agreement = Agreement(
        clauses = clauses,
        parties = parties,
        title = title,
        introduction = introduction,
    )

    @ConfisDsl
    fun party(named: String? = null, description: String? = null) = oneTimeProperty<Any?, Party> {
        val party = Party(named ?: it.name, description)
        parties.add(party)
        party
    }

    @ConfisDsl
    val party = party()

    @ConfisDsl
    fun thing(named: String? = null, description: String? = null) = oneTimeProperty<Any?, Named> { prop ->
        Named(named ?: prop.name, description)
    }

    @ConfisDsl
    val thing = thing()

    @ConfisDsl
    fun action(named: String? = null, description: String? = null) = oneTimeProperty<Any?, Action> { prop ->
        Action(named ?: prop.name, description)
    }

    @ConfisDsl
    val action = action()

    companion object Builder {
        operator fun invoke(builder: AgreementBuilder.() -> Unit) = AgreementBuilder().apply(builder).build()
        fun assemble(builder: AgreementBuilder) = builder.build()
    }
}
