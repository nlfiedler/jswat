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
 * $Id: BangPrefixInputProcessor.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Handles the !&lt;prefix&gt; shortcut for invoking a previously entered
 * command.
 *
 * @author Nathan Fiedler
 */
public class BangPrefixInputProcessor implements InputProcessor {

    public boolean canProcess(String input, CommandParser parser) {
        return input.startsWith("!") && input.length() > 1;
    }

    public boolean expandsInput() {
        return true;
    }

    public List<String> process(String input, CommandParser parser)
            throws CommandException {
        List<String> output = null;
        Iterator<String> iter = parser.getHistory(true);
        while (iter.hasNext()) {
            String prev = iter.next();
            if (input.regionMatches(1, prev, 0, input.length() - 1)) {
                output = new ArrayList<String>(1);
                output.add(prev);
                break;
            }
        }
        if (output == null) {
            // This should not have happened.
            throw new CommandException(NbBundle.getMessage(getClass(),
                    "ERR_BangPrefixIP_AmbiguousHistory"));
        }
        return output;
    }
}
