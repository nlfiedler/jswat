/*********************************************************************
 *
 *      Copyright (C) 2000-2005 Nathan Fiedler
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
 * $Id: CaptureAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Class CaptureAction allows the user to capture the output sent
 * to the message window.
 *
 * @author  Nathan Fiedler
 */
public class CaptureAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Key for storing file writer in properties. */
    protected static final String FILEWRITER_KEY = "capture.fileWriter";
    /** Key for storing output stream in properties. */
    protected static final String OUTPUTSTREAM_KEY = "capture.outputStream";
    /** Key for storing filename in properties. */
    protected static final String FILENAME_KEY = "capture.filename";

    /**
     * Creates a new CaptureAction object with the default action
     * command string of "capture".
     */
    public CaptureAction() {
        super("capture");
    } // CaptureAction

    /**
     * Performs the capture action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Frame win = getFrame(event);
        Session session = getSession(event);
        UIAdapter adapter = session.getUIAdapter();

        boolean captureStdout = false;
        boolean captureFile = false;

        // Get the properites from the UI adapter.
        OutputStream stdoutStream = (OutputStream)
            adapter.getProperty(OUTPUTSTREAM_KEY);
        if (stdoutStream != null) {
            captureStdout = true;
        }
        FileWriter fileWriter = (FileWriter)
            adapter.getProperty(FILEWRITER_KEY);
        if (fileWriter != null) {
            captureFile = true;
        }
        String filename = (String)
            adapter.getProperty(FILENAME_KEY);
        if (filename == null) {
            filename = "";
        }

        // Get desired settings from user.
        Object messages[] = {
            new JCheckBox(swat.getResourceString("captureToStdoutField"),
                          captureStdout),
            new JCheckBox(swat.getResourceString("captureToFileField"),
                          captureFile),
            swat.getResourceString("captureFileField"),
            new JTextField(filename, 20)
        };

        boolean responseOkay = false;
        while (!responseOkay) {
            int response = JOptionPane.showOptionDialog
                (win, messages, swat.getResourceString("captureTitle"),
                 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                 null, null, null);

            // If okay, see what the user has given.
            if (response != JOptionPane.OK_OPTION) {
                return;
            }

            // Assume response is okay.
            responseOkay = true;
            captureStdout = ((JCheckBox) messages[0]).isSelected();
            captureFile = ((JCheckBox) messages[1]).isSelected();
            filename = ((JTextField) messages[3]).getText();

            Log log = session.getStatusLog();
            if (captureStdout) {
                if (stdoutStream == null) {
                    // Grab the stdout stream and register it with Log.
                    stdoutStream = System.out;
                    log.attach(stdoutStream);
                }
            } else {
                if (stdoutStream != null) {
                    // Remove the stdout stream.
                    log.detach(stdoutStream);
                    // Leave the stdout stream open
                    stdoutStream = null;
                }
            }
            adapter.setProperty(OUTPUTSTREAM_KEY, stdoutStream);

            if (captureFile) {
                if ((filename == null) || (filename.length() == 0)) {
                    displayError(event, Bundle.getString("missingFilename"));
                    responseOkay = false;
                    continue;
                }
                if (fileWriter != null) {
                    // Close the existing file writer.
                    // User may have changed the file name so we'll
                    // next create a new file writer.
                    closeFileWriter(log, fileWriter);
                    adapter.setProperty(FILEWRITER_KEY, null);
                }

                if (fileWriter == null) {
                    // Create the file writer and register it with Log.
                    File f = new File(filename);
                    if (f.exists() && !f.canWrite()) {
                        displayError(event, Bundle.getString("readOnlyFile"));
                        responseOkay = false;
                        continue;
                    }

                    try {
                        fileWriter = new FileWriter(f);
                    } catch (FileNotFoundException fnfe) {
                        displayError(event, Bundle.getString("fileNotFound"));
                        responseOkay = false;
                        continue;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        responseOkay = false;
                        continue;
                    }
                    log.attach(fileWriter);
                    adapter.setProperty(FILEWRITER_KEY, fileWriter);
                    adapter.setProperty(FILENAME_KEY, filename);
                }
            } else {
                if (fileWriter != null) {
                    // Remove and close the file writer.
                    closeFileWriter(log, fileWriter);
                    adapter.setProperty(FILEWRITER_KEY, null);
                }
            }
        }
    } // actionPerformed

    /**
     * Removes the writer from the given Log object and closes it.
     *
     * @param  log     Log object to detach from.
     * @param  writer  writer to detach from log.
     */
    public void closeFileWriter(Log log, Writer writer) {
        // Remove and close the file writer.
        log.detach(writer);
        try {
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    } // closeFileWriter
} // CaptureAction
