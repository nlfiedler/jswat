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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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

    @Override
    public boolean canProcess(String input, CommandParser parser) {
        return input.equals("!!");
    }

    @Override
    public boolean expandsInput() {
        return true;
    }

    @Override
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
