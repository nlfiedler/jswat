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
 * are Copyright (C) 2000-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionListener.java 15 2007-06-03 00:01:17Z nfiedler $
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
