/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * PROJECT:     JSwat
 * MODULE:      JSwat UI
 * FILE:        SmartPopupMenu.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/25/02        Initial version
 *
 * $Id: SmartPopupMenu.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Stack;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.UIManager;

/**
 * A popup menu that listens for "lookAndFeel" property changes in the
 * <code>javax.swing.UIManager</code> and takes the appropriate action. It
 * invokes its own <code>updateUI()</code> method and signals its children
 * as well. You would think this would happen automatically, but apparently
 * not. In fact, it seems like a bug to me.
 *
 * @author  Nathan Fiedler
 */
public abstract class SmartPopupMenu extends JPopupMenu
    implements MouseListener, PropertyChangeListener {

    /**
     * Constructs a SmartPopupMenu.
     */
    public SmartPopupMenu() {
        super();
        UIManager.addPropertyChangeListener(this);
        // We assume this popup will never die, so we don't bother to
        // remove ourselves at any point in the future.
    } // SmartPopupMenu

    /**
     * Constructs a SmartPopupMenu with a label.
     *
     * @param  label  label for popup.
     */
    public SmartPopupMenu(String label) {
        super(label);
        UIManager.addPropertyChangeListener(this);
        // We assume this popup will never die, so we don't bother to
        // remove ourselves at any point in the future.
    } // SmartPopupMenu

    /**
     * Make the popup menu unconditionally disappear.
     *
     * @param  e  mouse event.
     */
    protected void hidePopup(MouseEvent e) {
        // Process the mouse event normally.
        // (Thanks to Peter Boothe for this bug fix.)
        MenuSelectionManager.defaultManager().processMouseEvent(e);
        // Make the menu disappear.
        MenuSelectionManager.defaultManager().clearSelectedPath();
    } // hidePopup

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
        if (!e.isConsumed()) {
            if (e.isPopupTrigger()) {
                showPopup(e);
                e.consume();
            } else {
                hidePopup(e);
            }
        }
    } // mousePressed

    /**
     * Invoked when a mouse button has been released on a component.
     * We use this opportunity to show the popup menu.
     *
     * @param  e  Mouse event.
     */
    public void mouseReleased(MouseEvent e) {
        if (!e.isConsumed()) {
            if (e.isPopupTrigger()) {
                showPopup(e);
                e.consume();
            } else {
                hidePopup(e);
            }
        }
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
     * This method gets called when a bound property is changed.
     *
     * @param  evt  a PropertyChangeEvent object describing the event source
     *              and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("lookAndFeel")) {
            // Seems like we're the last to find out...
            updateUI();
        }
    } // propertyChange

    /**
     * Display the popup menu.
     *
     * @param  evt  mouse event.
     */
    protected abstract void showPopup(MouseEvent evt);

    /**
     * Resets the UI property to a value from the current look and feel.
     */
    public void updateUI() {
        super.updateUI();
        // Bizarrely, popups are missed in the UI change, and so we must
        // handle this manually. Be sure to visit all of the constituent
        // elements, including their children.
        Stack stack = new Stack();
        stack.push(getComponents());
        while (!stack.empty()) {
            Component[] children = (Component[]) stack.pop();
            if (children != null && children.length > 0) {
                for (int ii = 0; ii < children.length; ii++) {
                    JComponent jcomp = (JComponent) children[ii];
                    jcomp.updateUI();
                    stack.push(jcomp.getComponents());
                }
            }
        }
    } // updateUI
} // SmartPopupMenu
