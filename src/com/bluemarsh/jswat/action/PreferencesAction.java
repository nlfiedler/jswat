/*********************************************************************
 *
 *      Copyright (C) 2000-2005 Nathan Fiedler
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
 * $Id: PreferencesAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.config.JConfigure;
import java.awt.Frame;
import java.awt.event.ActionEvent;

/**
 * Implements the preferences action used to allow the user to
 * set the application preferences.
 *
 * @author  Nathan Fiedler
 */
public class PreferencesAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new PreferencesAction object with the default action
     * command string of "preferences".
     */
    public PreferencesAction() {
        super("preferences");
    } // PreferencesAction

    /**
     * Performs the preferences action. This simply displays a dialog
     * showing the credits for the program.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // get JSwat window that contains our invoker
        Frame win = getFrame(event);
        JConfigure config = swat.getJConfigure();
        // Present the preferences dialog as a modal.
        config.showPreferences(win, true);
    } // actionPerformed
} // PreferencesAction
