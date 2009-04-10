/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.components;

import com.bluemarsh.jswat.core.actions.Actions;
import com.bluemarsh.jswat.core.actions.ClearAction;
import com.bluemarsh.jswat.core.actions.CopyAction;
import com.bluemarsh.jswat.core.actions.CutAction;
import com.bluemarsh.jswat.core.actions.PasteAction;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.runtime.RuntimeEvent;
import com.bluemarsh.jswat.core.runtime.RuntimeListener;
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
import com.bluemarsh.jswat.ui.actions.ManageRuntimesAction;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * The panel containing the components for the debuggee launcher action.
 *
 * @author  Nathan Fiedler
 */
public class LaunchDebuggeePanel extends JPanel implements
        PropertyChangeListener, RuntimeListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Name of the resume debuggee property. */
    private static final String PROP_RESUME_DEBUGGEE = "ResumeDebuggee";
    /** Editor for the classpath. */
    private PathEditorPanel classpathEditor;
    /** Dialog descriptor used to display this panel. */
    private DialogDescriptor descriptor;

    /**
     * Creates new form LaunchDebuggeePanel.
     */
    public LaunchDebuggeePanel() {
        initComponents();
        classpathEditor = new PathEditorPanel();
        classpathPanel.add(classpathEditor, BorderLayout.CENTER);

        javaHomeButton.addActionListener(SystemAction.get(
                ManageRuntimesAction.class));
        classNameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent event) {
            }
            @Override
            public void insertUpdate(DocumentEvent event) {
                validate(event);
            }
            @Override
            public void removeUpdate(DocumentEvent event) {
                validate(event);
            }
            private void validate(DocumentEvent event) {
                Document document = event.getDocument();
                boolean valid = false;
                if (document.getLength() > 0) {
                    try {
                        // Keep this in sync with the validateInput() method.
                        String name = document.getText(0, document.getLength());
                        if (!name.endsWith(".class") && !name.endsWith(".java")) {
                            valid = true;
                        }
                    } catch (BadLocationException ble) {
                    }
                }
                // This fires an event only if the value has changed.
                putClientProperty(NotifyDescriptor.PROP_VALID,
                        Boolean.valueOf(valid));
            }
        });

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

    @Override
    public void addNotify() {
        super.addNotify();
        classNameField.requestFocusInWindow();
        validateInput();
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
     * Rebuild the list of runtimes available from runtime manager.
     */
    private void buildRuntimesList() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        Iterator<JavaRuntime> iter = rm.iterateRuntimes();
        while (iter.hasNext()) {
            JavaRuntime rt = iter.next();
            // Only allow the user to select valid runtimes.
            if (rt.isValid()) {
                NameValuePair<JavaRuntime> nvp =
                        new NameValuePair<JavaRuntime>(rt.getName(), rt);
                model.addElement(nvp);
            }
        }
        runtimeComboBox.setModel(model);
    }

    /**
     * Build and display a dialog for launching the debuggee.
     *
     * @return  true if user is ready to launch debuggee, false otherwise.
     */
    public boolean display() {
        // Collect the launch dialog elements.
        String title = NbBundle.getMessage(LaunchDebuggeePanel.class,
                "LBL_LaunchDebuggeeTitle");
        descriptor = new DialogDescriptor(this, title);
        descriptor.setHelpCtx(new HelpCtx("jswat-launch-debuggee"));
        addPropertyChangeListener(this);
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        rm.addRuntimeListener(this);
        Object value = DialogDisplayer.getDefault().notify(descriptor);
        removePropertyChangeListener(this);
        rm.removeRuntimeListener(this);
        return value == DialogDescriptor.OK_OPTION;
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

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(NotifyDescriptor.PROP_VALID)) {
            // The input validity has changed in some way.
            Boolean b = (Boolean) e.getNewValue();
            descriptor.setValid(b.booleanValue());
            validateInput();
        }
    }

    @Override
    public void runtimeAdded(RuntimeEvent event) {
        buildRuntimesList();
    }

    @Override
    public void runtimeRemoved(RuntimeEvent event) {
        buildRuntimesList();
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
     * Verify the user input and display an appropriate message.
     */
    private void validateInput() {
        boolean valid = true;
        String name = classNameField.getText();
        // Keep this in sync with the text field validation listener.
        if (name == null || name.length() == 0) {
            validationLabel.setText(NbBundle.getMessage(
                    LaunchDebuggeePanel.class, "ERR_ClassName_Missing"));
            valid = false;
        } else if (name.endsWith(".class") || name.endsWith(".java")) {
            validationLabel.setText(NbBundle.getMessage(
                    LaunchDebuggeePanel.class, "ERR_ClassName_File"));
            valid = false;
        }
        if (valid) {
            validationLabel.setText("   ");
        }
        // This fires an event only if the value changes.
        putClientProperty(NotifyDescriptor.PROP_VALID, Boolean.valueOf(valid));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        validationLabel = new javax.swing.JLabel();

        javaHomeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_Runtime").charAt(0));
        javaHomeLabel.setLabelFor(runtimeComboBox);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms"); // NOI18N
        javaHomeLabel.setText(bundle.getString("LBL_JavaHomeLabel")); // NOI18N

        runtimeComboBox.setToolTipText(bundle.getString("TIP_Launch_Runtimes")); // NOI18N
        runtimeComboBox.setPreferredSize(new java.awt.Dimension(200, 24));

        javaHomeButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_AddRuntime").charAt(0));
        javaHomeButton.setText(bundle.getString("LBL_JavaHomeBrowseButton")); // NOI18N
        javaHomeButton.setToolTipText(bundle.getString("TIP_Launch_AddRuntime")); // NOI18N
        javaHomeButton.setActionCommand("JavaHomeBrowse");
        javaHomeButton.setMargin(new java.awt.Insets(2, 5, 2, 5));

        javaParamsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_JvmParams").charAt(0));
        javaParamsLabel.setLabelFor(javaParamsTextArea);
        javaParamsLabel.setText(bundle.getString("LBL_JavaParamsLabel")); // NOI18N

        javaParamsTextArea.setLineWrap(true);
        javaParamsTextArea.setRows(3);
        javaParamsTextArea.setToolTipText(bundle.getString("TIP_Launch_JavaArguments")); // NOI18N
        javaParamsTextArea.setWrapStyleWord(true);
        javaParamsScrollPane.setViewportView(javaParamsTextArea);

        classNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_ClassName").charAt(0));
        classNameLabel.setLabelFor(classNameField);
        classNameLabel.setText(bundle.getString("LBL_ClassNameLabel")); // NOI18N

        classNameField.setToolTipText(bundle.getString("TIP_Launch_ClassName")); // NOI18N

        classParamsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_ClassParams").charAt(0));
        classParamsLabel.setLabelFor(classParamsTextArea);
        classParamsLabel.setText(bundle.getString("LBL_ClassParamsLabel")); // NOI18N

        classParamsTextArea.setLineWrap(true);
        classParamsTextArea.setRows(3);
        classParamsTextArea.setToolTipText(bundle.getString("TIP_Launch_ClassArguments")); // NOI18N
        classParamsTextArea.setWrapStyleWord(true);
        classParamsScrollPane.setViewportView(classParamsTextArea);

        resumeCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Launch_Resume").charAt(0));
        resumeCheckBox.setText(bundle.getString("LBL_Launch_Resume")); // NOI18N
        resumeCheckBox.setToolTipText(bundle.getString("TIP_Launch_Resume")); // NOI18N

        javax.swing.GroupLayout parametersPanelLayout = new javax.swing.GroupLayout(parametersPanel);
        parametersPanel.setLayout(parametersPanelLayout);
        parametersPanelLayout.setHorizontalGroup(
            parametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(parametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resumeCheckBox)
                    .addGroup(parametersPanelLayout.createSequentialGroup()
                        .addGroup(parametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaParamsLabel)
                            .addComponent(javaHomeLabel)
                            .addComponent(classParamsLabel)
                            .addComponent(classNameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(parametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(parametersPanelLayout.createSequentialGroup()
                                .addComponent(runtimeComboBox, 0, 378, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(javaHomeButton))
                            .addComponent(javaParamsScrollPane, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(classParamsScrollPane, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(classNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE))))
                .addContainerGap())
        );
        parametersPanelLayout.setVerticalGroup(
            parametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parametersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(parametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaHomeLabel)
                    .addComponent(javaHomeButton)
                    .addComponent(runtimeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(parametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(javaParamsLabel)
                    .addComponent(javaParamsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(parametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(classNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(parametersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(classParamsLabel)
                    .addComponent(classParamsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resumeCheckBox)
                .addContainerGap())
        );

        tabbedPane.addTab(bundle.getString("LBL_Launch_ParamsTabTitle"), parametersPanel); // NOI18N

        classpathPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        classpathPanel.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab(bundle.getString("LBL_Launch_ClasspathTabTitle"), classpathPanel); // NOI18N

        validationLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        validationLabel.setText("   ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(validationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                    .addComponent(tabbedPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(validationLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField classNameField;
    private javax.swing.JLabel classNameLabel;
    private javax.swing.JLabel classParamsLabel;
    private javax.swing.JScrollPane classParamsScrollPane;
    private javax.swing.JTextArea classParamsTextArea;
    private javax.swing.JPanel classpathPanel;
    private javax.swing.JButton javaHomeButton;
    private javax.swing.JLabel javaHomeLabel;
    private javax.swing.JLabel javaParamsLabel;
    private javax.swing.JScrollPane javaParamsScrollPane;
    private javax.swing.JTextArea javaParamsTextArea;
    private javax.swing.JPanel parametersPanel;
    private javax.swing.JCheckBox resumeCheckBox;
    private javax.swing.JComboBox runtimeComboBox;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel validationLabel;
    // End of variables declaration//GEN-END:variables
}
