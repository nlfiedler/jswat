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
 * $Id: MultipleCommandInputProcessor.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.util.LinkedList;
import java.util.List;

/**
 * Handles the input of multiple commands, separated by semicolon (;).
 *
 * @author Nathan Fiedler
 */
public class MultipleCommandInputProcessor implements InputProcessor {
    /** Separate of multiple commands in a single input string. */
    private static final String COMMAND_SEPARATOR = ";";
    /** Separate of multiple commands in a single input string. */
    private static final char COMMAND_SEPARATOR_CHAR = ';';

    public boolean canProcess(String input, CommandParser parser) {
        return input.contains(COMMAND_SEPARATOR);
    }

    public boolean expandsInput() {
        return false;
    }

    public List<String> process(String input, CommandParser parser)
            throws CommandException {
        List<String> output = new LinkedList<String>();
        // The input could be multiple commands, or there could just be
        // a spurious separator embedded in one of the command arguments.
        int strlen = input.length();
        int start = 0;
        int index = 0;
        byte state = 0;
        char ch = '\0';
        while (index < strlen) {
            ch = input.charAt(index);
            switch (state) {
            case 0:
                // Not inside a quoted string.
                if (ch == '"') {
                    state = 1;
                } else if (ch == '\'') {
                    state = 2;
                } else if (ch == '\\') {
                    state = 3;
                } else if (ch == COMMAND_SEPARATOR_CHAR) {
                    String cmd = input.substring(start, index);
                    output.add(cmd);
                    start = index + 1;
                }
                break;

            case 1:
                // Inside a double-quoted string.
                if (ch == '"') {
                    state = 0;
                } else if (ch == '\\') {
                    state = 4;
                }
                break;

            case 2:
                // Inside a single-quoted string.
                if (ch == '\'') {
                    state = 0;
                } else if (ch == '\\') {
                    state = 5;
                }
                break;

            case 3:
                // Previous character was a slash.
                // Simply skip the character and move on.
                state = 0;
                break;

            case 4:
                // Previous character was a slash.
                // Simply skip the character and move on.
                state = 1;
                break;

            case 5:
                // Previous character was a slash.
                // Simply skip the character and move on.
                state = 2;
                break;
            default:
                throw new IllegalStateException("I am confused: " + input);
            }
            index++;
        }

        // Either we have processed nothing or processed all but the
        // last separated command. Both cases are happily handled in
        // exactly the same manner.
        String cmd = input.substring(start);
        output.add(cmd);
        return output;
    }
}
