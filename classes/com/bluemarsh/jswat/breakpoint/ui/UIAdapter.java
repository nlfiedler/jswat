/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 * MODULE:      Breakpoints
 * FILE:        UIAdapter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/30/01        Initial version
 *      nf      07/31/01        Added undo()
 *      nf      08/16/01        Removed to breakpoint.ui package
 *
 * $Id: UIAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import java.awt.Component;

/**
 * Interface UIAdapter defines the methods necessary for a breakpoint,
 * monitor, or condition UI adapter implementation.
 *
 * @author  Nathan Fiedler
 */
public interface UIAdapter {

    /**
     * Commit the values stored in the user interface elements to the
     * object this adapter is representing (breakpoint, condition, or
     * monitor).
     */
    void commit();

    /**
     * Return a reference to the user interface element that this
     * adapter uses to graphically represent the breakpoint, condition,
     * or monitor. This may be a container that has several user
     * interface elements inside it.
     *
     * @return  user interface ocmponent.
     */
    Component getUI();

    /**
     * Reverse the changes made to the object this adapter is
     * representing (breakpoint, condition, or monitor). This must
     * not modify the user interface widgets.
     */
    void undo();
} // UIAdapter
