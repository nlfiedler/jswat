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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.connect;

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

    @Override
    public void connect()
            throws IllegalConnectorArgumentsException, IOException,
            VMDisconnectedException, VMStartException {
        LaunchingConnector conn = (LaunchingConnector) getConnector();
        VirtualMachine vm = conn.launch(getConnectorArgs());
        setVM(vm);
    }
}
