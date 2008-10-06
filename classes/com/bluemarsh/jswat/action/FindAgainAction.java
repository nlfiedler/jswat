/*********************************************************************
 *
 *	Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: FindAgainAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.ui.NoOpenViewException;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.view.View;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

/**
 * Implements the find again action used to search for text within
 * a source view.
 *
 * @author  Nathan Fiedler
 */
public class FindAgainAction extends FindAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new FindAgainAction object with the default action
     * command string of "findAgain".
     */
    public FindAgainAction() {
        super("findAgain");
    } // FindAgainAction

    /**
     * Performs the find again action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // Get frame that contains our invoker.
        Frame frame = getFrame(event);
        Session session = getSession(event);
        UIAdapter adapter = session.getUIAdapter();

        // Find the currently active source view, if any.
        View view = adapter.getSelectedView();
        // If none, display a message indicating the problem.
        if (view == null) {
            displayError(frame, Bundle.getString("Find.noViewSelected"));
            return;
        }

        // Get the previous search phrase, if any.
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String query = prefs.get("searchString", "");
        if (query.length() == 0) {
            // Let the superclass deal with this.
            super.actionPerformed(event);
            return;
        }
        boolean ignoreCase = prefs.getBoolean("searchIgnoreCase", false);

        try {
            // Ask the source view to find the next occurrence of the string.
            if (!adapter.findString(query, ignoreCase)) {
                // String was not found anywhere in the view.
                displayError(frame, Bundle.getString("Find.stringNotFound"));
                return;
            }
        } catch (NoOpenViewException nove) {
            displayError(frame, Bundle.getString("Find.noViewSelected"));
        }
    } // actionPerformed
} // FindAgainAction
