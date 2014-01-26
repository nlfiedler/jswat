/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Marko van Dooren
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
 * $Id: OpenSessionAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.AppSettings;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;

/**
 * Class OpenSessionAction allows the user to load a saved session.
 *
 * @author Marko van Dooren
 */
public class OpenSessionAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new OpenSessionAction object with the default action
     * command string of "openSession".
     */
    public OpenSessionAction() {
        super("openSession");
    } // OpenSessionAction

    /**
     * Performs the open action. This presents the user with
     * a file selector and opens the selected session.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // get main window that contains our invoker
        Frame win = getFrame(event);
        String lastDir = AppSettings.instanceOf().getString(
            "opensessionLastdir");
        if (lastDir != null && lastDir.trim().length() == 0) {
            lastDir = null;
        }
        JFileChooser fc = new JFileChooser(lastDir);
        fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
        fc.setFileFilter(new JSwatFileFilter(".session", "Session Files"));
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        int response = fc.showOpenDialog(win);

        // Remember the last open directory for next time.
        AppSettings.instanceOf().setString(
            "opensessionLastdir", fc.getCurrentDirectory().getPath());

        if (response != JFileChooser.APPROVE_OPTION) {
            // user hit cancel or closed the dialog
            return;
        }

        // see if we can read from the file
        File f = fc.getSelectedFile();

        try {
            if (!f.canRead()) {
                displayError(event, swat.getResourceString("cantReadFileMsg"));
                return;
            }
        } catch (SecurityException se) {
            displayError(event, swat.getResourceString("noFileAccessMsg"));
            return;
        }

        // Load the session from the file
        getSession(event).loadProperties(f);
    } // actionPerformed
} // OpenSession
