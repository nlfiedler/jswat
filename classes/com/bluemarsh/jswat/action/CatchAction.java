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
 * $Id: CatchAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ExceptionBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Class CatchAction allows the user to define new exception catches.
 *
 * @author  Nathan Fiedler
 */
public class CatchAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new CatchAction object with the default action
     * command string of "catch".
     */
    public CatchAction() {
        super("catch");
    } // CatchAction

    /**
     * Performs the create exception catch action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Frame win = getFrame(event);
        Session session = getSession(event);

        Object[] messages = {
            Bundle.getString("Catch.exceptionField"),
            new JTextField(25),
        };

        String className = null;
        boolean responseOkay = false;
        while (!responseOkay) {
            // Show dialog asking user for catch information.
            int response = JOptionPane.showOptionDialog(
                win, messages, Bundle.getString("Catch.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                return;
            }

            // Now assume the response is okay unless we find otherwise.
            responseOkay = true;
            className = ((JTextField) messages[1]).getText();
            if (className.length() == 0) {
                session.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_ERROR,
                    Bundle.getString("Catch.missingClassName"));
                responseOkay = false;
            }
        }

        // Create the catch.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        try {
            Breakpoint bp = new ExceptionBreakpoint(className, true, true);
            brkman.addNewBreakpoint(bp);
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_NOTICE,
                Bundle.getString("Catch.added"));
        } catch (ClassNotFoundException cnfe) {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR,
                Bundle.getString("Catch.invalidClassMsg"));
        } catch (ResolveException re) {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR, re.errorMessage());
        }
    } // actionPerformed
} // CatchAction
