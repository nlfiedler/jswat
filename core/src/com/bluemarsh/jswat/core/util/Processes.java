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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
            @Override
            public String call() throws IOException {
                return readStream(proc.getInputStream());
            }
        });
        Future<String> efuture = es.submit(new Callable<String>() {
            @Override
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
