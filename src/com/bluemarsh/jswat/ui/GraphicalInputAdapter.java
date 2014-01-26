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
 * FILE:        GraphicalInputAdapter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/12/01        Moved from GraphicalAdapter.java
 *
 * DESCRIPTION:
 *      A user interface adapter class for graphical interface.
 *
 * $Id: GraphicalInputAdapter.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionListener;
import com.bluemarsh.jswat.panel.EditPopup;
import com.sun.jdi.VirtualMachine;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

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
 * <p>Fortunately, Java's method for reading from standard input
 * matches exactly the behavior we replicate. How convenient...</p>
 *
 * <p>For this class to operate correctly it must be added as a
 * session listener.</p>
 *
 * @author  Nathan Fiedler
 */
public class GraphicalInputAdapter implements ActionListener, SessionListener {
    /** Text area displaying the messages. */
    protected JTextField inputField;
    /** Output stream writer to write to the debuggee's stdin. */
    protected OutputStreamWriter inputWriter;

    /**
     * Constructs a GraphicalInputAdapter with the default text field.
     */
    public GraphicalInputAdapter() {
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        inputField.addActionListener(this);

        // Set up the edit popup menu.
        EditPopup popup = new EditPopup(inputField, true, false);
        inputField.add(popup);
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

        // Write the text to the output stream.
        String text = inputField.getText();
        try {
            inputWriter.write(text);
            // Need to send terminating line feed since the
            // debuggee VM is probably expecting it.
            inputWriter.write('\n');
            // Flush output immediately.
            inputWriter.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            try {
                inputWriter.close();
            } catch (IOException ioe2) { }
            inputWriter = null;
        }

        // Clear the text field.
        inputField.setText("");
    } // actionPerformed

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        VirtualMachine vm = session.getVM();
        if (vm.process() == null) {
            // Must be a remote process, which can't provide us
            // with an output stream.
            inputField.setText(Bundle.getString("remoteDebuggee"));
            inputField.setEnabled(false);
        } else {
            inputField.setText("");
            inputField.setEnabled(true);
            inputWriter = new OutputStreamWriter
                (vm.process().getOutputStream());
        }
    } // activate

    /**
     * Called when the Session is about to close down.
     *
     * @param  session  Session being closed.
     */
    public void close(Session session) {
    } // close

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Panels are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
        if (inputWriter != null) {
            try {
                inputWriter.close();
            } catch (IOException ioe) {
            }
            inputWriter = null;
        }

        // Clear the input field and disable it when inactive.
        inputField.setText("");
        inputField.setEnabled(false);
    } // deactivate

    /**
     * Returns a reference to the UI component.
     *
     * @return  ui component object
     */
    public JComponent getUI() {
        return inputField;
    } // getUI

    /**
     * Called after the Session has added this listener to the
     * Session listener list.
     *
     * @param  session  Session adding this listener.
     */
    public void init(Session session) {
    } // init
} // GraphicalInputAdapter
