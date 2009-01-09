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
 * are Copyright (C) 2006-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
        ListeningConnector conn = (ListeningConnector) getConnector();
        Map<String, ? extends Connector.Argument> args = getConnectorArgs();
        try {
            String address = conn.startListening(args);
            String msg = NbBundle.getMessage(ListeningConnection.class,
                    "LBL_ListeningConnector_Address", address);
            OutputProvider.getWriter().printOutput(msg);
            VirtualMachine vm = conn.accept(args);
            conn.stopListening(args);
            setVM(vm);
            fireEvent(new ConnectionEvent(this, ConnectionEvent.Type.CONNECTED));
        } catch (TransportTimeoutException tte) {
            // It doesn't stop listening when there is a timeout? Stupid...
            try {
                conn.stopListening(args);
            } catch (Exception e) {
                // Ignore, nothing we can do about it now.
            }
            String msg = NbBundle.getMessage(ListeningConnection.class,
                    "LBL_ListeningConnector_TimedOut");
            OutputProvider.getWriter().printOutput(msg);
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
