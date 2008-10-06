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
 * $Id: AliasInputProcessor.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the use of a command alias.
 *
 * @author Nathan Fiedler
 */
public class AliasInputProcessor implements InputProcessor {

    public boolean canProcess(String input, CommandParser parser) {
        int index = input.indexOf(' ');
        String alias;
        if (index > 0) {
            alias = input.substring(0, index);
        } else {
            alias = input;
        }
        return parser.getAlias(alias) != null;
    }

    public boolean expandsInput() {
        return false;
    }

    public List<String> process(String input, CommandParser parser)
            throws CommandException {
        List<String> output = new ArrayList<String>(1);
        int index = input.indexOf(' ');
        if (index > 0) {
            String alias = input.substring(0, index);
            // Replace the alias with the original command.
            alias = parser.getAlias(alias);
            // Append any new arguments given by the user.
            alias += input.substring(index);
            output.add(alias);
        } else {
            String alias = parser.getAlias(input);
            output.add(alias);
        }
        return output;
    }
}
