package eu.dcotta.confis.plugin.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class QuestionToolWindow {
  private ToolWindow toolWindow;
  private Project project;
//  val toolWindow: ToolWindow, val project: Project

  private JPanel content;
  private JComboBox<String> qComboBox;
  private JComboBox comboBox1;
  private JComboBox comboBox2;
  private JComboBox comboBox3;
  private JList list1;
  private JButton addButton;
  private JButton removeButton;
  private JList list2;
  private JButton askQuestionButton;

  public QuestionToolWindow(@NotNull ToolWindow toolWindow, @NotNull Project project) {
    this.toolWindow = toolWindow;
    this.project = project;


    qComboBox.setSelectedIndex(0);
  }

//  private val fileEditorManager:FileEditorManager =FileEditorManager.getInstance(project)
//
//  val selectedEditor
//
//  get() =fileEditorManager.selectedEditor


  void createUIComponents() {
  }

  public JPanel getContent() {
    return content;
  }
}
