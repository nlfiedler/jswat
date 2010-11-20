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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import org.openide.ErrorManager;

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
     * Waits for the given process to complete, returning the output from
     * that process.
     *
     * @param  proc  running process for which to wait.
     * @return  the standard output and standard error from the
     *          process mingled much as it would normally be.
     */
    public static String waitFor(final Process proc) {
        // Get the output and error readers started.
        StringBuffer buffer = new StringBuffer();
        ExecutorService es = Threads.getThreadPool();
        es.submit(new StreamReader(proc.getInputStream(), buffer));
        es.submit(new StreamReader(proc.getErrorStream(), buffer));

        // Wait for the process to terminate.
        try {
            proc.waitFor();
        } catch (InterruptedException ignored) {
        }
        return buffer.toString();
    }

    /**
     * Class StreamReader reads text from a stream and appends it to
     * a StringBuffer.
     */
    private static class StreamReader implements Runnable {

        /** From whence output is read. */
        private final InputStream stream;
        /** That to which output is sent. */
        private final StringBuffer buffer;

        /**
         * Creates a new instance of StreamReader.
         *
         * @param  stream  where output is read from.
         * @param  buffer  where output is sent.
         */
        StreamReader(InputStream stream, StringBuffer buffer) {
            this.stream = stream;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(stream);
                char[] buf = new char[1024];
                int len = isr.read(buf);
                while (len != -1) {
                    buffer.append(buf, 0, len);
                    len = isr.read(buf);
                }
            } catch (IOException ioe) {
                // Unlikely to occur, but notify user prominently.
                ErrorManager.getDefault().notify(ioe);
            }
        }
    }
}
