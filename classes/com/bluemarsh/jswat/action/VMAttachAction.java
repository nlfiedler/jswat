/*********************************************************************
 *
 *      Copyright (C) 2000-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: VMAttachAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.NoAttachingConnectorException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.VMConnection;
import com.bluemarsh.jswat.ui.EditPopup;
import com.sun.jdi.connect.AttachingConnector;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Class VMAttachAction allows the user to make a remote connection to a
 * debuggee VM.
 *
 * @author  Nathan Fiedler
 */
public class VMAttachAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new VMAttachAction object with the default action
     * command string of "vmAttach".
     */
    public VMAttachAction() {
        super("vmAttach");
    } // VMAttachAction

    /**
     * Performs the remote connect action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // is there an active session?
        Frame win = getFrame(event);
        Session session = getSession(event);

        AttachDialog dialog = new AttachDialog(win, session);
        dialog.pack();
        dialog.setLocationRelativeTo(win);
        dialog.setResizable(false);
        dialog.setVisible(true);
    } // actionPerformed

    /**
     * Dialog for getting remote attach parameters.
     */
    protected class AttachDialog extends JDialog
        implements ActionListener, ItemListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Text field for host name. */
        private JTextField hostNameField;
        /** Text field for port number. */
        private JTextField portNumberField;
        /** Text field for share name. */
        private JTextField shareNameField;
        /** True if the shared name should be used instead of
         * the host name and port number. */
        private boolean useShared;
        /** Card layout for shared memory and socket parameters. */
        private CardLayout cardLayout;
        /** Panel in which card layout is used. */
        private JPanel bottomPanel;
        /** Session through which we attach. */
        private Session session;

        /**
         * Constructs a AttachDialog with the given window and Session.
         *
         * @param  owner    Frame owner.
         * @param  session  Session through which to attach.
         */
        public AttachDialog(Frame owner, Session session) {
            super(owner, Bundle.getString("Attach.title"));
            this.session = session;

            // One panel to contain them all...
            // Label with description of attaching.
            // Label: Transport Type:
            // Panel with etched border
            // Button group with two buttons:
            // - button 1: shared memory
            // - button 2: socket
            // (selecting a button selects one of the following cards)
            // CardLayout with two cards:
            // - card 1: hostname, port number fields,
            //           and remote command-line example
            // - card 2: share name field,
            //           and remote command-line example

            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            Container contentPane = getContentPane();
            contentPane.setLayout(gbl);
            JPanel allPanel = new JPanel(new GridBagLayout());
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.insets = new Insets(10, 10, 10, 10);
            gbl.setConstraints(allPanel, gbc);
            contentPane.add(allPanel);
            gbl = (GridBagLayout) allPanel.getLayout();

            JLabel label = new JLabel(Bundle.getString("Attach.desc"));
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(3, 3, 3, 3);
            gbl.setConstraints(label, gbc);
            allPanel.add(label);

            label = new JLabel(Bundle.getString("Attach.type"));
            gbc.insets = new Insets(3, 3, 0, 3);
            gbl.setConstraints(label, gbc);
            allPanel.add(label);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBorder(BorderFactory.createEtchedBorder());
            gbc.insets = new Insets(0, 3, 3, 3);
            gbl.setConstraints(buttonPanel, gbc);
            allPanel.add(buttonPanel);

            String useShare = session.getProperty("useShare");
            useShared = Boolean.valueOf(useShare).booleanValue();

            ButtonGroup bg = new ButtonGroup();
            JRadioButton rbutton = new JRadioButton(
                Bundle.getString("Attach.shmemButton"), useShared);
            rbutton.setActionCommand("shared");
            rbutton.addItemListener(this);
            AttachingConnector connector =
                VMConnection.getAttachingConnector("dt_shmem");
            if (connector == null) {
                rbutton.setEnabled(false);
            }
            bg.add(rbutton);
            buttonPanel.add(rbutton);
            rbutton = new JRadioButton(
                Bundle.getString("Attach.socketButton"), !useShared);
            rbutton.setActionCommand("socket");
            rbutton.addItemListener(this);
            bg.add(rbutton);
            buttonPanel.add(rbutton);

            cardLayout = new CardLayout();
            bottomPanel = new JPanel(cardLayout);
            gbc.insets = new Insets(3, 3, 3, 3);
            gbl.setConstraints(bottomPanel, gbc);
            allPanel.add(bottomPanel);

            JPanel shmemPanel = buildShmemPanel(session);
            bottomPanel.add(shmemPanel, "shared");
            JPanel socketPanel = buildSocketPanel(session);
            bottomPanel.add(socketPanel, "socket");
            if (useShared) {
                cardLayout.show(bottomPanel, "shared");
            } else {
                cardLayout.show(bottomPanel, "socket");
            }

            JButton button = new JButton(
                Bundle.getString("Attach.attachButton"));
            button.setActionCommand("attach");
            button.addActionListener(this);
            button.setDefaultCapable(true);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbc.insets = new Insets(3, 10, 3, 10);
            gbc.weightx = 1.0;
            gbl.setConstraints(button, gbc);
            allPanel.add(button);
            getRootPane().setDefaultButton(button);

            button = new JButton(
                Bundle.getString("Attach.cancelButton"));
            button.setActionCommand("cancel");
            button.addActionListener(this);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbl.setConstraints(button, gbc);
            allPanel.add(button);

            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        } // AttachDialog

        /**
         * Invoked when a button has been pressed.
         *
         * @param  e  action event.
         */
        public void actionPerformed(ActionEvent e) {
            AbstractButton src = (AbstractButton) e.getSource();
            if (src.getActionCommand().equals("attach")) {
                boolean okay = false;
                if (useShared) {
                    okay = attachShmem();
                } else {
                    okay = attachSocket();
                }
                if (okay) {
                    dispose();
                }
            } else {
                dispose();
            }
        } // actionPerformed

        /**
         * Attach via the shared memory parameters.
         *
         * @return  true if successful, false otherwise.
         */
        protected boolean attachShmem() {
            if (session.isActive()) {
                // Deactivate current session.
                session.deactivate(false, this);
            }

            String shareName = shareNameField.getText();
            Frame win = (Frame) getParent();
            if (shareName.length() == 0) {
                displayError(win, Bundle.getString("Attach.missingShare"));
                return false;
            }

            // Try to make the connection.
            VMConnection connection = null;
            try {
                connection = VMConnection.buildConnection(shareName);
            } catch (NoAttachingConnectorException nace) {
                displayError(win, com.bluemarsh.jswat.Bundle.getString(
                    "noShmemVMsFound"));
                return false;
            }

            if (connection != null) {
                // Show a busy cursor while we attach to the debuggee.
                win.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    connection.attachDebuggee(session, true);
                } finally {
                    win.setCursor(Cursor.getDefaultCursor());
                }
            }

            // Save the values to the session settings for later reuse.
            session.setProperty("shareName", shareName);
            session.setProperty("useShare", "true");
            return true;
        } // attachShmem

        /**
         * Attach via the socket parameters.
         *
         * @return  true if successful, false otherwise.
         */
        protected boolean attachSocket() {
            if (session.isActive()) {
                // Deactivate current session.
                session.deactivate(false, this);
            }

            String remoteHost = hostNameField.getText();
            String remotePort = portNumberField.getText();
            Frame win = (Frame) getParent();
            if (remotePort.length() == 0) {
                displayError(win, Bundle.getString("Attach.missingPort"));
                return false;
            }

            // Try to make the connection.
            VMConnection connection = null;
            try {
                connection = VMConnection.buildConnection(
                    remoteHost, remotePort);
            } catch (NoAttachingConnectorException nace) {
                displayError(win, com.bluemarsh.jswat.Bundle.getString(
                    "noSocketVMsFound"));
                return false;
            }

            if (connection != null) {
                // Show a busy cursor while we attach to the debuggee.
                win.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    connection.attachDebuggee(session, true);
                } finally {
                    win.setCursor(Cursor.getDefaultCursor());
                }
            }

            // Save the values to the session settings for later reuse.
            session.setProperty("remoteHost", remoteHost);
            session.setProperty("remotePort", remotePort);
            session.setProperty("useShare", "false");
            return true;
        } // attachSocket

        /**
         * Builds the shared memory parameters panel.
         *
         * @param  session  Session from which to get settings.
         * @return  shared memory panel.
         */
        protected JPanel buildShmemPanel(Session session) {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel allPanel = new JPanel(gbl);

            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;

            JLabel name = new JLabel(
                Bundle.getString("Attach.shmemNameField"));
            gbl.setConstraints(name, gbc);
            allPanel.add(name);

            String shareName = session.getProperty("shareName");
            shareNameField = new JTextField(shareName, 20);
            gbl.setConstraints(shareNameField, gbc);
            allPanel.add(shareNameField);

            JLabel example = new JLabel(
                Bundle.getString("Attach.example"));
            gbl.setConstraints(example, gbc);
            allPanel.add(example);

            JTextArea exampleArea = new JTextArea(
                Bundle.getString("Attach.shmemExample"), 4, 50);
            exampleArea.setEditable(false);
            exampleArea.setLineWrap(true);
            exampleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            exampleArea.setBorder(BorderFactory.createEtchedBorder());
            EditPopup popup = new EditPopup(exampleArea, false, false);
            exampleArea.addMouseListener(popup);
            gbl.setConstraints(exampleArea, gbc);
            allPanel.add(exampleArea);

            return allPanel;
        } // buildShmemPanel

        /**
         * Builds the shared memory parameters panel.
         *
         * @param  session  Session from which to get settings.
         * @return  socket panel.
         */
        protected JPanel buildSocketPanel(Session session) {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel allPanel = new JPanel(gbl);

            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;

            JLabel name = new JLabel(
                Bundle.getString("Attach.hostNameField"));
            gbl.setConstraints(name, gbc);
            allPanel.add(name);

            String remoteHost = session.getProperty("remoteHost");
            hostNameField = new JTextField(remoteHost, 20);
            gbl.setConstraints(hostNameField, gbc);
            allPanel.add(hostNameField);

            JLabel port = new JLabel(
                Bundle.getString("Attach.portNumberField"));
            gbl.setConstraints(port, gbc);
            allPanel.add(port);

            String remotePort = session.getProperty("remotePort");
            portNumberField = new JTextField(20);
            portNumberField.setDocument(new NumericDocument());
            // Must set after changing the document.
            portNumberField.setText(remotePort);
            gbl.setConstraints(portNumberField, gbc);
            allPanel.add(portNumberField);

            JLabel example = new JLabel(
                Bundle.getString("Attach.example"));
            gbl.setConstraints(example, gbc);
            allPanel.add(example);

            JTextArea exampleArea = new JTextArea(
                Bundle.getString("Attach.socketExample"), 4, 50);
            exampleArea.setEditable(false);
            exampleArea.setLineWrap(true);
            exampleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            exampleArea.setBorder(BorderFactory.createEtchedBorder());
            EditPopup popup = new EditPopup(exampleArea, false, false);
            exampleArea.addMouseListener(popup);
            gbl.setConstraints(exampleArea, gbc);
            allPanel.add(exampleArea);

            return allPanel;
        } // buildSocketPanel

        /**
         * Invoked when an item has been selected or deselected by the
         * user.
         *
         * @param  e  item event.
         */
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                AbstractButton src = (AbstractButton) e.getItem();
                String action = src.getActionCommand();
                cardLayout.show(bottomPanel, action);
                useShared = action.equals("shared");
            }
        } // itemStateChanged
    } // AttachDialog

    /**
     * Implements a text document that only accepts digits.
     */
    protected class NumericDocument extends PlainDocument {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Insert a string into the document.
         *
         * @param  offs  offset in which to insert.
         * @param  str   string to insert.
         * @param  a     attribute set.
         * @throws  BadLocationException
         *          if the text offset is invalid.
         */
        public void insertString(int offs, String str, AttributeSet a)
            throws BadLocationException {

            if (str == null) {
                return;
            }
            char[] num = str.toCharArray();
            int j = 0;
            for (int i = 0; i < num.length; i++) {
                if (Character.isDigit(num[i])) {
                    // copy the digit to new location
                    num[j] = num[i];
                    // character is okay, advance count
                    j++;
                }
            }
            super.insertString(offs, new String(num, 0, j), a);
        } // insertString
    } // NumericDocument
} // VMAttachAction
