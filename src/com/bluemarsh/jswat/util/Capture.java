/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: Capture.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Utility class to manage the capture of log messages to alternative
 * output streams (e.g. standard output, standard error, or a file).
 *
 * @author  Nathan Fiedler
 */
public class Capture {
    /** Key for storing filename in properties. */
    private static final String FILE_NAME_KEY = "capture.file.name";
    /** Key for storing file writer in properties. */
    private static final String FILE_WRITER_KEY = "capture.file.writer";
    /** Key for storing output stream in properties. */
    private static final String STREAM_ERROR_KEY = "capture.stream.err";
    /** Key for storing output stream in properties. */
    private static final String STREAM_OUTPUT_KEY = "capture.stream.out";
    
    /**
     * None shall instantiate us.
     */
    private Capture() {
    } // Capture

    /**
     * Returns the name of the file to which output is being directed.
     *
     * @param  session  debugging session.
     * @return  file name, or null if no file is set.
     */
    public static String getFilename(Session session) {
        UIAdapter adapter = session.getUIAdapter();
        return (String) adapter.getProperty(FILE_NAME_KEY);
    } // getFilename

    /**
     * Returns the enabled state of capture to a file output stream.
     *
     * @param  session  debugging session.
     * @return  true if enabled, false otherwise.
     */
    public static boolean isFileEnabled(Session session) {
        UIAdapter adapter = session.getUIAdapter();
        return adapter.getProperty(FILE_WRITER_KEY) != null;
    } // isFileEnabled

    /**
     * Returns the enabled state of capture to standard output stream.
     *
     * @param  session  debugging session.
     * @return  true if enabled, false otherwise.
     */
    public static boolean isOutputEnabled(Session session) {
        UIAdapter adapter = session.getUIAdapter();
        return adapter.getProperty(STREAM_OUTPUT_KEY) != null;
    } // isOutputEnabled

    /**
     * Enables or disables capture to a file output stream.
     *
     * @param  enabled  true to enable capture, false to disable.
     * @param  file     file to write to, or null if enabled is false.
     * @param  session  debugging session.
     * @throws  IOException
     *          if an I/O error occurs.
     */
    public static void setFileEnabled(boolean enabled, File file,
                                      Session session)
        throws IOException {
        UIAdapter adapter = session.getUIAdapter();
        Log log = session.getStatusLog();
        FileWriter fileWriter = (FileWriter)
            adapter.getProperty(FILE_WRITER_KEY);

        // Detach the file writer from the Log and close it.
        // We do this unconditionally because the caller may be
        // providing a new file to write output to, in which case
        // we must first close the old file.
        if (fileWriter != null) {
            log.detach(fileWriter);
            fileWriter.close();
            adapter.setProperty(FILE_WRITER_KEY, null);
        }

        if (enabled) {
            // Create the file writer and attach it to the Log.
            fileWriter = new FileWriter(file);
            log.attach(fileWriter);
            adapter.setProperty(FILE_WRITER_KEY, fileWriter);
            adapter.setProperty(FILE_NAME_KEY, file.getCanonicalPath());
        }
    } // setFileEnabled

    /**
     * Enables or disables capture to the standard output stream.
     *
     * @param  enabled  true to enable capture, false to disable.
     * @param  session  debugging session.
     */
    public static void setOutputEnabled(boolean enabled, Session session) {
        UIAdapter adapter = session.getUIAdapter();
        OutputStream stream = (OutputStream)
            adapter.getProperty(STREAM_OUTPUT_KEY);
        Log log = session.getStatusLog();
        if (enabled) {
            if (stream == null) {
                // Attach the standard output stream to the Log.
                stream = System.out;
                log.attach(stream);
            }
        } else {
            if (stream != null) {
                // Detach the standard output stream from the Log.
                log.detach(stream);
                stream = null;
            }
        }
        adapter.setProperty(STREAM_OUTPUT_KEY, stream);
    } // setOutputEnabled
} // Capture
