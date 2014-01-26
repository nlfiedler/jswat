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
 * $Id: DefineMacroAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.MacroManager;
import com.bluemarsh.jswat.Session;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Class DefineMacroAction allows the user to define new macros.
 *
 * @author  Nathan Fiedler
 */
public class DefineMacroAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new DefineMacroAction object with the default action
     * command string of "defineMacro".
     */
    public DefineMacroAction() {
        super("defineMacro");
    } // DefineMacroAction

    /**
     * Performs the set define macro action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // is there an active session?
        Frame win = getFrame(event);
        Session session = getSession(event);

        // get macro name and definition from user
        Object messages[] = {
            Bundle.getString("DefineMacro.macroNameField"),
            new JTextField(25),
            Bundle.getString("DefineMacro.macroField"),
            new JScrollPane(new JTextArea(5, 40))
        };

        boolean responseOkay = false;
        while (!responseOkay) {

            // show dialog asking user for macro definition
            int response = JOptionPane.showOptionDialog
                (win, messages, swat.getResourceString("DefineMacro.title"),
                 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                 null, null, null);

            if (response != JOptionPane.OK_OPTION) {
                break;
            }

            // Assume response is okay.
            responseOkay = true;
            // if okay, try to create new macro
            String macroName = ((JTextField) messages[1]).getText();
            if (macroName == null || macroName.length() == 0) {
                JOptionPane.showMessageDialog
                    (win,
                     Bundle.getString("DefineMacro.missingName"),
                     Bundle.getString("DefineMacro.errorTitle"),
                     JOptionPane.ERROR_MESSAGE);
                responseOkay = false;
                continue;
            }

            JScrollPane sp = (JScrollPane) messages[3];
            JTextArea ta = (JTextArea) sp.getViewport().getView();
            String macro = ta.getText();

            // Get the MacroManager to create the macro.
            MacroManager macman = (MacroManager)
                session.getManager(MacroManager.class);

            if (macro.length() == 0) {
                // No macro definition, delete any existing macro
                // of the same name.
                macman.removeMacro(macroName);
            } else {
                // Split the macro text into separate lines.
                StringTokenizer st = new StringTokenizer(macro, "\r\n");
                Vector macroCommands = new Vector();
                while (st.hasMoreTokens()) {
                    macroCommands.add(st.nextToken());
                }
                macman.createMacro(macroName, macroCommands);
            }
        }
    } // actionPerformed
} // DefineMacroAction
