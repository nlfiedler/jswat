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
 * $Id: EchoCommand.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

/**
 * Used in unit testing the command parser.
 *
 * @author Nathan Fiedler
 */
public class EchoCommand extends AbstractCommand {

    public String getName() {
        return "echo";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        if (arguments.countTokens() == 0) {
            throw new MissingArgumentsException();
        }
        String msg = arguments.rest();
        context.getWriter().println(msg);
    }
}
