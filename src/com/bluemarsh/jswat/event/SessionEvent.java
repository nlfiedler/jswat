/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: SessionEvent.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.event;

import com.bluemarsh.jswat.Session;
import java.util.EventObject;

/**
 * Class SessionEvent encapsulates information about the Session and its
 * current state, as well as information about the event that is taking
 * place.
 *
 * <p>Note that the source of this event type is the object that made
 * the change happen. That is, it is not the Session itself.</p>
 *
 * @author  Nathan Fiedler
 */
public class SessionEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The Session related to this event. */
    private Session theSession;
    /** True if this event is expected to be brief in duration. This
     * affects the suspended and resumed states, in which the session is
     * changing state for a short period of time. */
    private boolean isBrief;

    /**
     * Constructs a new SessionEvent.
     *
     * @param  session  Session related to this event.
     * @param  source   source of this event.
     * @param  brief    true if event is expected to be brief in duration.
     */
    public SessionEvent(Session session, Object source, boolean brief) {
        super(source);
        theSession = session;
        isBrief = brief;
    } // SessionEvent

    /**
     * Returns the Session relating to this event.
     *
     * @return  Session for this event.
     */
    public Session getSession() {
        return theSession;
    } // getSession

    /**
     * Indicates if this event marks the beginning of a brief change.
     * That is, in a short amount of time the state is expected to
     * change again. For example, this will be true if the session
     * is resuming due to a single-step request.
     *
     * @return  true if event is brief; false otherwise.
     */
    public boolean isBrief() {
        return isBrief;
    } // isBrief

    /**
     * Returns a String representation of this SessionEvent.
     *
     * @return  a String representation of this.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("SessionEvent=[session=");
        buf.append(theSession);
        buf.append(", source=");
        buf.append(getSource());
        buf.append(", brief=");
        buf.append(isBrief);
        buf.append(']');
        return buf.toString();
    } // toString
} // SessionEvent
