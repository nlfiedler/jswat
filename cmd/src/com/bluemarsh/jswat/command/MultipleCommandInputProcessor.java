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
 * are Copyright (C) 2005-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.command;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles the input of multiple commands, separated by semicolon (;).
 *
 * @author Nathan Fiedler
 */
public class MultipleCommandInputProcessor implements InputProcessor {

    /**
     * Separate of multiple commands in a single input string.
     */
    private static final char COMMAND_SEPARATOR_CHAR = ';';

    @Override
    public boolean canProcess(String input, CommandParser parser) {
        // Check if there is a command separator that is not
        // inside a quoted string.
        boolean processable = false;
        int strlen = input != null ? input.length() : 0;
        int index = 0;
        byte state = 0;
        byte previousState = 0;
        while (index < strlen && !processable) {
            char ch = input.charAt(index);
            switch (state) {
                case 0:
                    // Not inside a quoted string.
                    if (ch == '"') {
                        state = 1;
                    } else if (ch == '\'') {
                        state = 2;
                    } else if (ch == '\\') {
                        previousState = state;
                        state = 3;
                    } else if (ch == COMMAND_SEPARATOR_CHAR) {
                        processable = true;
                    }
                    break;

                case 1:
                    // Inside a double-quoted string.
                    if (ch == '"') {
                        state = 0;
                    } else if (ch == '\\') {
                        previousState = state;
                        state = 3;
                    }
                    break;

                case 2:
                    // Inside a single-quoted string.
                    if (ch == '\'') {
                        state = 0;
                    } else if (ch == '\\') {
                        previousState = state;
                        state = 3;
                    }
                    break;

                case 3:
                    // Previous character was a slash.
                    // Simply skip the character and return to previous state.
                    state = previousState;
                    break;

                default:
                    throw new IllegalStateException("I am confused: " + input);
            }
            index++;
        }
        return processable;
    }

    @Override
    public boolean expandsInput() {
        return false;
    }

    @Override
    public List<String> process(String input, CommandParser parser)
            throws CommandException {
        List<String> output = new LinkedList<String>();
        if (input == null) {
            return null;
        }
        // The input could be multiple commands, or there could be
        // a separator embedded in one of the command arguments.
        int strlen = input.length();
        int start = 0;
        int index = 0;
        byte state = 0;
        byte previousState = 0;
        while (index < strlen) {
            char ch = input.charAt(index);
            switch (state) {
                case 0:
                    // Not inside a quoted string.
                    if (ch == '"') {
                        state = 1;
                    } else if (ch == '\'') {
                        state = 2;
                    } else if (ch == '\\') {
                        previousState = state;
                        state = 3;
                    } else if (ch == COMMAND_SEPARATOR_CHAR) {
                        output.add(input.substring(start, index));
                        start = index + 1;
                    }
                    break;

                case 1:
                    // Inside a double-quoted string.
                    if (ch == '"') {
                        state = 0;
                    } else if (ch == '\\') {
                        previousState = state;
                        state = 3;
                    }
                    break;

                case 2:
                    // Inside a single-quoted string.
                    if (ch == '\'') {
                        state = 0;
                    } else if (ch == '\\') {
                        previousState = state;
                        state = 3;
                    }
                    break;

                case 3:
                    // Previous character was a slash.
                    // Simply skip the character and return to previous state.
                    state = previousState;
                    break;

                default:
                    throw new IllegalStateException("I am confused: " + input);
            }
            index++;
        }

        // Either we have processed nothing or processed all but the
        // last separated command. Both cases are happily handled in
        // exactly the same manner.
        output.add(input.substring(start));
        // Remove empty commands.
        Iterator<String> iter = output.iterator();
        while (iter.hasNext()) {
            String s = iter.next();
            if (s.trim().isEmpty()) {
                iter.remove();
            }
        }
        return output;
    }
}
