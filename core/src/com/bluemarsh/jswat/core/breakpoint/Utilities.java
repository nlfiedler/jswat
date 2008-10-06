/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Utilities.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;
import org.openide.util.NbBundle;

/**
 * A collection of utility methods for the breakpoints package.
 *
 * @author Nathan Fiedler
 */
public class Utilities {

    /**
     * Creates a new instance of Utilities.
     */
    private Utilities() {
    }

    /**
     * Return a brief description of the given exception event.
     *
     * @param  ee  exception event to describe.
     * @return  exception description.
     */
    public static String describeException(ExceptionEvent ee) {
        // Show the type of exception that was thrown.
        String name = ee.exception().type().name();
        String thread = Threads.getIdentifier(ee.thread());

        // Get the message so the user has more information.
        String msg = "";
        try {
            ObjectReference obj = ee.exception();
            ReferenceType type = obj.referenceType();
            Field field = type.fieldByName("detailMessage");
            if (field != null) {
                Value value = obj.getValue(field);
                if (value != null) {
                    msg = value.toString();
                }
            }
        } catch (IllegalArgumentException iae) {
            // this cannot happen
        }
        return NbBundle.getMessage(Utilities.class,
                "Exception.description.stop", name, thread, msg);
    }

    /**
     * Generates a description of the watch event suitable for the user,
     * to be displayed when a breakpoint has been hit.
     *
     * @param  we  watchpoint event.
     * @return  the description of the event, or null if invalid event type.
     */
    public static String describeWatch(WatchpointEvent we) {
        String field = we.field().name();
        ObjectReference obj = we.object();
        String object;
        if (obj != null) {
            object = String.valueOf(obj.uniqueID());
        } else {
            object = we.field().declaringType().name();
        }
        String thread = Threads.getIdentifier(we.thread());
        if (we instanceof AccessWatchpointEvent) {
            Value v = we.valueCurrent();
            String value = v == null ? "null" : v.toString();
            String[] params = new String[] { field, object, thread, value };
            return NbBundle.getMessage(DefaultWatchBreakpoint.class,
                    "Watch.accessed", params);
        } else if (we instanceof ModificationWatchpointEvent) {
            ModificationWatchpointEvent mwe = (ModificationWatchpointEvent) we;
            Value v = mwe.valueToBe();
            String value = v == null ? "null" : v.toString();
            String[] params = new String[] { field, object, thread, value };
            return NbBundle.getMessage(DefaultWatchBreakpoint.class,
                    "Watch.modified", params);
        }
        return null;
    }
}
