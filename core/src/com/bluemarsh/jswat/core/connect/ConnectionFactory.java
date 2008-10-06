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
 * $Id: ConnectionFactory.java 15 2007-06-03 00:01:17Z nfiedler $
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
}
