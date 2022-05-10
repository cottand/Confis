package eu.dcotta.confis.plugin.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import eu.dcotta.confis.model.Action;
import eu.dcotta.confis.model.Obj;
import eu.dcotta.confis.model.Subject;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Set;

class QuestionToolWindow {
  private ToolWindow toolWindow;
  private Project project;
//  val toolWindow: ToolWindow, val project: Project

  private JPanel content;
  private JComboBox<String> qComboBox;
  private JComboBox<String> subjectCombo;
  private JComboBox<String> objectCombo;
  private JComboBox<String> actionCombo;
  private JList<String> circumstancesList;
  private JButton addButton;
  private JButton removeButton;
  private JList<String> resultsList;
  private JButton askQuestionButton;
  private JLabel title;

  public QuestionToolWindow(ToolWindow toolWindow, Project project) {
    this.toolWindow = toolWindow;
    this.project = project;

    qComboBox.setSelectedIndex(0);
  }

  void setSubjects(List<Subject> ss) {
    subjectCombo.removeAllItems();
    for (var s : ss) subjectCombo.addItem(s.render());
  }

  void setActions(Set<Action> as) {
    actionCombo.removeAllItems();
    for (var a : as) actionCombo.addItem(a.render());
  }

  void setObjects(Set<Obj> os) {
    objectCombo.removeAllItems();
    for (var o : os) objectCombo.addItem(o.render());
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
