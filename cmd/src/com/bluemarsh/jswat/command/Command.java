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
 * $Id: Command.java 15 2007-06-03 00:01:17Z nfiedler $
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
