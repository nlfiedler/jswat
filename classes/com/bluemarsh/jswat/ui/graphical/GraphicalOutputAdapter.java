/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: GraphicalOutputAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.VMConnection;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.ui.Bundle;
import com.bluemarsh.jswat.ui.EditPopup;
import com.bluemarsh.jswat.ui.FancyTextArea;
import java.awt.Font;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Class OutputAdapter is responsible for displaying the output of a
 * debuggee process to a text area. It reads both the standard output
 * and standard error streams from the debuggee VM. For it to operate
 * correctly it must be added as a session listener.
 *
 * @author  Nathan Fiedler
 */
public class GraphicalOutputAdapter implements SessionListener {
    /** Text area displaying the messages. */
    private FancyTextArea outputArea;
    /** Scroller for the output area. */
    private JScrollPane outputAreaScroller;

    /**
     * Constructs a GraphicalOutputAdapter with the default text area.
     */
    public GraphicalOutputAdapter() {
        outputArea = new FancyTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputAreaScroller = new JScrollPane
            (outputArea,
             JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
             JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        outputArea.autoScroll(outputAreaScroller);

        // Add popup menu to allow copying of text area.
        EditPopup popup = new EditPopup(outputArea, false, true);
        outputArea.addMouseListener(popup);
    } // GraphicalOutputAdapter

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
            outputArea.setText(Bundle.getString("remoteDebuggee"));
            outputArea.setEnabled(false);
        } else {
            outputArea.setText("");
            outputArea.setEnabled(true);
            // Create readers for the input and error streams.
            displayOutput(vmc.getProcess().getErrorStream());
            displayOutput(vmc.getProcess().getInputStream());
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
    public synchronized void deactivated(SessionEvent sevt) {
        // Let the output readers die on their own.
        if (!outputArea.isEnabled()) {
            // Text area is disabled when using remote debuggee, in
            // which case we can remove the remote debuggee message.
            outputArea.setText("");
        }
    } // deactivated

    /**
     * Create a thread that will retrieve and display any output
     * from the given input stream.
     *
     * @param  is  InputStream to read from.
     */
    protected void displayOutput(final InputStream is) {
        Thread thr = new Thread("output reader") {
            public void run() {
                try {
                    InputStreamReader isr = new InputStreamReader(is);
                    char[] buf = new char[8192];
                    // Dump until there's nothing left.
                    int len = isr.read(buf);
                    while (len != -1) {
                        String str = new String(buf, 0, len);
                        // Writing to the JTextArea is synchronized.
                        outputArea.append(str);
                        // Yield to the other output reader thread.
                        Thread.yield();
                        len = isr.read(buf);
                    }
                } catch (IOException ioe) {
                    outputArea.append(Bundle.getString("errorReadingOutput")
                                      + '\n');
                }
            }
        };
        thr.setPriority(Thread.MIN_PRIORITY);
        thr.start();
    } // displayOutput

    /**
     * Returns the text component to which output is displayed.
     *
     * @return  text component displaying output.
     */
    public JTextArea getOutputArea() {
        return outputArea;
    } // getOutputArea

    /**
     * Returns a reference to the UI component.
     *
     * @return  ui component object
     */
    public JComponent getUI() {
        return outputAreaScroller;
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
     * Sets the maximum number of lines to be shown in this panel.
     *
     * @param  count  maximum number of lines to show.
     */
    public void setMaxLineCount(int count) {
        outputArea.setMaxLineCount(count);
    } //  setMaxLineCount

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended
} // GraphicalOutputAdapter
