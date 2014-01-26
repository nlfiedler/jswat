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
 * MODULE:      JSwat
 * FILE:        DefaultManager.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/24/00        Initial version of Manager class
 *      nf      04/22/01        Renamed to DefaultManager.
 *
 * DESCRIPTION:
 *      Contains the class that defines the default manager class.
 *
 * $Id: DefaultManager.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

/**
 * Class DefaultManager provides empty implementations of the API required
 * by all the Manager interface. Managers are used to control a subset of
 * features in JSwat, such as breakpoints, source files, debugging context,
 * etc. This Manager API makes it easy for the Session class to deal with
 * several managers at once, and to handle future Managers.
 *<p>
 * Try to avoid circular dependencies between Managers when possible.
 * Unpredictable behavior can occur if one manager's init calls on
 * a second manager, which calls on the first manager (which has not
 * completed its initialization).
 *
 * @author  Nathan Fiedler
 * @version 1.0  6/24/00
 */
public class DefaultManager implements Manager {
    /** Instance of JSwat. */
    protected static JSwat swat = JSwat.instanceOf();

    /**
     * All Manager subclasses must provide a no-argument constructor.
     * This is used to instantiate the Managers at startup.
     * There is only ever one instance of any given Manager.
     * To avoid circular dependencies, do not call
     * <code>Session.getManager()</code> from within the constructor.
     *
     * @see #init
     */
    public DefaultManager() {
    } // DefaultManager

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Managers are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
    } // activate

    /**
     * Called when the Session is about to close down.
     *
     * @param  session  Session being closed.
     */
    public void close(Session session) {
    } // close

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Managers are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
    } // deactivate

    /**
     * Called after the Session has instantiated this mananger.
     * To avoid problems with circular dependencies between managers,
     * iniitialize data members before calling
     * <code>Session.getManager()</code>.
     *
     * @param  session  Session initializing this manager.
     */
    public void init(Session session) {
    } // init
} // DefaultManager
