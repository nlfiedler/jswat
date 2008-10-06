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
 * $Id: BuildClasspathAction.java 14 2007-06-02 23:50:55Z nfiedler $
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
 * Implements the build classpath action.
 *
 * @author  David Taylor
 */
public class BuildClasspathAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a BuildClasspathAction object with the default action
     * command string of "buildClasspath".
     */
    public BuildClasspathAction() {
        super("buildClasspath");
    } // BuildClasspathAction

    /**
     * Performs the build classpath action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // Get the current classpath setting.
        Session session = getSession(event);
        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        String[] classpath = pathman.getClassPath();

        // Build the PathBuilder.
        PathBuilder pathBuilder = new PathBuilder(session.isActive());
        pathBuilder.setPath(classpath);
        pathBuilder.setFileFilter(new PathBuilder.ClasspathFilter());
        pathBuilder.setMultiSelectionEnabled(true);

        Preferences preferences = Preferences.userNodeForPackage(
            this.getClass());
        String lastDirectory = preferences.get("buildCpLastDir", null);
        if (lastDirectory != null && lastDirectory.trim().length() > 0) {
            pathBuilder.setStartDirectory(lastDirectory);
        }

        // Build and display the classpath setting dialog.
        Frame window = getFrame(event);
        int response = JOptionPane.showOptionDialog(
            window, pathBuilder,
            Bundle.getString("BuildClasspath.title"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, null, null);

        lastDirectory = pathBuilder.getLastDirectorySeen();
        if (lastDirectory != null) {
            preferences.put("buildCpLastDir", lastDirectory);
        }
        if (response == JOptionPane.OK_OPTION) {
            // If ok, save the classpath given by the user.
            pathman.setClassPath(pathBuilder.getPath());
        }
    } // actionPerformed

    /**
     * Returns true to indicate that this action should be disabled
     * when the debuggee is resumed.
     *
     * @return  true to disable, false to enable.
     */
    public boolean disableOnResume() {
        return false;
    } // disableOnResume

    /**
     * Returns true to indicate that this action should be disabled
     * when the debuggee is suspended.
     *
     * @return  true to disable, false to enable.
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
        return true;
    } // disableWhenActive
} // BuildClasspathAction
