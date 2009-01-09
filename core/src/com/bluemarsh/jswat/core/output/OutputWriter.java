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
 * $Id$
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
