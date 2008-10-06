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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Dispatcher.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.event;

/**
 * A Dispatcher is responsible for taking events from the JDI event queue
 * and disseminating them to registered listeners. Concrete implementations
 * of this interface are acquired from the <code>DispatcherProvider</code>
 * class.
 *
 * @author  Nathan Fiedler
 */
public interface Dispatcher {

    /**
     * Register the given listener for JDI events. The event types are
     * determined by calling <code>eventTypes()</code> on the listener.
     *
     * @param  listener  listener to add.
     */
    void addListener(DispatcherListener listener);

    /**
     * Removes the given listener from the event listener list. It will
     * be unregistered for all of the event types that the listener
     * specifies in its <code>eventTypes()</code> method.
     *
     * @param  listener  listener to remove.
     */
    void removeListener(DispatcherListener listener);

    /**
     * Start the event handling thread to process JDI events.
     */
    void start();
}
