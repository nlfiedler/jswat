/*********************************************************************
 *
 *      Copyright (C) 2000-2002 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      JSwat
 * FILE:        VMConnection.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/09/00        Initial version
 *      nf      03/22/01        Fixed bug #87, null pointer exception
 *      nf      05/28/01        Added getConnectArg()
 *      nf      08/17/01        Moved common code to this class
 *      nf      04/04/02        Implemented #404, shmem transport
 *      nf      05/22/02        Fixed bug #535
 *
 * DESCRIPTION:
 *      Defines the class for holding objects for connecting to a
 *      debuggee VM.
 *
 * $Id: VMConnection.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class VMConnection contains the parameters necessary for making and
 * maintaining a connection to a debuggee VM. It provides methods for
 * constructing a connection and launching a debuggee VM.
 *
 * @author  Nathan Fiedler
 */
public class VMConnection {
    /** Connector. */
    protected Connector connector;
    /** Connector arguments. */
    protected Map connectorArgs;
    /** Debuggee VM. */
    protected VirtualMachine debuggeeVM;

    /**
     * Constructs a new VMConnection with the given connector and
     * arguments.
     *
     * @param  connector  connector.
     * @param  args       connector arguments.
     */
    public VMConnection(Connector connector, Map args) {
        this.connector = connector;
        this.connectorArgs = args;
    } // VMConnection

    /**
     * Attaches to a remote debuggee using this connection.
     *
     * @param  session  Session to activate when attached.
     * @return  true if successful, false if error.
     */
    public boolean attachDebuggee(Session session) {
        // Try to attach to the running VM.
        VirtualMachine vm = null;
        try {
            // Hopefully it really is an attaching connector.
            AttachingConnector conn = (AttachingConnector) connector;
            vm = conn.attach(connectorArgs);
        } catch (IOException ioe) {
            session.getStatusLog().writeln(ioe.toString());
            return false;
        } catch (IllegalConnectorArgumentsException icae) {
            session.getStatusLog().writeln(icae.toString());
            return false;
        }

        // Activate the Session.
        debuggeeVM = vm;
        session.activate(this);
        return true;
    } // attachDebuggee

    /**
     * Builds the connection parameters object using the given shared
     * memory name for the debuggee VM.
     *
     * @param  name     shared memory name.
     * @return  VMConnection, or null if error.
     * @exception  NoAttachingConnectorException
     *             Thrown if the appropriate connector could not be found.
     */
    public static VMConnection buildConnection(String name)
        throws NoAttachingConnectorException {
        // Get the VM manager and request attaching connectors.
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List connectors = vmm.attachingConnectors();

        // Find an attaching connector that uses 'dt_shmem'.
        AttachingConnector connector = null;
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector conn = (Connector) iter.next();
            if (conn.transport().name().equals("dt_shmem")) {
                connector = (AttachingConnector) conn;
                break;
            }
        }
        if (connector == null) {
            throw new NoAttachingConnectorException(
                "no shared memory connectors found");
        }

        // Set the shared memory name argument.
        Map connectArgs = connector.defaultArguments();
        ((Connector.Argument) connectArgs.get("name")).setValue(name);
        return new VMConnection(connector, connectArgs);
    } // buildConnection

    /**
     * Builds the connection parameters object using the given host
     * and port for the remote debuggee VM.
     *
     * @param  host  Host machine name.
     * @param  port  Port of remote machine.
     * @return  VMConnection, or null if error.
     * @exception  NoAttachingConnectorException
     *             Thrown if the appropriate connector could not be found.
     */
    public static VMConnection buildConnection(String host, String port)
        throws NoAttachingConnectorException {
        // Get the VM manager and request attaching connectors.
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List connectors = vmm.attachingConnectors();

        // Find an attaching connector that uses 'dt_socket'.
        AttachingConnector connector = null;
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector conn = (Connector) iter.next();
            if (conn.transport().name().equals("dt_socket")) {
                connector = (AttachingConnector) conn;
                break;
            }
        }
        if (connector == null) {
            throw new NoAttachingConnectorException(
                "no socket connectors found");
        }

        // Set the connector's arguments.
        Map connectArgs = connector.defaultArguments();
        if (host != null) {
            // Set hostname if given.
            ((Connector.Argument) connectArgs.get("hostname")).setValue(host);
        }
        // Set port to connect to.
        ((Connector.Argument) connectArgs.get("port")).setValue(port);
        return new VMConnection(connector, connectArgs);
    } // buildConnection

    /**
     * Builds a VMConnection object to contain all the necessary
     * parameters for launching a debuggee VM.
     *
     * @param  javaHome       home of JVM or null for default.
     * @param  jvmExecutable  name of JVM executable file or null for default.
     * @param  options        VM options to pass or null for none.
     * @param  cmdline        class to launch (with optional arguments).
     * @return  new VMConnection instance.
     */
    public static VMConnection buildConnection(String javaHome,
                                               String jvmExecutable,
                                               String options,
                                               String cmdline) {
        // Get launching connector from the VM manager.
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        LaunchingConnector connector = vmm.defaultConnector();
        Map args = connector.defaultArguments();
        ((Connector.Argument) args.get("main")).setValue(cmdline);
        if ((options != null) && options.length() > 0) {
            ((Connector.Argument) args.get("options")).setValue(options);
        }
        if ((javaHome != null) && javaHome.length() > 0) {
            ((Connector.Argument) args.get("home")).setValue(javaHome);
        }
        if ((jvmExecutable != null) && jvmExecutable.length() > 0) {
            ((Connector.Argument) args.get("vmexec")).setValue(jvmExecutable);
        }
        // Make sure debuggee is suspended to allow the Session enough
        // time to activate all the listeners.
        // Use BooleanArgument type to avoid problem in Japanese systems.
        // Solution found by Takeo Matsumura.
        ((Connector.BooleanArgument) args.get("suspend")).setValue(true);
        return new VMConnection(connector, args);
    } // buildConnection

    /**
     * Dumps the contents of the input stream to the message log.
     *
     * @param  is   input stream to read from.
     * @param  out  output Log to dump to.
     * @exception  IOException
     *             Thrown if error occurs.
     */
    protected static void dumpStream(InputStream is, Log out)
        throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer buf = new StringBuffer(256);
        while ((line = br.readLine()) != null) {
            buf.append(line);
            buf.append('\n');
        }
        out.write(buf.toString());
    } // dumpStream

    /**
     * Indicates whether some other object is "equal to" this one.
     * Compares just the main class argument for equality.
     *
     * @param  o  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object o) {
        try {
            VMConnection vo = (VMConnection) o;
            String vomc = vo.getMainClass();
            String mc = getMainClass();
            if ((vomc != mc) &&
                (vomc != null) && (mc != null) &&
                (!vomc.equals(mc))) {
                return false;
            }
            return true;
        } catch (ClassCastException cce) {
            return false;
        }
    } // equals

    /**
     * Returns the connector used to launch, listen, or attach to the
     * debuggee VM. This is one of the
     * <code>com.sun.jdi.connect.Connector</code> subclasses.
     *
     * @return  Connector, or null if there has never been a connection.
     */
    public Connector getConnector() {
        return connector;
    } // getConnector

    /**
     * Returns the connector arguments used to launch, listen, or attach
     * to the debuggee VM.
     *
     * @return  Map, or null if there has never been a connection.
     */
    public Map getConnectArgs() {
        return connectorArgs;
    } // getConnectArgs

    /**
     * Returns the named connector argument.
     *
     * @param  name  name of argument to retrieve.
     * @return  Named argument, or null if none (or no arguments).
     */
    public String getConnectArg(String name) {
        if (connectorArgs != null) {
            Object o = connectorArgs.get(name);
            if (o != null) {
                return ((Connector.Argument) o).value();
            }
        }
        return null;
    } // getConnectArg

    /**
     * Returns the "main" connector argument. This is the class name
     * of the class that is being debugged.
     *
     * @return  Main class name, or null if undefined.
     */
    public String getMainClass() {
        if (connectorArgs != null) {
            Object o = connectorArgs.get("main");
            if (o != null) {
                String s = ((Connector.Argument) o).value();
                if ((s != null) && (s.length() > 0)) {
                    int spaceIdx = s.indexOf(' ');
                    if (spaceIdx > 0) {
                        // Strip away the class arguments.
                        s = s.substring(0, spaceIdx);
                    }
                    return s;
                }
            }
        }
        return null;
    } // getMainClass

    /**
     * Returns the debuggee VirtualMachine associated with this connection.
     *
     * @return  VirtualMachine, or none if no connection.
     */
    public VirtualMachine getVM() {
        return debuggeeVM;
    } // getVM

    /**
     * Launches the debuggee VM using this connection. If there are problems,
     * this method will dump the errors to the Session's status Log.
     *
     * @param  session  Session to activate.
     * @return  true if VM launched, false if error.
     */
    public boolean launchDebuggee(Session session) {
        // Try to launch the new VM and handle all the errors.
        VirtualMachine vm = null;
        try {
            // Hopefully it really is a launching connector.
            LaunchingConnector conn = (LaunchingConnector) connector;
            vm = conn.launch(connectorArgs);
        } catch (VMDisconnectedException vmde) {
            session.getStatusLog().writeln
                (vmde.toString() + '\n' +
                 Bundle.getString("VMConn.checkClassExists"));
            return false;
        } catch (IOException ioe) {
            session.getStatusLog().writeln(ioe.toString());
            return false;
        } catch (IllegalConnectorArgumentsException icae) {
            session.getStatusLog().writeln(icae.toString());
            return false;
        } catch (VMStartException vmse) {
            // VM failed to start correctly, show what happened.
            Log out = session.getStatusLog();
            out.writeln(Bundle.getString("VMConn.causeOfDeath"));
            try {
                dumpStream(vmse.process().getErrorStream(), out);
                dumpStream(vmse.process().getInputStream(), out);
            } catch (IOException ioe) {
                out.writeln(Bundle.getString("VMConn.errorReadingOutput") +
                            ": " + ioe.getMessage());
            }
            return false;
        }

        // Activate the Session.
        debuggeeVM = vm;
        try {
            session.activate(this);
        } catch (VMDisconnectedException vmde) {
            // The VM may start and exit very soon after.
            // Still, indicates abnormal behavior.
            return false;
        }
        return true;
    } // launchDebuggee

    /**
     * Sets the VirtualMachine that is associated with this connection.
     * xxx - replace this method with more appropriate "detach" or
     * something like that
     *
     * @param  vm  VirtualMachine.
     */
    public void setVM(VirtualMachine vm) {
        debuggeeVM = vm;
    } // setVM
} // VMConnection
