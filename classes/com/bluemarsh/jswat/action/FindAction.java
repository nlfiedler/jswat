/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: FindAction.java 14 2007-06-02 23:50:55Z nfiedler $
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
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Implements the find action used to search for text within a source view.
 *
 * @author  Nathan Fiedler
 */
public class FindAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new FindAction object with the default action
     * command string of "find".
     */
    public FindAction() {
        super("find");
    } // FindAction

    /**
     * Creates a new FindAction object with the given action command.
     *
     * @param  command  action command string.
     */
    public FindAction(String command) {
        super(command);
    } // FindAction

    /**
     * Performs the find action.
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
        boolean ignoreCase = prefs.getBoolean("searchIgnoreCase", false);

        // Else, show a dialog asking for the string to find.
        Object[] messages = {
            Bundle.getString("Find.searchStringField"),
            new JTextField(query, 25),
            new JCheckBox(Bundle.getString("Find.ignoreCase"), ignoreCase)
        };

        // It seems that calling JOptionPane.showOptionDialog() will cause
        // focus to be on the OK button. However, if the pane and dialog
        // are created separately, focus will be on the text box...
        JOptionPane pane = new JOptionPane(
            messages, JOptionPane.QUESTION_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION, null, null, null);
        JDialog dialog = pane.createDialog(
            frame, Bundle.getString("Find.title"));
        ((JTextField) messages[1]).selectAll();
        dialog.setVisible(true);

        int response = JOptionPane.CLOSED_OPTION;
        Object selectedValue = pane.getValue();
        if (selectedValue == null) {
            response = JOptionPane.CLOSED_OPTION;
        } else {
            if (selectedValue instanceof Integer) {
                response = ((Integer) selectedValue).intValue();
            } else {
                response = JOptionPane.CLOSED_OPTION;
            }
        }

        // If okay, validate the input.
        if (response == JOptionPane.OK_OPTION) {
            query = ((JTextField) messages[1]).getText();
            ignoreCase = ((JCheckBox) messages[2]).isSelected();
            if (query.length() > 0) {

                // Ask the source view to find the first occurrence of
                // the string.
                try {
                    if (!adapter.findString(query, ignoreCase)) {
                        // String was not found anywhere in the view.
                        displayError(frame,
                                     Bundle.getString("Find.stringNotFound"));
                        // Continue on anyway, to save the query string.
                    }
                } catch (NoOpenViewException nove) {
                    displayError(frame, Bundle.getString(
                                     "Find.noViewSelected"));
                }
            }

            // Save the string value so the FindAgain action can use
            // it to find the next occurance.
            prefs.put("searchString", query);
            prefs.putBoolean("searchIgnoreCase", ignoreCase);
        }
    } // actionPerformed
} // FindAction
