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
 * $Id: CommandParser.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.io.PrintWriter;
import java.util.Iterator;

/**
 * A CommandParser interprets text-based commands entered by the user. It
 * breaks the input line into tokens and determines which command is to be
 * executed. It then calls on the appropriate command object to perform
 * the action.
 *
 * @author  Nathan Fiedler
 */
public interface CommandParser {

    /**
     * Returns the definition of the named alias.
     *
     * @param  name  alias name.
     * @return  alias value, or null if no alias defined by that name.
     */
    String getAlias(String name);

    /**
     * Returns an iterator of the names of all the available command aliases.
     *
     * @return  iterator of alias names.
     */
    Iterator<String> getAliases();

    /**
     * Retrieves the instance of the command by the given name.
     *
     * @param  name  name of command to retrieve.
     * @return  command, or null if not found.
     */
    Command getCommand(String name);

    /**
     * Returns an array of the available commands.
     *
     * @return  iterator of known commands.
     */
    Iterator<Command> getCommands();

    /**
     * Returns an iterator of the commands in the history chain. If the
     * reverse parameter is true, the history is returned in the reverse
     * order in which the commands were input (i.e. the most recent is
     * the first entry in the iterator).
     *
     * @param  reverse  return list in reverse order.
     * @return  iterator of command history.
     */
    Iterator<String> getHistory(boolean reverse);

    /**
     * Retrieves the command following the current position within the
     * command history. If null is returned, that indicates that the
     * current position is at the end of the history.
     *
     * @return  next command in history, or null if none.
     */
    String getHistoryNext();

    /**
     * Retrieves the command preceeding the current position within the
     * command history. If null is returned, that indicates that the
     * current position is at the start of the history.
     *
     * @return  previous command in history, or null if none.
     */
    String getHistoryPrev();

    /**
     * Hook in which to load the persistent settings from storage.
     */
    void loadSettings();

    /**
     * Parse the command input string and perform the appropriate action.
     * All registered InputProcessors are called upon to process the input.
     *
     * <p>This method will throw a runtime exception if errors occur.</p>
     *
     * @param  input  command input string.
     * @throws  CommandException
     *          thrown if command encounters an error condition.
     * @throws  MissingArgumentsException
     *          thrown if insufficient arguments were provided.
     */
    void parseInput(String input) throws CommandException, MissingArgumentsException;

    /**
     * Hook in which to save the persistent settings to storage.
     */
    void saveSettings();

    /**
     * Sets a command alias. If the command argument is null, any existing
     * alias by the given name will be removed. If the named alias already
     * exists, it will be redefined to the new command.
     *
     * @param  name  name of alias.
     * @param  cmnd  aliased command.
     */
    void setAlias(String name, String cmnd);

    /**
     * Set the command history size limit to the new value. The history
     * size will be truncated immediately to the new size.
     *
     * @param  size  new history size.
     */
    void setHistorySize(int size);

    /**
     * Set the writer to which command messages are written.
     *
     * @param  writer  command output writer.
     */
    void setOutput(PrintWriter writer);
}
