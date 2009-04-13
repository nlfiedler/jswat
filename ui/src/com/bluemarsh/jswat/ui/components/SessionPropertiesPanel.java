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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.components;

import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.path.PathProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel that displays the session properties and allows editing them.
 *
 * @author  Nathan Fiedler
 */
public class SessionPropertiesPanel extends JPanel implements
        PropertyChangeListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** If non-null, save the input values to this Session. */
    private Session session;
    /** Editor for the classpath. */
    private PathEditorPanel classpathEditor;
    /** Editor for the sourcepath. */
    private PathEditorPanel sourcepathEditor;
    /** Descriptor used to display this panel. */
    private DialogDescriptor descriptor;

    /**
     * Creates new form SessionPropertiesPanel.
     */
    public SessionPropertiesPanel() {
        this(null);
    }

    /**
     * Creates new form SessionPropertiesPanel. If the session is given,
     * its values will be loaded into the dialog, and if the input is
     * valid when the dialog is dismissed, the values will be saved back
     * to the Session instance.
     *
     * @param  session  the Session to be edited (may be null).
     */
    public SessionPropertiesPanel(Session session) {
        initComponents();
        this.session = session;
        classpathEditor = new PathEditorPanel();
        classpathEditor.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        String title = NbBundle.getMessage(SessionPropertiesPanel.class,
                "LBL_ClasspathTabTitle");
        tabbedPane.addTab(title, classpathEditor);
        sourcepathEditor = new PathEditorPanel();
        sourcepathEditor.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        title = NbBundle.getMessage(SessionPropertiesPanel.class,
                "LBL_SourcepathTabTitle");
        tabbedPane.addTab(title, sourcepathEditor);

        nameTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent event) {
            }

            @Override
            public void insertUpdate(DocumentEvent event) {
                // This fires an event only if the value has changed.
                putClientProperty(NotifyDescriptor.PROP_VALID,
                        Boolean.valueOf(event.getDocument().getLength() > 0));
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                // This fires an event only if the value has changed.
                putClientProperty(NotifyDescriptor.PROP_VALID,
                        Boolean.valueOf(event.getDocument().getLength() > 0));
            }
        });

        sourcepathEditor.addListDataListener(new ListDataListener() {

            @Override
            public void contentsChanged(ListDataEvent event) {
                validateInput();
            }

            @Override
            public void intervalAdded(ListDataEvent event) {
                validateInput();
            }

            @Override
            public void intervalRemoved(ListDataEvent event) {
                validateInput();
            }
        });

        if (session != null) {
            loadParameters(session);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        nameTextField.requestFocusInWindow();
        addPropertyChangeListener(this);
    }

    /**
     * Build and display a dialog for editing the session properties.
     *
     * @return  true if user input is valid and ready to save, false otherwise.
     */
    public boolean display() {
        String title = NbBundle.getMessage(SessionPropertiesPanel.class,
                "LBL_SessionPropertiesTitle");
        descriptor = new DialogDescriptor(this, title);
        descriptor.setHelpCtx(new HelpCtx("jswat-session-properties"));
        Object value = DialogDisplayer.getDefault().notify(descriptor);
        return value == DialogDescriptor.OK_OPTION;
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
        List<String> sourcepath = pm.getSourcePath();
        sourcepathEditor.setPath(sourcepath);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(NotifyDescriptor.PROP_VALID)) {
            // The input validity has changed in some way.
            if (descriptor != null) {
                Boolean b = (Boolean) event.getNewValue();
                descriptor.setValid(b.booleanValue());
            }
            validateInput();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        removePropertyChangeListener(this);
        // Auto-save if the session was set earlier.
        Boolean b = (Boolean) getClientProperty(NotifyDescriptor.PROP_VALID);
        if (session != null && b.booleanValue()) {
            saveParameters(session);
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
        pm.setSourcePath(paths);
    }

    /**
     * Verify the user input and display an appropriate message.
     */
    private void validateInput() {
        boolean valid = true;
        String name = nameTextField.getText();
        if (name == null || name.length() == 0) {
            validationLabel.setText(NbBundle.getMessage(
                    SessionPropertiesPanel.class, "ERR_MissingSessionName"));
            valid = false;
        } else {
            // Validate the sourcepath; classpath is ignored.
            List<String> paths = sourcepathEditor.getPath();
            for (String path : paths) {
                File file = new File(path);
                if (!file.exists()) {
                    validationLabel.setText(
                        NbBundle.getMessage(SessionPropertiesPanel.class,
                        "ERR_PathDoesNotExist", path));
                    valid = false;
                    break;
                } else if (!file.canRead()) {
                    validationLabel.setText(
                        NbBundle.getMessage(SessionPropertiesPanel.class,
                        "ERR_CannotAccessPath", path));
                    valid = false;
                    break;
                }
            }
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
        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        stratumLabel = new javax.swing.JLabel();
        stratumTextField = new javax.swing.JTextField();
        validationLabel = new javax.swing.JLabel();

        nameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_SessionProperties_Name").charAt(0));
        nameLabel.setLabelFor(nameTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms"); // NOI18N
        nameLabel.setText(bundle.getString("LBL_NameLabel")); // NOI18N

        nameTextField.setColumns(10);
        nameTextField.setToolTipText(bundle.getString("TIP_SessionProperties_Name")); // NOI18N

        stratumLabel.setLabelFor(stratumTextField);
        stratumLabel.setText(bundle.getString("LBL_StratumLabel")); // NOI18N

        stratumTextField.setColumns(10);
        stratumTextField.setEditable(false);
        stratumTextField.setToolTipText(bundle.getString("HINT_SessionProperties_LanguageField")); // NOI18N

        javax.swing.GroupLayout namePanelLayout = new javax.swing.GroupLayout(namePanel);
        namePanel.setLayout(namePanelLayout);
        namePanelLayout.setHorizontalGroup(
            namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabel)
                    .addComponent(stratumLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stratumTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
                .addContainerGap())
        );
        namePanelLayout.setVerticalGroup(
            namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stratumLabel)
                    .addComponent(stratumTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tabbedPane.addTab(bundle.getString("LBL_NamePanelTitle"), namePanel); // NOI18N

        validationLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        validationLabel.setText("   ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(validationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                        .addGap(37, 37, 37))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(validationLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel namePanel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel stratumLabel;
    private javax.swing.JTextField stratumTextField;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel validationLabel;
    // End of variables declaration//GEN-END:variables
}
