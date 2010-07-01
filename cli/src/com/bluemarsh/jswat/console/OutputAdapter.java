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
 * are Copyright (C) 2004-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.util.Threads;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import org.openide.util.NbBundle;

/**
 * Watches for new sessions, adding itself as a session listener. When a
 * session activates, attaches itself to the debuggee output streams to
 * read from them.
 *
 * @author  Nathan Fiedler
 */
public class OutputAdapter implements SessionListener, SessionManagerListener {
    /** Receiver of the debuggee output. */
    private PrintWriter outputSink;
    /** A Map of Session instances to Future instances. */
    private Map<Session, Future> inputFutures;

    /**
     * Creates a new instance of OutputAdapter. This instance should be
     * added as a listener to the SessionManager.
     *
     * @param  sink  writer to which output is written.
     */
    public OutputAdapter(PrintWriter sink) {
        outputSink = sink;
        inputFutures = new HashMap<Session, Future>();
    }

    @Override
    public void connected(SessionEvent sevt) {
        Session session = sevt.getSession();
        // Start reading from the debuggee output streams.
        JvmConnection conn = session.getConnection();
        if (!conn.isRemote()) {
            // We can read from a launched debuggee.
            Process process = conn.getVM().process();
            InputStream is = process.getInputStream();
            OutputReader or = new OutputReader(is, outputSink);
            Threads.getThreadPool().submit(or);

            is = process.getErrorStream();
            or = new OutputReader(is, outputSink);
            Threads.getThreadPool().submit(or);

            PipedReader pr = PipeProvider.getPipedReader(session);
            OutputStream os = process.getOutputStream();
            InputReader ir = new InputReader(pr, os, outputSink);
            Future future = Threads.getThreadPool().submit(ir);
            inputFutures.put(session, future);
        }
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        // Let the output/error reader threads die on their own.
        // The input reader thread, however, needs some help.
        Session session = sevt.getSession();
        Future future = inputFutures.remove(session);
        if (future != null) {
            // Interrupt the running task to make it stop.
            future.cancel(true);
        }
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    public void resuming(SessionEvent sevt) {
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        // the session will discard its listeners
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
    }

    @Override
    public void suspended(SessionEvent sevt) {
    }

    /**
     * Basically just reads from an input stream and writes to a writer.
     * This is used to read from the debuggee output streams.
     *
     * @author  Nathan Fiedler
     */
    private static class OutputReader implements Runnable {
        /** Stream from which we are to read. */
        private InputStream inputStream;
        /** Where the output goes to. */
        private PrintWriter printWriter;

        /**
         * Constructs a stream reader.
         *
         * @param  is  input stream to read from.
         * @param  pw  writer to write to.
         */
        public OutputReader(InputStream is, PrintWriter pw) {
            inputStream = is;
            printWriter = pw;
        }

        @Override
        public void run() {
            // Read from the input stream.
            try {
                InputStreamReader isr = new InputStreamReader(inputStream);
                char[] buf = new char[8192];
                int len = isr.read(buf);
                while (len != -1) {
                    printWriter.write(buf, 0, len);
                    printWriter.flush();
                    len = isr.read(buf);
                }
            } catch (InterruptedIOException iioe) {
                // Just stop reading.
            } catch (IOException ioe) {
                printWriter.println(NbBundle.getMessage(
                    getClass(), "ERR_OutputAdapter_Output", ioe));
            }
        }
    }

    /**
     * Basically just reads from a reader and writes to an output stream.
     * This is used to send input to the debuggee standard input stream.
     *
     * @author  Nathan Fiedler
     */
    private static class InputReader implements Runnable {
        /** Reader from which user input is acquired. */
        private Reader reader;
        /** Stream to which user input is sent. */
        private OutputStream outputStream;
        /** Where the input goes to, if we are echoing user input. */
        private PrintWriter printWriter;

        /**
         * Constructs an input reader.
         *
         * @param  r   reader to read user input from.
         * @param  os  output stream to send input to.
         * @param  pw  writer to which user input is echoed (and error messages).
         */
        public InputReader(Reader r, OutputStream os, PrintWriter pw) {
            reader = r;
            outputStream = os;
            printWriter = pw;
        }

        @Override
        public void run() {
            // Read from the reader.
            try {
                OutputStreamWriter osw = new OutputStreamWriter(outputStream);
                char[] buf = new char[512];
                int len = reader.read(buf);
                while (len != -1) {
                    osw.write(buf, 0, len);
                    osw.flush();
                    len = reader.read(buf);
                }
            } catch (InterruptedIOException iioe) {
                // Just stop reading.
            } catch (IOException ioe) {
                String msg = ioe.getMessage();
                // Check for the improperly typed interrupted I/O exception.
                if (msg == null || !msg.startsWith("Interrupted")) {
                    // This exception cannot be ignored.
                    printWriter.println(NbBundle.getMessage(
                        getClass(), "ERR_OutputAdapter_Input", ioe));
                }
            }
        }
    }
}
