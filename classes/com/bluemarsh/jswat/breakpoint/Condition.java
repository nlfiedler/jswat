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
 * FILE:        Condition.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/24/01        Initial version
 *      nf      06/14/01        Provides ui adapter
 *      nf      09/27/01        Change satisfied() name and pass Event
 *
 * DESCRIPTION:
 *      Defines the Condition interface.
 *
 * $Id: Condition.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.breakpoint.ui.ConditionUI;
import com.sun.jdi.event.Event;
import java.util.prefs.Preferences;

/**
 * Interface Condition defines a breakpoint conditional.
 *
 * @author  Nathan Fiedler
 */
public interface Condition {

    /**
     * Returns the user interface widget for customizing this condition.
     *
     * @return  Condition user interface adapter.
     */
    ConditionUI getUIAdapter();

    /**
     * Returns true if this condition is satisfied.
     *
     * @param  event  event that brought us here.
     * @return  true if satisfied, false otherwise.
     * @throws  Exception
     *          if a problem occurs.
     */
    boolean isSatisfied(Event event) throws Exception;

    /**
     * Reads the condition properties from the given preferences node.
     *
     * @param  prefs  preferences node from which to initialize this
     *                condition.
     * @return  true if successful, false otherwise.
     */
    boolean readObject(Preferences prefs);

    /**
     * Writes the condition properties to the given preferences node.
     * It is assumed that the preferences node is completely empty.
     *
     * @param  prefs  preferences node to which to serialize this
     *                condition.
     * @return  true if successful, false otherwise.
     */
    boolean writeObject(Preferences prefs);
} // Condition
