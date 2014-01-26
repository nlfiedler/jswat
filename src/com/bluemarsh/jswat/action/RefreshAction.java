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
 * $Id: RefreshAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;

/**
 * Implements the refresh action.
 *
 * @author  Nathan Fiedler
 */
public class RefreshAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new RefreshAction object with the default action
     * command string of "refresh".
     */
    public RefreshAction() {
        super("refresh");
    } // RefreshAction

    /**
     * Performs the refresh action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Session session = getSession(event);
        // Get the main window that contains our invoker.
        Frame win = getFrame(event);
        // Show a busy cursor while we refresh the display.
        win.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Cause the display to refresh.
        try {
            UIAdapter uiadapter = session.getUIAdapter();
            uiadapter.refreshDisplay();
        } finally {
            win.setCursor(Cursor.getDefaultCursor());
        }
    } // actionPerformed
} // RefreshAction
