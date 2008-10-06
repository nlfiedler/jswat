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
 * $Id: LaunchingConnection.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import java.io.IOException;
import java.util.Map;

/**
 * Implementation of a connection that launches the debuggee on the local
 * machine.
 *
 * @author Nathan Fiedler
 */
public class LaunchingConnection extends AbstractConnection {

    /**
     * Creates a new instance of LaunchingConnection.
     *
     * @param  connector  connector.
     * @param  args       connector arguments.
     */
    public LaunchingConnection(Connector connector,
            Map<String, ? extends Connector.Argument> args) {
        super(connector, args);
    }

    public void connect()
        throws IllegalConnectorArgumentsException, IOException,
               VMDisconnectedException, VMStartException {
        LaunchingConnector conn = (LaunchingConnector) getConnector();
        VirtualMachine vm = conn.launch(getConnectorArgs());
        setVM(vm);
    }
}
