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

package com.bluemarsh.jswat.ui.toolbar;

import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.VMDisconnectedException;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;


/**
 * Renders the list cell to indicate the session state and its name.
 *
 * @author  Nathan Fiedler
 */
public class SessionStatusRenderer extends DefaultListCellRenderer {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The icon for when the session is suspended. */
    private static Icon pausedIcon;
    /** The icon for when the session is running. */
    private static Icon startingIcon;
    /** The icon for when the session is disconnected. */
    private static Icon stoppedIcon;

    static {
        pausedIcon = new ImageIcon(ImageUtilities.loadImage(NbBundle.getMessage(
                SessionStatusRenderer.class, "IMG_SessionStatus_Paused")));
        startingIcon = new ImageIcon(ImageUtilities.loadImage(NbBundle.getMessage(
                SessionStatusRenderer.class, "IMG_SessionStatus_Starting")));
        stoppedIcon = new ImageIcon(ImageUtilities.loadImage(NbBundle.getMessage(
                SessionStatusRenderer.class, "IMG_SessionStatus_Stopped")));
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list,
                value, index, isSelected, cellHasFocus);
        // Set the label icon and text to suit the session state.
        Session session = (Session) value;
        if (session.isConnected()) {
            try {
                if (session.isSuspended()) {
                    label.setIcon(pausedIcon);
                } else {
                    label.setIcon(startingIcon);
                }
            } catch (IllegalStateException ise) {
                // Session disconnected between 'isConnected' and 'isSuspended'.
                label.setIcon(stoppedIcon);
	    } catch (VMDisconnectedException vmde) {
                label.setIcon(stoppedIcon);
            }
        } else {
            label.setIcon(stoppedIcon);
        }
        label.setText(session.getProperty(Session.PROP_SESSION_NAME));
        return label;
    }
}
