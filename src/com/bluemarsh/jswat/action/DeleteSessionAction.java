/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: DeleteSessionAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.SessionSettings;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;
import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Class DeleteSessionAction allows the user to delete a named session.
 *
 * @author Nathan Fiedler
 */
public class DeleteSessionAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new DeleteSessionAction object with the default action
     * command string of "deleteSession".
     */
    public DeleteSessionAction() {
        super("deleteSession");
    } // DeleteSessionAction

    /**
     * Performs the open action. This presents the user with a list
     * of available session names and deletes the chosen one.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // get main window that contains our invoker
        Frame topFrame = getFrame(event);
        Session session = getSession(event);
        try {
            DeleteSessionDialog dialog = new DeleteSessionDialog(
                topFrame, session);
            dialog.pack();
            dialog.setLocationRelativeTo(topFrame);
            dialog.setResizable(false);
            dialog.setVisible(true);
        } catch (BackingStoreException bse) {
            session.getStatusLog().writeStackTrace(bse);
        }
    } // actionPerformed
} // DeleteSessionAction

/**
 * Class DeleteSessionDialog deletes sessions.
 *
 * @author Nathan Fiedler
 */
class DeleteSessionDialog extends JDialog implements ActionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** List of session names. */
    private JList nameList;
    /** Model for the list of names. */
    private DefaultListModel listModel;
    /** Delete to delete sessions from. */
    private Session session;

    /**
     * Constructs a DeleteSessionDialog to handle deleting sessions.
     *
     * @param  parent   parent window.
     * @param  session  session on which to operate.
     * @throws  BackingStoreException
     *          if the preferences had a problem.
     */
    public DeleteSessionDialog(Frame parent, Session session)
        throws BackingStoreException {
        super(parent, Bundle.getString("DeleteSession.dialogTitle"));
        this.session = session;

        // Create the list of session names.
        listModel = new DefaultListModel();
        String[] names = SessionSettings.getAvailableSettings();
        for (int ii = 0; ii < names.length; ii++) {
            listModel.addElement(names[ii]);
        }
        nameList = new JList(listModel);
        nameList.setVisibleRowCount(5);

        // Create a panel with insets of 10 pixels all around.
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

        JLabel label = new JLabel(
            Bundle.getString("DeleteSession.currentName") + ' '
            + SessionSettings.currentSettings());
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbl.setConstraints(label, gbc);
        allPanel.add(label);

        label = new JLabel(Bundle.getString("DeleteSession.chooseFromList"));
        gbl.setConstraints(label, gbc);
        allPanel.add(label);

        JScrollPane listScroller = new JScrollPane(nameList);
        gbc.fill = GridBagConstraints.BOTH;
        gbl.setConstraints(listScroller, gbc);
        allPanel.add(listScroller);

        JButton button = new JButton(
            Bundle.getString("DeleteSession.deleteButton"));
        button.setActionCommand("delete");
        button.addActionListener(this);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(3, 10, 3, 10);
        gbc.weightx = 1.0;
        gbl.setConstraints(button, gbc);
        allPanel.add(button);

        button = new JButton(
            Bundle.getString("DeleteSession.doneButton"));
        button.setActionCommand("done");
        button.addActionListener(this);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(button, gbc);
        allPanel.add(button);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    } // DeleteSessionDialog

    /**
     * Invoked when a button has been pressed.
     *
     * @param  event  action event.
     */
    public void actionPerformed(ActionEvent event) {
        AbstractButton src = (AbstractButton) event.getSource();
        if (src.getActionCommand().equals("delete")) {

            // Did the user select anything from the list?
            int[] indices = nameList.getSelectedIndices();
            for (int ii = indices.length - 1; ii >= 0; ii--) {
                String newName = (String) listModel.get(indices[ii]);
                // Delete the named session.
                try {
                    SessionSettings.deleteSettings(newName);
                } catch (BackingStoreException bse) {
                    session.getStatusLog().writeStackTrace(bse);
                    return;
                }
                // Update the list model.
                listModel.remove(indices[ii]);
            }
        } else {
            dispose();
        }
    } // actionPerformed
} // DeleteSessionDialog
