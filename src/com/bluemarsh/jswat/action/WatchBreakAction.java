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
 * $Id: WatchBreakAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.WatchBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.expr.Evaluator;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Strings;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Class WatchBreakAction allows the user to define watch breakpoints.
 *
 * @author  Nathan Fiedler
 */
public class WatchBreakAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new WatchBreakAction object with the default action
     * command string of "watchBreak".
     */
    public WatchBreakAction() {
        super("watchBreak");
    } // WatchBreakAction

    /**
     * Performs the create watch breakpoint action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Frame topFrame = getFrame(event);
        Session session = getSession(event);

        Object[] messages = {
            Bundle.getString("WatchBreak.fieldnameField"),
            new JTextField(20),
            Bundle.getString("WatchBreak.objectfilterField"),
            new JTextField(20),
            new JCheckBox(Bundle.getString("WatchBreak.onAccess"), true),
            new JCheckBox(Bundle.getString("WatchBreak.onModify"), true)
        };

        // Show dialog asking user for trace information.
        String varname = null;
        ObjectReference objref = null;
        boolean askForInput = true;
        while (askForInput) {
            int response = JOptionPane.showOptionDialog(
                topFrame, messages, Bundle.getString("WatchBreak.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                return;
            }
            askForInput = false;
            varname = ((JTextField) messages[1]).getText();
            if (varname == null || varname.length() == 0) {
                displayError(topFrame, Bundle.getString(
                    "WatchBreak.missingField"));
                askForInput = true;
            }
            String objexpr = ((JTextField) messages[3]).getText();
            if (objexpr != null) {
                ContextManager ctxtman = (ContextManager)
                    session.getManager(ContextManager.class);
                ThreadReference thread = ctxtman.getCurrentThread();
                int frame = ctxtman.getCurrentFrame();
                Evaluator eval = new Evaluator(objexpr);
                try {
                    Object o = eval.evaluate(thread, frame);
                    if (o instanceof ObjectReference) {
                        objref = (ObjectReference) o;
                    } else {
                        displayError(topFrame, Bundle.getString(
                                         "WatchBreak.exprNotObject"));
                        askForInput = true;
                    }
                } catch (Exception e) {
                    displayError(topFrame, Bundle.getString("Evaluate.error")
                                 + ' ' + Strings.exceptionToString(e));
                    askForInput = true;
                }
            }
        }

        boolean onAccess = ((JCheckBox) messages[4]).isSelected();
        boolean onModify = ((JCheckBox) messages[5]).isSelected();

        // Create the watch breakpoint.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint bp = new WatchBreakpoint(varname, onAccess, onModify,
                                            objref);
        try {
            brkman.addNewBreakpoint(bp);
        } catch (ResolveException re) {
            // this can't happen
            session.getStatusLog().writeStackTrace(re);
            return;
        }

        // We know that watch breakpoints resolve immediately
        // when the session is active.
        if (!session.isActive() || !bp.isResolved()) {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_NOTICE,
                Bundle.getString("WatchBreak.disabledForNow1") + '\n'
                + Bundle.getString("WatchBreak.disabledForNow2"));
        } else {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_NOTICE,
                Bundle.getString("WatchBreak.added"));
        }
    } // actionPerformed
} // WatchBreakAction
