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
 * are Copyright (C) 1999-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ContextListener.java 6 2007-05-16 07:14:24Z nfiedler $
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
