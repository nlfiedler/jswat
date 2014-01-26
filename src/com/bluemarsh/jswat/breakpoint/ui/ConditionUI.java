/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * FILE:        ConditionUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/30/01        Initial version
 *      nf      06/14/01        Extends UIAdapter
 *      nf      08/16/01        Removed to breakpoint.ui package
 *      nf      09/23/01        Added descriptor() and getCondition()
 *
 * DESCRIPTION:
 *      Defines the ConditionUI interface.
 *
 * $Id: ConditionUI.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.Condition;

/**
 * Interface ConditionUI defines the methods necessary for a condition
 * UI adapter implementation.
 *
 * @author  Nathan Fiedler
 */
public interface ConditionUI extends UIAdapter {

    /**
     * Generates a string descriptor of this condition.
     *
     * @return  description.
     */
    public String descriptor();

    /**
     * Returns the Condition object this ui adapter represents.
     *
     * @return  Condition object.
     */
    public Condition getCondition();
} // ConditionUI
