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
 * $Id: ClassBreakAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ClassBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Class ClassBreakAction allows the user to define class breakpoints.
 *
 * @author  Nathan Fiedler
 */
public class ClassBreakAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new ClassBreakAction object with the default action
     * command string of "classBreak".
     */
    public ClassBreakAction() {
        super("classBreak");
    } // ClassBreakAction

    /**
     * Performs the create class breakpoint action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Frame topFrame = getFrame(event);
        Session session = getSession(event);

        Object[] messages = {
            Bundle.getString("ClassBreak.classesField"),
            new JTextField(25),
            new JCheckBox(Bundle.getString("ClassBreak.onPrepare"), true),
            new JCheckBox(Bundle.getString("ClassBreak.onUnload"), true)
        };

        // Show dialog asking user for trace information.
        int response = JOptionPane.showOptionDialog(
            topFrame, messages, Bundle.getString("ClassBreak.title"),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, null, null);
        if (response != JOptionPane.OK_OPTION) {
            return;
        }
        String classFilters = ((JTextField) messages[1]).getText();

        boolean onPrepare = ((JCheckBox) messages[2]).isSelected();
        boolean onUnload = ((JCheckBox) messages[3]).isSelected();

        // Create the class breakpoint.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint bp = new ClassBreakpoint(classFilters, onPrepare, onUnload);
        try {
            brkman.addNewBreakpoint(bp);
        } catch (ResolveException re) {
            // this can't happen
            session.getStatusLog().writeStackTrace(re);
            return;
        }

        session.getUIAdapter().showMessage(
            UIAdapter.MESSAGE_NOTICE,
            Bundle.getString("ClassBreak.added"));
    } // actionPerformed
} // ClassBreakAction
