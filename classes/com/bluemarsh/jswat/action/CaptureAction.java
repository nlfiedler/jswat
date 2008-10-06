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
 * $Id: CaptureAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.Capture;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Class CaptureAction allows the user to capture the message to an
 * alternative location.
 *
 * @author  Nathan Fiedler
 */
public class CaptureAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

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

        boolean captureStdout = Capture.isOutputEnabled(session);
        boolean captureFile = Capture.isFileEnabled(session);
        String filename = Capture.getFilename(session);

        // Get desired settings from user.
        Object[] messages = {
            new JCheckBox(Bundle.getString("Capture.toStdoutField"),
                          captureStdout),
            new JCheckBox(Bundle.getString("Capture.toFileField"),
                          captureFile),
            Bundle.getString("Capture.fileField"),
            new JTextField(filename, 20)
        };

        boolean responseOkay = false;
        while (!responseOkay) {
            int response = JOptionPane.showOptionDialog(
                win, messages, Bundle.getString("Capture.title"),
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

            Capture.setOutputEnabled(captureStdout, session);

            File file = null;
            if (captureFile) {
                // Check the file name input.
                if (filename == null || filename.length() == 0) {
                    displayError(event, Bundle.getString(
                        "Capture.missingFilename"));
                    responseOkay = false;
                    continue;
                }
                // Create the file and check that it exists.
                file = new File(filename);
                if (file.exists() && !file.canWrite()) {
                    displayError(event, Bundle.getString(
                        "Capture.readOnlyFile"));
                    responseOkay = false;
                    continue;
                }
            }

            // Enable or disable the file capture.
            try {
                Capture.setFileEnabled(captureFile, file, session);
            } catch (FileNotFoundException fnfe) {
                displayError(event, Bundle.getString("Capture.fileNotFound"));
                responseOkay = false;
            } catch (IOException ioe) {
                displayError(event, ioe.toString());
                responseOkay = false;
            }
        }
    } // actionPerformed
} // CaptureAction
