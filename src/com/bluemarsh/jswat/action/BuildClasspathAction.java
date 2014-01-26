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
 * $Id: BuildClasspathAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.AppSettings;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

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
        String classpath = pathman.getClassPathAsString();

        // Build the PathBuilder.
        PathBuilder pathBuilder = new PathBuilder();
        pathBuilder.setPath(classpath);
        pathBuilder.setFileFilter(new ClasspathFilter());
        // Avoid broken-ness in JDK 1.2.2
        //pathBuilder.setMultiSelectionEnabled(true);

        String lastDirectory = AppSettings.instanceOf().getString(
            "buildCpLastDir");
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
            AppSettings.instanceOf().setString("buildCpLastDir",
                                               lastDirectory);
        }
        if (response == JOptionPane.OK_OPTION) {
            // If ok, save the classpath given by the user.
            pathman.setClassPath(pathBuilder.getPath());
        }
    } // actionPerformed

    /**
     * Class ClasspathFilter implmenets a FileFilter that only accepts
     * directories and files ending in .jar or .zip.
     */
    class ClasspathFilter extends FileFilter {

        /**
         * Test if the given file or directory is acceptable.
         *
         * @param  f  file to consider.
         * @return  true if file is acceptable.
         */
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String fname = f.getName();
            int fnameLength = fname.length();
            if (fnameLength < 5) {
                return false;
            }

            String ext = fname.substring(fnameLength - 4, fnameLength);
            if (ext.equalsIgnoreCase(".jar") ||
                ext.equalsIgnoreCase(".zip")) {
                return true;
            }
            return false;
        } // accept

        /**
         * Returns the description of this file filter.
         *
         * @return  String description of this filter.
         */
        public String getDescription() {
            return "Classpath elements";
        } // getDescription
    } // ClasspathFilter
} // BuildClasspathAction
