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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: InputProcessor.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.util.List;

/**
 * An InputProcessor evaluates an input string from the user and determines
 * how to process it, possibly expanding or replacing the input.
 *
 * @author Nathan Fiedler
 */
public interface InputProcessor {

    /**
     * Indicates if this processor wants to process the given input.
     *
     * @param  input   user input.
     * @param  parser  command parser.
     * @return  true if parser should invoke this processor, false otherwise.
     */
    boolean canProcess(String input, CommandParser parser);

    /**
     * Indicates if the original input string should be replaced with the
     * processed input provided by this processor. This concerns the value
     * saved to the command history, not the processed input.
     *
     * @return  true to save processed input in history, false otherwise.
     */
    boolean expandsInput();

    /**
     * Process the given input, utilizing the parser if necessary.
     *
     * @param  input   user input.
     * @param  parser  command parser.
     * @return  process input, or null to silently ignore this input.
     * @throws  CommandException
     *          thrown if input was invalid or inappropriate.
     */
    List<String> process(String input, CommandParser parser)
            throws CommandException;
}
