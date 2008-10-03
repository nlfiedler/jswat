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
 * $Id: AliasInputProcessor.java 6 2007-05-16 07:14:24Z nfiedler $
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
