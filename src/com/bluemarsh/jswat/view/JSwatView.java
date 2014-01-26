/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * MODULE:      View
 * FILE:        JSwatView.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/10/99        Initial version
 *      nf      09/17/01        Remove SessionListener part
 *
 * DESCRIPTION:
 *      This file contains the JSwatView abstract class definition.
 *
 * $Id: JSwatView.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.Session;
import javax.swing.JComponent;

/**
 * This is the abstract class for classes wanting to provide a view
 * to some data.
 *
 * @author  Nathan Fiedler
 */
public abstract class JSwatView {

    /**
     * Returns a reference to the UI component.
     *
     * @return  UI component object
     */
    public abstract JComponent getUI();

    /**
     * Update the display on the screen. Use the given VM
     * to fetch the desired data.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
    } // refresh
} // JSwatView
