package eu.dcotta.confis.plugin.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.xdebugger.XExpression
import eu.dcotta.confis.dsl.CircumstanceBuilder
import eu.dcotta.confis.eval.AllowanceQuestion
import eu.dcotta.confis.eval.CircumstanceQuestion
import eu.dcotta.confis.eval.ComplianceQuestion
import eu.dcotta.confis.eval.QueryResponse
import eu.dcotta.confis.eval.allowance.ask
import eu.dcotta.confis.eval.compliance.ask
import eu.dcotta.confis.eval.inference.ask
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.plugin.ConfisCompiledNotifier
import eu.dcotta.confis.plugin.Resources
import eu.dcotta.confis.plugin.map
import eu.dcotta.confis.scripting.ConfisScriptDefinition
import eu.dcotta.confis.scripting.eu.dcotta.confis.scripting.ConfisSourceCode
import eu.dcotta.confis.util.mapValuesNotNull
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ResultWithDiagnostics.Success
import kotlin.script.experimental.api.valueOrNull
import kotlin.system.measureTimeMillis

data class ConfisAgreementListener(
    val onSubjectsUpdated: (List<Subject>) -> Unit = {},
    val onActionsUpdated: (List<Action>) -> Unit = {},
    val onObjectsUpdated: (List<Obj>) -> Unit = {},
    val onResults: (List<String>) -> Unit = {},
    val onCircumstances: (List<String>) -> Unit = {},
    val onDocument: (name: String?) -> Unit = {},
    val onNewCircumstanceContext: (ctx: PsiElement) -> Unit = {},
)

class QuestionWindowModel(project: Project) : Disposable {
    private val logger = thisLogger()
    private val listeners: MutableList<ConfisAgreementListener> = mutableListOf()

    private var latestAgreement: Agreement? = null
    private var latestCircumstanceContext: PsiElement? = null
    private var latestConfisFile: VirtualFile? = null

    val _currentDocTitle = AtomicProperty("Please open a valid Confis agreement")
    var currentDocTitle by _currentDocTitle

    private val scope = CoroutineScope(
        Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
            thisLogger().error("Error during QuestionWindowModel coroutine", throwable)
        }
    )

    private val topicListener = ConfisCompiledNotifier { file, result ->
        if (result is Success) {
            latestConfisFile = file
            latestAgreement = result.value
            currentDocTitle = "Examining Confis agreement ${file.name}"
            scope.launch {
                val crafted = craftCircumstanceContextFor(file)
                latestCircumstanceContext = crafted
                if (crafted != null) for (l in listeners) l.onNewCircumstanceContext(crafted)
            }
            for (l in listeners) {
                val a = result.value
                l.onDocument(file.name)
                l.onSubjectsUpdated(a.parties)
                l.onActionsUpdated(a.actions.toList())
                l.onObjectsUpdated(a.objs.toList())
            }
        } else for (l in listeners) l.onDocument(null)
    }

    init {
        project.messageBus.connect(this).subscribe(ConfisCompiledNotifier.CHANGE_ACTION_TOPIC, topicListener)
    }

    fun addListener(listener: ConfisAgreementListener) {
        latestAgreement?.let { a ->
            listener.onSubjectsUpdated(a.parties)
            listener.onActionsUpdated(a.actions.toList())
            listener.onObjectsUpdated(a.objs.toList())
        }
        listeners += listener
    }

    private val psiManager by lazy { PsiManager.getInstance(project) }
    private val docManager by lazy { FileDocumentManager.getInstance() }

    private val builderScope = """
        val builder: CircumstanceBuilder.() -> Unit = {
            this
        }
    """.trimIndent()

    private fun craftCircumstanceContextFor(agreementFile: VirtualFile): PsiElement? {
        val psi = ReadAction.compute<PsiElement?, Exception> { psiManager.findFile(agreementFile) }
        val doc = ReadAction.compute<Document?, Exception> { docManager.getDocument(agreementFile) }
        if (psi == null) {
            logger.warn("Expected to find a psiFile for Confis file $agreementFile", Exception())
            return null
        }
        val syntheticContents = (doc?.text ?: "") + "\n\n" + builderScope
        val lvf = LightVirtualFile("synthetic_${agreementFile.name}", kotlinFileType, syntheticContents)
        // TODO maybe I can just craft a new file with a handwritten psiElement rather than making one out of text
        return ReadAction.compute<PsiElement, Exception> {
            val synthPsi = psiManager.findFile(lvf) ?: error("")

            val block = synthPsi.lastChild?.lastChild?.lastChild?.lastChild?.firstChild?.children?.get(0)?.firstChild // this
            block
        }
    }

    fun askAllowance(
        sentence: Sentence,
        circumstancesText: XExpression,
        onResult: (QueryResponse?) -> Unit,
    ) {
        scope.launch {
            val cs = compileCircumstances(circumstancesText, latestConfisFile)
            if (cs == null || cs !is Success) {
                onResult(null)
            } else {
                latestAgreement?.ask(AllowanceQuestion(sentence, cs.value)).run(onResult)
            }
        }
    }

    fun askCircumstance(
        sentence: Sentence,
        onResult: (QueryResponse?) -> Unit,
    ) {
        scope.launch {
            latestAgreement?.ask(CircumstanceQuestion(sentence)).run(onResult)
        }
    }

    fun askCompliance(worldState: Map<Sentence, XExpression>, onResult: (QueryResponse?) -> Unit) {
        scope.launch {
            val compiled =
                worldState.mapValuesNotNull { compileCircumstances(it.value, latestConfisFile)?.valueOrNull() }

            latestAgreement?.ask(ComplianceQuestion(compiled.toPersistentMap())).run(onResult)
        }
    }

    private fun compileCircumstances(
        text: XExpression?,
        agreementFile: VirtualFile?,
    ): ResultWithDiagnostics<CircumstanceMap>? {
        val assembled: ResultWithDiagnostics<CircumstanceMap>
        val duration = measureTimeMillis {
            if (text == null || agreementFile == null) return null
            val fieldName = ConfisScriptDefinition::`$$questionCircumstances$$`.name
            val doc = ReadAction.compute<Document?, Exception> { docManager.getDocument(agreementFile) }
            val syntheticContents = buildString {
                append(doc?.text ?: "")
                append('\n')
                append("`$fieldName` = circumstanceContainer {")
                append(text.expression)
                append("\n}\n")
            }

            val src = ConfisSourceCode(null, "circumstances.confis.kts", syntheticContents)
            val result = Resources.scriptHost.rawEval(src)

            assembled = result.map {
                val init = it.`$$questionCircumstances$$`

                CircumstanceBuilder(Sentence(Party(""), Action(""), Obj("")))
                    .apply(init)
                    .`$$build$$`()
            }
        }
        logger.info("Compiled circumstances for `$text` in ${duration}ms")
        return assembled
    }

    override fun dispose() {
    }
}
