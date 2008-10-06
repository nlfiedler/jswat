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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AttachingConnection.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import java.io.IOException;
import java.util.Map;

/**
 * Implements a connection that attaches to a debuggee, either on the same
 * host as the debugger or on a different host elsewhere on the network.
 *
 * @author Nathan Fiedler
 */
public class AttachingConnection extends AbstractConnection {

    /**
     * Creates a new instance of AttachingConnection.
     *
     * @param  connector  connector.
     * @param  args       connector arguments.
     */
    public AttachingConnection(Connector connector,
            Map<String, ? extends Connector.Argument> args) {
        super(connector, args);
    }

    public void connect() throws IllegalConnectorArgumentsException, IOException {
        AttachingConnector conn = (AttachingConnector) getConnector();
        VirtualMachine vm = conn.attach(getConnectorArgs());
        setVM(vm);
        fireEvent(new ConnectionEvent(this, ConnectionEvent.Type.CONNECTED));
    }
}
