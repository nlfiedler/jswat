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
 * $Id: LookAndFeelMenu.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.ui.Bundle;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Specialized menu class that implements the look & feel menu for this
 * program. It automatically builds out the menu for selecting the
 * available look and feels.
 *
 * <p>This is one of the available special menus. It is requested in the
 * resources file using the "@lookAndFeel" special menu tag.</p>
 *
 * @author  Nathan Fiedler
 */
class LookAndFeelMenu extends JMenu implements ItemListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Array of LookAndFeel information objects. */
    private UIManager.LookAndFeelInfo[] lafInfo;
    /** User preferences for this package. */
    private Preferences preferences;

    /**
     * Constructs a LookAndFeelMenu.
     */
    public LookAndFeelMenu() {
        super(Bundle.getString("lookAndFeelLabel"), true);

        // Create menu items for changing Look & Feel.
        ButtonGroup group = new ButtonGroup();
        lafInfo = UIManager.getInstalledLookAndFeels();
        String curLAFName = UIManager.getLookAndFeel().getName();
        for (int i = 0; i < lafInfo.length; i++) {
            String lafName = lafInfo[i].getName();
            JRadioButtonMenuItem lafMenuItem =
                new JRadioButtonMenuItem(lafName);
            if (curLAFName.equals(lafName)) {
                lafMenuItem.setSelected(true);
            }
            add(lafMenuItem);
            group.add(lafMenuItem);
            lafMenuItem.addItemListener(this);
        }

        setToolTipText("<html><small>"
                       + Bundle.getString("lookAndFeelTooltip")
                       + "</small></html>");

        preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/ui/graphical");
    } // LookAndFeelMenu

    /**
     * One of the look & feels was selected. See which one it was
     * and switch the entire user interface to that look & feel.
     *
     * @param  e  Indicates which item was selected.
     */
    public void itemStateChanged(ItemEvent e) {
        JRadioButtonMenuItem rb = (JRadioButtonMenuItem) e.getSource();
        if (rb.isSelected()) {
            Frame window = SessionFrameMapper.getOwningFrame(e);
            window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                for (int i = 0; i < lafInfo.length; i++) {
                    if (rb.getText().equals(lafInfo[i].getName())) {
                        // Switch to the selected look & feel.
                        UIManager.setLookAndFeel(
                            lafInfo[i].getClassName());
                        // Save the setting in the preferences.
                        preferences.put("lookAndFeel",
                                        lafInfo[i].getClassName());
                        break;
                    }
                }
                SwingUtilities.updateComponentTreeUI(window);

            } catch (Exception exc) {
                rb.setEnabled(false);
                String msg = MessageFormat.format(
                    Bundle.getString("LookAndFeelMenu.error.loadLAF"),
                    new Object[] { rb.getText(), exc });
                Session session = SessionFrameMapper.getSessionForEvent(e);
                session.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_WARNING, msg);
                // Reset the previously selected item.
                String curLAFname = UIManager.getLookAndFeel().getName();
                Component[] components = getMenuComponents();
                for (int ii = 0; ii < components.length; ii++) {
                    AbstractButton item = (AbstractButton) components[ii];
                    if (item.getText().equals(curLAFname)) {
                        item.setSelected(true);
                        break;
                    }
                }
            }
            window.setCursor(Cursor.getPredefinedCursor(
                                 Cursor.DEFAULT_CURSOR));
        }
    } // itemStateChanged
} // LookAndFeelMenu
