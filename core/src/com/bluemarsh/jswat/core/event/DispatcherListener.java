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
 * are Copyright (C) 1999-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.event;

import com.sun.jdi.event.Event;
import java.util.EventListener;

/**
 * Listener interface for events sent from the debugger back-end.
 *
 * @author  Nathan Fiedler
 */
public interface DispatcherListener extends EventListener {

    /**
     * Invoked when a debugging event has occurred. This method is called
     * on the thread that is processing the JDI events, so care should be
     * taken to perform whatever work is necessary as quickly as possible
     * (e.g. do not block the thread waiting for user input).
     *
     * @param  event  JDI event.
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    boolean eventOccurred(Event event);
}
