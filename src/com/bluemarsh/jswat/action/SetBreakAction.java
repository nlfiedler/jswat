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
 * $Id: SetBreakAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import java.awt.Frame;
import java.awt.event.ActionEvent;
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
        Object messages[] = {
            Bundle.getString("SetBreak.classNameField"),
            new JTextField(25),
            Bundle.getString("SetBreak.lineNumberField"),
            new JTextField(5)
        };

        String className = null;
        int lineNumber = -1;

        boolean responseOkay = false;
        while (!responseOkay) {
            // Show dialog asking user for breakpoint location.
            int response = JOptionPane.showOptionDialog(
                win, messages, Bundle.getString("SetBreak.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                return;
            }

            // Now assume the response is okay unless we find otherwise.
            responseOkay = true;
            className = ((JTextField) messages[1]).getText();
            String lineNum = ((JTextField) messages[3]).getText();
            try {
                lineNumber = Integer.parseInt(lineNum);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(
                    win,
                    Bundle.getString("SetBreak.invalidLineMsg"),
                    Bundle.getString("SetBreak.errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
                responseOkay = false;
            }
            if (className.length() == 0) {
                JOptionPane.showMessageDialog(
                    win,
                    Bundle.getString("SetBreak.missingClassName"),
                    Bundle.getString("SetBreak.errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
                responseOkay = false;
            }
        }

        // Create the breakpoint.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        try {
            brkman.createBreakpoint(className, lineNumber);
            session.getStatusLog().writeln(
                Bundle.getString("SetBreak.breakpointAdded"));
        } catch (ClassNotFoundException cnfe) {
            JOptionPane.showMessageDialog(
                win,
                Bundle.getString("SetBreak.invalidClassMsg"),
                Bundle.getString("SetBreak.errorTitle"),
                JOptionPane.ERROR_MESSAGE);
            return;
        } catch (ResolveException re) {
            JOptionPane.showMessageDialog(
                win, re.errorMessage(),
                Bundle.getString("SetBreak.errorTitle"),
                JOptionPane.ERROR_MESSAGE);
            return;
        }
    } // actionPerformed
} // SetBreakAction
