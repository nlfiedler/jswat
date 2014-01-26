/*********************************************************************
 *
 *      Copyright (C) 2000-2001 Nathan Fiedler
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
 * MODULE:      JSwat Commands
 * FILE:        listenCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/07/00        Initial version
 *      nf      12/26/00        Implemented the listen command.
 *      nf      09/03/01        Handle session active error case
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'listen' command.
 *
 * $Id: listenCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.VMConnection;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Defines the class that handles the 'listen' command.
 *
 * @author  Nathan Fiedler
 */
public class listenCommand extends JSwatCommand {

    /**
     * Perform the 'listen' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        String address = "";
        String transport = "dt_socket";
        if (args.hasMoreTokens()) {
            // Determine if the argument is a port or share name.
            address = args.nextToken();
            try {
                Integer.parseInt(address);
            } catch (NumberFormatException nfe) {
                // Use the shared memory connector.
                transport = "dt_shmem";
            }
        }

        // Get the VM manager and request listening connectors.
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List connectors = vmm.listeningConnectors();

        // Find a listening connector that uses 'transport'.
        ListeningConnector connector = null;
        for (int i = 0; i < connectors.size(); i++) {
            Connector conn = (Connector) connectors.get(i);
            if (conn.transport().name().equals(transport)) {
                connector = (ListeningConnector) conn;
                break;
            }
        }
        if (connector == null) {
            out.writeln(Bundle.getString("listen.noConnectorFound"));
            return;
        }

        // Set the connector's arguments.
        Map connectArgs = connector.defaultArguments();
        if (transport.equals("dt_socket")) {
            ((Connector.Argument) connectArgs.get("port")).setValue(address);
        } else {
            ((Connector.Argument) connectArgs.get("name")).setValue(address);
        }
        VMConnection connection = new VMConnection(connector, connectArgs);

        // Create the listener and run it in a new thread.
        Listener listener = new Listener(session, connection, out);
        Thread th = new Thread(listener, "listen command");
        th.start();
    } // perform

    /**
     * Listens for an incoming connection from a debuggee VM.
     *
     * @author  Nathan Fiedler
     */
    class Listener implements Runnable {
        /** Session object. */
        protected Session session;
        /** VMConnection object. */
        protected VMConnection connection;
        /** Log object. */
        protected Log out;

        /**
         * Create a new Listener.
         *
         * @param  session     Session to activate.
         * @param  connection  Connection to listen on.
         * @param  out         Out to print messages to.
         */
        Listener(Session session, VMConnection connection, Log out) {
            this.session = session;
            this.connection = connection;
            this.out = out;
        } // Listener

        /**
         * Starts listening for a remote VM connection and activates
         * the Session when a connection is eventually made.
         */
        public void run() {
            // Try to listen for an attaching VM.
            VirtualMachine vm = null;
            try {
                ListeningConnector connector = (ListeningConnector)
                    connection.getConnector();
                Map connectArgs = connection.getConnectArgs();
                String address = connector.startListening(connectArgs);
                out.write(swat.getResourceString("listeningOn") + ' ' +
                          address + '\n');
                vm = connector.accept(connectArgs);
                connector.stopListening(connectArgs);
            } catch (IOException ioe) {
                out.writeln(ioe.toString());
                return;
            } catch (IllegalConnectorArgumentsException icae) {
                out.writeln(icae.toString());
                return;
            }

            connection.setVM(vm);
            if (session.isActive()) {
                // Eek, session is already active!
                // Disconnect from the new remote VM.
                out.writeln(Bundle.getString("listen.disconnecting"));
                vm.dispose();
            } else {
                session.activate(connection);
            }
        } // run
    } // Listener
} // listenCommand
