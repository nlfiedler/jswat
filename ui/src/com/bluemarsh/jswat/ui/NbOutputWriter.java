/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: NbOutputWriter.java 6 2007-05-16 07:14:24Z nfiedler $
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
