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
 * are Copyright (C) 2000-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.session;

import com.bluemarsh.jswat.core.session.Session;
import java.util.EventListener;

/**
 * The listener interface for receiving Session events.
 *
 * @author  Nathan Fiedler
 */
public interface SessionListener extends EventListener {

    /**
     * Called when the Session has connected to the debuggee.
     *
     * @param  sevt  session event.
     */
    void connected(SessionEvent sevt);

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    void closing(SessionEvent sevt);

    /**
     * Called when the Session has disconnected from the debuggee.
     *
     * @param  sevt  session event.
     */
    void disconnected(SessionEvent sevt);

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    void opened(Session session);

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    void resuming(SessionEvent sevt);

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    void suspended(SessionEvent sevt);
}
