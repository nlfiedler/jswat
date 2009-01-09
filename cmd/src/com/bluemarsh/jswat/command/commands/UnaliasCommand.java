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
 * The Original Software is the JSwat Command Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.CommandParser;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Deletes command aliases.
 *
 * @author Nathan Fiedler
 */
public class UnaliasCommand extends AbstractCommand {

    public String getName() {
        return "unalias";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        CommandParser parser = context.getParser();
        String alias = arguments.nextToken();
        if (parser.getAlias(alias) != null) {
            parser.setAlias(alias, null);
        }
    }

    public boolean requiresArguments() {
        return true;
    }
}
