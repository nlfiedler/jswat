/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: InputProcessor.java 15 2007-06-03 00:01:17Z nfiedler $
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
