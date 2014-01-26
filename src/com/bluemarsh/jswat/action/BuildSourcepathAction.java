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
 * $Id: BuildSourcepathAction.java 1814 2005-07-17 05:56:32Z nfiedler $
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
        String sourcepath = pathman.getSourcePathAsString();

        // Build the PathBuilder.
        PathBuilder pathBuilder = new PathBuilder();
        if (sourcepath != null) {
            pathBuilder.setPath(sourcepath);
        }
        pathBuilder.setFileFilter(new SourcepathFilter());
        // Avoid broken-ness in JDK 1.2.2
        //pathBuilder.setMultiSelectionEnabled(true);

        String lastDirectory = AppSettings.instanceOf().getString(
            "buildSpLastDir");
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
            AppSettings.instanceOf().setString("buildSpLastDir",
                                               lastDirectory);
        }
        if (response == JOptionPane.OK_OPTION) {
            // If ok, save the sourcepath given by the user.
            pathman.setSourcePath(pathBuilder.getPath());
        }
    } // actionPerformed

    /**
     * Class SourcepathFilter implmenets a FileFilter that only accepts
     * directories.
     */
    class SourcepathFilter extends FileFilter {

        /**
         * Test if the given file or directory is acceptable.
         *
         * @param  f  file to consider.
         * @return  true if file is acceptable.
         */
        public boolean accept(File f) {
            String fname = f.getName().toLowerCase();
            if (f.isDirectory() ||
                fname.endsWith(".zip") || fname.endsWith(".jar")) {
                return true;
            } else {
                return false;
            }
        } // accept

        /**
         * Returns the description of this file filter.
         *
         * @return  String description of this filter.
         */
        public String getDescription() {
            return "Sourcepath elements";
        } // getDescription
    } // SourcepathFilter
} // BuildSourcepathAction
