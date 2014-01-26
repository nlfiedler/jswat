/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: AddBreakGroupAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ui.BasicBreakpointUI;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Class AddBreakGroupAction allows the user to define new breakpoint groups.
 *
 * @author  Nathan Fiedler
 */
public class AddBreakGroupAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new AddBreakGroupAction object with the default action
     * command string of "addBreakGroup".
     */
    public AddBreakGroupAction() {
        super("addBreakGroup");
    } // AddBreakGroupAction

    /**
     * Performs the set breakpoint action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Frame win = getFrame(event);
        Session session = getSession(event);
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);

        JComboBox groupCombo = BasicBreakpointUI.buildGroupList(
            brkman.getDefaultGroup());

        // get class and line number from user
        Object[] messages = {
            Bundle.getString("AddBreakGroup.nameField"),
            new JTextField(25),
            Bundle.getString("AddBreakGroup.parentField"),
            groupCombo
        };

        boolean responseOkay = false;
        while (!responseOkay) {

            // Show dialog asking user for breakpoint group location and name.
            int response = JOptionPane.showOptionDialog(
                win, messages, Bundle.getString("AddBreakGroup.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);

            if (response != JOptionPane.OK_OPTION) {
                return;
            }

            responseOkay = true;
            // If okay, try to create breakpoint.
            String name = ((JTextField) messages[1]).getText();
            if (name == null || name.length() == 0) {
                session.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_ERROR,
                     Bundle.getString("AddBreakGroup.missingName"));
                responseOkay = false;
                continue;
            }

            // Create the breakpoint.
            BreakpointGroup bg = brkman.createBreakpointGroup(
                name, BasicBreakpointUI.getSelectedGroup(groupCombo));
        }
    } // actionPerformed
} // AddBreakGroupAction
