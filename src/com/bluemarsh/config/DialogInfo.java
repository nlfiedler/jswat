/*********************************************************************
 *
 *	Copyright (C) 2000 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * PROJECT:     JConfigure
 * FILE:        DialogInfo.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      NF      11/26/00        Initial version
 *
 * DESCRIPTION:
 *      This file defines the dialog information container.
 *
 * $Id: DialogInfo.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import javax.swing.JButton;
import javax.swing.JDialog;

/**
 * Class DialogInfo contains references to the preferences dialog,
 * and the Ok and Cancel buttons.
 *
 * @author  Nathan Fiedler
 * @version 1.0  11/26/00
 */
class DialogInfo {
    /** Preferences dialog. */
    public JDialog dialog;
    /** Ok button. */
    public JButton okButton;
    /** Cancel button. */
    public JButton cancelButton;

    /**
     * Constructs a new DialogInfo object.
     *
     * @param  dialog  Dialog
     * @param  ok      Ok button
     * @param  cancel  Cancel button
     */
    public DialogInfo(JDialog dialog, JButton ok, JButton cancel) {
        this.dialog = dialog;
        this.okButton = ok;
        this.cancelButton = cancel;
    } // DialogInfo
} // DialogInfo
