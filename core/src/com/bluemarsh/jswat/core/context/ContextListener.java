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
 * are Copyright (C) 1999-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ContextListener.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.context;

import java.util.EventListener;

/**
 * The listener interface for receiving changes in the current debugger
 * context. If the listener is looking for both location information and
 * thread information, then the listener should save the thread change
 * information when the thread change event occurs. The subsequent event
 * will often be a location change event.
 *
 * @author  Nathan Fiedler
 */
public interface ContextListener extends EventListener {

    /**
     * Invoked when the current stack frame has been changed.
     *
     * @param  ce  context event.
     */
    void changedFrame(ContextEvent ce);

    /**
     * Invoked when the current location has been changed.
     *
     * @param  ce  context event.
     */
    void changedLocation(ContextEvent ce);

    /**
     * Invoked when the current thread has been changed.
     *
     * @param  ce  context event.
     */
    void changedThread(ContextEvent ce);
}
