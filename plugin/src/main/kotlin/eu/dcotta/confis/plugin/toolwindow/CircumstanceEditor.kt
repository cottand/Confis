package eu.dcotta.confis.plugin.toolwindow

import com.intellij.lang.Language
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.xdebugger.evaluation.EvaluationMode.CODE_FRAGMENT
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.intellij.xdebugger.impl.ui.XDebuggerExpressionEditor
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage

val kotlinLang by lazy { Language.findLanguageByID(KotlinLanguage.INSTANCE.id) }
val kotlinFileType by lazy { FileTypeManager.getInstance().getStdFileType(KotlinFileType.INSTANCE.name) }

class CircumstanceEditor(
    project: Project,
) : XDebuggerExpressionEditor(
    project,
    JavaDebuggerEditorsProvider(),
    null,
    null,
    XExpressionImpl("", kotlinLang, "", CODE_FRAGMENT),
    true,
    true,
    true,
) {
    private val app: Application = ApplicationManager.getApplication()

    override fun setContext(context: PsiElement?) {
        // ensure we are executing async on UI thread
        app.invokeLater {
            super.setContext(context)
        }
    }
}
