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
 * are Copyright (C) 1999-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DispatcherListener.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.event;

import java.util.EventListener;
import java.util.Iterator;

/**
 * Listener interface for events sent from the debugger back-end.
 *
 * @author  Nathan Fiedler
 */
public interface DispatcherListener extends EventListener {

    /**
     * Invoked when a debugging event has occurred.
     *
     * @param  e  debugging event.
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    boolean eventOccurred(DispatcherEvent e);

    /**
     * Return an iterator of event types for which this listener wants to
     * register. When the listener is added and removed from the handler,
     * it will be registered and unregistered for these JDI event types.
     *
     * @return  the JDI event types of interest.
     */
    Iterator<Class> eventTypes();
}
