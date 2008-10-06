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
 * $Id: RepeaterInputProcessor.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.util.Collections;
import java.util.List;
import org.openide.ErrorManager;

/**
 * Handles the integer prefix to repeat a given command multiple times.
 *
 * @author Nathan Fiedler
 */
public class RepeaterInputProcessor implements InputProcessor {

    public boolean canProcess(String input, CommandParser parser) {
        int index = input.indexOf(' ');
        // See if the first token is an integer.
        if (index > 0) {
            String val = input.substring(0, index);
            try {
                Integer.parseInt(val);
                return true;
            } catch (NumberFormatException nfe) {
                // Ignore to return false.
            }
        }
        return false;
    }

    public boolean expandsInput() {
        return false;
    }

    public List<String> process(String input, CommandParser parser)
            throws CommandException {
        try {
            int index = input.indexOf(' ');
            String mult = input.substring(0, index);
            int times = Integer.parseInt(mult);
            String command = input.substring(index + 1);
            return Collections.nCopies(times, command);
        } catch (NumberFormatException nfe) {
            // This should not have happened.
            ErrorManager.getDefault().notify(nfe);
        }
        return null;
    }
}
