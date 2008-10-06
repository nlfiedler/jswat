/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Processes.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Utility methods for dealing with external processes.
 *
 * @author Nathan Fiedler
 */
public class Processes {

    /**
     * Creates a new instance of Processes.
     */
    private Processes() {
    }

    /**
     * Reads the contents of the input stream and returns it as a String.
     *
     * @param  is  input stream to read from.
     * @return  contents of stream as a String.
     * @throws  IOException
     *          if error occurs.
     */
    public static String readStream(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int len = isr.read(buf);
        while (len != -1) {
            sb.append(buf, 0, len);
            len = isr.read(buf);
        }
        return sb.toString();
    }

    /**
     * Waits for the given process to complete, returning the output from
     * that process.
     *
     * @param  proc  running process for which to wait.
     * @return  the standard output followed by the standard error from
     *          the process.
     */
    public static String waitFor(final Process proc) {
        // Get the output and error readers started.
        ExecutorService es = Threads.getThreadPool();
        Future<String> ofuture = es.submit(new Callable<String>() {
            public String call() throws IOException {
                return readStream(proc.getInputStream());
            }
        });
        Future<String> efuture = es.submit(new Callable<String>() {
            public String call() throws IOException {
                return readStream(proc.getErrorStream());
            }
        });

        // Wait for the process to terminate.
        try {
            proc.waitFor();
        } catch (InterruptedException ie) {
            // ignore
        }

        // Wait for the readers to finish.
        String out = null;
        try {
            out = ofuture.get();
        } catch (ExecutionException ee) {
            out = ee.toString();
        } catch (InterruptedException ie) {
            out = ie.toString();
        }
        String err = null;
        try {
            err = efuture.get();
        } catch (ExecutionException ee) {
            err = ee.toString();
        } catch (InterruptedException ie) {
            err = ie.toString();
        }

        // Construct the output.
        if (out.length() > 0 && err.length() > 0) {
            return out + "\n" + err;
        } else if (out.length() > 0) {
            return out;
        } else if (err.length() > 0) {
            return err;
        }
        return null;
    }
}
