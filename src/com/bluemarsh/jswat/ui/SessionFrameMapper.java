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
 * MODULE:      JSwat UI
 * FILE:        SessionFrameMapper.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/08/01        Initial version
 *      nf      10/07/01        Moving JSwatAction utility methods here
 *
 * DESCRIPTION:
 *      Maps Session objects to Frame objects.
 *
 * $Id: SessionFrameMapper.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.Session;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.util.EventObject;
import java.util.Hashtable;
import javax.swing.JPopupMenu;

/**
 * Class SessionFrameMapper is responsible for providing a mapping
 * between Frame objects and Session objects. This is used by the
 * JSwatAction classes to convert a Frame reference to a Session
 * instance. All UIAdapter implementations that provide instances
 * of the JSwatActions must keep this table up to date.
 *
 * @author  Nathan Fiedler
 */
public class SessionFrameMapper {
    /** Mapping of Frames to Sessions. */
    protected static Hashtable framesToSessions = new Hashtable();

    /**
     * Adds the given Frame to Session mapping.
     *
     * @param  f  top-level frame that is connected to Session.
     * @param  s  Session connect to top-level frame.
     */
    static void addFrameSessionMapping(Frame f, Session s) {
        framesToSessions.put(f, s);
    } // addFrameSessionMapping

    /**
     * Find the hosting frame for this object. Often used when
     * displaying dialogs which require a host frame.
     *
     * @param  o  Object with which to find the parent frame.
     *            Could be a subclass of EventObject or Component.
     * @return hosting frame or null if none.
     */
    public static Frame getOwningFrame(Object o) {
        // Get the Component object.
        if (o instanceof EventObject) {
            o = ((EventObject) o).getSource();
        }
        if (!(o instanceof Component)) {
            throw new IllegalArgumentException(
                "o is not an instance of EventObject or Component");
        }
        if (o instanceof Frame) {
            return (Frame) o;
        }

        // Find the top frame parent of the component.
        Container p = ((Component) o).getParent();
        while (p != null) {
            if (p instanceof JPopupMenu) {
                // Special case for popup menus which do not
                // have parents but have invokers instead.
                p = ((JPopupMenu) p).getInvoker().getParent();
            }
            if (p instanceof Frame) {
                return (Frame) p;
            }
            p = p.getParent();
        }

        // If we got here, the child simply has no parent frame.
        throw new IllegalArgumentException("o is not a child of any Frame");
    } // getOwningFrame

    /**
     * Finds the Session that is associated with the window that
     * contains the component that is the source of the given action
     * event.
     *
     * @param  e  event with which to find Session.
     * @return Session instance, or null if error.
     */
    public static Session getSessionForEvent(EventObject e) {
        // Use the SessionFrameMapper to get the Session for the Frame.
        return getSessionForFrame(getOwningFrame(e));
    } // getSessionForEvent

    /**
     * Looks for a Session that is associated with the given Frame.
     *
     * @param  f  Frame to look up Session with.
     * @return  Session for the given frame, if any.
     */
    public static Session getSessionForFrame(Frame f) {
        return (Session) framesToSessions.get(f);
    } // getSessionForFrame

    /**
     * Removes the given Frame to Session mapping.
     *
     * @param  f  top-level frame to be removed from the mapping.
     */
    static void removeFrameSessionMapping(Frame f) {
        framesToSessions.remove(f);
    } // removeFrameSessionMapping
} // SessionFrameMapper
