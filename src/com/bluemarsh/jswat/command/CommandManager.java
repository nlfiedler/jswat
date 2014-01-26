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
 * $Id: CommandManager.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Manager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Strings;
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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Class CommandManager is responsible for interpreting text-based
 * commands entered by the user. It breaks up the input into tokens and
 * determines which command is to be executed. It then calls on the
 * appropriate command object to perform the action.
 *
 * @author  Nathan Fiedler
 */
public class CommandManager implements Manager {
    /** Separate of multiple commands in a single input string. */
    private static final char COMMAND_SEPARATOR_CHAR = ';';
    /** Table of commands keyed by their action command string. */
    private Hashtable commandTable;
    /** Sorted list of all command names. */
    private ArrayList commandNames;
    /** Table of command aliases, keyed by their alias name. */
    private Hashtable aliasTable;
    /** Log to write to when commands perform their tasks or
     * have errors to report. */
    private Log outputLog;
    /** JSwat session that we're associated with. This is passed
     * to the commands to assist them in performing their tasks. */
    private Session owningSession;
    /** List of commands executed by the user, stored in order. */
    private LinkedList historyChain;
    /** Maximum number of commands to store in history. */
    private int historySizeLimit;
    /** Index into historyChain indicating current old command
     * being examined by the user. */
    private int currentHistory;
    /** Command object that wishes to grab the next input.
     * Set by the <code>grabInput()</code> method and reset
     * by <code>handleInputGrab()</code>. */
    private JSwatCommand grabInputCommand;

    /**
     * Constructs a CommandManager with the default input field.
     */
    public CommandManager() {
        commandTable = new Hashtable();
        commandNames = new ArrayList(50);
        aliasTable = new Hashtable();
        historyChain = new LinkedList();
        historySizeLimit = 50;
    } // CommandManager

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
    } // activated

    /**
     * Check for a possible matching command prefix. That is, using the
     * input, find a command whose name starts with the given input.
     *
     * @param  input  possible command prefix to look for.
     * @return  name of command that matches prefix, or null if none.
     * @throws  AmbiguousMatchException
     *          if the input matched more than one command.
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
                    StringBuffer buf = new StringBuffer(
                        Bundle.getString("CommandManager.ambiguousMatch"));
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
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/aliases");
        // Clear out all the persisted aliases.
        try {
            prefs.clear();
        } catch (BackingStoreException bse) {
            // Just overwrite the entries.
        }
        // Store the aliases to the persistent store.
        Enumeration aliases = aliasTable.keys();
        while (aliases.hasMoreElements()) {
            String key = (String) aliases.nextElement();
            String val = (String) aliasTable.get(key);
            prefs.put(key, val);
        }
    } // closing

    /**
     * Creates a command alias. Subsequent uses of the alias name will
     * result in executing the matching command.
     *
     * @param  name  Name of new alias.
     * @param  cmnd  Command string to alias.
     */
    public void createAlias(String name, String cmnd) {
        // Add the command alias to the list.
        aliasTable.put(name, cmnd);
    } // createAlias

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
    } // deactivated

    /**
     * Displays a list of the commands in the history chain to
     * the main message window.
     */
    public void displayHistory() {
        int size = historyChain.size();
        if (size <= 0) {
            outputLog.writeln(Bundle.getString("CommandManager.noHistory"));
        } else {
            StringBuffer buf = new StringBuffer(
                Bundle.getString("CommandManager.historyHeader"));
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
     * Returns the definition of the named alias.
     *
     * @param  name  alias name.
     * @return  alias value, or null if no alias defined by that name.
     */
    public String getAlias(String name) {
        return (String) aliasTable.get(name);
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
     * Fetches an instance of the command by the given name. If the
     * command object does not exist, it will be instantiated.
     *
     * @param  commandName    Name of command to fetch.
     * @param  displayErrors  True to display errors if they occur;
     *                        otherwise be silent and simply return null.
     * @return  Command object, or null if error.
     */
    public JSwatCommand getCommand(String commandName, boolean displayErrors) {
        JSwatCommand command = (JSwatCommand) commandTable.get(commandName);
        if (command == null) {
            // Instantiate command object if it doesn't currently exist.
            Class commandClass = loadCommand(commandName);
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
            commandTable.put(commandName, command);
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
     * Returns an array of the names of the known commands.
     *
     * @return  array of known command names.
     */
    public String[] getCommandNames() {
        return (String[]) commandNames.toArray(
            new String[commandNames.size()]);
    } // getCommandNames

    /**
     * Retrieves the command following the current position within the
     * command history. If null is returned, that indicates that the
     * current position is at the end of the history.
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
     * Retrieves the command preceeding the current position within the
     * command history. If null is returned, that indicates that the
     * current position is at the start of the history.
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
     * Tell the CommandManager that the given command should be passed
     * the next input from the user. The input will not be processed by
     * the CommandManager. The CommandManger will call the
     * <code>receiveInput()</code> method of the command. Subsequent
     * user input will be processed normally.
     *
     * @param  command  the command grabbing input.
     */
    public void grabInput(JSwatCommand command) {
        grabInputCommand = command;
        owningSession.getUIAdapter().updateInputPrompt(
            command.getPromptString());
    } // grabInput

    /**
     * Parse the input and execute the command. The given input will
     * also be added to the command history. The command input may start
     * with an exclamation mark (!) and will be processed appropriately.
     *
     * <p>This method may throw any runtime exception, and they should
     * be caught and handled appropriately.</p>
     *
     * @param  inputStr  command input.
     */
    public void handleInput(String inputStr) {
        // Handle the input grab, if any.
        if (!handleInputGrab(inputStr)) {

            if (inputStr.length() == 0) {
                // Blank line; should only happen in unit testing.
                return;
            }

            // Handle the '!' command specially.
            if ((inputStr.charAt(0) == '!') && (inputStr.length() > 1)) {
                // Have to handle the ! operator specially.
                // If a match was found, execute the new input.
                String newInputStr = parseBang(inputStr);
                if (newInputStr != null) {
                    inputStr = newInputStr;
                } else {
                    outputLog.writeln(
                        Bundle.getString("CommandManager.historyNotFound"));
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
                    while (historyChain.size() > historySizeLimit) {
                        historyChain.removeFirst();
                    }
                }
            }

            // Parse the command input and perform the appropriate action.
            parseInput(inputStr);

            // Reset the history pointer to the end of the chain.
            currentHistory = historyChain.size();
        }
    } // handleInput

    /**
     * Handle the input grab of some arbitrary command. This will check
     * if an input grab is in progress, call the interested command, and
     * return true. Otherwise, it returns false and does nothing.
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
            // Reset the command input prompt.
            owningSession.getUIAdapter().updateInputPrompt(null);
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
     * List all of the created command aliases to the main message
     * window.
     */
    public void listAliases() {
        if (aliasTable.isEmpty()) {
            outputLog.writeln(Bundle.getString("CommandManager.noAliases"));
            return;
        }

        // Enumerate the keys in the hashtable and store them
        // in another list.
        Enumeration keys = aliasTable.keys();
        ArrayList list = new ArrayList();
        while (keys.hasMoreElements()) {
            list.add(keys.nextElement());
        }
        // Sort the list.
        Collections.sort(list);

        // Iterate over the sorted list, show the alias and
        // its associated command.
        Iterator iter = list.iterator();
        StringBuffer buf = new StringBuffer(
            Bundle.getString("CommandManager.listOfAliases"));
        buf.append('\n');
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = (String) aliasTable.get(key);
            buf.append("  ");
            buf.append(key);
            // +2 to account for the spaces
            if (key.length() + 2 >= 8) {
                buf.append('\t');
            } else {
                buf.append("\t\t");
            }
            buf.append(value);
            buf.append('\n');
        }
        outputLog.writeln(buf.toString());
    } // listAliases

    /**
     * List all of the loaded commands with a brief description of each.
     */
    public void listCommands() {
        // List the add-on commands. Add-on commands are implemented
        // in separate command classes, in the 'command' package.
        // Load each command and ask for description.
        StringBuffer buf = new StringBuffer(
            Bundle.getString("CommandManager.listOfCommands"));
        buf.append('\n');
        for (int ii = 0; ii < commandNames.size(); ii++) {
            String command = (String) commandNames.get(ii);
            buf.append("  ");
            buf.append(command);
            // +2 to account for the spaces
            if (command.length() + 2 >= 8) {
                buf.append('\t');
            } else {
                buf.append("\t\t");
            }
            JSwatCommand cmd = getCommand(command);
            if (cmd != null) {
                buf.append(cmd.description());
                buf.append('\n');
            } else {
                buf.append(Bundle.getString(
                    "CommandManager.errorCommandLoad"));
                buf.append('\n');
            }
        }
        outputLog.writeln(buf.toString());
    } // listCommands

    /**
     * Find the class of the command, given the command name.
     *
     * @param  cmd  name of command to find.
     * @return  command class, or null if error.
     */
    protected Class loadCommand(String cmd) {
        Class commandClass = null;
        try {
            commandClass = Class.forName("com.bluemarsh.jswat.command."
                                         + cmd + "Command");
        } catch (ClassNotFoundException cnfe) {
            // this is expected
        } catch (NoClassDefFoundError ncdfe) {
            // this is expected
        }
        return commandClass;
    } // loadCommand

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
        owningSession = session;
        outputLog = owningSession.getStatusLog();

        // Get list of commands from the properties file.
        // This primes the command completion list.
        String commandList = Bundle.getString("commandList");
        if (commandList == null || commandList.length() == 0) {
            outputLog.writeln(Bundle.getString(
                "CommandManager.errorCommandList"));
            return;
        }
        // Tokenize into array of command names.
        String[] commands = Strings.tokenize(commandList);
        for (int ii = 0; ii < commands.length; ii++) {
            commandNames.add(commands[ii]);
        }
        Collections.sort(commandNames);

        // Read the aliases from the persistent store.
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/aliases");
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                String val = prefs.get(key, null);
                aliasTable.put(key, val);
            }
        } catch (BackingStoreException bse) {
            owningSession.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING,
                Bundle.getString("CommandManager.errorReadingAliases"));
        }
    } // opened

    /**
     * Handle the '!' and '!!' operators by looking for a matching
     * command in the command history and returning the input string to
     * be executed.
     *
     * @param  inputStr  Command input string.
     * @return  Command input string to execute, or null if no matching
     *          command was found (an error condition).
     */
    protected String parseBang(String inputStr) {
        CommandArguments args = new CommandArguments(inputStr);
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
                        args.returnAsIs(true);
                        newCommand.append(args.rest());
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
     * action. Handles command aliases and multiple commands separated
     * by the special separator character. Does not handle the ! or !!
     * operators, nor is the command inserted into the command history.
     *
     * <p>This method may throw any runtime exception, and they should
     * be caught and handled appropriately.</p>
     *
     * @param  inputStr  Input command string.
     */
    public void parseInput(String inputStr) {
        // See if the separator even exists here.
        if (inputStr.indexOf(COMMAND_SEPARATOR_CHAR) == -1) {
            // There are no separator to speak of.
            parseInputBase(inputStr);
        } else {
            // Could be multiple commands.
            // Make sure we only accept separators that are not
            // inside of a quoted string.
            // Use a simple finite-state machine to parse the input.
            int strlen = inputStr.length();
            int start = 0;
            int index = 0;
            byte state = 0;
            char ch = '\0';
            while (index < strlen) {
                char prevch = ch;
                ch = inputStr.charAt(index);
                switch (state) {
                case 0:
                    // Not inside a quoted string.
                    if (ch == '"') {
                        state = 1;
                    } else if (ch == '\'') {
                        state = 2;
                    } else if (ch == '\\') {
                        state = 3;
                    } else if (ch == COMMAND_SEPARATOR_CHAR) {
                        // Run the command between 'start' and 'index'.
                        String cmd = inputStr.substring(start, index);
                        parseInputBase(cmd);
                        start = index + 1;
                    }
                    break;

                case 1:
                    // Inside a double-quoted string.
                    if (ch == '"') {
                        state = 0;
                    } else if (ch == '\\') {
                        state = 4;
                    }
                    break;

                case 2:
                    // Inside a single-quoted string.
                    if (ch == '\'') {
                        state = 0;
                    } else if (ch == '\\') {
                        state = 5;
                    }
                    break;

                case 3:
                    // Previous character was a slash.
                    // Simply skip the character and move on.
                    state = 0;
                    break;

                case 4:
                    // Previous character was a slash.
                    // Simply skip the character and move on.
                    state = 1;
                    break;

                case 5:
                    // Previous character was a slash.
                    // Simply skip the character and move on.
                    state = 2;
                    break;
                default:
                    throw new IllegalStateException(
                        "confused finite state machine");
                }
                index++;
            }

            // Either we have processed nothing or processed all but
            // the last separated command. Both cases are happily
            // handled in exactly the same manner.
            String cmd = inputStr.substring(start);
            parseInputBase(cmd);
        }
    } // parseInput

    /**
     * Parse the command input string and perform the appropriate
     * action. Handles the multiplier syntax, whereby the command is
     * executed multiple times.
     *
     * @param  inputStr  Input command string.
     */
    protected void parseInputBase(String inputStr) {
        CommandArguments args = new CommandArguments(inputStr);
        if (!args.hasMoreTokens()) {
            // Blank command line.
            return;
        }

        // See if the first token is a number.
        try {
            String firstPart = args.peek();
            int runNtimes = Integer.parseInt(firstPart);
            // First token was a number, consume it.
            args.nextToken();

            // Run the basic command interpreter runNtimes times.
            while (runNtimes > 0) {
                runCommand(args);
                args.reset();
                // Consume the number token.
                args.nextToken();
                runNtimes--;
            }

        } catch (NumberFormatException nfe) {
            // Nope, it is just a regular command string.
            runCommand(args);
        }
    } // parseInputBase

    /**
     * Remove the specified alias.
     *
     * @param  alias  Name of alias to remove.
     */
    public void removeAlias(String alias) {
        // Try to remove the alias.
        if (aliasTable.remove(alias) == null) {
            outputLog.writeln(Bundle.getString(
                "CommandManager.aliasNotFound"));
        }
    } // removeAlias

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Evaluate the command. If the command is one of the built-ins,
     * runs that command. If the command is an alias, calls parseInput()
     * on the expanded command. If the command matches a command prefix,
     * runs that command.
     *
     * @param  args  command and arguments.
     */
    protected void runCommand(CommandArguments args) {
        String commandName = args.nextToken();

        // Silently try to find the command in various places.
        if (Collections.binarySearch(commandNames, commandName) < 0) {
            if (loadCommand(commandName) == null) {
                // Command does not exist anywhere.

                // See if the input matches an alias name.
                String alias = getAlias(commandName);
                if (alias != null) {
                    // Build up the rest of the command input.
                    StringBuffer newCommand = new StringBuffer(alias);
                    if (args.hasMoreTokens()) {
                        newCommand.append(' ');
                        args.returnAsIs(true);
                        newCommand.append(args.rest());
                    }
                    parseInput(newCommand.toString());
                    // Done with alias, exit immediately.
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
        if (command != null) {
            // Call the perform() method of the command object.
            try {
                command.perform(owningSession, args, outputLog);
            } catch (MissingArgumentsException mae) {
                // Special form of command exception.
                if (mae.getMessage() != null) {
                    outputLog.writeln(mae.getMessage());
                } else {
                    StringBuffer buf = new StringBuffer(512);
                    buf.append(Bundle.getString(
                                   "CommandManager.missingArguments"));
                    buf.append('\n');
                    command.help(outputLog, buf);
                }
            } catch (CommandException ce) {
                // We assume CommandException has a localized message.
                String msg = ce.getMessage();
                if (msg != null) {
                    outputLog.writeln(msg);
                } else {
                    outputLog.writeln(Bundle.getString(
                        "CommandManager.errorRunningCommand"));
                    outputLog.writeStackTrace(ce.getCause());
                }
            }
            // Do not catch all other exceptions here. Let the caller
            // handle them appropriately.
        }
    } // runCommand

    /**
     * Read the lines from the given file and parse each line as if it
     * were a command.
     *
     * @param  filepath  Path and filename of script file.
     */
    public void runScript(String filepath) {
        FileReader fr;
        try {
            fr = new FileReader(filepath);
        } catch (FileNotFoundException fnfe) {
            StringBuffer buf = new StringBuffer(
                Bundle.getString("CommandManager.fileNotFound"));
            buf.append(": ");
            buf.append(filepath);
            throw new CommandException(buf.toString());
        }

        // Read the script file line by line and parse it.
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
                            outputLog.writeln(
                                Bundle.getString(
                                    "CommandManager.bangNotAllowed"));
                        }
                    }
                }
                line = br.readLine();
            }
            br.close();

        } catch (IOException ioe) {
            StringBuffer buf = new StringBuffer(
                Bundle.getString("CommandManager.errorReadingFile"));
            buf.append(' ');
            buf.append(filepath);
            throw new CommandException(buf.toString(), ioe);
        }
    } // runScript

    /**
     * Set the command history size limit to the new value. The history
     * size will be truncated when the next command is executed.
     *
     * @param  size  new history size.
     */
    public void setHistorySize(int size) {
        if (size < 0 || size > 1000) {
            throw new IllegalArgumentException("require 0 < size < 1000");
        }
        historySizeLimit = size;
    } // setHistorySize

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended

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
