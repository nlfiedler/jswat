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
 * MODULE:      Report
 * FILE:        FileReporter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/07/01        Initial version
 *
 * DESCRIPTION:
 *      This file defines the FileReporter class.
 *
 * $Id: FileReporter.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Class FileReporter writes logging events to an output stream.
 *
 * @author  Nathan Fiedler
 * @version 1.0  4/7/01
 */
public class FileReporter implements Reporter {
    /** Stream to which we print logging events. */
    protected PrintStream printStream;

    /**
     * Constructs a FileReporter that uses the given output stream.
     *
     * @param  output  output stream to write to.
     */
    public FileReporter(OutputStream stream) {
        setOutput(stream);
    } // FileReporter

    /**
     * Report the given logging event.
     *
     * @param  event  logging event.
     */
    public void report(LoggingEvent event) {
        synchronized (this) {
            printStream.print('[');
            printStream.print(event.getCategoryName());
            printStream.print("] (");
            printStream.print(event.getThreadName());
            printStream.print(") ");
            printStream.println(event.getMessage());
        }
    } // report

    /**
     * Set the output stream to which this reporter writes logging events.
     *
     * @param  output  output stream to write to.
     */
    public void setOutput(OutputStream output) {
        if (output == null) {
            throw new NullPointerException("output must not be null");
        }
        synchronized (this) {
            if (output instanceof PrintStream) {
                printStream = (PrintStream) output;
            } else {
                printStream = new PrintStream(output);
            }
        }
    } // setOutput
} // FileReporter
