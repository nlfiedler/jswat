/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.actions;

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

    @Override
    public void showPopup(MouseEvent e) {
        popup.show(e.getComponent(), e.getX(), e.getY());
    }
}
