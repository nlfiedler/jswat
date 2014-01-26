/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: CommandManager.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.command.JSwatCommand;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * Class CommandManager is responsible for parsing text-based commands
 * entered by the user. It breaks up the input into tokens and
 * determines which command is to be executed. It then calls on
 * the appropriate command object to perform the action.
 *
 * @author  Nathan Fiedler
 */
public class CommandManager extends DefaultManager {
    /** Table of commands keyed by their action command string. */
    protected Hashtable commandList;
    /** Sorted list of all command names. */
    protected ArrayList commandNames;
    /** Table of command aliases, keyed by their alias name. */
    protected Hashtable aliasList;
    /** Log to write to when commands perform their tasks or
     * have errors to report. */
    protected Log outputLog;
    /** JSwat session that we're associated with. This is passed
     * to the commands to assist them in performing their tasks. */
    protected Session owningSession;
    /** List of commands executed by the user, stored in order. */
    protected LinkedList historyChain;
    /** Index into historyChain indicating current old command
     * being examined by the user. */
    protected int currentHistory;
    /** Command object that wishes to grab the next input.
     * Set by the <code>grabInput()</code> method and reset
     * by <code>handleInputGrab()</code>. */
    protected JSwatCommand grabInputCommand;
    /** Reference to the manager of macros. */
    protected MacroManager macroManager;

    /**
     * Constructs a CommandManager with the default input field.
     */
    public CommandManager() {
        commandList = new Hashtable();
        commandNames = new ArrayList(50);
        aliasList = new Hashtable();
        historyChain = new LinkedList();
    } // CommandManager

    /**
     * Check for a possible matching command prefix. That is,
     * using the input, find a command whose name starts with
     * the given input.
     *
     * @param  input  possible command prefix to look for.
     * @return  name of command that matches prefix, or null if none.
     * @exception  AmbiguousMatchException
     *             Thrown if the input matched more than one command.
     */
    protected String checkPrefix(String input) throws AmbiguousMatchException {
        // Search for the first possible match. Note the command
        // name list should be in sorted order. That way we can
        // easily check for more possible matches later.
        int index = 0;
        String firstMatch = null;
        while (index < commandNames.size()) {
            String match = (String) commandNames.get(index);
            // Increment right away, so we don't have to down below.
            index++;
            if (match.startsWith(input)) {
                firstMatch = match;
                break;
            }
        }

        if (firstMatch != null) {
            // Check for more than one possible match.
            // Index is already plus'd one (from above).
            if (index < commandNames.size()) {
                String more = (String) commandNames.get(index);
                if (more.startsWith(input)) {
                    // Ambiguous match!
                    StringBuffer buf = new StringBuffer
                        (Bundle.getString("CommandManager.ambiguousMatch"));
                    buf.append(' ');
                    buf.append(firstMatch);
                    while (index < commandNames.size()) {
                        more = (String) commandNames.get(index);
                        if (more.startsWith(input)) {
                            buf.append(", ");
                            buf.append(more);
                        }
                        index++;
                    }
                    throw new AmbiguousMatchException(buf.toString());
                }
            }
            // Single match, return the whole command name.
            return firstMatch;

        } else {
            // No matching command name was found.
            return null;
        }
    } // checkPrefix

    /**
     * Creates a command alias. Subsequent uses of the alias
     * name will result in executing the matching command.
     *
     * @param  name  Name of new alias.
     * @param  cmnd  Command string to alias.
     */
    public void createAlias(String name, String cmnd) {
        // Add the command alias to the list.
        aliasList.put(name, cmnd);
    } // createAlias

    /**
     * Displays a list of the commands in the history chain to
     * the main message window.
     */
    public void displayHistory() {
        int size = historyChain.size();
        if (size <= 0) {
            outputLog.writeln(Bundle.getString("CommandManager.noHistory"));
        } else {
            StringBuffer buf = new StringBuffer
                (Bundle.getString("CommandManager.historyHeader"));
            buf.append('\n');
            for (int i = 0; i < size; i++) {
                buf.append(i + 1);
                buf.append(". ");
                buf.append((String) historyChain.get(i));
                buf.append('\n');
            }
            outputLog.write(buf.toString());
        }
    } // displayHistory

    /**
     * Find the class of the command, given the command name.
     *
     * @param  commandName  Name of command to find.
     * @return  Command class, or null if error.
     */
    protected Class findCommand(String commandName) {
        Class commandClass = null;
        try {
            commandClass = Class.forName("com.bluemarsh.jswat.command." +
                                         commandName + "Command");
        } catch (ClassNotFoundException cnfe) {
            // This is the expected exception.
        } catch (NoClassDefFoundError ncdfe) {
            // This is what happens on Win32.
        }
        return commandClass;
    } // findCommand

    /**
     * Returns the definition of the named alias.
     *
     * @param  name  alias name.
     * @return  alias value, or null if no alias defined by that name.
     */
    public String getAlias(String name) {
        return (String) aliasList.get(name);
    } // getAlias

    /**
     * Fetches an instance of the command by the given name. If the
     * command object does not exist, it will be instantiated. This
     * method will display errors to the screen, if they occur.
     *
     * @param  commandName  Name of command to fetch.
     * @return  Command object, or null if error.
     */
    public JSwatCommand getCommand(String commandName) {
        return getCommand(commandName, true);
    } // getCommand

    /**
     * Fetches an instance of the command by the given name.
     * If the command object does not exist, it will be
     * instantiated.
     *
     * @param  commandName    Name of command to fetch.
     * @param  displayErrors  True to display errors if they occur;
     *                        otherwise be silent and simply return null.
     * @return  Command object, or null if error.
     */
    public JSwatCommand getCommand(String commandName, boolean displayErrors) {
        JSwatCommand command = (JSwatCommand) commandList.get(commandName);
        if (command == null) {
            // Instantiate command object if it doesn't currently exist.
            Class commandClass = findCommand(commandName);
            if (commandClass == null) {
                // User probably typed the name incorrectly.
                if (displayErrors) {
                    StringBuffer buf = new StringBuffer(
                        Bundle.getString("CommandManager.commandNotFound"));
                    buf.append(' ');
                    buf.append(commandName);
                    buf.append('\n');
                    buf.append(Bundle.getString(
                        "CommandManager.useHelpCommands"));
                    outputLog.writeln(buf.toString());
                }
                return null;
            }

            try {
                command = (JSwatCommand) commandClass.newInstance();
            } catch (Exception e) {
                // This should not happen, but if it does we need to know.
                StringBuffer buf = new StringBuffer(
                    Bundle.getString(
                        "CommandManager.cantInstantiateCommand"));
                buf.append(' ');
                buf.append(commandName);
                outputLog.writeln(buf.toString());
                return null;
            }

            // Store command in the table for later recall.
            commandList.put(commandName, command);
            // Add command name to name list for later reference.
            int index = Collections.binarySearch(commandNames, commandName);
            if (index < 0) {
                // Element not found in the list, add it in the
                // sorted position within the list.
                index = -index;
                if (index > commandNames.size()) {
                    index = commandNames.size();
                }
                commandNames.add(index, commandName);
            }
        }
        return command;
    } // getCommand

    /**
     * Retrieves the command following the current position within
     * the command history. If null is returned, that indicates
     * that the current position is at the end of the history.
     *
     * @return  next command in history, or null if none.
     */
    public String getHistoryNext() {
        if (currentHistory < (historyChain.size() - 1)) {
            currentHistory++;
            return (String) historyChain.get(currentHistory);
        } else {
            // Reached the bottom of the history.
            currentHistory = historyChain.size();
            return null;
        }
    } // getHistoryNext

    /**
     * Retrieves the command preceeding the current position within
     * the command history. If null is returned, that indicates
     * that the current position is at the start of the history.
     *
     * @return  previous command in history, or null if none.
     */
    public String getHistoryPrev() {
        if (currentHistory > 0) {
            currentHistory--;
            return (String) historyChain.get(currentHistory);
        } else {
            // Reached the top of the history.
            currentHistory = 0;
            return null;
        }
    } // getHistoryPrev

    /**
     * Tell the CommandManager that the given command should be
     * passed the next input from the user. The input will not
     * be processed by the CommandManager. The CommandManger
     * will call the <code>receiveInput()</code> method of the
     * command. Subsequent user input will be processed normally.
     */
    public void grabInput(JSwatCommand command) {
        grabInputCommand = command;
    } // grabInput

    /**
     * Get the prompt string that should be used for input from the
     * user.  This will change when a specific command has grabbed
     * the input focus.  Returning null means the default should be used.
     *
     * @return String to use for a prompt, or null to use the default
     */
    public String getInputPrompt() {
        if (grabInputCommand != null) {
            return grabInputCommand.getPromptString();
        } else {
            return null;
        }
    } // getInputPrompt

    /**
     * Parse the input and execute the command. The given input
     * will also be added to the command history. The command
     * input may start with an exclamation mark (!) and will be
     * processed appropriately.
     *
     * @param  inputStr  command input.
     */
    public void handleInput(String inputStr) {
        // Handle the input grab, if any.
        if (!handleInputGrab(inputStr)) {

            // Handle the '!' command specially.
            if ((inputStr.charAt(0) == '!') && (inputStr.length() > 1)) {
                // Have to handle the ! operator specially.
                // If a match was found, execute the new input.
                String newInputStr = parseBang(inputStr);
                if (newInputStr != null) {
                    inputStr = newInputStr;
                } else {
                    outputLog.writeln
                        (Bundle.getString("CommandManager.historyNotFound"));
                }
            } else {
                // Add the command to this history list. Note we do
                // this only for commands manually input by the user,
                // not commands from a command script or invoked via !.
                String prev = null;
                if (currentHistory > 0) {
                    prev = (String) historyChain.get(historyChain.size() - 1);
                }
                if (!inputStr.equals(prev)) {
                    historyChain.addLast(inputStr);
                }
            }

            // Parse the command input and perform the appropriate action.
            parseInput(inputStr);

            // Reset the history pointer to the end of the chain.
            currentHistory = historyChain.size();
        }
    } // handleInput

    /**
     * Handle the input grab of some arbitrary command. This will
     * check if an input grab is in progress, call the interested
     * command, and return true. Otherwise, it returns false and
     * does nothing.
     *
     * @param  inputStr  Command input string.
     * @return  True if input was grabbed, false otherwise.
     */
    protected boolean handleInputGrab(String inputStr) {
        if (grabInputCommand != null) {
            // Save this first.
            JSwatCommand grabber = grabInputCommand;
            // Reset the command so we process input normally.
            grabInputCommand = null;
            // A command wants to receive this input.
            grabber.receiveInput(owningSession, outputLog, this, inputStr);
            // Don't clear the grabInputCommand now, the command's
            // receiveInput method may have just set it again.
            return true;
        } else {
            return false;
        }
    } // handleInputGrab

    /**
     * Called after the Session has instantiated this mananger.
     * To avoid problems with circular dependencies between managers,
     * iniitialize data members before calling
     * <code>Session.getManager()</code>.
     *
     * @param  session  Session initializing this manager.
     */
    public void init(Session session) {
        this.owningSession = session;
        outputLog = session.getStatusLog();

        macroManager = (MacroManager) session.getManager(MacroManager.class);

        // Get list of commands from the properties file.
        String commandList = Bundle.getString("commands");
        if ((commandList == null) || (commandList.equals(""))) {
            outputLog.writeln(Bundle.getString
                              ("CommandManager.errorCommandList"));
        } else {
            // Tokenize into array of command names.
            String[] commands = StringUtils.tokenize(commandList);
            // Load each command.
            for (int i = 0; i < commands.length; i++) {
                getCommand(commands[i]);
            }
        }
    } // init

    /**
     * List all of the created command aliases to the main
     * message window.
     */
    public void listAliases() {
        if (aliasList.isEmpty()) {
            outputLog.writeln(Bundle.getString("CommandManager.noAliases"));
            return;
        }

        // Enumerate the keys in the hashtable and store them
        // in another list.
        Enumeration keys = aliasList.keys();
        ArrayList list = new ArrayList();
        while (keys.hasMoreElements()) {
            list.add(keys.nextElement());
        }
        // Sort the list.
        Collections.sort(list);

        // Iterate over the sorted list, show the alias and
        // its associated command.
        Iterator iter = list.iterator();
        StringBuffer buf = new StringBuffer
            (Bundle.getString("CommandManager.listOfAliases"));
        buf.append('\n');
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = (String) aliasList.get(key);
            buf.append('\t');
            buf.append(key);
            buf.append("\t\t");
            buf.append(value);
            buf.append('\n');
        }
        outputLog.writeln(buf.toString());
    } // listAliases

    /**
     * List all of the loaded commands with a brief description of each.
     */
    public void listCommands() {
        // Get list of commands from properties file.
        String commandList = Bundle.getString("commands");
        if ((commandList == null) || (commandList.equals(""))) {
            outputLog.writeln(Bundle.getString
                              ("CommandManager.errorCommandList"));
            return;
        }
        // Tokenize into array of command names.
        String[] commands = StringUtils.tokenize(commandList);

        // List the add-on commands. Add-on commands are implemented
        // in separate command classes, in the 'command' package.
        // Load each command and ask for description.
        StringBuffer buf = new StringBuffer
            (Bundle.getString("CommandManager.listOfCommands"));
        buf.append('\n');
        for (int i = 0; i < commands.length; i++) {
            buf.append('\t');
            buf.append(commands[i]);
            if (commands[i].length() >= 8) {
                buf.append('\t');
            } else {
                buf.append("\t\t");
            }
            JSwatCommand command = getCommand(commands[i]);
            if (command != null) {
                buf.append(command.description());
                buf.append('\n');
            } else {
                buf.append(Bundle.getString
                           ("CommandManager.errorCommandLoad"));
                buf.append('\n');
            }
        }
        outputLog.writeln(buf.toString());
    } // listCommands

    /**
     * List all of the created command macros to the main
     * message window.
     */
    public void listMacros() {
        Iterator macros = macroManager.macroNames();
        if (macros == null) {
            outputLog.writeln(Bundle.getString("CommandManager.noMacros"));
            return;
        }

        // Iterate over the sorted list, show the macro name.
        StringBuffer buf = new StringBuffer
            (Bundle.getString("CommandManager.listOfMacros"));
        buf.append('\n');
        while (macros.hasNext()) {
            buf.append('\t');
            String name = (String) macros.next();
            buf.append(name);
            buf.append('\n');
        }
        outputLog.writeln(buf.toString());
    } // listMacros

    /**
     * Handle the '!' and '!!' operators by looking for a matching
     * command in the command history and returning the input string
     * to be executed.
     *
     * @param  inputStr  Command input string.
     * @return  Command input string to execute, or null if no matching
     *          command was found (an error condition).
     */
    protected String parseBang(String inputStr) {
        StringTokenizer args = new StringTokenizer(inputStr);
        // We know there's at least one token.
        String token = args.nextToken();
        // The history expanded command to run.
        String inputToRun = null;
        // We assume first character is a '!'.
        if (token.charAt(1) == '!') {
            try {
                // Run the last entered command.
                inputToRun = (String) historyChain.getLast();
            } catch (NoSuchElementException nsee) {
                // No previous command, null will be returned.
            }
        } else {

            // Take the token without the '!' prefix and go
            // through the list looking for a matching command.
            token = token.substring(1);
            int size = historyChain.size();
            for (int i = size - 1; i >= 0; i--) {
                String oldInput = (String) historyChain.get(i);
                if (oldInput.startsWith(token)) {
                    // Found an old command whose prefix matches
                    // the token from the user.
                    // Build out the rest of the command arguments.
                    StringBuffer newCommand = new StringBuffer(oldInput);
                    if (args.hasMoreTokens()) {
                        newCommand.append(' ');
                        newCommand.append(args.rest(true));
                    }
                    inputToRun = newCommand.toString();
                    break;
                }
                // No previous command found, null will be returned.
            }
        }
        return inputToRun;
    } // parseBang

    /**
     * Parse the command input string and perform the appropriate
     * action. Handles command aliases and macros. Does not handle
     * the ! or !! operators, nor is the command inserted into the
     * command history.
     *
     * @param  inputStr  Input command string.
     */
    public void parseInput(String inputStr) {
        StringTokenizer tokenizer = new StringTokenizer(inputStr);
        if (!tokenizer.hasMoreTokens()) {
            // Blank command line.
            return;
        }

        // See if the first token is a number.
        String firstToken = tokenizer.nextToken();
        String commandName;
        int nvalue;
        try {
            // First token was a number, we'll execute the rest
            // of the line that number of times.
            nvalue = Integer.parseInt(firstToken);
            commandName = tokenizer.nextToken();
        } catch (NumberFormatException nfe) {
            // First token was not a number, evaluate as usual.
            nvalue = 1;
            commandName = firstToken;
        }

        // Check for command aliases.
        String alias = (String) aliasList.get(commandName);
        StringBuffer newCommand;
        if ((alias != null) && (alias.length() > 0)) {
            // Command is in fact an alias. Use the alias command.
            newCommand = new StringBuffer(alias);
        } else {
            // Not an alias, use the given command.
            newCommand = new StringBuffer(commandName);
        }

        // Build up the rest of the command input.
        if (tokenizer.hasMoreTokens()) {
            newCommand.append(' ');
            newCommand.append(tokenizer.rest(true));
        }
        // Set the new command input to run.
        inputStr = newCommand.toString();

        // Run the basic command interpreter nvalue times.
        while (nvalue > 0) {
            runCommand(inputStr);
            nvalue--;
        }
    } // parseInput

    /**
     * Remove the specified alias.
     *
     * @param  alias  Name of alias to remove.
     */
    public void removeAlias(String alias) {
        // Try to remove the alias.
        if (aliasList.remove(alias) == null) {
            outputLog.writeln(Bundle.getString
                              ("CommandManager.aliasNotFound"));
        }
    } // removeAlias

    /**
     * Runs the given command input, without handling '!' or
     * command aliases. This does, however, handle macros.
     *
     * @param  inputStr  Original command input string.
     */
    protected void runCommand(String inputStr) {
        StringTokenizer tokenizer = new StringTokenizer(inputStr);
        String commandName = tokenizer.nextToken();

        // Silently try to find the command in various places.
        if (Collections.binarySearch(commandNames, commandName) < 0) {
            if (findCommand(commandName) == null) {
                // Command does not exist anywhere.

                // See if the input matches a macro name.
                Vector macros = (Vector) macroManager.getMacro(commandName);
                if (macros != null) {
                    int size = macros.size();
                    for (int i = 0; i < size; i++) {
                        // Run each command in the macro.
                        parseInput((String) macros.get(i));
                    }
                    // Done with the macro, exit immediately.
                    return;
                }

                // See if the input matches a unique command prefix.
                try {
                    String result = checkPrefix(commandName);
                    if (result != null) {
                        commandName = result;
                    }
                } catch (AmbiguousMatchException ame) {
                    // Show the message and return.
                    outputLog.writeln(ame.getMessage());
                    return;
                }
            }
        }

        // Load the command, possibly instantiating it.
        JSwatCommand command = getCommand(commandName);
        // Call the perform() method of the command object.
        if (command != null) {
            try {
                command.perform(owningSession, tokenizer, outputLog);
            } catch (Exception e) {
                // Catch any and all exceptions and print them.
                outputLog.writeln(Bundle.getString
                                  ("CommandManager.errorRunningCommand"));
                outputLog.writeStackTrace(e);
            }
        }
    } // runCommand

    /**
     * Read the lines from the given file and parse each line as
     * if it were a command.
     *
     * @param  filepath  Path and filename of script file.
     */
    public void runScript(String filepath) {
        FileReader fr;
        try {
            fr = new FileReader(filepath);
        } catch (FileNotFoundException fnfe) {
            StringBuffer buf = new StringBuffer
                (swat.getResourceString("fileNotFound"));
            buf.append(": ");
            buf.append(filepath);
            outputLog.writeln(buf.toString());
            return;
        }

        // Read the script file line by line and parse it.
        outputLog.writeln(Bundle.getString
                          ("CommandManager.parsingScript"));
        BufferedReader br = new BufferedReader(fr);
        try {
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() > 0) {
                    char firstChar = line.charAt(0);
                    // Skip over comment lines.
                    if (firstChar != '#') {
                        if (firstChar != '!') {
                            // Handle input grab.
                            if (!handleInputGrab(line)) {
                                // Parse the input line.
                                parseInput(line);
                            }
                        } else {
                            // We can't allow ! usage when the commands
                            // are not being saved to the history chain.
                            outputLog.writeln
                                (swat.getResourceString("bangNotAllowed"));
                        }
                    }
                }
                line = br.readLine();
            }
            br.close();

        } catch (IOException ioe) {
            StringBuffer buf = new StringBuffer
                (Bundle.getString("CommandManager.errorReadingFile"));
            buf.append(' ');
            buf.append(filepath);
            outputLog.writeln(buf.toString());
        }
    } // runScript

    /**
     * Class AmbiguousMatchException is an internal exception for use
     * only by the CommandManager.
     */
    class AmbiguousMatchException extends Exception {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a AmbiguousMatchException with no message.
         */
        public AmbiguousMatchException() {
            super();
        } // AmbiguousMatchException

        /**
         * Constructs a AmbiguousMatchException with the given message.
         *
         * @param  msg  message.
         */
        public AmbiguousMatchException(String msg) {
            super(msg);
        } // AmbiguousMatchException
    } // AmbiguousMatchException
} // CommandManager
