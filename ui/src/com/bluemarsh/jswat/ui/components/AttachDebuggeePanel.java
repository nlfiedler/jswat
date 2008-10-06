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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AttachDebuggeePanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.components;

import com.bluemarsh.jswat.core.connect.ConnectionFactory;
import com.bluemarsh.jswat.core.connect.ConnectionProvider;
import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.connect.NoAttachingConnectorException;
import com.bluemarsh.jswat.core.connect.NoListeningConnectorException;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.NameValuePair;
import com.sun.jdi.connect.Connector;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * The panel containing the components for the debuggee attach action.
 *
 * @author  Nathan Fiedler
 */
public class AttachDebuggeePanel extends javax.swing.JPanel
        implements ActionListener, ItemListener {
    /** Name of property value for resume immediately. */
    private static final String PROP_RESUME = "ResumeAttachee";
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Label and action command for the Attach button. */
    private String attachLabel;
    /** Dialog for displaying this panel. */
    private Dialog inputDialog;
    /** True if the user has provided valid input and wants to launch,
     * false otherwise. */
    private boolean okayToGo;

    /**
     * How to connect to the debuggee.
     */
    private enum Transport {
        ATTACH_SHARED, ATTACH_SOCKET, LISTEN_SHARED, LISTEN_SOCKET
    };

    /**
     * Creates new form AttachDebuggeePanel.
     */
    public AttachDebuggeePanel() {
        initComponents();
        SpinnerNumberModel snm = (SpinnerNumberModel) portSpinner.getModel();
        snm.setValue(new Integer(1));
        snm.setMinimum(new Integer(1));
        snm.setMaximum(new Integer(65535));
        attachLabel = NbBundle.getMessage(getClass(), "LBL_AttachButton");
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        Connector connector = ConnectionProvider.getAttachingConnector("dt_shmem");
        if (connector != null) {
            NameValuePair<Transport> pair = new NameValuePair<Transport>(
                    NbBundle.getMessage(getClass(), "CTL_Attach_Shared"),
                    Transport.ATTACH_SHARED);
            model.addElement(pair);
        }
        connector = ConnectionProvider.getAttachingConnector("dt_socket");
        if (connector != null) {
            NameValuePair<Transport> pair = new NameValuePair<Transport>(
                    NbBundle.getMessage(getClass(), "CTL_Attach_Socket"),
                    Transport.ATTACH_SOCKET);
            model.addElement(pair);
        }
        connector = ConnectionProvider.getListeningConnector("dt_shmem");
        if (connector != null) {
            NameValuePair<Transport> pair = new NameValuePair<Transport>(
                    NbBundle.getMessage(getClass(), "CTL_Listen_Shared"),
                    Transport.LISTEN_SHARED);
            model.addElement(pair);
        }
        connector = ConnectionProvider.getListeningConnector("dt_socket");
        if (connector != null) {
            NameValuePair<Transport> pair = new NameValuePair<Transport>(
                    NbBundle.getMessage(getClass(), "CTL_Listen_Socket"),
                    Transport.LISTEN_SOCKET);
            model.addElement(pair);
        }
        connectorComboBox.setModel(model);
        if (model.getSize() > 0) {
            connectorComboBox.addItemListener(this);
            connectorComboBox.setSelectedIndex(0);
            NameValuePair<?> pair =
                    (NameValuePair<?>) connectorComboBox.getSelectedItem();
            showConnectorPanel(pair);
        }
    }

    /**
     * Invoked by the press of a button.
     *
     * @param  event  action event.
     */
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals(attachLabel)) {
            if (validateInput()) {
                okayToGo = true;
                inputDialog.dispose();
            }
        } else {
            inputDialog.dispose();
        }
    }

    /**
     * Builds a JvmConnection instance based on the input values.
     *
     * @return  new connection instance, or null if something was wrong.
     * @throws  NoAttachingConnectorException
     *          if selected connector is not available.
     */
    public JvmConnection buildConnection() throws NoAttachingConnectorException,
            NoListeningConnectorException{
        // Build a JvmConnection from the input field values.
        JvmConnection connection = null;
        NameValuePair<?> pair =
                (NameValuePair<?>) connectorComboBox.getSelectedItem();
        Transport type = (Transport) pair.getValue();
        ConnectionFactory factory = ConnectionProvider.getConnectionFactory();
        switch (type) {
            case ATTACH_SHARED:
                String name = shmemTextField.getText();
                connection = factory.createShared(name);
                break;
            case ATTACH_SOCKET:
                String host = hostTextField.getText();
                Integer pval = (Integer) portSpinner.getValue();
                String port = pval.toString();
                connection = factory.createSocket(host, port);
                break;
            case LISTEN_SHARED:
                name = shmemTextField.getText();
                connection = factory.createListening("dt_shmem", null, name);
                break;
            case LISTEN_SOCKET:
                host = hostTextField.getText();
                pval = (Integer) portSpinner.getValue();
                port = pval.toString();
                connection = factory.createListening("dt_socket", host, port);
                break;
        }
        return connection;
    }

    /**
     * Build and display a dialog for launching the debuggee.
     *
     * @return  true if user input is valid and ready to launch debuggee,
     *          false otherwise.
     */
    public boolean display() {
        // Collect the launch dialog elements.
        String title = NbBundle.getMessage(getClass(), "LBL_AttachDebuggeeTitle");
        String closeLabel = NbBundle.getMessage(getClass(), "LBL_CloseButton");
        Object[] options = { attachLabel, closeLabel };
        // Display dialog and get the user response.
        DialogDescriptor dd = new DialogDescriptor(
            this, title, true, options, attachLabel, DialogDescriptor.BOTTOM_ALIGN,
            new HelpCtx("jswat-attach-debuggee"), this);
        inputDialog = DialogDisplayer.getDefault().createDialog(dd);
        // Set the default focus (once there is a parent window).
        connectorComboBox.requestFocusInWindow();
        okayToGo = false;
        inputDialog.setVisible(true);
        // (blocks until dialog is disposed...)
        return okayToGo;
    }

    /**
     * Invoked when an item has been selected or deselected by the user.
     *
     * @param  e  item event.
     */
    public void itemStateChanged(ItemEvent e) {
        ItemSelectable selc = e.getItemSelectable();
        Object[] sels = selc.getSelectedObjects();
        if (sels != null && sels.length == 1) {
            NameValuePair<?> pair = (NameValuePair<?>) sels[0];
            showConnectorPanel(pair);
        }
    }

    /**
     * Load the launch parameters from the given properties.
     *
     * @param  session  Session from which launch parameters are read.
     */
    public void loadParameters(Session session) {
        shmemTextField.setText(session.getProperty(Session.PROP_SHARED_NAME));
        hostTextField.setText(session.getProperty(Session.PROP_SOCKET_HOST));
        String port = session.getProperty(Session.PROP_SOCKET_PORT);
        if (port != null) {
            try {
                Integer p = new Integer(port);
                portSpinner.setValue(p);
            } catch (NumberFormatException nfe) {
                // fall through...
            }
        }
        String connector = session.getProperty(Session.PROP_CONNECTOR);
        if (connector != null) {
            if (connector.equals(Session.PREF_SHARED)) {
                selectConnector(Transport.ATTACH_SHARED);
            } else if (connector.equals(Session.PREF_SOCKET)) {
                selectConnector(Transport.ATTACH_SOCKET);
            } else if (connector.equals(Session.PREF_SHARED_LISTEN)) {
                selectConnector(Transport.LISTEN_SHARED);
            } else if (connector.equals(Session.PREF_SOCKET_LISTEN)) {
                selectConnector(Transport.LISTEN_SOCKET);
            } else {
                // Unknown value, default to socket.
                selectConnector(Transport.ATTACH_SOCKET);
            }
        } else {
            // Undefined value, default to socket.
            selectConnector(Transport.ATTACH_SOCKET);
        }
        String value = session.getProperty(PROP_RESUME);
        Boolean resume = Boolean.parseBoolean(value);
        resumeCheckBox.setSelected(resume.booleanValue());
        value = session.getProperty(PathManager.PROP_IGNORE_DEBUGGEE);
        ignoreCheckBox.setSelected(value != null && value.length() > 0);
    }

    /**
     * Save the launch parameters to the given properties.
     *
     * @param  session  Session to which we persist our values.
     */
    public void saveParameters(Session session) {
        session.setProperty(Session.PROP_SHARED_NAME, shmemTextField.getText());
        session.setProperty(Session.PROP_SOCKET_HOST, hostTextField.getText());
        Integer port = (Integer) portSpinner.getValue();
        session.setProperty(Session.PROP_SOCKET_PORT, port.toString());
        NameValuePair<?> pair =
                (NameValuePair<?>) connectorComboBox.getSelectedItem();
        Transport type = (Transport) pair.getValue();
        switch (type) {
            case ATTACH_SHARED:
                session.setProperty(Session.PROP_CONNECTOR, Session.PREF_SHARED);
                break;
            case ATTACH_SOCKET:
                session.setProperty(Session.PROP_CONNECTOR, Session.PREF_SOCKET);
                break;
            case LISTEN_SHARED:
                session.setProperty(Session.PROP_CONNECTOR, Session.PREF_SHARED_LISTEN);
                break;
            case LISTEN_SOCKET:
                session.setProperty(Session.PROP_CONNECTOR, Session.PREF_SOCKET_LISTEN);
                break;
        }
        if (resumeCheckBox.isSelected()) {
            session.setProperty(PROP_RESUME, "true");
        } else {
            session.setProperty(PROP_RESUME, "false");
        }
        if (ignoreCheckBox.isSelected()) {
            session.setProperty(PathManager.PROP_IGNORE_DEBUGGEE, "true");
        } else {
            session.setProperty(PathManager.PROP_IGNORE_DEBUGGEE, null);
        }
    }

    /**
     * Change the connector selection to the desired type.
     *
     * @param  type  type of transport to set as selected.
     */
    private void selectConnector(Transport type) {
        int size = connectorComboBox.getItemCount();
        for (int ii = 0; ii < size; ii++) {
            NameValuePair<?> pair =
                    (NameValuePair<?>) connectorComboBox.getItemAt(ii);
            Transport pairtype = (Transport) pair.getValue();
            if (pairtype.equals(type)) {
                connectorComboBox.setSelectedIndex(ii);
                break;
            }
        }
    }

    /**
     * Show the appropriate connector panel.
     *
     * @param  pair  name/value pair containing transport information.
     */
    private void showConnectorPanel(NameValuePair<?> pair) {
        Transport type = (Transport) pair.getValue();
        CardLayout layout = (CardLayout) transportsPanel.getLayout();
        switch (type) {
            case ATTACH_SHARED:
            case LISTEN_SHARED:
                layout.show(transportsPanel, "shared");
                transportTextField.setText("dt_shmem");
                break;
            case ATTACH_SOCKET:
            case LISTEN_SOCKET:
                layout.show(transportsPanel, "socket");
                transportTextField.setText("dt_socket");
                break;
        }
    }

    /**
     * Indicates if the user wants the attached debuggee to be resumed.
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
        NameValuePair<?> pair =
                (NameValuePair<?>) connectorComboBox.getSelectedItem();
        Transport type = (Transport) pair.getValue();
        switch (type) {
            case ATTACH_SHARED:
            case LISTEN_SHARED:
                String name = shmemTextField.getText();
                if (name == null || name.length() == 0) {
                    validationLabel.setText(NbBundle.getMessage(getClass(),
                            "ERR_Attach_Missing_ShareName"));
                    valid = false;
                }
                break;
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
        connectorLabel = new javax.swing.JLabel();
        connectorComboBox = new javax.swing.JComboBox();
        transportLabel = new javax.swing.JLabel();
        transportTextField = new javax.swing.JTextField();
        comboSeparator = new javax.swing.JSeparator();
        transportsPanel = new javax.swing.JPanel();
        socketPanel = new javax.swing.JPanel();
        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portSpinner = new javax.swing.JSpinner();
        shmemPanel = new javax.swing.JPanel();
        shmemLabel = new javax.swing.JLabel();
        shmemTextField = new javax.swing.JTextField();
        checkboxSeparator = new javax.swing.JSeparator();
        resumeCheckBox = new javax.swing.JCheckBox();
        ignoreCheckBox = new javax.swing.JCheckBox();
        validationLabel = new javax.swing.JLabel();

        connectorLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Connector").charAt(0));
        connectorLabel.setLabelFor(connectorComboBox);
        connectorLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Attach_Connector"));

        connectorComboBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_Attach_Connector"));

        transportLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Transport").charAt(0));
        transportLabel.setLabelFor(transportTextField);
        transportLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Attach_Transport"));

        transportTextField.setEditable(false);
        transportTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_Attach_Transport"));

        transportsPanel.setLayout(new java.awt.CardLayout());

        hostLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Host").charAt(0));
        hostLabel.setLabelFor(hostTextField);
        hostLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Attach_Host"));

        hostTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_Attach_Host"));

        portLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Port").charAt(0));
        portLabel.setLabelFor(portSpinner);
        portLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Attach_Port"));

        portSpinner.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_Attach_Port"));

        org.jdesktop.layout.GroupLayout socketPanelLayout = new org.jdesktop.layout.GroupLayout(socketPanel);
        socketPanel.setLayout(socketPanelLayout);
        socketPanelLayout.setHorizontalGroup(
            socketPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(socketPanelLayout.createSequentialGroup()
                .add(socketPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(hostLabel)
                    .add(portLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(socketPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(hostTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                    .add(portSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)))
        );
        socketPanelLayout.setVerticalGroup(
            socketPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(socketPanelLayout.createSequentialGroup()
                .add(socketPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostLabel)
                    .add(hostTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(socketPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portLabel)
                    .add(portSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        transportsPanel.add(socketPanel, "socket");

        shmemLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_SharedName").charAt(0));
        shmemLabel.setLabelFor(shmemTextField);
        shmemLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Attach_SharedName"));

        shmemTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_Attach_Shared"));

        org.jdesktop.layout.GroupLayout shmemPanelLayout = new org.jdesktop.layout.GroupLayout(shmemPanel);
        shmemPanel.setLayout(shmemPanelLayout);
        shmemPanelLayout.setHorizontalGroup(
            shmemPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(shmemPanelLayout.createSequentialGroup()
                .add(shmemLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(shmemTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
        );
        shmemPanelLayout.setVerticalGroup(
            shmemPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(shmemPanelLayout.createSequentialGroup()
                .add(shmemPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(shmemLabel)
                    .add(shmemTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        transportsPanel.add(shmemPanel, "shared");

        resumeCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Resume").charAt(0));
        resumeCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Attach_Resume"));
        resumeCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_Attach_Resume"));

        ignoreCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Ignore").charAt(0));
        ignoreCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Attach_Ignore"));
        ignoreCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_Attach_Ignore"));

        validationLabel.setText(" ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(connectorLabel)
                            .add(transportLabel))
                        .add(15, 15, 15)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(transportTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                            .add(connectorComboBox, 0, 318, Short.MAX_VALUE)))
                    .add(comboSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .add(transportsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(resumeCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 245, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(ignoreCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 207, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(validationLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .add(checkboxSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectorLabel)
                    .add(connectorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(transportLabel)
                    .add(transportTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(comboSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(transportsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkboxSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(resumeCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ignoreCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator checkboxSeparator;
    private javax.swing.JSeparator comboSeparator;
    private javax.swing.JComboBox connectorComboBox;
    private javax.swing.JLabel connectorLabel;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JCheckBox ignoreCheckBox;
    private javax.swing.JLabel portLabel;
    private javax.swing.JSpinner portSpinner;
    private javax.swing.JCheckBox resumeCheckBox;
    private javax.swing.JLabel shmemLabel;
    private javax.swing.JPanel shmemPanel;
    private javax.swing.JTextField shmemTextField;
    private javax.swing.JPanel socketPanel;
    private javax.swing.JLabel transportLabel;
    private javax.swing.JTextField transportTextField;
    private javax.swing.JPanel transportsPanel;
    private javax.swing.JLabel validationLabel;
    // End of variables declaration//GEN-END:variables
}
