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

/**
 * A Command is a singleton that is invoked by the user via the command
 * parser. Each command must have a unique name, some description, and
 * a brief "help" message (syntax and usage).
 *
 * @author Nathan Fiedler
 */
public interface Command {

    /**
     * Returns the one-line description of what this command does.
     *
     * @return  command description.
     */
    String getDescription();

    /**
     * Returns the (possibly) multiple-line syntax and usage instructions
     * for this command.
     *
     * @return  command help.
     */
    String getHelp();

    /**
     * Returns the unique name of this command.
     *
     * @return  command name.
     */
    String getName();

    /**
     * Perform the command within the given context.
     *
     * @param  context    provides environment for command invocation.
     * @param  arguments  the user-provided arguments for the command.
     * @throws  CommandException
     *          thrown if command encounters an error condition.
     * @throws  MissingArgumentsException
     *          thrown if insufficient arguments were provided.
     */
    void perform(CommandContext context, CommandArguments arguments)
        throws CommandException, MissingArgumentsException;

    /**
     * Indicates if this command must be provided with at least one argument.
     *
     * @return  true if this command must have arguments, false otherwise.
     */
    boolean requiresArguments();

    /**
     * Indicates if this command requires that the session be connected to
     * a debuggee in order to perform its task.
     *
     * @return  true if this command requires connection, false otherwise.
     */
    boolean requiresDebuggee();

    /**
     * Indicates if this command requires that a thread be set as the
     * current thread and that it be suspended by the debugger.
     *
     * @return  true if this command requires a thread, false otherwise.
     */
    boolean requiresThread();
}
