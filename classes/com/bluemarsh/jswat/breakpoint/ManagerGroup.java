/*********************************************************************
 *
 *      Copyright (C) 2001-2004 Nathan Fiedler
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
 * $Id: ManagerGroup.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import java.util.prefs.Preferences;

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
    /** The group's name. */
    protected static final String DEFAULT_NAME = "Default";
    /** Owning session. */
    protected Session session;

    /**
     * Constructs a ManagerGroup object.
     *
     * @param  session  Session that we belong to.
     */
    ManagerGroup(Session session) {
        super(DEFAULT_NAME);
        this.session = session;
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
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        if (!super.readObject(prefs)) {
            return false;
        }
        // Make sure our name is always the default.
        setName(DEFAULT_NAME);
        return true;
    } // readObject

    /**
     * Returns a string representation of this.
     *
     * @return  string of this.
     */
    public String toString() {
        return "ManagerGroup";
    } // toString
} // ManagerGroup
