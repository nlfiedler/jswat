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
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.output.OutputWriter;
import java.io.PrintWriter;

/**
 * Implementation of OutputWriter for the console mode.
 *
 * @author  Nathan Fiedler
 */
public class ConsoleOutputWriter implements OutputWriter {

    /** Sink for all output. */
    private PrintWriter output;

    /**
     * Default constructor for ConsoleOutputWriter.
     */
    public ConsoleOutputWriter() {
        // Turn on auto-flush so we don't have to bother.
        output = new PrintWriter(System.out, true);
    }

    @Override
    public void ensureVisible() {
        // Nothing to do.
    }

    @Override
    public void printError(String msg) {
        output.println(msg);
    }

    @Override
    public void printOutput(String msg) {
        output.println(msg);
    }
}
