/*********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
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
 * MODULE:      JSwat
 * FILE:        Log.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/14/99        Initial version
 *      nf      12/27/00        Removed the need for PrintWriter
 *      nf      08/05/01        Manages its own flushing thread
 *      nf      08/31/01        Use StringBuffer efficiently
 *      nf      04/06/02        Default to System.out in flush
 *      nf      05/06/02        Fixed bug #522
 *
 * DESCRIPTION:
 *      This file contains the Log class, which implements a general
 *      logging facility for JSwat.
 *
 * $Id: Log.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Class for providing a general logging facility. An instance of
 * this class be attached to multiple OutputStreams or Writers for
 * the log messages to be sent to. This object is synchronized in
 * various places to ensure only reasonable output is generated.
 *
 * <p>The Log class can automatically flush the buffer contents to
 * the attach streams and writers if its <code>start()</code> method
 * is called. This will create a flusher thread that flushes the
 * buffer whenever something is written to the Log. Alternatively,
 * the buffer can be flushed manually by calling <code>flush()</code>.</p>
 *
 * @author  Nathan Fiedler
 */
public class Log implements Runnable {
    /** Default length for the log buffer. */
    protected static final int DEFAULT_BUFFER_LEN = 1024;
    /** List of all the OutputStream objects. */
    protected List oslist;
    /** List of all the Writer objects. */
    protected List wlist;
    /** The messages pending writing to the writers and output streams. */
    protected volatile StringBuffer logBuffer;
    /** Object used to synchronize access to the logBuffer reference.
     * Must acquire this lock before accessing the logBuffer _reference_
     * itself. The log buffer gets reallocated and reassigned to the
     * field variable over and over, so this lock is used to make sure
     * methods are not using a stale reference to the buffer. */
    protected Object bufferLock;
    /** Object used to synchronize flushing the buffer. If acquiring
     * this lock and the bufferLock, always get this lock first. */
    protected Object flushLock;
    /** Thread that handles the flushing of the buffer. */
    protected Thread runningThread;

    /**
     * Constructs a Log object. Note that the Log will not flush the
     * buffer contents automatically until the <code>start()</code>
     * method is called.
     */
    public Log() {
        // initialize lists and string buffer
        oslist = new ArrayList();
        wlist = new ArrayList();
        logBuffer = new StringBuffer(DEFAULT_BUFFER_LEN);
        flushLock = new Object();
        bufferLock = new Object();
    } // Log

    /**
     * Attach to an output stream in order to write log messages to it.
     *
     * @param  os  output stream to write to
     */
    public void attach(OutputStream os) {
        synchronized (oslist) {
            oslist.add(os);
        }
    } // attach

    /**
     * Attach to a writer in order to write log messages to it.
     *
     * @param  w  writer to write to
     */
    public void attach(Writer w) {
        synchronized (wlist) {
            wlist.add(w);
        }
    } // attach

    /**
     * Detach an output stream from this log. No more log messages
     * will go to this stream.
     *
     * @param  os  output stream to detach from
     */
    public void detach(OutputStream os) {
        synchronized (oslist) {
            oslist.remove(os);
        }
    } // detach

    /**
     * Remove a writer from this log. No more log messages
     * will go to this writer.
     *
     * @param  w  writer to detach from
     */
    public void detach(Writer w) {
        synchronized (wlist) {
            wlist.remove(w);
        }
    } // detach

    /**
     * Takes the log buffer and writes its contents to the writers.
     * This is multi-thread safe in that it makes a copy of the
     * buffer before writing to the writers and output streams.
     *
     * @return  true if something was flushed, false if buffer was empty.
     */
    public boolean flush() {
        String str;
        synchronized (bufferLock) {
            // Copy the buffer to a string, then clear it out.
            str = logBuffer.toString();
            logBuffer = new StringBuffer(DEFAULT_BUFFER_LEN);
        }
        if (str.length() == 0) {
            return false;
        }

        // Write str to all the OutputStream objects.
        boolean nothingWritten = true;
        synchronized (oslist) {
            byte[] bytes = str.getBytes();
            for (int i = oslist.size() - 1; i >= 0; i--) {
                OutputStream os = (OutputStream) oslist.get(i);
                try {
                    os.write(bytes);
                    os.flush();
                    nothingWritten = false;
                } catch (IOException ioe) {
                    // remove the offending os and report the problem
                    oslist.remove(os);
                    ioe.printStackTrace();
                }
            }
        }

        // Write str to all the Writer objects.
        synchronized (wlist) {
            for (int i = wlist.size() - 1; i >= 0; i--) {
                Writer w = (Writer) wlist.get(i);
                try {
                    w.write(str);
                    w.flush();
                    nothingWritten = false;
                } catch (IOException ioe) {
                    // remove the offending writer and report the problem
                    wlist.remove(w);
                    ioe.printStackTrace();
                }
            }
        }

        if (nothingWritten) {
            // There were no streams or writers successfully written to,
            // so fall back to System.out so the message is not lost.
            System.out.print(str);
            System.out.flush();
        }

        return true;
    } // flush

    /**
     * Run the buffer flusher. It is important that this run continuously
     * in its own thread, in order to avoid problems with those blasted
     * piped readers and writers.
     */
    public void run() {
        while (!runningThread.isInterrupted()) {
            synchronized (flushLock) {
                boolean shouldWait = true;
                // Check this in case we missed a notification.
                synchronized (bufferLock) {
                    shouldWait = logBuffer.length() == 0;
                }
                if (shouldWait) {
                    // Only wait when there's nothing in the buffer.
                    try {
                        flushLock.wait();
                    } catch (InterruptedException ie) { }
                }
                flush();
            }
        }
    } // run

    /**
     * Start the Log flusher thread with the given thread priority.
     *
     * @param  priority  Thread priority.
     * @see #stop
     */
    public void start(int priority) {
        runningThread = new Thread(this, "log flusher");
        runningThread.setPriority(priority);
        runningThread.start();
    } // start

    /**
     * Start the Log flusher thread, using normal thread priority.
     *
     * @see #stop
     */
    public void start() {
        start(Thread.NORM_PRIORITY);
    } // start

    /**
     * Stop the flusher thread synchronously. This method waits until
     * the flusher thread has actually stopped.
     */
    public void stop() {
        // Terminate the run() loop synchronously (i.e. wait until
        // the run loop stops running).
        if ((runningThread != null) && runningThread.isAlive()) {
            runningThread.interrupt();
            try {
                // Wait for the running thread to stop.
                runningThread.join();
            } catch (InterruptedException ie) { }
        }
    } // stop

    /**
     * Write a string to the log (without a linefeed).
     *
     * @param  s  string to write to log
     */
    public void write(String s) {
        synchronized (bufferLock) {
            logBuffer.append(s);
        }
        synchronized (flushLock) {
            flushLock.notify();
        }
    } // write

    /**
     * Write a string to the log (with a linefeed).
     *
     * @param  s  string to write to log
     */
    public void writeln(String s) {
        synchronized (bufferLock) {
            logBuffer.append(s);
            logBuffer.append('\n');
        }
        synchronized (flushLock) {
            flushLock.notify();
        }
    } // writeln

    /**
     * Write the given throwable's stack trace to the Log.
     *
     * @param  t  throwable to print stack for.
     */
    public void writeStackTrace(Throwable t) {
        // Creating a bunch of writers, but we don't expect to
        // do this very often, so it's okay.
        StringWriter stringWriter = new StringWriter(512);
        PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        write(stringWriter.toString());
    } // writeStackTrace
} // Log
