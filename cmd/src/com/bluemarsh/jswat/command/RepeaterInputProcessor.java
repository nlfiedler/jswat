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
 * $Id$
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
