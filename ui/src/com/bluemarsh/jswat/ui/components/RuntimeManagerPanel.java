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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: RuntimeManagerPanel.java 22 2007-12-25 07:30:24Z nfiedler $
 */

package com.bluemarsh.jswat.ui.components;

import com.bluemarsh.jswat.core.CoreSettings;
import com.bluemarsh.jswat.core.runtime.JavaRuntime;
import com.bluemarsh.jswat.core.runtime.RuntimeFactory;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import com.bluemarsh.jswat.core.util.NameValuePair;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Displays the user interface for managing a set of runtimes.
 *
 * @author  Nathan Fiedler
 */
public class RuntimeManagerPanel extends JPanel {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Editor panel for the runtime sources. */
    private PathEditorPanel sourceEditorPanel;
    /** This is the active list entry (the one being edited). */
    private JavaRuntime activeListEntry;
    /** Dialog descriptor used to display this panel. */
    private DialogDescriptor descriptor;

    /**
     * Creates new form RuntimeManagerPanel.
     */
    public RuntimeManagerPanel() {
        initComponents();
        // Add instance of PathEditorPanel to the sourcesPanel
        sourceEditorPanel = new PathEditorPanel();
        sourcesPanel.add(sourceEditorPanel, BorderLayout.CENTER);
        CoreSettings cs = CoreSettings.getDefault();
        boolean hideFiles = !cs.getShowHiddenFiles();
        fileChooser.setFileHidingEnabled(hideFiles);
        runtimeList.setCellRenderer(new RuntimeRenderer());

        // Listener that reacts to document changes only after a key
        // has been pressed for that corresponding text field. Avoids
        // responding to changes made via the setText() method.
        class KeyInputListener extends KeyAdapter implements DocumentListener {
            private boolean keyPressed;

            @Override
            public void keyPressed(KeyEvent event) {
                keyPressed = true;
            }

            public void changedUpdate(DocumentEvent event) {
            }

            public void insertUpdate(DocumentEvent event) {
                validate(event);
            }

            public void removeUpdate(DocumentEvent event) {
                validate(event);
            }

            private void validate(DocumentEvent event) {
                if (keyPressed) {
                    Document document = event.getDocument();
                    boolean valid = false;
                    if (document.getLength() > 0) {
                        try {
                            // Keep this in sync with the validateInput() method.
                            String exec = document.getText(0, document.getLength());
                            File bd = new File(activeListEntry.getBase());
                            File execFile = activeListEntry.findExecutable(bd, exec);
                            if (execFile != null) {
                                valid = true;
                            }
                        } catch (BadLocationException ble) {
                        }
                    }
                    // This fires an event only if the value has changed.
                    putClientProperty(NotifyDescriptor.PROP_VALID,
                            Boolean.valueOf(valid));
                    keyPressed = false;
                }
            }
        }
        KeyInputListener kil = new KeyInputListener();
        execTextField.getDocument().addDocumentListener(kil);
        execTextField.addKeyListener(kil);

        runtimeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    // Save whatever the user entered to the active runtime,
                    // even if the input is invalid.
                    if (activeListEntry != null) {
                        saveRuntime(activeListEntry);
                    }
                    ListModel model = runtimeList.getModel();
                    int index = runtimeList.getSelectedIndex();
                    // Must validate the selected index as it can be anything.
                    if (index != -1 && index < model.getSize()) {
                        NameValuePair<?> pair = (NameValuePair<?>)
                                model.getElementAt(index);
                        activeListEntry = (JavaRuntime) pair.getValue();
                        loadRuntime(activeListEntry);
                        setFieldsEnabled(true);
                        // Make a statement about this loaded runtime.
                        validateInput();
                    } else {
                        activeListEntry = null;
                        clearAllFields();
                    }
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
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
                        // Add the new runtime to the list.
                        DefaultListModel model = (DefaultListModel)
                                runtimeList.getModel();
                        model.addElement(new NameValuePair<JavaRuntime>(
                                rt.getName(), rt));
                        runtimeList.setSelectedIndex(model.getSize() - 1);
                        // Enable the editable fields now that a runtime is available.
                        setFieldsEnabled(true);
                        // Force the new selection to be validated in case
                        // the user selected something that does not appear
                        // to be a Java runtime.
                        validateInput();
                    }
                }
            }

            private boolean validateDirectory(File dir) {
                // Validates the given directory to ensure that it exists,
                // is readable, is really a directory, and is accessible.
                boolean valid = false;
                try {
                    if (!dir.exists()) {
                        validationLabel.setText(NbBundle.getMessage(
                                RuntimeManagerPanel.class, "ERR_DirDoesNotExist"));
                    } else if (!dir.canRead()) {
                        validationLabel.setText(NbBundle.getMessage(
                                RuntimeManagerPanel.class, "ERR_CannotAccessDir"));
                    } else if (!dir.isDirectory()) {
                        validationLabel.setText(NbBundle.getMessage(
                                RuntimeManagerPanel.class, "ERR_FileIsNotDirectory"));
                    } else {
                        valid = true;
                    }
                } catch (SecurityException se) {
                    validationLabel.setText(NbBundle.getMessage(
                            RuntimeManagerPanel.class, "ERR_CannotAccessDir"));
                }
                return valid;
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // Immediately remove the active runtime.
                DefaultListModel model = (DefaultListModel) runtimeList.getModel();
                int index = runtimeList.getSelectedIndex();
                NameValuePair<?> pair = (NameValuePair<?>) model.elementAt(index);
                model.removeElement(pair);
                // If only one runtime left, disable remove button.
                if (model.size() == 1) {
                    removeButton.setEnabled(false);
                }
                // Set the selection to something, to load the fields
                // with new values and force input validation.
                runtimeList.setSelectedIndex(Math.max(0, index - 1));
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        runtimeList.requestFocusInWindow();
        validateInput();
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
        // Build up the available runtimes list.
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        Iterator<JavaRuntime> iter = rm.iterateRuntimes();
        DefaultListModel model = new DefaultListModel();
        while (iter.hasNext()) {
            // Clone so we avoid clobbering the runtime's deep structure.
            JavaRuntime rt = iter.next().clone();
            model.addElement(new NameValuePair<JavaRuntime>(rt.getName(), rt));
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

        String title = NbBundle.getMessage(RuntimeManagerPanel.class,
                "LBL_RuntimeManager_Title");
        descriptor = new DialogDescriptor(this, title);
        descriptor.setHelpCtx(new HelpCtx("jswat-runtime-manager"));
        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(NotifyDescriptor.PROP_VALID)) {
                    // The input validity has changed in some way.
                    Boolean b = (Boolean) e.getNewValue();
                    descriptor.setValid(b.booleanValue());
                    validateInput();
                }
            }
        };
        addPropertyChangeListener(pcl);
        Object value = DialogDisplayer.getDefault().notify(descriptor);
        removePropertyChangeListener(pcl);
        if (value == DialogDescriptor.OK_OPTION) {
            if (activeListEntry != null) {
                saveRuntime(activeListEntry);
            }
            // Re-populate the runtimes being managed with our list.
            // Those runtimes that are the same as before will still
            // be referenceable via their unchanging identifiers.
            iter = rm.iterateRuntimes();
            List<JavaRuntime> list = new ArrayList<JavaRuntime>();
            while (iter.hasNext()) {
                list.add(iter.next());
            }
            for (JavaRuntime rt : list) {
                rm.remove(rt);
            }
            ListModel lm = runtimeList.getModel();
            int size = lm.getSize();
            for (int ii = 0; ii < size; ii++) {
                NameValuePair<?> pair = (NameValuePair<?>) lm.getElementAt(ii);
                JavaRuntime rt = (JavaRuntime) pair.getValue();
                // Save only the valid entries.
                if (rt.isValid()) {
                    rm.add(rt);
                }
            }
        }
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
     * Verify the user input and display an appropriate message.
     */
    private void validateInput() {
        boolean valid = true;
        // Only need validation if an entry is actively being edited.
        if (activeListEntry != null) {
            String exec = execTextField.getText();
            if (exec == null || exec.length() == 0) {
                validationLabel.setText(NbBundle.getMessage(
                        RuntimeManagerPanel.class, "ERR_RuntimeManager_ExecRequired"));
                valid = false;
            } else {
                File bd = new File(activeListEntry.getBase());
                File execFile = activeListEntry.findExecutable(bd, exec);
                if (execFile == null) {
                    validationLabel.setText(NbBundle.getMessage(
                            RuntimeManagerPanel.class,
                            "ERR_RuntimeManager_BadExec", exec));
                    valid = false;
                }
            }
        }
        if (valid) {
            validationLabel.setText("   ");
        }
        // This fires an event only if the value changes.
        putClientProperty(NotifyDescriptor.PROP_VALID, Boolean.valueOf(valid));
    }

    /**
     * Renders the entries in the runtimes list.
     */
    private static class RuntimeRenderer extends DefaultListCellRenderer {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean hasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, hasFocus);
            NameValuePair<?> pair = (NameValuePair<?>) value;
            JavaRuntime rt = (JavaRuntime) pair.getValue();
            if (!rt.isValid()) {
                label.setFont(label.getFont().deriveFont(Font.ITALIC));
            }
            return label;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        validationLabel = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms"); // NOI18N
        fileChooser.setDialogTitle(bundle.getString("LBL_RuntimeChooser")); // NOI18N
        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        runtimeList.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        runtimeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText(bundle.getString("LBL_RuntimeManager_NameLabel")); // NOI18N

        nameTextField.setEditable(false);

        baseLabel.setLabelFor(baseTextField);
        baseLabel.setText(bundle.getString("LBL_RuntimeManager_BaseLabel")); // NOI18N

        baseTextField.setColumns(10);
        baseTextField.setEditable(false);

        execLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_RuntimeManager_Executable").charAt(0));
        execLabel.setLabelFor(execTextField);
        execLabel.setText(bundle.getString("LBL_RuntimeManager_ExecLabel")); // NOI18N

        execTextField.setColumns(10);
        execTextField.setToolTipText(bundle.getString("TIP_RuntimeManager_ExecName")); // NOI18N

        srcLabel.setLabelFor(sourcesPanel);
        srcLabel.setText(bundle.getString("LBL_RuntimeManager_SourceLabel")); // NOI18N

        sourcesPanel.setLayout(new java.awt.BorderLayout());

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_RuntimeManager_Add").charAt(0));
        addButton.setText(bundle.getString("LBL_RuntimeManager_AddButton")); // NOI18N
        addButton.setToolTipText(bundle.getString("TIP_RuntimeManager_Add")); // NOI18N

        removeButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_RuntimeManager_Remove").charAt(0));
        removeButton.setText(bundle.getString("LBL_RemoveButton")); // NOI18N
        removeButton.setToolTipText(bundle.getString("TIP_RuntimeManager_Remove")); // NOI18N

        validationLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        validationLabel.setText("   ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(validationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(runtimeList, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(nameLabel)
                                    .addComponent(baseLabel)
                                    .addComponent(execLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(execTextField)
                                    .addComponent(baseTextField)
                                    .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addComponent(sourcesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(srcLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameLabel)
                            .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(baseLabel)
                            .addComponent(baseTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(execLabel)
                            .addComponent(execTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(srcLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourcesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(runtimeList, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(validationLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel baseLabel;
    private javax.swing.JTextField baseTextField;
    private javax.swing.JLabel execLabel;
    private javax.swing.JTextField execTextField;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JList runtimeList;
    private javax.swing.JPanel sourcesPanel;
    private javax.swing.JLabel srcLabel;
    private javax.swing.JLabel validationLabel;
    // End of variables declaration//GEN-END:variables
}
