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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui;

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
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Watches for new sessions, adding itself as a session listener. When a
 * session activates, attaches itself to the debuggee output streams to
 * read from them.
 *
 * @author  Nathan Fiedler
 */
public class OutputAdapter implements SessionListener, SessionManagerListener {
    /** A Map of Session instances to InputOutput instances. */
    private Map<Session, InputOutput> outputTabs;
    /** A Map of Session instances to Future instances. */
    private Map<Session, Future> inputFutures;

    /**
     * Creates a new instance of OutputAdapter. This instance should be
     * added as a listener to the SessionManager.
     */
    public OutputAdapter() {
        outputTabs = new HashMap<Session, InputOutput>();
        inputFutures = new HashMap<Session, Future>();
    }

    /**
     * Called when the Session has connected to the debuggee.
     *
     * @param  sevt  session event.
     */
    public void connected(SessionEvent sevt) {
        // Create a new output tab for this session.
        Session session = sevt.getSession();
        String name = session.getProperty(Session.PROP_SESSION_NAME);
        String label = NbBundle.getMessage(getClass(), "IO_Process", name);
        InputOutput io = IOProvider.getDefault().getIO(label, false);
        io.setOutputVisible(true);
        io.setInputVisible(true);
        io.setErrSeparated(false);
        // Need to call select() or the window may not be opened at all,
        // and then the user won't see the output or input prompts from
        // the debuggee, which could be very confusing when nothing seems
        // to happen because the debuggee is waiting for the user.
        io.select();
        try {
            // Clear the output pane so the user will not think the
            // old output was from the current debuggee.
            io.getOut().reset();
            io.getErr().reset();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        outputTabs.put(session, io);

        // Start reading from the debuggee output streams.
        JvmConnection conn = session.getConnection();
        if (conn.isRemote()) {
            // A remote debuggee sends its output elsewhere.
            io.getOut().println(NbBundle.getMessage(getClass(),
                    "MSG_OutputAdapter_RemoteProcess"));
            io.setInputVisible(false);
        } else {
            // We can read from a launched debuggee.
            Process process = conn.getVM().process();
            InputStream is = process.getInputStream();
            OutputReader or = new OutputReader(is, io.getOut());
            Threads.getThreadPool().submit(or);

            is = process.getErrorStream();
            or = new OutputReader(is, io.getErr());
            Threads.getThreadPool().submit(or);

            OutputStream os = process.getOutputStream();
            InputReader ir = new InputReader(io.getIn(), os, io.getOut());
            Future future = Threads.getThreadPool().submit(ir);
            inputFutures.put(session, future);
        }
    }

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
        // Close the output tab for this session.
        Session session = sevt.getSession();
        InputOutput tab = outputTabs.remove(session);
        if (tab != null) {
            tab.closeInputOutput();
        }
    }

    /**
     * Called when the Session has disconnected from the debuggee.
     *
     * @param  sevt  session event.
     */
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

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
        // ignored
    }

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
        // ignored
    }

    /**
     * Called when a Session has been added to the SessionManager.
     *
     * @param  e  session manager event.
     */
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
    }

    /**
     * Called when a Session has been removed from the SessionManager.
     *
     * @param  e  session manager event.
     */
    public void sessionRemoved(SessionManagerEvent e) {
        // the session will discard its listeners
    }

    /**
     * Called when a Session has been made the current session.
     *
     * @param  e  session manager event.
     */
    public void sessionSetCurrent(SessionManagerEvent e) {
        // ignored
    }

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
        // ignored
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

        /**
         * Read from the input stream.
         */
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(inputStream);
                char[] buf = new char[1024];
                int len = isr.read(buf);
                while (len != -1) {
                    String str = new String(buf, 0, len);
                    printWriter.println(str);
                    // Note that yield() is not effective on multi-CPU systems.
                    Thread.sleep(1);
                    len = isr.read(buf);
                }
            } catch (InterruptedException ie) {
                // Just stop reading.
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

        /**
         * Read from the reader.
         */
        public void run() {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(outputStream);
                char[] buf = new char[256];
                int len = reader.read(buf);
                while (len != -1) {
                    osw.write(buf, 0, len);
                    // Must flush each time.
                    osw.flush();
                    // Note that yield() is not effective on multi-CPU systems.
                    Thread.sleep(1);
                    len = reader.read(buf);
                }
            } catch (InterruptedException ie) {
                // Just stop reading.
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
