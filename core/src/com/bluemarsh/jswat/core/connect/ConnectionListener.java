/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ConnectionListener.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

import java.util.EventListener;

/**
 * A ConnectionListener is notified of changes in a JvmConnection.
 *
 * @author Nathan Fiedler
 */
public interface ConnectionListener extends EventListener {

    /**
     * Invoked when the connection has successfully connected.
     *
     * @param  event  connection event.
     */
    void connected(ConnectionEvent event);
}
