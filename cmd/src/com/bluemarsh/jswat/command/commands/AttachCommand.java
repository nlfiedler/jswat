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
 * The Original Software is the JSwat Command Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.connect.ConnectionEvent;
import com.bluemarsh.jswat.core.connect.ConnectionFactory;
import com.bluemarsh.jswat.core.connect.ConnectionListener;
import com.bluemarsh.jswat.core.connect.ConnectionProvider;
import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.connect.NoAttachingConnectorException;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import java.io.IOException;
import java.io.PrintWriter;
import org.openide.util.NbBundle;

/**
 * Attaches to a remote debuggee.
 *
 * @author Nathan Fiedler
 */
public class AttachCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "attach";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter writer = context.getWriter();
        final Session session = context.getSession();
        if (session.isConnected()) {
            writer.println(NbBundle.getMessage(AttachCommand.class,
                    "CTL_attach_activeSession"));
        } else {
            // Get the previously attachment parameters.
            String socketHost = session.getProperty(Session.PROP_SOCKET_HOST);
            String socketPort = session.getProperty(Session.PROP_SOCKET_PORT);
            String sharedName = session.getProperty(Session.PROP_SHARED_NAME);
            String processId = session.getProperty(Session.PROP_PROCESS_ID);
            String connector = session.getProperty(Session.PROP_CONNECTOR);
            if (arguments.hasMoreTokens()) {
                // Determine what type of address the user provided.
                String address = arguments.nextToken();
                int colon = address.indexOf(':');
                if (colon >= 0) {
                    // Argument is of the form "host:port".
                    socketHost = address.substring(0, colon);
                    socketPort = address.substring(colon + 1);
                    connector = Session.PREF_SOCKET;
                } else {
                    try {
                        Integer.parseInt(address);
                        // Argument is a process identifier.
                        processId = address;
                        connector = Session.PREF_PROCESS;
                    } catch (NumberFormatException nfe) {
                        // Argument is a shared memory name.
                        sharedName = address;
                        connector = Session.PREF_SHARED;
                    }
                }
                // Set the attachment parameters in the session properties.
                session.setProperty(Session.PROP_SOCKET_HOST, socketHost);
                session.setProperty(Session.PROP_SOCKET_PORT, socketPort);
                session.setProperty(Session.PROP_SHARED_NAME, sharedName);
                session.setProperty(Session.PROP_PROCESS_ID, processId);
                session.setProperty(Session.PROP_CONNECTOR, connector);
            }

            if (connector == null || connector.isEmpty()) {
                throw new MissingArgumentsException(
                        NbBundle.getMessage(AttachCommand.class,
                        "ERR_attach_NoAddress"));
            }

            // Create a connection to the remote debuggee.
            ConnectionFactory factory = ConnectionProvider.getConnectionFactory();
            final JvmConnection connection;
            try {
                if (connector.equals(Session.PREF_PROCESS)) {
                    connection = factory.createProcess(processId);
                } else if (connector.equals(Session.PREF_SHARED)) {
                    connection = factory.createShared(sharedName);
                } else if (connector.equals(Session.PREF_SOCKET)) {
                    connection = factory.createSocket(socketHost, socketPort);
                } else {
                    throw new CommandException(NbBundle.getMessage(
                            AttachCommand.class, "ERR_attach_UnsupportedConnector",
                            connector));
                }
                // The actual connection may be made some time from now,
                // so set up a listener to be notified at that time.
                connection.addConnectionListener(new ConnectionListener() {
                    @Override
                    public void connected(ConnectionEvent event) {
                        if (session.isConnected()) {
                            // The user already connected to something else.
                            JvmConnection c = event.getConnection();
                            c.getVM().dispose();
                            c.disconnect();
                        } else {
                            session.connect(connection);
                        }
                    }
                });
                connection.connect();
            } catch (IllegalConnectorArgumentsException icae) {
                throw new CommandException(NbBundle.getMessage(
                        AttachCommand.class, "ERR_attach_BadConnectorArgs"), icae);
            } catch (IOException ioe) {
                throw new CommandException(NbBundle.getMessage(
                        AttachCommand.class, "ERR_attach_IOError"), ioe);
            } catch (VMDisconnectedException vmde) {
                throw new CommandException(NbBundle.getMessage(
                        AttachCommand.class, "ERR_attach_Disconnected"), vmde);
            } catch (VMStartException vmse) {
                throw new CommandException(NbBundle.getMessage(
                        AttachCommand.class, "ERR_attach_BadStart"), vmse);
            } catch (NoAttachingConnectorException nace) {
                throw new CommandException(NbBundle.getMessage(
                        AttachCommand.class, "ERR_attach_NoAttaching"), nace);
            }
        }
    }
}
