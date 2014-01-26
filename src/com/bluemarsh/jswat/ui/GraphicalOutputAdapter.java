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
 * $Id: GraphicalOutputAdapter.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.config.JConfigure;
import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionListener;
import com.bluemarsh.jswat.panel.EditPopup;
import com.sun.jdi.VirtualMachine;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 * Class OutputAdapter is responsible for displaying the output of
 * a debuggee process to a text area. It reads both the standard output
 * and standard error streams from the debuggee VM. For it to operate
 * correctly it must be added as a session listener.
 *
 * @author  Nathan Fiedler
 */
public class GraphicalOutputAdapter implements SessionListener {
    /** Text area displaying the messages. */
    protected FancyTextArea outputArea;
    /** Scroller for the output area. */
    protected JScrollPane outputAreaScroller;

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
        outputArea.add(popup);
        outputArea.addMouseListener(popup);
    } // GraphicalOutputAdapter

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        JConfigure config = JSwat.instanceOf().getJConfigure();
        boolean readOutput = config.getBooleanProperty("launch.readOutput");
        if (!readOutput) {
            outputArea.setText(Bundle.getString("notReadingOutput"));
            outputArea.setEnabled(false);
            return;
        }

        // Attach to the stderr and stdout input streams of the passed
        // VirtualMachine and begin reading from them. Everything read
        // will be displayed in the text area.
        VirtualMachine vm = session.getVM();
        if (vm.process() == null) {
            // Must be a remote process, which can't provide us
            // with an input and error streams.
            outputArea.setText(Bundle.getString("remoteDebuggee"));
            outputArea.setEnabled(false);
        } else {
            outputArea.setText("");
            outputArea.setEnabled(true);
            // Create readers for the input and error streams.
            displayOutput(vm.process().getErrorStream());
            displayOutput(vm.process().getInputStream());
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
    public synchronized void deactivate(Session session) {
        // Let the output reader threads die on their own.
        if (!outputArea.isEnabled()) {
            // Text area is disabled when using remote debuggee, in
            // which case we can remove the remote debuggee message.
            outputArea.setText("");
        }
    } // deactivate

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
                    int len = -1;
                    while ((len = isr.read(buf)) != -1) {
                        String str = new String(buf, 0, len);
                        // Writing to the JTextArea is synchronized.
                        outputArea.append(str);
                        // Yield to the other output reader thread.
                        Thread.yield();
                    }
                } catch (IOException ioe) {
                    outputArea.append(Bundle.getString("errorReadingOutput") +
                                      '\n');
                }
	    }
	};
	thr.setPriority(Thread.MIN_PRIORITY);
	thr.start();
    } // displayOutput

    /**
     * Returns a reference to the UI component.
     *
     * @return  ui component object
     */
    public JComponent getUI() {
        return outputAreaScroller;
    } // getUI

    /**
     * Called after the Session has added this listener to the
     * Session listener list.
     *
     * @param  session  Session adding this listener.
     */
    public void init(Session session) {
    } // init

    /**
     * Sets the maximum number of lines to be shown in this panel.
     *
     * @param  count  maximum number of lines to show.
     */
    public void setMaxLineCount(int count) {
        outputArea.setMaxLineCount(count);
    } //  setMaxLineCount
} // GraphicalOutputAdapter
