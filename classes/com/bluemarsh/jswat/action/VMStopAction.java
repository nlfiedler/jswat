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
 * $Id: VMStopAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import java.awt.event.ActionEvent;

/**
 * Implements the Virtual Machine stop action. It doens't do
 * much except contact the running VM and stop it.
 *
 * @author  Nathan Fiedler
 */
public class VMStopAction extends JSwatAction implements SessionAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new VMStopAction object with the default action
     * command string of "vmStop".
     */
    public VMStopAction() {
        super("vmStop");
    } // VMStopAction

    /**
     * Performs the virtual machine stop action. Finds the
     * appropriate VM and stops it.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Session session = getSession(event);
        if (session.isActive()) {
            // Kill the debuggee VM abruptly.
            session.deactivate(true, this);
        }
    } // actionPerformed

    /**
     * Returns true to indicate that this action should be disabled
     * when the debuggee is resumed.
     *
     * @return  true to disable, false to leave as-is.
     */
    public boolean disableOnResume() {
        return false;
    } // disableOnResume

    /**
     * Returns true to indicate that this action should be disabled
     * when the debuggee is suspended.
     *
     * @return  true to disable, false to leave as-is.
     */
    public boolean disableOnSuspend() {
        return false;
    } // disableOnSuspend

    /**
     * Returns true to indicate that this action should be disabled
     * while the session is active, and enabled when the session
     * is not active. This is the opposite of how SessionActions
     * normally behave.
     *
     * @return  true to disable when active, false to enable.
     */
    public boolean disableWhenActive() {
        return false;
    } // disableWhenActive
} // VMStopAction
