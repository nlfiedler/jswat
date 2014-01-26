/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: OpenFileAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.config.JConfigure;
import com.bluemarsh.jswat.AppSettings;
import com.bluemarsh.jswat.FileSource;
import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;

/**
 * Implements the open file action used to open a source file.
 * This class also defines a file filter used with the file
 * selector to limit the user to choosing only source files.
 *
 * @author  Nathan Fiedler
 */
public class OpenFileAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new OpenFileAction object with the default action
     * command string of "openFile".
     */
    public OpenFileAction() {
        super("openFile");
    } // OpenFileAction

    /**
     * Performs the open action. This presents the user with
     * a file selector and opens the selected file.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // get main window that contains our invoker
        Frame win = getFrame(event);
        String lastDir = AppSettings.instanceOf().getString("openfileLastdir");
        if (lastDir == null || lastDir.trim().length() == 0) {
            lastDir = System.getProperty("user.dir");
        }
        JFileChooser fc = new JFileChooser(lastDir);
        fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
        JConfigure config = JSwat.instanceOf().getJConfigure();
        String ext = config.getProperty("files.defaultExtension");
        fc.setFileFilter(new JSwatFileFilter(ext, "Source Files"));
        int response = fc.showOpenDialog(win);

        // Remember the last open directory for next time.
        AppSettings.instanceOf().setString("openfileLastdir",
                                           fc.getCurrentDirectory().getPath());

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

        // Create a new source file and have it read the file.
        UIAdapter adapter = getSession(event).getUIAdapter();
        SourceSource src = new FileSource(f);
        if (!adapter.showFile(src, 0, 0)) {
            displayError(event, swat.getResourceString("couldntOpenFileMsg"));
        }
    } // actionPerformed
} // OpenFileAction
