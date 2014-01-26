/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 * FILE:        GraphicalInputAdapter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/12/01        Moved from GraphicalAdapter.java
 *
 * $Id: GraphicalInputAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.VMConnection;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.ui.Bundle;
import com.bluemarsh.jswat.ui.EditPopup;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * <p>Class GraphicalInputAdapter is responsible for providing the input
 * to a debuggee process through a text field. This class uses the
 * ActionListener interface to catch text field action events.
 * Unfortunately, this has two disadvantages:</p>
 *
 * <ul>
 *  <li>Individual key events are not sent to the debuggee.</li>
 *  <li>The debuggee only gets the input when the user hits 'Enter'.</li>
 * </ul>
 *
 * <p>Fortunately, Java's method for reading from standard input matches
 * exactly the behavior we replicate. How convenient...</p>
 *
 * <p>For this class to operate correctly it must be added as a session
 * listener.</p>
 *
 * @author  Nathan Fiedler
 */
public class GraphicalInputAdapter implements ActionListener, SessionListener {
    /** Text area displaying the messages. */
    private JTextArea outputArea;
    /** Text area displaying the messages. */
    private JTextField inputField;
    /** Output stream writer to write to the debuggee's stdin. */
    private OutputStreamWriter inputWriter;

    /**
     * Constructs a GraphicalInputAdapter with the default text field.
     *
     * @param  area  text area to output to, in addition to the text
     *               field this class maintains, or null if none.
     */
    public GraphicalInputAdapter(JTextArea area) {
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        inputField.addActionListener(this);

        outputArea = area;

        // Set up the edit popup menu.
        EditPopup popup = new EditPopup(inputField, true, false);
        inputField.addMouseListener(popup);
    } // GraphicalInputAdapter

    /**
     * Invoked when user hits Enter in the input text field.
     *
     * @param  event  Action event.
     */
    public void actionPerformed(ActionEvent event) {
        if (inputWriter == null) {
            return;
        }

        String text = inputField.getText();

        // Write the text to the output area, if available.
        if (outputArea != null) {
            outputArea.append(text);
            outputArea.append("\n");
        }

        // Write the text to the output stream.
        try {
            inputWriter.write(text);
            // Need to send terminating line feed since the
            // debuggee VM is probably expecting it.
            inputWriter.write('\n');
            // Flush output immediately.
            inputWriter.flush();
            // Clear the text field (only when successful).
            inputField.setText("");
        } catch (IOException ioe) {
            inputField.setText("(error: " + ioe.getMessage() + ')');
            inputField.setEnabled(false);
            try {
                inputWriter.close();
            } catch (IOException ioe2) {
                // ignored
            }
            inputWriter = null;
        }
    } // actionPerformed

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        VMConnection vmc = sevt.getSession().getConnection();
        if (vmc.isRemote()) {
            // A remote process can't provide us with an output stream.
            inputField.setText(Bundle.getString("remoteDebuggee"));
            inputField.setEnabled(false);
        } else {
            inputField.setText("");
            inputField.setEnabled(true);
            inputWriter = new OutputStreamWriter(
                vmc.getProcess().getOutputStream());
        }
    } // activated

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
    } // closing

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
        if (inputWriter != null) {
            try {
                inputWriter.close();
            } catch (IOException ioe) {
                // ignored
            }
            inputWriter = null;
        }

        // Clear the input field and disable it when inactive.
        inputField.setText("");
        inputField.setEnabled(false);
    } // deactivated

    /**
     * Returns a reference to the UI component.
     *
     * @return  ui component object
     */
    public JComponent getUI() {
        return inputField;
    } // getUI

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
    } // opened

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended
} // GraphicalInputAdapter
