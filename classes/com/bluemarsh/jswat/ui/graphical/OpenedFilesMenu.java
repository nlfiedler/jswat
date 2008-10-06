/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: OpenedFilesMenu.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.FileSource;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceFactory;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.ui.Bundle;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * This is a specialized menu class that implements the most recently
 * opened files menu. This menu will hold a list of file names
 * representing the files that were recently opened, up to a fixed
 * number of files. Selecting a menu item will reopen that file in a
 * source view.
 *
 * @author  Nathan Fiedler
 */
class OpenedFilesMenu extends JMenu
    implements ActionListener, PropertyChangeListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Maximum length in characters for menu items. */
    private static final int MENU_MAX_LENGTH = 50;
    /** Prefix for the file node names. */
    private static final String NODE_PREFIX = "file";
    /** List of recent files. */
    private List recentFiles;
    /** Our preferences node. */
    private Preferences preferences;
    /** Session we belong to. */
    private Session ourSession;

    /**
     * Constructor for this class.
     *
     * @param  session  session we belong to.
     */
    public OpenedFilesMenu(Session session) {
        super(Bundle.getString("openedFilesLabel"), true);
        ourSession = session;
        recentFiles = new LinkedList();
        UIAdapter adapter = session.getUIAdapter();
        adapter.addPropertyChangeListener(this);
        setToolTipText("<html><small>"
                       + Bundle.getString("openedFilesTooltip")
                       + "</small></html>");
        preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/ui/mro");

        // Populate the list from the preferences.
        int maxSize = preferences.getInt("maxListSize",
                                         Defaults.MRO_LIST_SIZE);
        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        SourceFactory sf = SourceFactory.getInstance();
        for (int ii = maxSize; ii >= 1; ii--) {
            String path = preferences.get(NODE_PREFIX + ii, null);
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    JMenuItem item = createMenuItem(path);
                    SourceSource ss = sf.create(file, pathman);
                    SourceItemPair fip = new SourceItemPair(ss, item);
                    recentFiles.add(fip);
                }
            }
        }
    } // OpenedFilesMenu

    /**
     * A menu item was selected.
     *
     * @param  evt  the action event.
     */
    public void actionPerformed(ActionEvent evt) {
        JMenuItem src = (JMenuItem) evt.getSource();
        // Find the menu-file pair in the list.
        SourceItemPair fip = findPair(src);
        if (fip != null) {
            UIAdapter adapter = ourSession.getUIAdapter();
            adapter.showFile(fip.getSource(), 0, 0);
        }
    } // actionPerformed

    /**
     * Builds a menu item for file name.
     *
     * @param  filename  full path of the file.
     * @return  new menu item.
     */
    protected JMenuItem createMenuItem(String filename) {
        int len = filename.length();
        if (len > MENU_MAX_LENGTH) {
            filename = "..." + filename.substring(len - MENU_MAX_LENGTH);
        }
        JMenuItem item = new JMenuItem(filename);
        item.addActionListener(this);
        // Add to the top of the list.
        add(item, 0);
        return item;
    } // createMenuItem

    /**
     * Find the SourceItemPair by looking for the given file source.
     *
     * @param  src  source to look for.
     * @return  SourceItemPair if found, null if not.
     */
    protected SourceItemPair findPair(SourceSource src) {
        for (int ii = recentFiles.size() - 1; ii >= 0; ii--) {
            SourceItemPair fip = (SourceItemPair) recentFiles.get(ii);
            if (fip.getSource().equals(src)) {
                return fip;
            }
        }
        return null;
    } // findPair

    /**
     * Find the SourceItemPair by looking for the given menu item.
     *
     * @param  item  menu item to look for.
     * @return  SourceItemPair if found, null if not.
     */
    protected SourceItemPair findPair(JMenuItem item) {
        for (int ii = recentFiles.size() - 1; ii >= 0; ii--) {
            SourceItemPair fip = (SourceItemPair) recentFiles.get(ii);
            if (fip.getMenuItem().equals(item)) {
                return fip;
            }
        }
        return null;
    } // findPair

    /**
     * This method gets called when a bound property is changed.
     *
     * @param  evt  A PropertyChangeEvent object describing the event source
     *              and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("fileOpened")) {
            Object o = evt.getNewValue();
            // We only care about files on the local file system.
            if (o instanceof FileSource) {
                SourceSource ss = (SourceSource) o;
                // Is this file already in our list?
                SourceItemPair fip = findPair(ss);
                if (fip == null) {

                    // Make a new menu item for this file.
                    String filepath = ss.getPath();
                    JMenuItem item = createMenuItem(filepath);
                    fip = new SourceItemPair(ss, item);

                    // Trim the file-item list.
                    int maxSize = preferences.getInt("maxListSize",
                                                     Defaults.MRO_LIST_SIZE);
                    recentFiles.add(fip);
                    while (recentFiles.size() > maxSize) {
                        recentFiles.remove(0);
                    }

                    // Trim the menu down to size.
                    while (getMenuComponentCount() > maxSize) {
                        remove(getMenuComponentCount() - 1);
                    }

                    // Update the preferences information.
                    for (int ii = maxSize - 1, jj = maxSize;
                         ii >= 1; ii--, jj--) {
                        String val = preferences.get(NODE_PREFIX + ii, null);
                        if (val != null) {
                            preferences.put(NODE_PREFIX + jj, val);
                        }
                    }
                    // Now store the new file path.
                    preferences.put(NODE_PREFIX + '1', filepath);
                }
            }
        }
    } // propertyChange

    /**
     * A pairing of a SourceSource and a JMenuItem.
     *
     * @author  Nathan Fiedler
     */
    protected class SourceItemPair {
        /** The source. */
        private SourceSource source;
        /** The menu item. */
        private JMenuItem item;

        /**
         * Constructs a source and item pairing.
         *
         * @param  src   source.
         * @param  item  menu item.
         */
        public SourceItemPair(SourceSource src, JMenuItem item) {
            this.source = src;
            this.item = item;
        } // SourceItemPair

        /**
         * Returns the source.
         *
         * @return  the source.
         */
        public SourceSource getSource() {
            return source;
        } // getSource

        /**
         * Returns the menu item.
         *
         * @return  the menu item.
         */
        public JMenuItem getMenuItem() {
            return item;
        } // getMenuItem
    } // SourceItemPair
} // OpenedFilesMenu
