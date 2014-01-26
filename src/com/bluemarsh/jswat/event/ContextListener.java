/*********************************************************************
 *
 *      Copyright (C) 1999 Nathan Fiedler
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
 * MODULE:      JSwat Events
 * FILE:        ContextListener.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      10/16/99        Initial version
 *
 * DESCRIPTION:
 *      Defines the context change listener interface.
 *
 ********************************************************************/

package com.bluemarsh.jswat.event;

import java.util.EventListener;

/**
 * The listener interface for receiving changes in the current
 * debugger context. If the listener is looking for both location
 * information and thread information, then the listener should
 * save the thread change information when the thread change
 * event occurs. The following event will often be a location
 * change event.
 *
 * @author  Nathan Fiedler
 * @version 1.0  10/16/99
 */
public interface ContextListener extends EventListener {

    /**
     * Invoked when the current context has changed. The context
     * change event identifies which aspect of the context has
     * changed.
     *
     * @param  cce  context change event
     */
    public void contextChanged(ContextChangeEvent cce);
} // ContextListener
