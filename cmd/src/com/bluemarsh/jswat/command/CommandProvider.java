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
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.command;

import org.openide.util.Lookup;

/**
 * Class CommandProvider provides convenient accessor methods for
 * retrieving an instance of the CommandParser.
 *
 * @author Nathan Fiedler
 */
public class CommandProvider {
    /** The CommandParser instance, if it has already been retrieved. */
    private static CommandParser commandParser;

    /**
     * Creates a new instance of CommandProvider.
     */
    private CommandProvider() {
    }

    /**
     * Retrieve the CommandParser instance, creating one if necessary.
     *
     * @return  CommandParser instance.
     */
    public static synchronized CommandParser getCommandParser() {
        if (commandParser == null) {
            // Perform lookup to find a CommandParser instance.
            commandParser = Lookup.getDefault().lookup(CommandParser.class);
        }
        return commandParser;
    }
}
