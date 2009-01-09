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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.connect;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.ListeningConnector;
import java.util.List;
import org.openide.util.Lookup;

/**
 * Class ConnectionProvider manages an instance of ConnectionFactory.
 *
 * @author Nathan Fiedler
 */
public class ConnectionProvider {
    /** The ConnectionFactory instance, if it has already been retrieved. */
    private static ConnectionFactory connectionFactory;

    /**
     * Creates a new instance of ConnectionProvider.
     */
    private ConnectionProvider() {
    }

    /**
     * Locates the attaching connector for the desired transport. If one
     * cannot be found, <code>null</code> is returned. This method can
     * be used to determine if a particular transport is available.
     *
     * @param  transport  the transport name (e.g. "dt_shmem", "dt_socket").
     * @return  the attaching connector, or null if not found.
     */
    public static AttachingConnector getAttachingConnector(String transport) {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<AttachingConnector> connectors = vmm.attachingConnectors();
        for (AttachingConnector conn : connectors) {
            if (conn.transport().name().equals(transport)) {
                return conn;
            }
        }
        return null;
    }

    /**
     * Locates the listening connector for the desired transport. If one
     * cannot be found, <code>null</code> is returned. This method can
     * be used to determine if a particular transport is available.
     *
     * @param  transport  the transport name (e.g. "dt_shmem", "dt_socket").
     * @return  the listening connector, or null if not found.
     */
    public static ListeningConnector getListeningConnector(String transport) {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<ListeningConnector> connectors = vmm.listeningConnectors();
        for (ListeningConnector conn : connectors) {
            if (conn.transport().name().equals(transport)) {
                return conn;
            }
        }
        return null;
    }

    /**
     * Retrieve the ConnectionFactory instance, creating one if necessary.
     *
     * @return  ConnectionFactory instance.
     */
    public static synchronized ConnectionFactory getConnectionFactory() {
        if (connectionFactory == null) {
            // Perform lookup to find a ConnectionFactory instance.
            connectionFactory = Lookup.getDefault().lookup(
                    ConnectionFactory.class);
        }
        return connectionFactory;
    }
}
