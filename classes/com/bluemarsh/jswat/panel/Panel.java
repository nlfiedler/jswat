/*********************************************************************
 *
 *      Copyright (C) 2003 Nathan Fiedler
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
 * MODULE:      Panel
 * FILE:        Panel.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/06/03        Initial version
 *
 * $Id: Panel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.Session;
import javax.swing.JComponent;

/**
 * Panel defines the methods for all panels to implement.
 *
 * @author  Nathan Fiedler
 */
public interface Panel {

    /**
     * Returns a reference to the UI component which can be added to the
     * user interface component tree.
     *
     * @return  interface component.
     */
    JComponent getUI();

    /**
     * Update the display in the panel. Use the given session to fetch
     * any necessary data.
     *
     * @param  session  owning Session.
     */
    void refresh(Session session);

    /**
     * Update the display in the panel at some point in the near future.
     */
    void refreshLater();
} // Panel
