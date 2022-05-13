package eu.dcotta.confis.plugin.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.xdebugger.XExpression
import eu.dcotta.confis.dsl.CircumstanceBuilder
import eu.dcotta.confis.eval.QueryResponse
import eu.dcotta.confis.eval.allowance.AllowanceQuestion
import eu.dcotta.confis.eval.allowance.ask
import eu.dcotta.confis.eval.compliance.ComplianceQuestion
import eu.dcotta.confis.eval.compliance.ask
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
import eu.dcotta.confis.plugin.toolwindow.ConfisQueryType.Allowance
import eu.dcotta.confis.plugin.toolwindow.ConfisQueryType.Compliance
import eu.dcotta.confis.scripting.ConfisScriptDefinition
import eu.dcotta.confis.scripting.eu.dcotta.confis.scripting.ConfisSourceCode
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ResultWithDiagnostics.Success

data class ConfisAgreementListener(
    val onSubjectsUpdated: (List<Subject>) -> Unit,
    val onActionsUpdated: (Set<Action>) -> Unit,
    val onObjectsUpdated: (Set<Obj>) -> Unit,
    val onResults: (List<String>) -> Unit,
    val onCircumstances: (List<String>) -> Unit,
    val onDocument: (name: String?) -> Unit,
    val onNewCircumstanceContext: (ctx: PsiElement) -> Unit = {},
)

class QuestionWindowModel(project: Project) : Disposable {
    private val logger = thisLogger()
    private val listeners: MutableList<ConfisAgreementListener> = mutableListOf()

    var latestAgreement: Agreement? = null
    var latestCircumstanceContext: PsiElement? = null
    var latestConfisFile: VirtualFile? = null

    val scope = CoroutineScope(
        Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
            thisLogger().error("Error during QuestionWindowModel coroutine", throwable)
        }
    )

    private val topicListener = ConfisCompiledNotifier { file, result ->
        if (result is Success) {
            latestConfisFile = file
            latestAgreement = result.value
            scope.launch {
                val crafted = craftCircumstanceContextFor(file)
                latestCircumstanceContext = crafted
                if (crafted != null) for (l in listeners) l.onNewCircumstanceContext(crafted)
            }
            for (l in listeners) {
                val a = result.value
                l.onDocument(file.name)
                l.onSubjectsUpdated(a.parties)
                l.onActionsUpdated(a.actions)
                l.onObjectsUpdated(a.objs)
            }
        } else for (l in listeners) l.onDocument(null)
    }

    init {
        project.messageBus.connect(this).subscribe(ConfisCompiledNotifier.CHANGE_ACTION_TOPIC, topicListener)
    }

    fun addListener(listener: ConfisAgreementListener) {
        listeners += listener
    }

    val psiManager by lazy { PsiManager.getInstance(project) }
    val docManager by lazy { FileDocumentManager.getInstance() }

    val builderScope = """
        val builder: CircumstanceBuilder.() -> Unit = {
        
        }
    """.trimIndent()

    private suspend fun craftCircumstanceContextFor(agreementFile: VirtualFile): PsiElement? {
        val psi = ReadAction.compute<PsiElement?, Exception> { psiManager.findFile(agreementFile) }
        val doc = ReadAction.compute<Document?, Exception> { docManager.getDocument(agreementFile) }
        if (psi == null) {
            logger.warn("Expected to find a psiFile for Confis file $agreementFile", Exception())
            return null
        }
        val syntheticContents = (doc?.text ?: "") + "\n\n" + builderScope
        val lvf = LightVirtualFile("${agreementFile.name}.synthetic", kotlinFileType, syntheticContents)
        // TODO maybe I can just craft a new file with a handwritten psiElement rather than making one out of text
        return ReadAction.compute<PsiElement, Exception> {
            val synthPsi = psiManager.findFile(lvf) ?: error("")

            val block = synthPsi
            // val block = synthPsi.lastChild.lastChild
            // val block = synthPsi.lastChild?.lastChild?.lastChild?.lastChild?.firstChild?.children?.get(0)
            block
        }
    }

    fun askAsync(
        type: ConfisQueryType,
        sentence: Sentence,
        circumstancesText: XExpression?,
        onResult: (QueryResponse) -> Unit,
    ) {
        scope.launch {
            val cs = compileCircumstances(circumstancesText, latestConfisFile)
            if (cs == null || cs !is Success) {
                // TODO POPUP
                return@launch
            }
            val res = latestAgreement?.let { agreement ->
                when (type) {
                    Allowance -> agreement.ask(AllowanceQuestion(sentence, cs.value))
                    Compliance -> agreement.ask(ComplianceQuestion())
                }
            }
            if (res != null) onResult(res)
        }
    }

    private fun compileCircumstances(text: XExpression?, agreementFile: VirtualFile?): ResultWithDiagnostics<CircumstanceMap>? {
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
        return result.map {
            val init = it.`$$questionCircumstances$$`

            CircumstanceBuilder(Sentence(Party(""), Action(""), Obj("")))
                .apply(init)
                .`$$build$$`()
        }
    }

    override fun dispose() {
    }
}

enum class ConfisQueryType {
    Allowance, Compliance;
}
