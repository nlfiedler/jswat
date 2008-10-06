/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionPropertiesPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.components;

import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.path.PathProvider;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel that displays the session properties and allows editing them.
 *
 * @author  Nathan Fiedler
 */
public class SessionPropertiesPanel extends javax.swing.JPanel
    implements ActionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Dialog for displaying this panel. */
    private Dialog inputDialog;
    /** True if the user has provided valid input and is ready,
     * false otherwise. */
    private boolean okayToGo;
    /** If non-null, save the session parameters to this Session. */
    private Session commitSession;
    /** Editor for the classpath. */
    private PathEditorPanel classpathEditor;
    /** Editor for the sourcepath. */
    private PathEditorPanel sourcepathEditor;

    /**
     * Creates new form SessionPropertiesPanel.
     */
    public SessionPropertiesPanel() {
        initComponents();
        classpathEditor = new PathEditorPanel();
        String title = NbBundle.getMessage(getClass(), "LBL_ClasspathTabTitle");
        tabbedPane.addTab(title, classpathEditor);
        sourcepathEditor = new PathEditorPanel();
        title = NbBundle.getMessage(getClass(), "LBL_SourcepathTabTitle");
        tabbedPane.addTab(title, sourcepathEditor);
    }

    /**
     * Invoked by the press of a button.
     *
     * @param  event  action event.
     */
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("OK")) {
            if (validateInput()) {
                if (commitSession != null) {
                    // We are to auto-commit the user's changes.
                    saveParameters(commitSession);
                }
                okayToGo = true;
                inputDialog.dispose();
            }
        } else {
            inputDialog.dispose();
        }
    }

    /**
     * Constructs the dialog to contain this panel.
     *
     * @return  dialog for displaying this panel.
     */
    private Dialog construct() {
        // Collect the dialog elements.
        String title = NbBundle.getMessage(getClass(), "LBL_SessionPropertiesTitle");
        // Display dialog and get the user response.
        DialogDescriptor dd = new DialogDescriptor(
                this, title, true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN,
                new HelpCtx("jswat-session-properties"), this);
        dd.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        // Set the default focus (once there is a parent window).
        nameTextField.requestFocusInWindow();
        return dialog;
    }

    /**
     * Construct a dialog for this panel and prepare to auto-commit the
     * changes to the given session. This method will load the parameters,
     * construct the dialog, then when the dialog is dismissed via the Ok
     * button, with valid input, the parameters will be commited back to
     * this session.
     *
     * @param  session  Session to be customized.
     * @return  dialog to display this panel.
     */
    public Dialog customize(Session session) {
        loadParameters(session);
        inputDialog = construct();
        commitSession = session;
        return inputDialog;
    }

    /**
     * Build and display a dialog for editing the session properties.
     *
     * @return  true if user input is valid and ready to go, false otherwise.
     */
    public boolean display() {
        inputDialog = construct();
        okayToGo = false;
        inputDialog.setVisible(true);
        // (blocks until dialog is disposed...)
        return okayToGo;
    }

    /**
     * Initialize the fields of this panel using the given Session.
     *
     * @param  session  session to be displayed and edited.
     */
    public void loadParameters(Session session) {
        // Load the basic session properties.
        String name = session.getProperty(Session.PROP_SESSION_NAME);
        nameTextField.setText(name);
        stratumTextField.setText(session.getStratum());
        if (session.isConnected()) {
            // Not allowed to edit classpath will session is active, because
            // doing so would not affect the debuggee until it was re-launched.
            classpathEditor.setWritable(false);
        }

        // Load the classpath setting.
        PathManager pm = PathProvider.getPathManager(session);
        List<String> classpath = pm.getClassPath();
        if (classpath != null && classpath.size() > 0) {
            classpathEditor.setPath(classpath);
        }

        // Load the sourcepath setting.
        List<FileObject> sourcepath = pm.getSourcePath();
        if (sourcepath != null && sourcepath.size() > 0) {
            List<String> srcpath = new LinkedList<String>();
            for (FileObject fo : sourcepath) {
                String path = FileUtil.getFileDisplayName(fo);
                srcpath.add(path);
            }
            sourcepathEditor.setPath(srcpath);
        }
    }

    /**
     * Save the editable properties of this panel to the given Session.
     *
     * @param  session  session to which settings are saved.
     */
    public void saveParameters(Session session) {
        // Save the basic session properties.
        String name = nameTextField.getText();
        session.setProperty(Session.PROP_SESSION_NAME, name);

        // Save the classpath setting.
        PathManager pm = PathProvider.getPathManager(session);
        if (!session.isConnected()) {
            // Okay to save classpath if session is inactive.
            List<String> paths = classpathEditor.getPath();
            pm.setClassPath(paths);
        }

        // Save the sourcepath setting.
        List<String> paths = sourcepathEditor.getPath();
        List<FileObject> roots = new LinkedList<FileObject>();
        for (String path : paths) {
            File file = new File(path);
            file = FileUtil.normalizeFile(file);
            FileObject fo = FileUtil.toFileObject(file);
            roots.add(fo);
        }
        pm.setSourcePath(roots);
    }

    /**
     * Validates the input for this dialog. If invalid, a message will be
     * displayed at the bottom of the dialog.
     *
     * @return  true if input valid, false if invalid.
     */
    private boolean validateInput() {
        boolean valid = true;
        String name = nameTextField.getText();
        if (name == null || name.length() == 0) {
            inputMessageLabel.setText(NbBundle.getMessage(getClass(), "ERR_MissingSessionName"));
            valid = false;
        } else {
            // Validate the sourcepath; classpath is ignored.
            List<String> paths = sourcepathEditor.getPath();
            for (String path : paths) {
                File file = new File(path);
                if (!file.exists()) {
                    inputMessageLabel.setText(
                        NbBundle.getMessage(getClass(), "ERR_PathDoesNotExist", path));
                    valid = false;
                    break;
                } else if (!file.canRead()) {
                    inputMessageLabel.setText(
                        NbBundle.getMessage(getClass(), "ERR_CannotAccessPath", path));
                    valid = false;
                    break;
                }
            }
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
        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        stratumLabel = new javax.swing.JLabel();
        stratumTextField = new javax.swing.JTextField();
        inputMessageLabel = new javax.swing.JLabel();

        nameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_SessionProperties_Name").charAt(0));
        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_NameLabel"));

        nameTextField.setColumns(10);
        nameTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_SessionProperties_Name"));

        stratumLabel.setLabelFor(stratumTextField);
        stratumLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_StratumLabel"));

        stratumTextField.setColumns(10);
        stratumTextField.setEditable(false);
        stratumTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_SessionProperties_LanguageField"));

        org.jdesktop.layout.GroupLayout namePanelLayout = new org.jdesktop.layout.GroupLayout(namePanel);
        namePanel.setLayout(namePanelLayout);
        namePanelLayout.setHorizontalGroup(
            namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameLabel)
                    .add(stratumLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(stratumTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
                .addContainerGap())
        );
        namePanelLayout.setVerticalGroup(
            namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(namePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(stratumLabel)
                    .add(stratumTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        tabbedPane.addTab(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_NamePanelTitle"), namePanel);

        inputMessageLabel.setForeground(new java.awt.Color(255, 0, 0));
        inputMessageLabel.setText("   ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(inputMessageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(tabbedPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tabbedPane)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(inputMessageLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel inputMessageLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel namePanel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel stratumLabel;
    private javax.swing.JTextField stratumTextField;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
    
}
