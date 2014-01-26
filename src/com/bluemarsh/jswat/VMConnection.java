/*********************************************************************
 *
 *      Copyright (C) 2000-2003 Nathan Fiedler
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
 *      nf      03/22/01        Fixed bug 87
 *      nf      05/28/01        Added getConnectArg()
 *      nf      08/17/01        Moved common code to this class
 *      nf      04/04/02        Implemented RFE 404
 *      nf      05/22/02        Fixed bug 535
 *      nf      07/19/02        Added loadingString()
 *      nf      04/06/03        Fixed bug 744
 *
 * $Id: VMConnection.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.ui.UIAdapter;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.VMStartException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Class VMConnection contains the parameters necessary for making and
 * maintaining a connection to a debuggee VM. It provides methods for
 * constructing a connection and launching a debuggee VM.
 *
 * @author  Nathan Fiedler
 */
public class VMConnection {
    /** Connector. */
    private Connector connector;
    /** Connector arguments. */
    private Map connectorArgs;
    /** Debuggee VM. */
    private VirtualMachine debuggeeVM;
    /** Name of the main class, if known. */
    private String mainClass;
    /** True if this is a remote connection. */
    private boolean isRemoteConnection;

    /**
     * Builds the connection parameters object using the given shared
     * memory name for the debuggee VM.
     *
     * @param  name     shared memory name.
     * @return  VMConnection, or null if error.
     * @throws  NoAttachingConnectorException
     *          if the appropriate connector could not be found.
     */
    public static VMConnection buildConnection(String name)
        throws NoAttachingConnectorException {
        // Find an attaching connector that uses 'dt_shmem'.
        AttachingConnector connector = getAttachingConnector("dt_shmem");
        if (connector == null) {
            throw new NoAttachingConnectorException(
                "no shared memory connectors found");
        }

        // Set the shared memory name argument.
        Map connectArgs = connector.defaultArguments();
        ((Connector.Argument) connectArgs.get("name")).setValue(name);
        VMConnection vmc = new VMConnection(connector, connectArgs);
        return vmc;
    } // buildConnection

    /**
     * Builds the connection parameters object using the given host and
     * port for the remote debuggee VM.
     *
     * @param  host  host machine name.
     * @param  port  port of remote machine.
     * @return  VMConnection, or null if error.
     * @throws  NoAttachingConnectorException
     *          if the appropriate connector could not be found.
     */
    public static VMConnection buildConnection(String host, String port)
        throws NoAttachingConnectorException {
        // Find an attaching connector that uses 'dt_socket'.
        AttachingConnector connector = getAttachingConnector("dt_socket");
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
        VMConnection vmc = new VMConnection(connector, connectArgs);
        return vmc;
    } // buildConnection

    /**
     * Builds a VMConnection object to contain all the necessary
     * parameters for launching a debuggee VM.
     *
     * <p>Throws <code>IllegalArgumentException</code> if
     * <code>javaHome</code> directory does not exist.</p>
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
            File home = new File(javaHome);
            if (!home.exists()) {
                throw new IllegalArgumentException(
                    "javaHome is invalid: " + javaHome);
            }
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
     * Dumps the contents of the input stream to the string buffer.
     *
     * @param  is  input stream to read from.
     * @param  sb  string buffer to dump to.
     * @throws  IOException
     *          if error occurs.
     */
    protected static void dumpStream(InputStream is, StringBuffer sb)
        throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append('\n');
            line = br.readLine();
        }
    } // dumpStream

    /**
     * Locates the attaching connector for the desired transport. If one
     * cannot be found, <code>null</code> is returned. This method can
     * be used to determine if a particular transport is available.
     *
     * @param  transport  the transport name (e.g. "dt_shmem", "dt_socket").
     * @return  the attaching connector, or null if not found.
     */
    public static AttachingConnector getAttachingConnector(String transport) {
        AttachingConnector connector = null;
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List connectors = vmm.attachingConnectors();
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector conn = (Connector) iter.next();
            if (conn.transport().name().equals(transport)) {
                connector = (AttachingConnector) conn;
                break;
            }
        }
        return connector;
    } // getAttachingConnector

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
        if (connector instanceof AttachingConnector
            || connector instanceof ListeningConnector) {
            isRemoteConnection = true;
        }
    } // VMConnection

    /**
     * Attaches to a remote debuggee using this connection.
     *
     * @param  session     Session to activate when attached.
     * @param  showSource  true to show the source for the main class.
     * @return  true if successful, false if error.
     */
    public boolean attachDebuggee(Session session, boolean showSource) {
        // Try to attach to the running VM.
        VirtualMachine vm = null;
        try {
            // Hopefully it really is an attaching connector.
            AttachingConnector conn = (AttachingConnector) connector;
            vm = conn.attach(connectorArgs);
        } catch (IOException ioe) {
            session.getUIAdapter().showMessage(UIAdapter.MESSAGE_ERROR,
                                               ioe.toString());
            return false;
        } catch (IllegalConnectorArgumentsException icae) {
            session.getUIAdapter().showMessage(UIAdapter.MESSAGE_ERROR,
                                               icae.toString());
            return false;
        }

        // Activate the Session.
        debuggeeVM = vm;
        session.activate(this, showSource, this);
        return true;
    } // attachDebuggee

    /**
     * Set the current VM reference to the one given.
     *
     * @param  vm  new virtual machine reference.
     */
    public void connectVM(VirtualMachine vm) {
        debuggeeVM = vm;
    } // connectVM

    /**
     * Clear the current VM reference. This should be called in the
     * event that the debuggee has terminated.
     */
    public void disconnect() {
        debuggeeVM = null;
        mainClass = null;
    } // disconnect

    /**
     * Indicates whether some other object is "equal to" this one.
     * Compares just the main class argument for equality.
     *
     * @param  o  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o != null && o instanceof VMConnection) {
            String omc = ((VMConnection) o).getMainClass();
            String mc = getMainClass();
            if (mc == omc) {
                // handles both-null case
                return true;
            }
            if (mc == null || omc == null) {
                return false;
            }
            return mc.equals(omc);
        } else {
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
     * Returns the "main" connector argument. This is the class name of
     * the class that is being debugged.
     *
     * @return  Main class name, or null if undefined.
     */
    public String getMainClass() {
        if (mainClass == null && connectorArgs != null) {
            Object o = connectorArgs.get("main");
            if (o != null) {
                String s = ((Connector.Argument) o).value();
                if (s != null && s.length() > 0) {
                    int spaceIdx = s.indexOf(' ');
                    if (spaceIdx > 0) {
                        // Strip away the class arguments.
                        s = s.substring(0, spaceIdx);
                    }

                    // Try to read the Main-Class from the jar file.
                    if (s.endsWith(".jar")) {
                        try {
                            JarFile jf = new JarFile(s);
                            Manifest man = jf.getManifest();
                            Attributes attrs = man.getMainAttributes();
                            s = (String) attrs.getValue("Main-Class");
                        } catch (IOException ioe) {
                            // oh well, too bad.
                            s = null;
                        }
                    }
                    mainClass = s;
                }
            }
        }
        return mainClass;
    } // getMainClass

    /**
     * Returns the debuggee Process associated with this connection.
     *
     * @return  Process, or null if no connection or if the connection is
     *          remote.
     */
    public Process getProcess() {
        if (debuggeeVM != null) {
            return debuggeeVM.process();
        } else {
            return null;
        }
    } // getProcess

    /**
     * Returns the debuggee VirtualMachine associated with this
     * connection.
     *
     * @return  VirtualMachine, or none if no connection.
     */
    public VirtualMachine getVM() {
        return debuggeeVM;
    } // getVM

    /**
     * Returns true if this connection is to a remote debuggee.
     *
     * @return  true if debuggee is remote, false if debuggee was
     *          launched.
     */
    public boolean isRemote() {
        return isRemoteConnection;
    } // isRemote

    /**
     * Launches the debuggee VM using this connection. If there are
     * problems, this method will dump the errors to the Session's
     * status Log.
     *
     * @param  session     Session to activate.
     * @param  showSource  true to show the source for the main class.
     * @return  true if VM launched, false if error.
     */
    public boolean launchDebuggee(Session session, boolean showSource) {
        // Try to launch the new VM and handle all the errors.
        VirtualMachine vm = null;
        try {
            // Hopefully it really is a launching connector.
            LaunchingConnector conn = (LaunchingConnector) connector;
            vm = conn.launch(connectorArgs);
        } catch (VMDisconnectedException vmde) {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR,
                vmde.toString() + '\n'
                + Bundle.getString("VMConn.checkClassExists"));
            return false;
        } catch (IOException ioe) {
            session.getUIAdapter().showMessage(UIAdapter.MESSAGE_ERROR,
                                               ioe.toString());
            return false;
        } catch (IllegalConnectorArgumentsException icae) {
            session.getUIAdapter().showMessage(UIAdapter.MESSAGE_ERROR,
                                               icae.toString());
            return false;
        } catch (VMStartException vmse) {
            // VM failed to start correctly, show what happened.
            StringBuffer sb = new StringBuffer(256);
            sb.append(Bundle.getString("VMConn.causeOfDeath"));
            sb.append('\n');
            try {
                dumpStream(vmse.process().getErrorStream(), sb);
                sb.append('\n');
                dumpStream(vmse.process().getInputStream(), sb);
            } catch (IOException ioe) {
                sb.append(Bundle.getString("VMConn.errorReadingOutput"));
                sb.append(": ");
                sb.append(ioe.getMessage());
                sb.append('\n');
            }
            session.getUIAdapter().showMessage(UIAdapter.MESSAGE_ERROR,
                                               sb.toString());
            return false;
        }

        // Activate the Session.
        debuggeeVM = vm;
        try {
            session.activate(this, showSource, this);
        } catch (VMDisconnectedException vmde) {
            // The VM may start and exit very soon after.
            // Still, indicates abnormal behavior.
            return false;
        }
        return true;
    } // launchDebuggee

    /**
     * Returns a string describing the action of launching the debuggee
     * using this connection. The string is prefixed with the
     * 'vmLoading' resource, followed by the 'home', 'vmexec',
     * 'options', and 'main' connector arguments.
     *
     * @return  string describing the launching arguments.
     */
    public String loadingString() {
        StringBuffer buf = new StringBuffer(256);
        buf.append(Bundle.getString("vmLoading"));
        buf.append('\n');
        buf.append(getConnectArg("home"));
        buf.append(File.separator);
        // HACK - assumes 'bin' is where the executable is located.
        buf.append("bin");
        buf.append(File.separator);
        buf.append(getConnectArg("vmexec"));
        buf.append(' ');
        buf.append(getConnectArg("options"));
        buf.append('\n');
        buf.append(getConnectArg("main"));
        return buf.toString();
    } // loadingString
} // VMConnection
