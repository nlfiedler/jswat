/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Nathan Fiedler
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
 * FILE:        Condition.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/24/01        Initial version
 *      nf      06/14/01        Extends Serializable, provides
 *                              ui adapter
 *      nf      09/27/01        Change satisfied() name and pass Event
 *
 * DESCRIPTION:
 *      Defines the Condition interface.
 *
 * $Id: Condition.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.breakpoint.ui.ConditionUI;
import com.sun.jdi.event.Event;
import java.io.Serializable;

/**
 * Interface Condition defines a breakpoint conditional.
 *
 * @author  Nathan Fiedler
 */
public interface Condition extends Serializable {

    /**
     * Returns the user interface widget for customizing this condition.
     *
     * @return  Condition user interface adapter.
     */
    public ConditionUI getUIAdapter();

    /**
     * Returns true if this condition is satisfied.
     *
     * @param  event  JDI Event that brought us here.
     * @return  True if satisfied, false otherwise.
     */
    public boolean isSatisfied(Event event) throws Exception;
} // Condition
