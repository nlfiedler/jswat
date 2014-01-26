/*********************************************************************
 *
 *      Copyright (C) 2000-2001 Nathan Fiedler
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
 * FILE:        Manager.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/24/00        Initial version
 *      nf      04/22/01        Turned into an interface.
 *
 * DESCRIPTION:
 *      Contains the interface that defines a manager.
 *
 * $Id: Manager.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

/**
 * The Manager interface defines the API required by all manager objects
 * in JSwat. Managers are used to control a subset of features in JSwat,
 * such as breakpoints, source files, debugging context, etc. This Manager
 * API makes it easy for the Session class to deal with several managers at
 * once, and to handle future Managers.
 *<p>
 * Try to avoid circular dependencies between Managers when possible.
 * Unpredictable behavior can occur if one manager's init calls on
 * a second manager, which calls on the first manager (which has not
 * completed its initialization).
 *
 * @author  Nathan Fiedler
 * @version 1.1  4/22/01
 */
public interface Manager extends SessionListener {

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Managers are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session);

    /**
     * Called when the Session is about to close down.
     * Managers are not closed in any particular order.
     *
     * @param  session  Session being closed.
     */
    public void close(Session session);

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Managers are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session);

    /**
     * Called after the Session has instantiated this mananger.
     * To avoid problems with circular dependencies between managers,
     * iniitialize data members before calling
     * <code>Session.getManager()</code>.
     * Managers are not initialized in any particular order.
     *
     * @param  session  Session initializing this manager.
     */
    public void init(Session session);
} // Manager
