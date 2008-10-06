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
 * $Id: OpenFileAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceFactory;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;

/**
 * Implements the open file action used to open a source file. This
 * class also defines a file filter used with the file selector to limit
 * the user to choosing only source files.
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
     * Performs the open action. This presents the user with a file
     * selector and opens the selected file.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // get main window that contains our invoker
        Frame win = getFrame(event);
        Preferences preferences = Preferences.userNodeForPackage(
            this.getClass());
        String lastDir = preferences.get("openfileLastdir", null);
        if (lastDir == null || lastDir.trim().length() == 0) {
            lastDir = System.getProperty("user.dir");
        }
        JFileChooser fc = new JFileChooser(lastDir);
        fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
        fc.setFileHidingEnabled(false);
        // Hard-coded list of known source file extensions.
        java.util.List ext = new java.util.LinkedList();
        ext.add(".java");
        ext.add(".jsp");
        ext.add(".nice");
        fc.setFileFilter(new JSwatFileFilter(ext, "Source Files"));
        int response = fc.showOpenDialog(win);

        // Remember the last open directory for next time.
        preferences.put("openfileLastdir",
                        fc.getCurrentDirectory().getPath());

        if (response != JFileChooser.APPROVE_OPTION) {
            // user hit cancel or closed the dialog
            return;
        }

        // see if we can read from the file
        File f = fc.getSelectedFile();
        try {
            if (!f.canRead()) {
                displayError(event, Bundle.getString("cantReadFileMsg"));
                return;
            }
        } catch (SecurityException se) {
            displayError(event, Bundle.getString("noFileAccessMsg"));
            return;
        }

        // Create a new source file and have it read the file.
        Session session = getSession(event);
        UIAdapter adapter = session.getUIAdapter();
        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        SourceSource src = SourceFactory.getInstance().create(f, pathman);
        if (!src.exists() || !adapter.showFile(src, 0, 0)) {
            displayError(event, Bundle.getString("couldntOpenFileMsg"));
        }
    } // actionPerformed
} // OpenFileAction
