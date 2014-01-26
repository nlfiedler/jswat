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
 * FILE:        GraphicalMessageAdapter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/12/01        Moved from GraphicalAdapter.java
 *
 * $Id: GraphicalMessageAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.ui.EditPopup;
import com.bluemarsh.jswat.ui.FancyTextArea;
import com.bluemarsh.jswat.ui.ReaderToTextArea;
import java.awt.Font;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 * Class MessageAdapter builds and maintains the text area for
 * displaying the contents of the status log.
 *
 * @author  Nathan Fiedler
 */
public class GraphicalMessageAdapter {
    /** Text area displaying the status log contents. */
    private FancyTextArea messageArea;
    /** Scroll pane for the messages area. */
    private JScrollPane messageAreaScroller;
    /** Piped writer from which we receive log messages. */
    private PipedWriter logWriter;
    /** Our message reader which reads from the PipedWriter. */
    private ReaderToTextArea logReader;

    /**
     * Constructs a GraphicalMessageAdapter.
     */
    public GraphicalMessageAdapter() {
        messageArea = new FancyTextArea();
        Font font = new Font("Monospaced", Font.PLAIN, 12);
        messageArea.setFont(font);
        messageArea.setEditable(false);
        messageAreaScroller = new JScrollPane
            (messageArea,
             JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
             JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        messageArea.autoScroll(messageAreaScroller);

        // Add popup menu to allow copying of text area.
        EditPopup popup = new EditPopup(messageArea, false, true);
        messageArea.addMouseListener(popup);
    } // GraphicalMessageAdapter

    /**
     * Destroys the user interface.
     *
     * @param  log  status log to detach from.
     */
    public void destroy(Log log) {
        // Detach the message area from the status log.
        if (logWriter != null) {
            log.detach(logWriter);
            logReader.stop();
            try {
                logWriter.close();
            } catch (IOException ioe) {
                // ignored
            }
        }
    } // destroy

    /**
     * Returns the message area widget.
     *
     * @return  message area widget.
     */
    public JComponent getUI() {
        return messageAreaScroller;
    } // getUI

    /**
     * Attach to the given log object in order to receive messages
     * from it. As messages are written to the log, they will be
     * echoed to the text area.
     *
     * @param  log  status log to attach to.
     */
    public void init(Log log) {
        // Create the pipe to read from the status log.
        logWriter = new PipedWriter();
        PipedReader r;
        try {
            r = new PipedReader(logWriter);
        } catch (IOException ioe) {
            // this simply cannot happen
            logWriter = null;
            return;
        }

        // Create the reader to get text for our text area.
        logReader = new ReaderToTextArea(r, messageArea);
        logReader.start();
        log.attach(logWriter);
    } // init

    /**
     * Sets the maximum number of lines to be shown in this panel.
     *
     * @param  count  maximum number of lines to show.
     */
    public void setMaxLineCount(int count) {
        messageArea.setMaxLineCount(count);
    } //  setMaxLineCount
} // GraphicalMessageAdapter
