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
 * $Id: EditPopup.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.text.JTextComponent;

/**
 * Class Popup defines a subclass of JPopupMenu that works for any text
 * component to provide copy, paste, and clear functions. Like any
 * JPopupMenu, you must add this popup as a child to the text component
 * in question. It also must be added as a mouse listener to the text
 * component.
 *
 * @author  Nathan Fiedler
 */
public class EditPopup extends JPopupMenu implements ActionListener, MouseListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Text component to operate on. */
    protected JTextComponent textComponent;
    /** The Clear menu item. */
    protected JMenuItem clearMenuItem;
    /** The Copy menu item. */
    protected JMenuItem copyMenuItem;
    /** The Paste menu item. */
    protected JMenuItem pasteMenuItem;
    /** The Select All menu item. */
    protected JMenuItem selectAllMenuItem;

    /**
     * Constructs an EditPopup that interacts with the given text
     * component. The popup can offer pasting as well as clearing
     * of the text component. By default, the popup will allow
     * copying the selected text to the clipboard.
     *
     * @param  text   text component to manage.
     * @param  paste  true to allow pasting.
     * @param  clear  true to allow clearing.
     */
    public EditPopup(JTextComponent text, boolean paste, boolean clear) {
        super(Bundle.getString("Edit.label"));
        textComponent = text;

        if (clear) {
            clearMenuItem = new JMenuItem(Bundle.getString("Edit.clearLabel"));
            clearMenuItem.addActionListener(this);
            add(clearMenuItem);
        }

        // We will always allow copying the selected text.
        copyMenuItem = new JMenuItem(Bundle.getString("Edit.copyLabel"));
        copyMenuItem.addActionListener(this);
        add(copyMenuItem);

        if (paste) {
            pasteMenuItem = new JMenuItem(Bundle.getString("Edit.pasteLabel"));
            pasteMenuItem.addActionListener(this);
            add(pasteMenuItem);
        }

        // We will always allow selecting all the text.
        selectAllMenuItem = new JMenuItem(
            Bundle.getString("Edit.selectAllLabel"));
        selectAllMenuItem.addActionListener(this);
        add(selectAllMenuItem);
    } // EditPopup

    /**
     * Invoked when a menu item has been selected.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        JMenuItem source = (JMenuItem) event.getSource();
        if (source == copyMenuItem) {
            textComponent.copy();
        } else if (source == clearMenuItem) {
            textComponent.setText("");
        } else if (source == selectAllMenuItem) {
            textComponent.selectAll();
            textComponent.getCaret().setSelectionVisible(true);
        } else {
            textComponent.paste();
        }
    } // actionPerformed

    /**
     * Invoked when the mouse has been clicked on a component.
     *
     * @param  e  Mouse event.
     */
    public void mouseClicked(MouseEvent e) {
    } // mouseClicked

    /**
     * Invoked when a mouse button has been pressed on a component.
     * We use this opportunity to show the popup menu.
     *
     * @param  e  Mouse event.
     */
    public void mousePressed(MouseEvent e) {
        // Must check this in both 'pressed' and 'released'.
        showPopup(e);
    } // mousePressed

    /**
     * Invoked when a mouse button has been released on a component.
     * We use this opportunity to show the popup menu.
     *
     * @param  e  Mouse event.
     */
    public void mouseReleased(MouseEvent e) {
        // Must check this in both 'pressed' and 'released'.
        showPopup(e);
    } // mouseReleased

    /**
     * Invoked when the mouse enters a component.
     *
     * @param  e  Mouse event.
     */
    public void mouseEntered(MouseEvent e) {
    } // mouseEntered

    /**
     * Invoked when the mouse exits a component.
     *
     * @param  e  Mouse event.
     */
    public void mouseExited(MouseEvent e) {
    } // mouseExited

    /**
     * Decide whether or not to show the popup menu.
     *
     * @param  e  Mouse event.
     */
    protected void showPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            // Enable the 'Clear' item if text component is enabled.
            if (clearMenuItem != null) {
                if (textComponent.isEnabled()) {
                    clearMenuItem.setEnabled(true);
                } else {
                    clearMenuItem.setEnabled(false);
                }
            }

            // If no text selected, disable Copy menu item.
            if (textComponent.getSelectionStart() ==
                textComponent.getSelectionEnd()) {
                copyMenuItem.setEnabled(false);
            } else {
                copyMenuItem.setEnabled(true);
            }

            if (pasteMenuItem != null) {
                // If nothing on the clipboard, disable Paste menu item.
                Toolkit tk = Toolkit.getDefaultToolkit();
                Clipboard cb = tk.getSystemClipboard();
                Transferable content = cb.getContents(this);
                if ((content != null) && (textComponent.isEnabled())) {
                    pasteMenuItem.setEnabled(true);
                } else {
                    pasteMenuItem.setEnabled(false);
                }
            }

            // Enable the 'Select all' item if text component is enabled.
            if (selectAllMenuItem != null) {
                if (textComponent.isEnabled()) {
                    selectAllMenuItem.setEnabled(true);
                } else {
                    selectAllMenuItem.setEnabled(false);
                }
            }

            // Show the popup menu.
            show(e.getComponent(), e.getX(), e.getY());
        } else {
            // Process the mouse event normally.
            MenuSelectionManager.defaultManager().processMouseEvent(e);
            // Make the menu disappear.
            MenuSelectionManager.defaultManager().clearSelectedPath();
        }
    } // showPopup
} // EditPopup
