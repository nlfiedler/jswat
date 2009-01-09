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
 * are Copyright (C) 2000-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.connect;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import java.io.IOException;

/**
 * The JvmConnection interface defines the required behavior of all
 * connection implementations in the debugger.
 *
 * @author  Nathan Fiedler
 */
public interface JvmConnection {

    /**
     * Adds a ConnectionListener to this connection.
     *
     * @param  listener  ConnectionListener to add to this connection.
     */
    void addConnectionListener(ConnectionListener listener);

    /**
     * Connects to the debuggee.
     *
     * @throws  IllegalConnectorArgumentsException
     *          if one of the arguments is invalid.
     * @throws  IOException
     *          an I/O error occurred.
     * @throws  VMDisconnectedException
     *          if the debuggee disconnected immediately after starting.
     * @throws  VMStartException
     *          if the debuggee failed to launch properly.
     */
    void connect() throws IllegalConnectorArgumentsException, IOException,
           VMDisconnectedException, VMStartException;

    /**
     * Disconnects from the debuggee.
     */
    void disconnect();

    /**
     * Returns the address of the debuggee in a transport-specific manner.
     *
     * @return  debuggee address.
     */
    String getAddress();

    /**
     * Returns the debuggee VirtualMachine associated with this
     * connection.
     *
     * @return  virtual machine, or null if no connection.
     */
    VirtualMachine getVM();

    /**
     * Returns true if connected to the debuggee.
     *
     * @return  true if connected to debuggee, false otherwise.
     */
    boolean isConnected();

    /**
     * Returns true if this connection is to a remote debuggee.
     *
     * @return  true if debuggee is remote, false if debuggee was launched.
     */
    boolean isRemote();

    /**
     * Removes the ConnectionListener from this connection.
     *
     * @param  listener  ConnectionListener to remove from this connection.
     */
    void removeConnectionListener(ConnectionListener listener);
}
