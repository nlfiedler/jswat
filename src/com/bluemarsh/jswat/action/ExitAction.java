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
 * $Id: ExitAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Main;
import com.bluemarsh.jswat.Session;
import java.awt.event.ActionEvent;

/**
 * Implements the exit program action.
 *
 * @author  Nathan Fiedler
 */
public class ExitAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new ExitAction object with the default action
     * command string of "exit".
     */
    public ExitAction() {
        super("exit");
    } // ExitAction

    /**
     * Performs the exit action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // Get the Session so we can close it.
        Session session = getSession(event);
        if (session != null) {
            // Close the session, which may force an exit.
            Main.endSession(session);
        }
    } // actionPerformed
} // ExitAction
