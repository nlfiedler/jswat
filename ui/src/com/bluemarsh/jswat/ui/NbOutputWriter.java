/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: NbOutputWriter.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.core.output.OutputWriter;
import java.awt.EventQueue;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * NetBeans Output window implementation of the OutputWriter interface.
 * This versions uses the NetBeans io module to display the messages in
 * the Output TopComponent. All output is dispatched on the AWT thread.
 *
 * @author Nathan Fiedler
 */
public class NbOutputWriter implements OutputWriter {
    /** Name for the output tab. */
    private String tabname;

    /**
     * Creates a new instance of NbOutputWriter.
     */
    public NbOutputWriter() {
        tabname = NbBundle.getMessage(getClass(), "IO_Console");
    }

    public void ensureVisible() {
        Runnable runner = new Runnable() {
            public void run() {
                getIO().select();
            }
        };
        EventQueue.invokeLater(runner);
    }

    /**
     * Retrieve the input/output instance for this OutputWriter.
     *
     * @return  the InputOutput instance.
     */
    private InputOutput getIO() {
        // Get the input/output panel for debugger messages. Note that
        // we should not open this until the NetBeans interface is ready,
        // otherwise the output TopComponent will go to the editor mode.
        InputOutput io = IOProvider.getDefault().getIO(tabname, false);
        io.setErrSeparated(false);
        return io;
    }

    public void printError(final String msg) {
        Runnable runner = new Runnable() {
            public void run() {
                getIO().getErr().println(msg);
            }
        };
        EventQueue.invokeLater(runner);
    }

    public void printOutput(final String msg) {
        Runnable runner = new Runnable() {
            public void run() {
                getIO().getOut().println(msg);
            }
        };
        EventQueue.invokeLater(runner);
    }
}
