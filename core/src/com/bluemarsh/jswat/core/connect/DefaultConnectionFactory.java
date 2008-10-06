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
 * $Id: DefaultConnectionFactory.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

import com.bluemarsh.jswat.core.CoreSettings;
import com.bluemarsh.jswat.core.runtime.JavaRuntime;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.ListeningConnector;
import java.io.File;
import java.util.Map;

/**
 * Class DefaultConnectionFactory implements ConnectionFactory to construct
 * default implementations of JvmConnection.
 *
 * @author Nathan Fiedler
 */
public class DefaultConnectionFactory implements ConnectionFactory {

    public JvmConnection createShared(String name)
            throws NoAttachingConnectorException {
        // Find a shared-memory attaching connector.
        AttachingConnector connector =
                ConnectionProvider.getAttachingConnector("dt_shmem");
        if (connector == null) {
            throw new NoAttachingConnectorException("no shared memory connectors");
        }

        // Set the connector arguments.
        Map<String, ? extends Connector.Argument> args = connector.defaultArguments();
        args.get("name").setValue(name);

        // Create the actual connection.
        return setTimeout(connector, args);
    }

    public JvmConnection createSocket(String hostname, String port)
            throws NoAttachingConnectorException {
        // Find a socket-based attaching connector.
        AttachingConnector connector =
                ConnectionProvider.getAttachingConnector("dt_socket");
        if (connector == null) {
            throw new NoAttachingConnectorException("no socket connectors");
        }

        // Set the connector arguments.
        Map<String, ? extends Connector.Argument> args = connector.defaultArguments();
        if (hostname != null && hostname.length() > 0) {
            args.get("hostname").setValue(hostname);
        }
        args.get("port").setValue(port);

        // Create the actual connection.
        return setTimeout(connector, args);
    }

    public JvmConnection createLaunching(JavaRuntime runtime, String options, String main) {
        if (runtime == null) {
            throw new IllegalArgumentException("runtime must be non-null");
        }
        // Get launching connector from the VM manager.
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        LaunchingConnector connector = vmm.defaultConnector();

        // Set the connector arguments.
        Map<String, ? extends Connector.Argument> args = connector.defaultArguments();
        args.get("main").setValue(main);
        if (options != null && options.length() > 0) {
            args.get("options").setValue(options);
        }
        File base = new File(runtime.getBase());
        if (!base.exists()) {
            throw new IllegalArgumentException("runtime base does not exist");
        }
        args.get("home").setValue(base.getAbsolutePath());
        String exec = runtime.getExec();
        if (exec != null && exec.length() > 0) {
            args.get("vmexec").setValue(exec);
        }

        // Create the actual connection.
        return new LaunchingConnection(connector, args);
    }

    public JvmConnection createListening(String transport, String host,
            String address) throws NoListeningConnectorException {
        ListeningConnector connector =
                ConnectionProvider.getListeningConnector(transport);
        if (connector == null) {
            throw new NoListeningConnectorException(transport);
        }
        Map<String, ? extends Connector.Argument> args = connector.defaultArguments();
        if (transport.equals("dt_socket")) {
            args.get("port").setValue(address);
            if (host != null && host.length() > 0) {
                args.get("localAddress").setValue(host);
            }
        } else {
            args.get("name").setValue(address);
        }
        // Set the connection timeout.
        CoreSettings cs = CoreSettings.getDefault();
        int timeout = cs.getConnectionTimeout();
        args.get("timeout").setValue(String.valueOf(timeout));

        return new ListeningConnection(connector, args);
    }

    /**
     * Sets the timeout value for the attaching connector, then builds the
     * connection and returns it.
     *
     * @param  connector  the attaching connector.
     * @param  args       the connector arguments.
     * @return  the new connection.
     */
    private static JvmConnection setTimeout(Connector connector,
            Map<String, ? extends Connector.Argument> args) {
        // Set the connection timeout.
        CoreSettings cs = CoreSettings.getDefault();
        int timeout = cs.getConnectionTimeout();
        args.get("timeout").setValue(String.valueOf(timeout));

        // Create the actual connection.
        return new AttachingConnection(connector, args);
    }
}
