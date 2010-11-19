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
 * are Copyright (C) 2009-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.session.Session;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to the input and output of a pipe, permitting text
 * to be sent through the pipe. There is a pipe associated with each
 * active session.
 *
 * @author  Nathan Fiedler
 */
public class PipeProvider {

    /** Logger for gracefully reporting unexpected errors. */
    private static final Logger logger = Logger.getLogger(
            PipeProvider.class.getName());
    /** Used to control access to the instance maps. */
    private static final Object mapsLock;
    /** Map of PipedReader instances, keyed by Session instance. */
    private static final Map<Session, PipedReader> pipedReaders;
    /** Map of PipedWriter instances, keyed by Session instance. */
    private static final Map<Session, PipedWriter> pipedWriters;

    static {
        mapsLock = new Object();
        pipedReaders = new WeakHashMap<Session, PipedReader>();
        pipedWriters = new WeakHashMap<Session, PipedWriter>();
    }

    /**
     * Creates a new instance of PipeProvider.
     */
    private PipeProvider() {
    }

    /**
     * Set up the two ends of the pipe and store them in the maps.
     *
     * @param  session  key for the maps.
     */
    private static void createPipe(Session session) {
        synchronized (mapsLock) {
            PipedReader pr = new PipedReader();
            try {
                PipedWriter pw = new PipedWriter(pr);
                pipedReaders.put(session, pr);
                pipedWriters.put(session, pw);
            } catch (IOException ioe) {
                // This simply will not happen in the above code.
                logger.log(Level.SEVERE, null, ioe);
            }
        }
    }

    /**
     * Retrieve the reader end of the pipe for the given session,
     * creating it if necessary.
     *
     * @param  session  session for which to get the piped reader.
     * @return  the piped reader.
     */
    public static PipedReader getPipedReader(Session session) {
        synchronized (mapsLock) {
            PipedReader pr = pipedReaders.get(session);
            if (pr == null) {
                // Create the pipe and install it in the maps.
                createPipe(session);
                pr = pipedReaders.get(session);
            }
            return pr;
        }
    }

    /**
     * Retrieve the writer end of the pipe for the given session,
     * creating it if necessary.
     *
     * @param  session  session for which to get the piped writer.
     * @return  the piped writer.
     */
    public static PipedWriter getPipedWriter(Session session) {
        synchronized (mapsLock) {
            PipedWriter pw = pipedWriters.get(session);
            if (pw == null) {
                // Create the pipe and install it in the maps.
                createPipe(session);
                pw = pipedWriters.get(session);
            }
            return pw;
        }
    }
}
