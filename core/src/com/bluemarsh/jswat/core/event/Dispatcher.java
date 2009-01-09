/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.event;

import com.sun.jdi.event.EventQueue;
import com.sun.jdi.request.EventRequest;

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
     * Register the given listener for JDI events resulting from the
     * given event request.
     *
     * @param  listener  listener to add.
     * @param  request   the event request.
     */
    void register(DispatcherListener listener, EventRequest request);

    /**
     * Start the event handling thread to process JDI events. Optional
     * listeners may be provided, which are invoked in special cases for
     * which event requests are not possible (e.g. VMStartEvent).
     *
     * @param  queue      JDI event queue from which events are received.
     * @param  started    called when VMStartEvent is received (may be null).
     * @param  stopped    called once when debuggee dies or is disconnected
     *                    (may be null).
     * @param  suspended  called whenever the debuggee is suspended by an
     *                    event (may be null).
     */
    void start(EventQueue queue, DispatcherListener started, Runnable stopped,
            DispatcherListener suspended);

    /**
     * Disassociates any listener from the event request.
     *
     * @param  request  the event request.
     */
    void unregister(EventRequest request);
}
