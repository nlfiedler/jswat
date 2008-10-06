/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: PopupAdapter.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.actions;

import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import org.openide.awt.MouseUtils;

/**
 * Displays a popup menu at the appropriate times.
 *
 * @author Nathan Fiedler
 */
public class PopupAdapter extends MouseUtils.PopupMouseAdapter {
    /** The popup menu to be shown. */
    private JPopupMenu popup;

    /**
     * Creates a new instance of PopupAdapter.
     *
     * @param  popup  the menu to be displayed.
     */
    public PopupAdapter(JPopupMenu popup) {
        this.popup = popup;
    }

    /**
     * Called when the sequnce of mouse events should lead to actual
     * showing of the popup menu.
     *
     * @param  e  should be used to obtain the position of the popup menu.
     */
    public void showPopup(MouseEvent e) {
        popup.show(e.getComponent(), e.getX(), e.getY());
    }
}
