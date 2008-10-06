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
 * $Id: ListeningConnection.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

import com.bluemarsh.jswat.core.output.OutputProvider;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.TransportTimeoutException;
import java.io.IOException;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Implements a connection that listens for a connection from a debuggee.
 *
 * @author Nathan Fiedler
 */
public class ListeningConnection extends AbstractConnection
        implements Cancellable, Runnable {
    /** If true, this listener has been cancelled by the user. */
    private boolean cancelled;

    /**
     * Creates a new instance of ListeningConnection.
     *
     * @param  connector  connector.
     * @param  args       connector arguments.
     */
    public ListeningConnection(Connector connector,
            Map<String, ? extends Connector.Argument> args) {
        super(connector, args);
    }

    public boolean cancel() {
        ListeningConnector conn = (ListeningConnector) getConnector();
        try {
            cancelled = true;
            conn.stopListening(getConnectorArgs());
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return false;
        } catch (IllegalConnectorArgumentsException icae) {
            ErrorManager.getDefault().notify(icae);
            return false;
        }
        return true;
    }

    public void connect()
            throws IllegalConnectorArgumentsException, IOException {
        cancelled = false;
        RequestProcessor.getDefault().post(this);
    }

    public void run() {
        ProgressHandle ph = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(ListeningConnection.class,
                "LBL_ListeningConnector_Waiting"), this);
        ph.start();
        VirtualMachine vm = null;
        try {
            ListeningConnector conn = (ListeningConnector) getConnector();
            Map<String, ? extends Connector.Argument> args = getConnectorArgs();
            String address = conn.startListening(args);
            String msg = NbBundle.getMessage(ListeningConnection.class,
                    "LBL_ListeningConnector_Address", address);
            OutputProvider.getWriter().printOutput(msg);
            vm = conn.accept(args);
            conn.stopListening(args);
            setVM(vm);
            fireEvent(new ConnectionEvent(this, ConnectionEvent.Type.CONNECTED));
        } catch (TransportTimeoutException tte) {
            String msg = NbBundle.getMessage(ListeningConnection.class,
                    "LBL_ListeningConnector_TimedOut");
            OutputProvider.getWriter().printOutput(msg);
            ph.finish();
        } catch (IOException ioe) {
            if (!cancelled) {
                ErrorManager.getDefault().notify(ioe);
            }
        } catch (IllegalConnectorArgumentsException icae) {
            ErrorManager.getDefault().notify(icae);
        } finally {
            ph.finish();
        }
    }
}
