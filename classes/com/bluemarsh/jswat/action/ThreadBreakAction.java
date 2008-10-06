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
 * $Id: ThreadBreakAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.breakpoint.ThreadBreakpoint;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Class ThreadBreakAction allows the user to define new thread
 * breakpoints.
 *
 * @author  Nathan Fiedler
 */
public class ThreadBreakAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new ThreadBreakAction object with the default action
     * command string of "threadBreak".
     */
    public ThreadBreakAction() {
        super("threadBreak");
    } // ThreadBreakAction

    /**
     * Performs the create thread breakpoint action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Frame topFrame = getFrame(event);
        Session session = getSession(event);

        Object[] messages = {
            Bundle.getString("ThreadBreak.threadNameField"),
            new JTextField(20),
            new JCheckBox(Bundle.getString("ThreadBreak.onStart"), true),
            new JCheckBox(Bundle.getString("ThreadBreak.onDeath"), true)
        };

        // Show dialog asking user for trace information.
        String threadName = null;
        while (true) {
            int response = JOptionPane.showOptionDialog(
                topFrame, messages, Bundle.getString("ThreadBreak.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                return;
            }
            threadName = ((JTextField) messages[1]).getText();
            if (threadName.length() == 0) {
                displayError(topFrame, Bundle.getString(
                    "ThreadBreak.missingName"));
            } else {
                break;
            }
        }

        boolean onStart = ((JCheckBox) messages[2]).isSelected();
        boolean onDeath = ((JCheckBox) messages[3]).isSelected();

        // Create the thread breakpoint.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint bp = new ThreadBreakpoint(threadName, onStart, onDeath);
        try {
            brkman.addNewBreakpoint(bp);
        } catch (ResolveException re) {
            // This cannot happen for this type of breakpoint.
            session.getStatusLog().writeStackTrace(re);
            return;
        }

        session.getUIAdapter().showMessage(
            UIAdapter.MESSAGE_NOTICE,
            Bundle.getString("ThreadBreak.added"));
    } // actionPerformed
} // ThreadBreakAction
