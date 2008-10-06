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
 * $Id: ConnectionFactory.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

import com.bluemarsh.jswat.core.runtime.JavaRuntime;

/**
 * A ConnectionFactory creates JvmConnection instances. A concrete
 * implementation can be acquired from the <code>ConnectionProvider</code>
 * class.
 *
 * @author Nathan Fiedler
 */
public interface ConnectionFactory {

    /**
     * Builds a JvmConnection to launch a debuggee VM.
     *
     * @param  runtime  Java runtime to use for launching.
     * @param  options  VM options to pass, or null for none.
     * @param  main     class to launch, with optional arguments.
     * @return  new JvmConnection.
     */
    JvmConnection createLaunching(JavaRuntime runtime, String options, String main);

    /**
     * Builds a JvmConnection to listen for a connection from a debuggee VM.
     *
     * @param  transport  name of the transport to use (e.g. "dt_socket").
     * @param  host       the 'localAddress' value for "dt_socket" transport.
     * @param  address    the address on which to listen, or null to use default.
     * @return  new JvmConnection.
     * @throws  NoListeningConnectorException
     *          if the appropriate connector could not be found.
     */
    JvmConnection createListening(String transport, String host,
            String address) throws NoListeningConnectorException;

    /**
     * Builds a connection to connect using the given process.
     *
     * @param  pid  the process identifer.
     * @return  new JvmConnection.
     * @throws  NoAttachingConnectorException
     *          if the appropriate connector could not be found.
     */
    JvmConnection createProcess(String pid) throws NoAttachingConnectorException;

    /**
     * Builds a connection to connect using the given shared memory name.
     *
     * @param  name  shared memory name.
     * @return  new JvmConnection.
     * @throws  NoAttachingConnectorException
     *          if the appropriate connector could not be found.
     */
    JvmConnection createShared(String name) throws NoAttachingConnectorException;

    /**
     * Builds a connection to connect using the given host and port.
     *
     * @param  hostname  host machine name, may be null.
     * @param  port      port number of listening debuggee.
     * @return  new JvmConnection.
     * @throws  NoAttachingConnectorException
     *          if the appropriate connector could not be found.
     */
    JvmConnection createSocket(String hostname, String port)
            throws NoAttachingConnectorException;
}
