/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * MODULE:      JSwat UI
 * FILE:        ReaderToTextArea.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/08/01        Moved to its own file
 *
 * DESCRIPTION:
 *      Mediator that reads from a Reader and writes to a text area.
 *
 * $Id: ReaderToTextArea.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import java.io.IOException;
import java.io.Reader;
import javax.swing.JTextArea;

/**
 * Class for reading from a java.io.Reader and writing to a JTextArea.
 *
 * @author  Nathan Fiedler
 */
class ReaderToTextArea implements Runnable {
    /** Reader from which to read text. */
    protected Reader incomingReader;
    /** Text area to write incoming text to. We may be one of several
     * readers writing to this text area, so we'll synchronize on it. */
    protected JTextArea outgoingTextArea;
    /** Thread that we're running on. */
    protected Thread runningThread;

    /**
     * Constructs a new ReaderToTextArea to read from the reader and
     * write to the text area.
     *
     * @param  reader  Reader to read from.
     * @param  text    Text area to write to.
     */
    public ReaderToTextArea(Reader reader, JTextArea text) {
        this.outgoingTextArea = text;
        this.incomingReader = reader;
    } // ReaderToTextArea

    /**
     * Close the underlying I/O streams. First stops the reader, if
     * it is currently running.
     */
    public void close() {
        stop();
        try {
            incomingReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        incomingReader = null;
        outgoingTextArea = null;
        runningThread = null;
    } // close

    /**
     * Read input from the reader and append it to the text area.
     * Runs until the <code>stop()</code> method is called or an
     * I/O exception occurs.
     *
     * @see #start
     * @see #stop
     */
    public void run() {
        char[] buf = new char[8192];
        // Run until we're interrupted.
        while (!runningThread.isInterrupted()) {
            try {
                int len = incomingReader.read(buf);
                if (len == -1) {
                    // Die if no input is available.
                    break;
                } else if (len > 0) {
                    String str = new String(buf, 0, len);
                    // Writing to the JTextArea is synchronized.
                    outgoingTextArea.append(str);
                }
            } catch (IOException ioe) {
                // Bail out when things go wrong.
                break;
            }
        }
    } // run

    /**
     * Start the reader to text area thread, using normal thread
     * priority.
     *
     * @see #stop
     */
    public void start() {
        runningThread = new Thread(this, "reader->textarea");
        runningThread.start();
    } // start

    /**
     * Stop the reader thread synchronously. Waits until the
     * reader thread has actually stopped.
     *
     * @see #start
     */
    public void stop() {
        // Terminate the run() loop synchronously (i.e. wait until
        // the run loop stops running).
        if ((runningThread != null) && runningThread.isAlive()) {
            runningThread.interrupt();
            try {
                // Wait for the running thread to stop.
                runningThread.join();
            } catch (InterruptedException ie) {
                // I couldn't care less.
            }
        }
    } // stop
} // ReaderToTextArea
