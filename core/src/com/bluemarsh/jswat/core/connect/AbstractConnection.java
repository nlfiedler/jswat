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
 * $Id: AbstractConnection.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.ListeningConnector;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * An abstract implementation of a JvmConnection.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractConnection implements JvmConnection {
    /** Connector. */
    private Connector connector;
    /** Connector arguments. */
    private Map<String, ? extends Connector.Argument> connectorArgs;
    /** True if this is a remote connection. */
    private boolean isRemoteConnection;
    /** Debuggee VM. */
    private VirtualMachine debuggeeVM;
    /** List of ConnectionListener objects. */
    private ConnectionListener listenerList;

    /**
     * Constructs a new JvmConnection with the given connector and
     * arguments.
     *
     * @param  connector  connector.
     * @param  args       connector arguments.
     */
    public AbstractConnection(Connector connector,
            Map<String, ? extends Connector.Argument> args) {
        this.connector = connector;
        connectorArgs = args;
        if (connector instanceof AttachingConnector
            || connector instanceof ListeningConnector) {
            isRemoteConnection = true;
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (this) {
            listenerList = ConnectionEventMulticaster.add(listenerList, listener);
        }
        if (isConnected()) {
            ConnectionEvent se = new ConnectionEvent(this, ConnectionEvent.Type.CONNECTED);
            se.getType().fireEvent(se, listener);
        }
    }

    public void disconnect() {
        debuggeeVM = null;
    }

    /**
     * Fires the event to all of the registered listeners.
     *
     * @param  se  session event.
     */
    protected void fireEvent(ConnectionEvent se) {
        ConnectionListener sl;
        synchronized (this) {
            sl = listenerList;
        }
        if (sl != null) {
            se.getType().fireEvent(se, sl);
        }
    }

    public String getAddress() {
        String name = getConnectorArg("name");
        if (name != null) {
            return name;
        }
        String port = getConnectorArg("port");
        if (port != null) {
            String hostname = getConnectorArg("hostname");
            if (hostname == null || hostname.length() == 0) {
                hostname = "localhost";
            }
            return hostname + ':' + port;
        }
        String main = getConnectorArg("main");
        if (main != null) {
            return NbBundle.getMessage(getClass(), "address.launched");
        }
        return "";
    }

    /**
     * Returns the JDI connector associated with this connection instance.
     *
     * @return  a connector.
     */
    protected Connector getConnector() {
        return connector;
    }

    /**
     * Returns the named connector argument value as a String.
     *
     * @param  name  name of argument to retrieve.
     * @return  named argument value, or null if not available.
     */
    protected String getConnectorArg(String name) {
        if (connectorArgs != null) {
            Connector.Argument arg = connectorArgs.get(name);
            if (arg != null) {
                return arg.value();
            }
        }
        return null;
    }

    /**
     * Returns the connector arguments for this connection.
     *
     * @return  an argument map.
     */
    protected Map<String, ? extends Connector.Argument> getConnectorArgs() {
        return connectorArgs;
    }

    public VirtualMachine getVM() {
        return debuggeeVM;
    }

    public boolean isConnected() {
        if (debuggeeVM != null) {
            try {
                return debuggeeVM.topLevelThreadGroups() != null;
            } catch (VMDisconnectedException vmde) {
                //  ignore and fall to the return false
            }
        }
        return false;
    }

    public boolean isRemote() {
        return isRemoteConnection;
    }

    public void removeConnectionListener(ConnectionListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (this) {
            listenerList = ConnectionEventMulticaster.remove(listenerList, listener);
        }
    }

    /**
     *
     * @param  vm  virtual machine we are now connected to.
     */
    protected void setVM(VirtualMachine vm) {
        debuggeeVM = vm;
    }
}
