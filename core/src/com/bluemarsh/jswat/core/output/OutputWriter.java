/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: OutputWriter.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.output;

/**
 * An OutputWriter manages the display of textual messages to an output
 * window. It does not handle user input in any way.
 *
 * @author Nathan Fiedler
 */
public interface OutputWriter {

    /**
     * Ensure that the output window is visible to the user. This may not
     * have any meaning with certain implementations.
     */
    void ensureVisible();

    /**
     * Send the given String to the error portion of the output window,
     * or otherwise highlight the text to indicate it is an error.
     *
     * @param  msg  error message.
     */
    void printError(String msg);

    /**
     * Send the given String to the output portion of the output window.
     *
     * @param  msg  output message.
     */
    void printOutput(String msg);
}
