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
 * $Id: RuntimeManagerPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.components;

import com.bluemarsh.jswat.core.CoreSettings;
import com.bluemarsh.jswat.core.runtime.JavaRuntime;
import com.bluemarsh.jswat.core.runtime.RuntimeFactory;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Displays the user interface for managing a set of runtimes.
 *
 * @author  Nathan Fiedler
 */
public class RuntimeManagerPanel extends javax.swing.JPanel
    implements ActionListener, ListSelectionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Dialog for displaying this panel. */
    private Dialog inputDialog;
    /** Editor panel for the runtime sources. */
    private PathEditorPanel sourceEditorPanel;
    /** This is the active list entry (the one being edited). */
    private int activeListEntry = -1;
    /** Label and action command for the Close button. */
    private String closeLabel;

    /**
     * Creates new form RuntimeManagerPanel.
     */
    public RuntimeManagerPanel() {
        initComponents();
        // Add instance of PathEditorPanel to the sourcesPanel
        sourceEditorPanel = new PathEditorPanel();
        sourcesPanel.add(sourceEditorPanel, BorderLayout.CENTER);
        // Become an action listener for add/remove buttons
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        runtimeList.addListSelectionListener(this);
        closeLabel = NbBundle.getMessage(getClass(), "LBL_CloseButton");
        CoreSettings cs = CoreSettings.getDefault();
        boolean hideFiles = !cs.getShowHiddenFiles();
        fileChooser.setFileHidingEnabled(hideFiles);
    }

    /**
     * Invoked by the press of a button.
     *
     * @param  event  action event.
     */
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals(closeLabel)) {
            ListModel model = runtimeList.getModel();
            if (activeListEntry != -1 && activeListEntry < model.getSize()) {
                // Validate the input before saving the data and closing.
                if (validateInput()) {
                    ListEntry le = (ListEntry) model.getElementAt(activeListEntry);
                    JavaRuntime rt = le.getRuntime();
                    saveRuntime(rt);
                    inputDialog.dispose();
                }
            } else {
                // Else there are no items and the user wants to leave.
                inputDialog.dispose();
            }

        } else if (cmd.equals("Add Runtime")) {
            // Display a file chooser for the user to select Java home.
            Frame frame = WindowManager.getDefault().getMainWindow();
            int response = fileChooser.showOpenDialog(frame);
            if (response == JFileChooser.APPROVE_OPTION) {
                File dir = fileChooser.getSelectedFile();
                if (validateDirectory(dir)) {
                    // Create the runtime based on the selected directory.
                    RuntimeManager rm = RuntimeProvider.getRuntimeManager();
                    String id = rm.generateIdentifier();
                    RuntimeFactory rf = RuntimeProvider.getRuntimeFactory();
                    JavaRuntime rt = rf.createRuntime(dir.getAbsolutePath(), id);
                    rm.add(rt);
                    // Add the new runtime to the list.
                    DefaultListModel model = (DefaultListModel) runtimeList.getModel();
                    model.addElement(new ListEntry(rt));
                    runtimeList.setSelectedIndex(model.getSize() - 1);
                    // Eable the editable fields now that a runtime is available.
                    setFieldsEnabled(true);
                }
            }

        } else if (cmd.equals("Remove")) {
            // Immediately remove the active runtime.
            DefaultListModel model = (DefaultListModel) runtimeList.getModel();
            ListEntry le = (ListEntry) model.elementAt(activeListEntry);
            model.removeElement(le);
            RuntimeManager rm = RuntimeProvider.getRuntimeManager();
            rm.remove(le.getRuntime());
            // If only one runtime left, disable remove buttton.
            if (model.size() == 1) {
                removeButton.setEnabled(false);
            }
            // At this point the list selection is zapped, so pretend that
            // there is nothing to edit and clear all the fields.
            clearAllFields();
        }
    }

    /**
     * Clears all the fields (read-only and writable) and disables them.
     */
    private void clearAllFields() {
        removeButton.setEnabled(false);
        execTextField.setEnabled(false);
        nameTextField.setText("");
        baseTextField.setText("");
        execTextField.setText("");
        sourceEditorPanel.setWritable(false);
        List<String> empty = Collections.emptyList();
        sourceEditorPanel.setPath(empty);
    }

    /**
     * Build and display a dialog for editing the runtimes.
     */
    public void display() {
        // Iterate the available runtimes, adding them to the list.
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        Iterator<JavaRuntime> iter = rm.iterateRuntimes();
        DefaultListModel model = new DefaultListModel();
        while (iter.hasNext()) {
            JavaRuntime rt = iter.next();
            model.addElement(new ListEntry(rt));
        }
        runtimeList.setModel(model);
        if (model.size() == 0) {
            // Disable the editable fields until a runtime is added.
            setFieldsEnabled(false);
        } else {
            // Perform the initial enabling logic on the remove button.
            setFieldsEnabled(true);
            runtimeList.setSelectedIndex(0);
        }

        // Collect the dialog elements.
        String title = NbBundle.getMessage(getClass(), "LBL_RuntimeManager_Title");
        // Display dialog and get the user response.
        Object[] options = { closeLabel };
        DialogDescriptor dd = new DialogDescriptor(
                this, title, true, options,
                DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN,
                new HelpCtx("jswat-runtime-manager"), this);
        dd.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
        inputDialog = DialogDisplayer.getDefault().createDialog(dd);
        // Set the default focus (once there is a parent window).
        runtimeList.requestFocusInWindow();
        inputDialog.setVisible(true);
        // (blocks until dialog is disposed...)
    }

    /**
     * Load the fields with the attributes from the given runtime.
     *
     * @param  rt  runtime to be displayed.
     */
    private void loadRuntime(JavaRuntime rt) {
        nameTextField.setText(rt.getName());
        baseTextField.setText(rt.getBase());
        execTextField.setText(rt.getExec());
        List<String> files = rt.getSources();
        if (files == null || files.isEmpty()) {
            // Clear the path editor.
            List<String> empty = Collections.emptyList();
            sourceEditorPanel.setPath(empty);
        } else {
            sourceEditorPanel.setPath(files);
        }
    }

    /**
     * Save the fields to the given runtime.
     *
     * @param  rt  runtime to be modified.
     */
    private void saveRuntime(JavaRuntime rt) {
        rt.setExec(execTextField.getText());
        List<String> paths = sourceEditorPanel.getPath();
        rt.setSources(paths);
    }

    /**
     * Enables or disables the editable fields of our panel.
     *
     * @param  enable  true to enable, false to disable.
     */
    private void setFieldsEnabled(boolean enable) {
        execTextField.setEnabled(enable);
        sourceEditorPanel.setWritable(enable);
        ListModel model = runtimeList.getModel();
        if (model.getSize() > 1) {
            // We allow deletion only when multiple runtimes exist
            // (i.e. user can never delete all of the runtimes).
            removeButton.setEnabled(enable);
        } else {
            removeButton.setEnabled(false);
        }
    }

    /**
     * Validates the given directory to ensure that it exists, is readable,
     * is really a directory, and is accessible. If invalid, a message will
     * be displayed at the bottom of the dialog.
     *
     * @param  dir  directory to validate.
     * @return  true if directory is valid, false if invalid.
     */
    private boolean validateDirectory(File dir) {
        boolean valid = false;
        try {
            if (!dir.exists()) {
                inputMessageLabel.setText(
                    NbBundle.getMessage(getClass(), "ERR_DirDoesNotExist"));
            } else if (!dir.canRead()) {
                inputMessageLabel.setText(
                    NbBundle.getMessage(getClass(), "ERR_CannotAccessDir"));
            } else if (!dir.isDirectory()) {
                inputMessageLabel.setText(
                    NbBundle.getMessage(getClass(), "ERR_FileIsNotDirectory"));
            } else {
                valid = true;
            }
        } catch (SecurityException se) {
            inputMessageLabel.setText(
                NbBundle.getMessage(getClass(), "ERR_CannotAccessDir"));
        }
        return valid;
    }

    /**
     * Validates the input for this dialog. If invalid, a message will be
     * displayed at the bottom of the dialog.
     *
     * @return  true if input valid, false if invalid.
     */
    private boolean validateInput() {
        assert activeListEntry != -1;
        boolean valid = true;
        // Clear any stale text from the message field.
        inputMessageLabel.setText("");
        String exec = execTextField.getText();
        if (exec == null || exec.length() == 0) {
            inputMessageLabel.setText(
                    NbBundle.getMessage(getClass(),
                    "ERR_RuntimeManager_ExecRequired"));
            valid = false;
        } else {
            ListModel model = runtimeList.getModel();
            ListEntry le = (ListEntry) model.getElementAt(activeListEntry);
            JavaRuntime rt = le.getRuntime();
            File bd = new File(rt.getBase());
            File execFile = rt.findExecutable(bd, exec);
            if (execFile == null) {
                inputMessageLabel.setText(
                        NbBundle.getMessage(getClass(),
                        "ERR_RuntimeManager_BadExec", exec));
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Called whenever the value of the selection changes.
     *
     * @param  event  the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            // When selection changes, save editable fields to old selected
            // runtime, then set field contents to newly selected runtime
            // attributes.
            ListModel model = runtimeList.getModel();
            // Must check index against list size in the event an entry
            // was removed and the selection is changing.
            if (activeListEntry != -1 && activeListEntry < model.getSize()) {
                // Validate the input before switching selection.
                if (validateInput()) {
                    ListEntry le = (ListEntry) model.getElementAt(activeListEntry);
                    JavaRuntime rt = le.getRuntime();
                    if (rt != null) {
                        saveRuntime(rt);
                    }
                }
            }

            activeListEntry = runtimeList.getSelectedIndex();
            // Have to validate the selected index as it can be anything.
            if (activeListEntry != -1 && activeListEntry < model.getSize()) {
                ListEntry le = (ListEntry) model.getElementAt(activeListEntry);
                JavaRuntime rt = le.getRuntime();
                loadRuntime(rt);
                setFieldsEnabled(true);
            } else {
                clearAllFields();
            }
        }
    }

    /**
     * Represents a Runtime instance in the JList.
     */
    private static class ListEntry {
        /** The runtime that we represent. */
        private JavaRuntime runtime;

        /**
         * Constructs a new instance of ListEntry.
         *
         * @param  rt  runtime to represent.
         */
        public ListEntry(JavaRuntime rt) {
            runtime = rt;
        }

        /**
         * @return  the runtime.
         */
        public JavaRuntime getRuntime() {
            return runtime;
        }

        /**
         * @return  the runtime name.
         */
        public String toString() {
            return runtime.getName();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        fileChooser = new javax.swing.JFileChooser();
        runtimeList = new javax.swing.JList();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        baseLabel = new javax.swing.JLabel();
        baseTextField = new javax.swing.JTextField();
        execLabel = new javax.swing.JLabel();
        execTextField = new javax.swing.JTextField();
        srcLabel = new javax.swing.JLabel();
        sourcesPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        inputMessageLabel = new javax.swing.JLabel();

        fileChooser.setDialogTitle(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_RuntimeChooser"));
        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        runtimeList.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        runtimeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_RuntimeManager_NameLabel"));

        nameTextField.setEditable(false);

        baseLabel.setLabelFor(baseTextField);
        baseLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_RuntimeManager_BaseLabel"));

        baseTextField.setColumns(10);
        baseTextField.setEditable(false);

        execLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_RuntimeManager_Executable").charAt(0));
        execLabel.setLabelFor(execTextField);
        execLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_RuntimeManager_ExecLabel"));

        execTextField.setColumns(10);
        execTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_RuntimeManager_ExecName"));

        srcLabel.setLabelFor(sourcesPanel);
        srcLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_RuntimeManager_SourceLabel"));

        sourcesPanel.setLayout(new java.awt.BorderLayout());

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_RuntimeManager_Add").charAt(0));
        addButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_RuntimeManager_AddButton"));
        addButton.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_RuntimeManager_Add"));

        removeButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_RuntimeManager_Remove").charAt(0));
        removeButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_RemoveButton"));
        removeButton.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_RuntimeManager_Remove"));

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
                        .add(runtimeList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 182, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(nameLabel)
                                    .add(baseLabel)
                                    .add(execLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(execTextField)
                                    .add(baseTextField)
                                    .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(sourcesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(srcLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))))
                    .add(layout.createSequentialGroup()
                        .add(inputMessageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(nameLabel)
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(baseLabel)
                            .add(baseTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(execLabel)
                            .add(execTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(srcLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sourcesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.BASELINE, runtimeList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(removeButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(inputMessageLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel baseLabel;
    private javax.swing.JTextField baseTextField;
    private javax.swing.JLabel execLabel;
    private javax.swing.JTextField execTextField;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel inputMessageLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JList runtimeList;
    private javax.swing.JPanel sourcesPanel;
    private javax.swing.JLabel srcLabel;
    // End of variables declaration//GEN-END:variables
    
}
