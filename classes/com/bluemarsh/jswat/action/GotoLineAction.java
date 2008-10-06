/*********************************************************************
 *
 *      Copyright (C) 2003-2005 Neeraj Apte
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
 * $Id: GotoLineAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.view.View;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JDialog;

/**
 * Implements the Goto Line action used to jump to a specified line number
 * in the source view.
 *
 * @author  Neeraj Apte
 */
public class GotoLineAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new GotoLineAction object with the default action
     * command string of "gotoLine".
     */
    public GotoLineAction() {
        super("gotoLine");
    } // GotoLineAction

    /**
     * Creates a new GotoLineAction object with the given action command.
     *
     * @param  command  action command string.
     */
    public GotoLineAction(String command) {
        super(command);
    } // GotoLineAction

    /**
     * Performs the gotoLine action.
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
        if (view == null) {
            displayError(frame, Bundle.getString("GotoLine.noViewSelected"));
            return;
        }

        // Get the previous line number, if any.
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String lineNumber = prefs.get("gotoLineNumber", "");

        // Show a dialog asking for the line number.
        Object[] messages = {
            Bundle.getString("GotoLine.lineNumberField"),
            new JTextField(lineNumber, 5),
        };

        // Construct a JDialog with line number selected and focus on it.
        JOptionPane pane = new JOptionPane(
            messages, JOptionPane.QUESTION_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION, null, null, null);
        JDialog dialog = pane.createDialog(
            frame, Bundle.getString("GotoLine.title"));
        ((JTextField) messages[1]).selectAll();
        dialog.setModal(true);
        dialog.setVisible(true);

        int response = getResponse(pane);

        // If okay, validate the input.
        while (response == JOptionPane.OK_OPTION) {
            lineNumber = ((JTextField) messages[1]).getText();
            if (lineNumber.length() > 0) {
                // Ask the source view to scroll to the line number.
                try {
                    int number = Integer.parseInt(lineNumber);
                    view.scrollToLine(number);
                    // Save the line number for next time.
                    prefs.put("gotoLineNumber", lineNumber);
                    break;
                } catch (NumberFormatException nfe) {
                    displayError(frame, Bundle.getString(
                                     "GotoLine.error.invalidLineNumber"));
                }
                // Try again until we get something valid.
                dialog.setVisible(true);
                response = getResponse(pane);
            }
        }

        dialog.dispose();
    } // actionPerformed

    /**
     * Returns the response code from the option pane.
     *
     * @param  pane  option pane from which to get response.
     * @return  response code.
     */
    private static int getResponse(JOptionPane pane) {
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
        return response;
    } // getResponse
} // GotoLineAction
