package eu.dcotta.confis.plugin.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.CollectionListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import eu.dcotta.confis.model.Action;
import eu.dcotta.confis.model.Obj;
import eu.dcotta.confis.model.Sentence;
import eu.dcotta.confis.model.Subject;
import kotlin.Unit;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Set;

class QuestionToolWindow {
  private ToolWindow toolWindow;
  private Project project;
//  val toolWindow: ToolWindow, val project: Project

  private JPanel content;
  private JComboBox<ConfisQueryType> qComboBox;
  private JComboBox<Subject> subjectCombo;
  private JComboBox<Obj> objectCombo;
  private JComboBox<Action> actionCombo;
  private final CollectionListModel<String> resultsModel = new CollectionListModel<>();
  private JList<String> resultsList;
  private JButton askQuestionButton;
  private JLabel title;
  private JPanel circumstancePanel;

  public QuestionToolWindow(
      ToolWindow toolWindow,
      Project project,
      QuestionWindowModel model,
      CircumstanceEditor circumstanceEditor
  ) {
    this.toolWindow = toolWindow;
    this.project = project;

    for (var t : ConfisQueryType.values()) qComboBox.addItem(t);
    qComboBox.setSelectedIndex(0);

    resultsList.setModel(resultsModel);
    var circumstanceEditorComponent = circumstanceEditor.getEditorComponent();
    GridConstraints gc = new GridConstraints();
    gc.setFill(GridConstraints.FILL_HORIZONTAL);
    circumstancePanel.add(circumstanceEditorComponent, gc, 0);

    askQuestionButton.addActionListener(actionEvent -> {
      int subjectIndex = subjectCombo.getSelectedIndex();
      int actionIndex = actionCombo.getSelectedIndex();
      int objectIndex = objectCombo.getSelectedIndex();
      int typeIndex = qComboBox.getSelectedIndex();
      if (subjectIndex != -1 && actionIndex != -1 && objectIndex != -1 && typeIndex != -1) {
        var subject = subjectCombo.getItemAt(subjectIndex);
        var action = actionCombo.getItemAt(actionIndex);
        var obj = objectCombo.getItemAt(objectIndex);
        var sentence = new Sentence(subject, action, obj);
        var type = ConfisQueryType.values()[typeIndex];

        model.askAsync(
            type,
            sentence,
            circumstanceEditor.getExpression(),
            (res) -> {
              ApplicationManager.getApplication().invokeLater(() -> {
                if (res != null) resultsModel.add(res.render());
              });
              return Unit.INSTANCE;
            }
        );
      }
    });
  }

  void setSubjects(List<Subject> ss) {
    subjectCombo.removeAllItems();
    for (var s : ss) subjectCombo.addItem(s);
    subjectCombo.setSelectedIndex(0);
  }

  void setActions(Set<Action> as) {
    actionCombo.removeAllItems();
    for (var a : as) actionCombo.addItem(a);
    subjectCombo.setSelectedIndex(0);
  }

  void setObjects(Set<Obj> os) {
    objectCombo.removeAllItems();
    for (var o : os) objectCombo.addItem(o);
    subjectCombo.setSelectedIndex(0);
  }

  void setDocName(@Nullable String name) {
    if (name == null) {
      title.setText("Please open a Confis agreement");
    } else {
      title.setText("<html><B>Querying document " + name + "</B></html>");
    }
  }


  void createUIComponents() {

  }

  public JPanel getContent() {
    return content;
  }
}
