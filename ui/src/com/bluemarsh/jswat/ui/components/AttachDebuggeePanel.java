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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * The panel containing the components for the debuggee attach action.
 *
 * @author  Nathan Fiedler
 */
public class AttachDebuggeePanel extends JPanel implements
        DocumentListener, ItemListener, PropertyChangeListener {

    /** Name of property value for resume immediately. */
    private static final String PROP_RESUME = "ResumeAttachee";
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Dialog descriptor used to display this panel. */
    private DialogDescriptor descriptor;

    /**
     * How to connect to the debuggee.
     */
    private enum Transport {

        /** By shared memory attach */
        ATTACH_SHARED {

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(Transport.class, "CTL_Attach_Shared");
            }

            @Override
            public String getName() {
                return "dt_shmem";
            }

            @Override
            public boolean isListening() {
                return false;
            }
        },
        /** By socket attach */
        ATTACH_SOCKET {

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(Transport.class, "CTL_Attach_Socket");
            }

            @Override
            public String getName() {
                return "dt_socket";
            }

            @Override
            public boolean isListening() {
                return false;
            }
        },
        /** By process ID (place after shared and socket so user is
         * presented with more sensible options first). */
        ATTACH_PROCESS {

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(Transport.class, "CTL_Attach_Process");
            }

            @Override
            public String getName() {
                return "local";
            }

            @Override
            public boolean isListening() {
                return false;
            }
        },
        /** By shared memory listen */
        LISTEN_SHARED {

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(Transport.class, "CTL_Listen_Shared");
            }

            @Override
            public String getName() {
                return "dt_shmem";
            }

            @Override
            public boolean isListening() {
                return true;
            }
        },
        /** By socket listen */
        LISTEN_SOCKET {

            @Override
            public String getDisplayName() {
                return NbBundle.getMessage(Transport.class, "CTL_Listen_Socket");
            }

            @Override
            public String getName() {
                return "dt_socket";
            }

            @Override
            public boolean isListening() {
                return true;
            }
        };

        /**
         * Returns the display name for the transport.
         *
         * @return  the transport display name (e.g. "Attach by socket").
         */
        public abstract String getDisplayName();

        /**
         * Returns the transport name.
         *
         * @return  the transport name (e.g. "dt_shmem").
         */
        public abstract String getName();

        /**
         * Indicates if transport is a listening or attaching transport.
         *
         * @return  true if listening, false if attaching.
         */
        public abstract boolean isListening();
    };

    /**
     * Creates new form AttachDebuggeePanel.
     */
    public AttachDebuggeePanel() {
        initComponents();

        // Add our input validation listeners.
        processTextField.getDocument().addDocumentListener(this);
        shmemTextField.getDocument().addDocumentListener(this);

        SpinnerNumberModel snm = (SpinnerNumberModel) portSpinner.getModel();
        snm.setValue(new Integer(1));
        snm.setMinimum(new Integer(1));
        snm.setMaximum(new Integer(65535));
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (Transport transport : EnumSet.allOf(Transport.class)) {
            Connector connector = transport.isListening()
                    ? ConnectionProvider.getListeningConnector(transport.getName())
                    : ConnectionProvider.getAttachingConnector(transport.getName());
            if (connector != null) {
                NameValuePair<Transport> pair = new NameValuePair<Transport>(
                        transport.getDisplayName(), transport);
                model.addElement(pair);
            }
        }
        connectorComboBox.setModel(model);
        if (model.getSize() > 0) {
            connectorComboBox.addItemListener(this);
            connectorComboBox.setSelectedIndex(0);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        connectorComboBox.requestFocusInWindow();
        validateInput();
    }

    /**
     * Builds a JvmConnection instance based on the input values.
     *
     * @return  new connection instance, or null if something was wrong.
     * @throws  NoAttachingConnectorException
     *          if selected connector is not available.
     * @throws  NoListeningConnectorException
     *          if there is no listening connector available.
     */
    public JvmConnection buildConnection() throws NoAttachingConnectorException,
            NoListeningConnectorException {
        // Build a JvmConnection from the input field values.
        JvmConnection connection = null;
        NameValuePair<?> pair =
                (NameValuePair<?>) connectorComboBox.getSelectedItem();
        Transport type = (Transport) pair.getValue();
        ConnectionFactory factory = ConnectionProvider.getConnectionFactory();
        switch (type) {
            case ATTACH_PROCESS:
                String pid = processTextField.getText();
                connection = factory.createProcess(pid);
                break;
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

    @Override
    public void changedUpdate(DocumentEvent event) {
    }

    /**
     * Build and display a dialog for launching the debuggee.
     *
     * @return  true if user input is valid and ready to launch debuggee,
     *          false otherwise.
     */
    public boolean display() {
        String title = NbBundle.getMessage(AttachDebuggeePanel.class,
                "LBL_AttachDebuggeeTitle");
        descriptor = new DialogDescriptor(this, title);
        descriptor.setHelpCtx(new HelpCtx("jswat-attach-debuggee"));
        addPropertyChangeListener(this);
        Object value = DialogDisplayer.getDefault().notify(descriptor);
        removePropertyChangeListener(this);
        return value == DialogDescriptor.OK_OPTION;
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        // This fires an event only if the value has changed.
        putClientProperty(NotifyDescriptor.PROP_VALID,
                Boolean.valueOf(event.getDocument().getLength() > 0));
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        Object[] sels = event.getItemSelectable().getSelectedObjects();
        if (sels != null && sels.length == 1) {
            NameValuePair<?> pair = (NameValuePair<?>) sels[0];
            showConnectorPanel((Transport) pair.getValue());
            validateInput();
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
        processTextField.setText(session.getProperty(Session.PROP_PROCESS_ID));
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
        Transport type = null;
        if (connector != null) {
            if (connector.equals(Session.PREF_PROCESS)) {
                type = Transport.ATTACH_PROCESS;
            } else if (connector.equals(Session.PREF_SHARED)) {
                type = Transport.ATTACH_SHARED;
            } else if (connector.equals(Session.PREF_SOCKET)) {
                type = Transport.ATTACH_SOCKET;
            } else if (connector.equals(Session.PREF_SHARED_LISTEN)) {
                type = Transport.LISTEN_SHARED;
            } else if (connector.equals(Session.PREF_SOCKET_LISTEN)) {
                type = Transport.LISTEN_SOCKET;
            } else {
                // Unknown value, default to socket.
                type = Transport.ATTACH_SOCKET;
            }
        } else {
            // Undefined value, default to socket.
            type = Transport.ATTACH_SOCKET;
        }
        selectConnector(type);
        showConnectorPanel(type);
        String value = session.getProperty(PROP_RESUME);
        Boolean resume = Boolean.parseBoolean(value);
        resumeCheckBox.setSelected(resume.booleanValue());
        value = session.getProperty(PathManager.PROP_IGNORE_DEBUGGEE);
        ignoreCheckBox.setSelected(value != null && value.length() > 0);
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
    public void removeUpdate(DocumentEvent event) {
        // This fires an event only if the value has changed.
        putClientProperty(NotifyDescriptor.PROP_VALID,
                Boolean.valueOf(event.getDocument().getLength() > 0));
    }

    /**
     * Save the launch parameters to the given properties.
     *
     * @param  session  Session to which we persist our values.
     */
    public void saveParameters(Session session) {
        session.setProperty(Session.PROP_SHARED_NAME, shmemTextField.getText());
        session.setProperty(Session.PROP_SOCKET_HOST, hostTextField.getText());
        session.setProperty(Session.PROP_PROCESS_ID, processTextField.getText());
        Integer port = (Integer) portSpinner.getValue();
        session.setProperty(Session.PROP_SOCKET_PORT, port.toString());
        NameValuePair<?> pair =
                (NameValuePair<?>) connectorComboBox.getSelectedItem();
        Transport type = (Transport) pair.getValue();
        switch (type) {
            case ATTACH_PROCESS:
                session.setProperty(Session.PROP_CONNECTOR, Session.PREF_PROCESS);
                break;
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
     * @param  type  type of transport to show.
     */
    private void showConnectorPanel(Transport type) {
        CardLayout layout = (CardLayout) transportsPanel.getLayout();
        switch (type) {
            case ATTACH_PROCESS:
                layout.show(transportsPanel, "process");
                transportTextField.setText("local");
                break;
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
     * Verify the user input and display an appropriate message.
     */
    private void validateInput() {
        boolean valid = true;
        NameValuePair<?> pair =
                (NameValuePair<?>) connectorComboBox.getSelectedItem();
        Transport type = (Transport) pair.getValue();
        switch (type) {
            case ATTACH_PROCESS:
                String pid = processTextField.getText();
                if (pid == null || pid.length() == 0) {
                    validationLabel.setText(NbBundle.getMessage(
                            AttachDebuggeePanel.class,
                            "ERR_Attach_Missing_ProcessID"));
                    valid = false;
                }
                break;
            case ATTACH_SHARED:
            case LISTEN_SHARED:
                String name = shmemTextField.getText();
                if (name == null || name.length() == 0) {
                    validationLabel.setText(NbBundle.getMessage(
                            AttachDebuggeePanel.class,
                            "ERR_Attach_Missing_ShareName"));
                    valid = false;
                }
                break;
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
        processPanel = new javax.swing.JPanel();
        processLabel = new javax.swing.JLabel();
        processTextField = new javax.swing.JTextField();
        checkboxSeparator = new javax.swing.JSeparator();
        resumeCheckBox = new javax.swing.JCheckBox();
        ignoreCheckBox = new javax.swing.JCheckBox();
        validationLabel = new javax.swing.JLabel();

        connectorLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Connector").charAt(0));
        connectorLabel.setLabelFor(connectorComboBox);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms"); // NOI18N
        connectorLabel.setText(bundle.getString("LBL_Attach_Connector")); // NOI18N

        connectorComboBox.setToolTipText(bundle.getString("HINT_Attach_Connector")); // NOI18N

        transportLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Transport").charAt(0));
        transportLabel.setLabelFor(transportTextField);
        transportLabel.setText(bundle.getString("LBL_Attach_Transport")); // NOI18N

        transportTextField.setEditable(false);
        transportTextField.setToolTipText(bundle.getString("HINT_Attach_Transport")); // NOI18N

        transportsPanel.setLayout(new java.awt.CardLayout());

        hostLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Host").charAt(0));
        hostLabel.setLabelFor(hostTextField);
        hostLabel.setText(bundle.getString("LBL_Attach_Host")); // NOI18N

        hostTextField.setToolTipText(bundle.getString("HINT_Attach_Host")); // NOI18N

        portLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Port").charAt(0));
        portLabel.setLabelFor(portSpinner);
        portLabel.setText(bundle.getString("LBL_Attach_Port")); // NOI18N

        portSpinner.setToolTipText(bundle.getString("HINT_Attach_Port")); // NOI18N

        javax.swing.GroupLayout socketPanelLayout = new javax.swing.GroupLayout(socketPanel);
        socketPanel.setLayout(socketPanelLayout);
        socketPanelLayout.setHorizontalGroup(
            socketPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(socketPanelLayout.createSequentialGroup()
                .addGroup(socketPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostLabel)
                    .addComponent(portLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(socketPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                    .addComponent(portSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)))
        );
        socketPanelLayout.setVerticalGroup(
            socketPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(socketPanelLayout.createSequentialGroup()
                .addGroup(socketPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(socketPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        transportsPanel.add(socketPanel, "socket");

        shmemLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_SharedName").charAt(0));
        shmemLabel.setLabelFor(shmemTextField);
        shmemLabel.setText(bundle.getString("LBL_Attach_SharedName")); // NOI18N

        shmemTextField.setToolTipText(bundle.getString("HINT_Attach_Shared")); // NOI18N

        javax.swing.GroupLayout shmemPanelLayout = new javax.swing.GroupLayout(shmemPanel);
        shmemPanel.setLayout(shmemPanelLayout);
        shmemPanelLayout.setHorizontalGroup(
            shmemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shmemPanelLayout.createSequentialGroup()
                .addComponent(shmemLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shmemTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE))
        );
        shmemPanelLayout.setVerticalGroup(
            shmemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shmemPanelLayout.createSequentialGroup()
                .addGroup(shmemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shmemLabel)
                    .addComponent(shmemTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        transportsPanel.add(shmemPanel, "shared");

        processLabel.setLabelFor(processTextField);
        processLabel.setText(bundle.getString("LBL_Attach_ProcessID")); // NOI18N

        processTextField.setToolTipText(bundle.getString("HINT_Attach_Process")); // NOI18N

        javax.swing.GroupLayout processPanelLayout = new javax.swing.GroupLayout(processPanel);
        processPanel.setLayout(processPanelLayout);
        processPanelLayout.setHorizontalGroup(
            processPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(processPanelLayout.createSequentialGroup()
                .addComponent(processLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
        );
        processPanelLayout.setVerticalGroup(
            processPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(processPanelLayout.createSequentialGroup()
                .addGroup(processPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(processLabel)
                    .addComponent(processTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        transportsPanel.add(processPanel, "process");

        resumeCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Resume").charAt(0));
        resumeCheckBox.setText(bundle.getString("LBL_Attach_Resume")); // NOI18N
        resumeCheckBox.setToolTipText(bundle.getString("TIP_Attach_Resume")); // NOI18N

        ignoreCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_Attach_Ignore").charAt(0));
        ignoreCheckBox.setText(bundle.getString("LBL_Attach_Ignore")); // NOI18N
        ignoreCheckBox.setToolTipText(bundle.getString("HINT_Attach_Ignore")); // NOI18N

        validationLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        validationLabel.setText("   ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(connectorLabel)
                            .addComponent(transportLabel))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(transportTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                            .addComponent(connectorComboBox, 0, 318, Short.MAX_VALUE)))
                    .addComponent(comboSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addComponent(transportsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resumeCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ignoreCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(checkboxSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addComponent(validationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectorLabel)
                    .addComponent(connectorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(transportLabel)
                    .addComponent(transportTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transportsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkboxSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(resumeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ignoreCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(validationLabel))
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
    private javax.swing.JLabel processLabel;
    private javax.swing.JPanel processPanel;
    private javax.swing.JTextField processTextField;
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
