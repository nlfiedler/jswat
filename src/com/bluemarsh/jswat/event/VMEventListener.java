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
 * MODULE:      JSwat
 * FILE:        VMEventListener.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/11/99        Initial version
 *      nf      06/03/01        Added breakpoint priority level
 *
 * DESCRIPTION:
 *      Defines the interface for listeners wishing to receive events
 *      from the JPDA debugger back-end.
 *
 * $Id: VMEventListener.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.event;

import com.sun.jdi.event.Event;
import java.util.EventListener;

/**
 * Listener interface for VM events sent from the debugger back-end.
 *
 * @author  Nathan Fiedler
 */
public interface VMEventListener extends EventListener {
    /** Value for which no priority is higher. */
    public static final int PRIORITY_HIGHEST = 1023;
    /** Value for a Breakpoint listener. */
    public static final int PRIORITY_BREAKPOINT = 1023;
    /** Value for Session listener. */
    public static final int PRIORITY_SESSION = 511;
    /** Value for a high priority listener. */
    public static final int PRIORITY_HIGH = 255;
    /** Value for a default priority listener. */
    public static final int PRIORITY_DEFAULT = 127;
    /** Value for a low priority listener. */
    public static final int PRIORITY_LOW = 1;
    /** Value for which no priority is lower. */
    public static final int PRIORITY_LOWEST = 1;

    /**
     * Invoked when a VM event has occurred.
     *
     * @param  e  VM event
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e);
} // VMEventListener
