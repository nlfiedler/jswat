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
 * FILE:        ManagerGroup.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/14/01        Initial version
 *
 * DESCRIPTION:
 *      Defines the breakpoint group used especially by the breakpoint
 *      manager to keep a reference to the owning Session.
 *
 * $Id: ManagerGroup.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;

/**
 * Special subclass of BreakpointGroup that has a reference to
 * the Session object. It is created by the breakpoint manager
 * and holds a reference to the Session that created the manager.
 * Other breakpoint groups and breakpoints contained within this
 * group will then have easy access to the owning Session.
 *
 * @author  Nathan Fiedler
 */
class ManagerGroup extends BreakpointGroup {
    /** Owning session. */
    protected transient Session session;
    /** serial version */
    static final long serialVersionUID = 6737175720680565537L;

    /**
     * Constructs a ManagerGroup object.
     */
    ManagerGroup() {
        super("Default");
    } // ManagerGroup

    /**
     * Returns a reference to the owning Session.
     *
     * @return  owning Session.
     */
    public Session getSession() {
        return session;
    } // getSession

    /**
     * Sets the reference to the owning Session.
     *
     * @param  session  owning Session.
     */
    public void setSession(Session session) {
        this.session = session;
    } // setSession
} // ManagerGroup
