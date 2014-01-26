/*********************************************************************
 *
 *      Copyright (C) 2001-2005 David Taylor
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
 * $Id: BuildSourcepathAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;

/**
 * Implements the build sourcepath action.
 *
 * @author  David Taylor
 */
public class BuildSourcepathAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a BuildSourcepathAction object with the default action
     * command string of "buildSourcepath".
     */
    public BuildSourcepathAction() {
        super("buildSourcepath");
    } // BuildSourcepathAction

    /**
     * Performs the build sourcepath.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // Get the current sourcepath setting.
        Session session = getSession(event);
        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        String[] sourcepath = pathman.getSourcePath();

        // Build the PathBuilder.
        // (the session is never active for us)
        PathBuilder pathBuilder = new PathBuilder(false);
        pathBuilder.setPath(sourcepath);
        pathBuilder.setFileFilter(new PathBuilder.SourcepathFilter());
        pathBuilder.setMultiSelectionEnabled(true);

        Preferences preferences = Preferences.userNodeForPackage(
            this.getClass());
        String lastDirectory = preferences.get("buildSpLastDir", null);
        if (lastDirectory != null && lastDirectory.trim().length() > 0) {
            pathBuilder.setStartDirectory(lastDirectory);
        }

        // Build and display the sourcepath setting dialog.
        Frame window = getFrame(event);
        int response = JOptionPane.showOptionDialog(
            window, pathBuilder,
            Bundle.getString("BuildSourcepath.title"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, null, null);

        lastDirectory = pathBuilder.getLastDirectorySeen();
        if (lastDirectory != null) {
            preferences.put("buildSpLastDir", lastDirectory);
        }
        if (response == JOptionPane.OK_OPTION) {
            // If ok, save the sourcepath given by the user.
            pathman.setSourcePath(pathBuilder.getPath());
        }
    } // actionPerformed
} // BuildSourcepathAction
