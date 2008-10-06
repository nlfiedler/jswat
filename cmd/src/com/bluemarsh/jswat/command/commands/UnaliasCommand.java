/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: UnaliasCommand.java 15 2007-06-03 00:01:17Z nfiedler $
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
