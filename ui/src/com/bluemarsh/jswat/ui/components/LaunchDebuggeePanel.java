/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: LaunchDebuggeePanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.components;

import com.bluemarsh.jswat.core.actions.Actions;
import com.bluemarsh.jswat.core.actions.ClearAction;
import com.bluemarsh.jswat.core.actions.CopyAction;
import com.bluemarsh.jswat.core.actions.CutAction;
import com.bluemarsh.jswat.core.actions.PasteAction;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.connect.ConnectionFactory;
import com.bluemarsh.jswat.core.connect.ConnectionProvider;
import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.runtime.JavaRuntime;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import com.bluemarsh.jswat.core.util.NameValuePair;
import com.bluemarsh.jswat.core.util.Strings;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * The panel containing the components for the debuggee launcher action.
 *
 * @author  Nathan Fiedler
 */
public class LaunchDebuggeePanel extends JPanel implements ActionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Name of the resume debuggee property. */
    private static final String PROP_RESUME_DEBUGGEE = "ResumeDebuggee";
    /** Label and action command for the Launch button. */
    private String launchLabel;
    /** Dialog for displaying this panel. */
    private Dialog inputDialog;
    /** True if the user has provided valid input and wants to launch,
     * false otherwise. */
    private boolean okayToGo;
    /** Editor for the classpath. */
    private PathEditorPanel classpathEditor;

    /**
     * Creates new form LaunchDebuggeePanel.
     */
    public LaunchDebuggeePanel() {
        initComponents();
        javaHomeButton.addActionListener(this);
        launchLabel = NbBundle.getMessage(getClass(), "LBL_LaunchButton");
        classpathEditor = new PathEditorPanel();
        classpathPanel.add(classpathEditor, BorderLayout.CENTER);

        Action[] actions = new Action[] {
            new CutAction(),
            new CopyAction(),
            new PasteAction(),
            new ClearAction(),
        };
        Actions.attachActions(actions, javaParamsTextArea,
                classParamsTextArea, classNameField);
        Actions.attachShortcuts(actions, this);
    }

    /**
     * Invoked by the press of a button.
     *
     * @param  event  action event.
     */
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == javaHomeButton) {
            RuntimeManagerPanel rmp = new RuntimeManagerPanel();
            // Display dialog and get the user response.
            rmp.display();
            // Rebuild the runtimes list since it may have changed.
            buildRuntimesList();
        } else {
            String cmd = event.getActionCommand();
            if (cmd.equals(launchLabel)) {
                if (validateInput()) {
                    okayToGo = true;
                    inputDialog.dispose();
                }
            } else {
                inputDialog.dispose();
            }
        }
    }

    /**
     * Builds a JvmConnection instance based on the input values.
     *
     * @param  session  Session for which to build connection.
     * @return  new connection instance, or null if something was wrong.
     */
    public JvmConnection buildConnection(Session session) {
        // Build a JvmConnection from the input field values.
        String javaParams = javaParamsTextArea.getText();
        // Because saveParameters() may not have been called yet, get the
        // classpath from the classpath editor.
        List<String> classpath = classpathEditor.getPath();
        if (classpath != null && classpath.size() > 0) {
            String cp = Strings.listToString(classpath, File.pathSeparator);
            // Must quote classpath in case it contains spaces.
            javaParams = javaParams + " -cp \"" + cp + '"';
        }
        String className = classNameField.getText() +
            " " + classParamsTextArea.getText();
        NameValuePair<?> nvp =
                (NameValuePair<?>) runtimeComboBox.getSelectedItem();
        JavaRuntime runtime = (JavaRuntime) nvp.getValue();
        PathManager pm = PathProvider.getPathManager(session);
        mergeSourcePath(runtime, pm);
        JvmConnection connection = null;
        ConnectionFactory factory = ConnectionProvider.getConnectionFactory();
        connection = factory.createLaunching(runtime, javaParams, className);
        // This may throw an IllegalArgumentException.
        return connection;
    }

    /**
     * Clears the runtimes combobox of all items and rebuilds the list
     * according to the current information available.
     */
    private void buildRuntimesList() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        Iterator<JavaRuntime> iter = rm.iterateRuntimes();
        while (iter.hasNext()) {
            JavaRuntime rt = iter.next();
            NameValuePair<JavaRuntime> nvp = new NameValuePair<JavaRuntime>(
                    rt.getName(), rt);
            model.addElement(nvp);
        }
        runtimeComboBox.setModel(model);
    }

    /**
     * Build and display a dialog for launching the debuggee.
     *
     * @return  true if user input is valid and ready to launch debuggee,
     *          false otherwise.
     */
    public boolean display() {
        // Collect the launch dialog elements.
        String title = NbBundle.getMessage(getClass(), "LBL_LaunchDebuggeeTitle");
        String closeLabel = NbBundle.getMessage(getClass(), "LBL_CloseButton");
        Object[] options = { launchLabel, closeLabel };
        // Display dialog and get the user response.
        DialogDescriptor dd = new DialogDescriptor(
            this, title, true, options, launchLabel, DialogDescriptor.BOTTOM_ALIGN,
            new HelpCtx("jswat-launch-debuggee"), this);
        inputDialog = DialogDisplayer.getDefault().createDialog(dd);
        // Set the default focus (once there is a parent window).
        classNameField.requestFocusInWindow();
        okayToGo = false;
        inputDialog.setVisible(true);
        // (blocks until dialog is disposed...)
        return okayToGo;
    }

    /**
     * Load the launch parameters from the given properties.
     *
     * @param  session  Session from which launch parameters are read.
     */
    public void loadParameters(Session session) {
        String id = session.getProperty(Session.PROP_RUNTIME_ID);
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        JavaRuntime rt = null;
        if (id != null && id.length() > 0) {
            // Try to find the runtime, if it is still around.
            rt = rm.findById(id);
        }
        if (rt == null) {
            // Runtime not specified or it has been uninstalled.
            Iterator<JavaRuntime> iter = rm.iterateRuntimes();
            if (iter.hasNext()) {
                // Use the first available runtime.
                rt = iter.next();
            }
        }
        // Build the runtimes list for the combobox.
        buildRuntimesList();
        DefaultComboBoxModel model = (DefaultComboBoxModel)
                runtimeComboBox.getModel();
        for (int ii = 0; ii < model.getSize(); ii++) {
            NameValuePair<?> nvp = (NameValuePair<?>) model.getElementAt(ii);
            JavaRuntime jr = (JavaRuntime) nvp.getValue();
            if (jr.equals(rt)) {
                model.setSelectedItem(nvp);
                break;
            }
        }

        String value = session.getProperty(Session.PROP_JAVA_PARAMS);
        javaParamsTextArea.setText(value == null ? "" : value);
        value = session.getProperty(Session.PROP_CLASS_NAME);
        classNameField.setText(value == null ? "" : value);
        value = session.getProperty(Session.PROP_CLASS_PARAMS);
        classParamsTextArea.setText(value == null ? "" : value);
        PathManager pm = PathProvider.getPathManager(session);
        List<String> classpath = pm.getClassPath();
        if (classpath != null && classpath.size() > 0) {
            classpathEditor.setPath(classpath);
        }
        value = session.getProperty(PROP_RESUME_DEBUGGEE);
        Boolean resume = Boolean.parseBoolean(value);
        resumeCheckBox.setSelected(resume.booleanValue());
    }

    /**
     * Merge the source paths of the runtime into the PathManager sourcepath.
     *
     * @param  rt  runtime from which to get source paths.
     * @param  pm  path manager in which to set source path.
     */
    private void mergeSourcePath(JavaRuntime rt, PathManager pm) {
        List<String> paths = rt.getSources();
        if (paths != null && !paths.isEmpty()) {
            List<FileObject> fos = new LinkedList<FileObject>();
            for (String path : paths) {
                File dir = new File(path);
                dir = FileUtil.normalizeFile(dir);
                FileObject fo = FileUtil.toFileObject(dir);
                if (fo == null) {
                    // Not sure why, but this has happened (bug 1101).
                    continue;
                }
                if (FileUtil.isArchiveFile(fo)) {
                    // Convert archive files to the archive's root folder.
                    fo = FileUtil.getArchiveRoot(fo);
                }
                fos.add(fo);
            }
            List<FileObject> srcpath = pm.getSourcePath();
            if (srcpath == null) {
                srcpath = new LinkedList<FileObject>();
            } else {
                // Have to make a modifiable list.
                srcpath = new LinkedList<FileObject>(srcpath);
            }
            boolean changed = false;
            for (FileObject fo : fos) {
                if (!srcpath.contains(fo)) {
                    srcpath.add(fo);
                    changed = true;
                }
            }
            if (changed) {
                pm.setSourcePath(srcpath);
            }
        }
    }

    /**
     * Save the launch parameters to the given properties.
     *
     * @param  session  Session to which we persist our values.
     */
    public void saveParameters(Session session) {
        NameValuePair<?> nvp =
                (NameValuePair<?>) runtimeComboBox.getSelectedItem();
        JavaRuntime runtime = (JavaRuntime) nvp.getValue();
        session.setProperty(Session.PROP_RUNTIME_ID, runtime.getIdentifier());
        session.setProperty(Session.PROP_JAVA_PARAMS, javaParamsTextArea.getText());
        session.setProperty(Session.PROP_CLASS_NAME, classNameField.getText());
        session.setProperty(Session.PROP_CLASS_PARAMS, classParamsTextArea.getText());
        if (resumeCheckBox.isSelected()) {
            session.setProperty(PROP_RESUME_DEBUGGEE, "true");
        } else {
            session.setProperty(PROP_RESUME_DEBUGGEE, "false");
        }
        PathManager pm = PathProvider.getPathManager(session);
        List<String> paths = classpathEditor.getPath();
        pm.setClassPath(paths);
    }

    /**
     * Indicates if the user wants the launched debuggee to be resumed.
     *
     * @return  true to resume immediately, false to do nothing.
     */
    public boolean shouldResume() {
        return resumeCheckBox.isSelected();
    }

    /**
     * Validates the input for this dialog. If invalid, a message will be
     * displayed at the bottom of the dialog.
     *
     * @return  true if input valid, false if invalid.
     */
    private boolean validateInput() {
        boolean valid = true;
        String className = classNameField.getText();
        if (className == null || className.length() == 0) {
            inputMessageLabel.setText(NbBundle.getMessage(getClass(), "ERR_ClassName_Missing"));
            valid = false;
        } else if (className.endsWith(".class") || className.endsWith(".java")) {
            inputMessageLabel.setText(NbBundle.getMessage(getClass(), "ERR_ClassName_File"));
            valid = false;
        }
        return valid;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        tabbedPane = new javax.swing.JTabbedPane();
        parametersPanel = new javax.swing.JPanel();
        javaHomeLabel = new javax.swing.JLabel();
        runtimeComboBox = new javax.swing.JComboBox();
        javaHomeButton = new javax.swing.JButton();
        javaParamsLabel = new javax.swing.JLabel();
        javaParamsScrollPane = new javax.swing.JScrollPane();
        javaParamsTextArea = new javax.swing.JTextArea();
        classNameLabel = new javax.swing.JLabel();
        classNameField = new javax.swing.JTextField();
        classParamsLabel = new javax.swing.JLabel();
        classParamsScrollPane = new javax.swing.JScrollPane();
        classParamsTextArea = new javax.swing.JTextArea();
        resumeCheckBox = new javax.swing.JCheckBox();
        classpathPanel = new javax.swing.JPanel();
        inputMessageLabel = new javax.swing.JLabel();

        javaHomeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_Runtime").charAt(0));
        javaHomeLabel.setLabelFor(runtimeComboBox);
        javaHomeLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_JavaHomeLabel"));

        runtimeComboBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_Launch_Runtimes"));
        runtimeComboBox.setPreferredSize(new java.awt.Dimension(200, 24));

        javaHomeButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_AddRuntime").charAt(0));
        javaHomeButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_JavaHomeBrowseButton"));
        javaHomeButton.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_Launch_AddRuntime"));
        javaHomeButton.setActionCommand("JavaHomeBrowse");
        javaHomeButton.setMargin(new java.awt.Insets(2, 5, 2, 5));

        javaParamsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_JvmParams").charAt(0));
        javaParamsLabel.setLabelFor(javaParamsTextArea);
        javaParamsLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_JavaParamsLabel"));

        javaParamsTextArea.setLineWrap(true);
        javaParamsTextArea.setRows(3);
        javaParamsTextArea.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_Launch_JavaArguments"));
        javaParamsTextArea.setWrapStyleWord(true);
        javaParamsScrollPane.setViewportView(javaParamsTextArea);

        classNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_ClassName").charAt(0));
        classNameLabel.setLabelFor(classNameField);
        classNameLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_ClassNameLabel"));

        classNameField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_Launch_ClassName"));

        classParamsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_ClassParams").charAt(0));
        classParamsLabel.setLabelFor(classParamsTextArea);
        classParamsLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_ClassParamsLabel"));

        classParamsTextArea.setLineWrap(true);
        classParamsTextArea.setRows(3);
        classParamsTextArea.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_Launch_ClassArguments"));
        classParamsTextArea.setWrapStyleWord(true);
        classParamsScrollPane.setViewportView(classParamsTextArea);

        resumeCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_Resume").charAt(0));
        resumeCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Launch_Resume"));
        resumeCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_Launch_Resume"));

        org.jdesktop.layout.GroupLayout parametersPanelLayout = new org.jdesktop.layout.GroupLayout(parametersPanel);
        parametersPanel.setLayout(parametersPanelLayout);
        parametersPanelLayout.setHorizontalGroup(
            parametersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(parametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(parametersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resumeCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 371, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(parametersPanelLayout.createSequentialGroup()
                        .add(parametersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(javaParamsLabel)
                            .add(javaHomeLabel)
                            .add(classParamsLabel)
                            .add(classNameLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(parametersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(parametersPanelLayout.createSequentialGroup()
                                .add(runtimeComboBox, 0, 353, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(javaHomeButton))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, javaParamsScrollPane)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, classParamsScrollPane)
                            .add(classNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE))))
                .addContainerGap())
        );
        parametersPanelLayout.setVerticalGroup(
            parametersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(parametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(parametersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(javaHomeLabel)
                    .add(javaHomeButton)
                    .add(runtimeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(parametersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(javaParamsLabel)
                    .add(javaParamsScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(parametersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(classNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(classNameLabel))
                .add(8, 8, 8)
                .add(parametersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(classParamsLabel)
                    .add(classParamsScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resumeCheckBox)
                .add(329, 329, 329))
        );
        tabbedPane.addTab(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Launch_ParamsTabTitle"), parametersPanel);

        classpathPanel.setLayout(new java.awt.BorderLayout());

        tabbedPane.addTab(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Launch_ClasspathTabTitle"), classpathPanel);

        inputMessageLabel.setForeground(new java.awt.Color(255, 51, 51));
        inputMessageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        inputMessageLabel.setText(" ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, inputMessageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 232, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(inputMessageLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField classNameField;
    private javax.swing.JLabel classNameLabel;
    private javax.swing.JLabel classParamsLabel;
    private javax.swing.JScrollPane classParamsScrollPane;
    private javax.swing.JTextArea classParamsTextArea;
    private javax.swing.JPanel classpathPanel;
    private javax.swing.JLabel inputMessageLabel;
    private javax.swing.JButton javaHomeButton;
    private javax.swing.JLabel javaHomeLabel;
    private javax.swing.JLabel javaParamsLabel;
    private javax.swing.JScrollPane javaParamsScrollPane;
    private javax.swing.JTextArea javaParamsTextArea;
    private javax.swing.JPanel parametersPanel;
    private javax.swing.JCheckBox resumeCheckBox;
    private javax.swing.JComboBox runtimeComboBox;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
