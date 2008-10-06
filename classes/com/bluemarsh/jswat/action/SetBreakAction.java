/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: SetBreakAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Class SetBreakAction allows the user to define new breakpoints.
 *
 * @author  Nathan Fiedler
 */
public class SetBreakAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new SetBreakAction object with the default action
     * command string of "setBreak".
     */
    public SetBreakAction() {
        super("setBreak");
    } // SetBreakAction

    /**
     * Performs the set breakpoint action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // is there an active session?
        Frame win = getFrame(event);
        Session session = getSession(event);

        // get class and line number from user
        Object[] messages = {
            new JLabel(Bundle.getString("SetBreak.classField")),
            new JTextField(25),
            Bundle.getString("SetBreak.locationField"),
            new JTextField(25)
        };

        String className = null;
        String location = null;
        while (true) {
            // Show dialog asking user for breakpoint location.
            int response = JOptionPane.showOptionDialog(
                win, messages, Bundle.getString("SetBreak.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                return;
            }

            // Now assume the response is okay unless we find otherwise.
            className = ((JTextField) messages[1]).getText();
            location = ((JTextField) messages[3]).getText();
            if (location.length() == 0) {
                displayError(win, Bundle.getString(
                    "SetBreak.missingLocation"));
                continue;
            }

            // Attempt to create the breakpoint.
            BreakpointManager brkman = (BreakpointManager)
                session.getManager(BreakpointManager.class);
            try {
                brkman.parseBreakpointSpec(className, location);
                session.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_NOTICE,
                    Bundle.getString("SetBreak.breakpointAdded"));
                break;
            } catch (ClassNotFoundException cnfe) {
                displayError(win, Bundle.getString(
                    "SetBreak.invalidClassMsg"));
            } catch (ResolveException re) {
                displayError(win, re.errorMessage());
            } catch (Exception e) {
                displayError(win, e.getMessage());
            }
        }
    } // actionPerformed
} // SetBreakAction
