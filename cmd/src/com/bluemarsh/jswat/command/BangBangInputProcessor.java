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
 * $Id: BangBangInputProcessor.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Handles the !! shortcut for invoking the last-entered command.
 *
 * @author Nathan Fiedler
 */
public class BangBangInputProcessor implements InputProcessor {

    public boolean canProcess(String input, CommandParser parser) {
        return input.equals("!!");
    }

    public boolean expandsInput() {
        return true;
    }

    public List<String> process(String input, CommandParser parser)
            throws CommandException {
        List<String> output = null;
        String prev = parser.getHistoryPrev();
        if (prev != null) {
            output = new ArrayList<String>(1);
            output.add(prev);
        } else {
            throw new CommandException(NbBundle.getMessage(getClass(),
                    "ERR_BangBangIP_NoHistory"));
        }
        return output;
    }
}
