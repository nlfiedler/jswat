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
 * are Copyright (C) 1999-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.ThreadReference;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 * Class DefaultCommandParser provides a default implementation of
 * CommandParser.
 *
 * @author  Nathan Fiedler
 */
public class DefaultCommandParser extends AbstractCommandParser {

    /** Logger for gracefully reporting unexpected errors. */
    private static final Logger logger = Logger.getLogger(
            DefaultCommandParser.class.getName());
    /** Result of looking up the available input processors. */
    private Lookup.Result<InputProcessor> processorLookupResult;
    /** Synchronize on this to access inputProcessors reference. */
    private final Object inputProcessorsLock;
    /** List of the available input processors. */
    private Collection<InputProcessor> inputProcessors;
    /** Result of looking up the available commands. */
    private Lookup.Result<Command> commandLookupResult;
    /** Synchronize on this to access loadedCommands and commandMap. */
    private final Object commandsLock;
    /** Collection of the available Command instances. */
    private Collection<Command> loadedCommands;
    /** Map of Command instances keyed by their name. */
    private Map<String, Command> commandMap;

    /**
     * Creates a new instance of DefaultCommandParser.
     */
    public DefaultCommandParser() {
        inputProcessorsLock = new Object();
        Lookup.Template<InputProcessor> templ =
                new Lookup.Template<InputProcessor>(InputProcessor.class);
        processorLookupResult = Lookup.getDefault().lookup(templ);
        processorLookupResult.addLookupListener(new InputProcessLookupListener());
        rebuildProcessors();

        commandsLock = new Object();
        Lookup.Template<Command> ctempl = new Lookup.Template<Command>(Command.class);
        commandLookupResult = Lookup.getDefault().lookup(ctempl);
        commandLookupResult.addLookupListener(new CommandLookupListener());
        rebuildCommands();
    }

    /**
     * Search for a command given the user input. The input may be the
     * unique prefix to a command name (e.g. "vmi" for "vminfo").
     *
     * @param  input  user input used to find command.
     * @return  command, or null if not found.
     * @throws  AmbiguousMatchException
     *          if the input matched more than one Command.
     */
    @Override
    public Command findCommand(String input) throws AmbiguousMatchException {
        int index = input.indexOf(' ');
        String name = null;
        if (index > 0) {
            name = input.substring(0, index);
        } else {
            name = input;
        }
        Command command = getCommand(name);
        if (command == null) {
            // Check for a command whose name starts with 'name'.
            String match = null;
            Iterator<String> iter;
            synchronized (commandsLock) {
                iter = commandMap.keySet().iterator();
            }
            StringBuilder sb = null;
            while (iter.hasNext()) {
                String nm = iter.next();
                if (nm.startsWith(name)) {
                    if (match != null) {
                        // There is more than one matching command.
                        if (sb == null) {
                            sb = new StringBuilder(match);
                        }
                        sb.append(", ");
                        sb.append(nm);
                    } else {
                        match = nm;
                    }
                }
            }

            if (sb != null) {
                throw new AmbiguousMatchException(NbBundle.getMessage(
                        getClass(), "CTL_CommandParser_AmbiguousPrefix",
                        sb.toString()));
            }
            if (match != null) {
                synchronized (commandsLock) {
                    command = commandMap.get(match);
                }
            }
        }
        return command;
    }

    @Override
    public Command getCommand(String name) {
        synchronized (commandsLock) {
            return commandMap.get(name);
        }
    }

    @Override
    public Iterator<Command> getCommands() {
        synchronized (commandsLock) {
            return loadedCommands.iterator();
        }
    }

    @Override
    public void parseInput(String inputStr)
            throws CommandException, MissingArgumentsException {
        String input = inputStr.trim();
        // Check for trivial input, which is always ignored.
        if (input.length() == 0) {
            return;
        }

        // Reset the current history so that processors (e.g. !!) can
        // get the last command entered by the user.
        resetCurrentHistory();

        // Invoke the input processors to massage the user input until
        // it has resolved down to a plain command and its arguments.
        List<String> processedInput = new ArrayList<String>(1);
        processedInput.add(input);
        boolean reprocess = true;
        while (reprocess) {
            // Now that we are in the loop, treat it as the last time
            // through, unless the input gets modified by a processor.
            reprocess = false;
            int pindex = 0;
            while (pindex < processedInput.size()) {
                String pinput = processedInput.get(pindex).trim();
                Iterator<InputProcessor> procs;
                synchronized (inputProcessorsLock) {
                    procs = inputProcessors.iterator();
                }
                while (procs.hasNext()) {
                    InputProcessor proc = procs.next();
                    if (proc.canProcess(pinput, this)) {
                        List<String> ninput = proc.process(pinput, this);
                        // Replace current line with new input.
                        processedInput.remove(pindex);
                        processedInput.addAll(pindex, ninput);
                        if (proc.expandsInput()) {
                            // The input to be saved to the history needs to
                            // be replaced with the new value.
                            input = ninput.get(0);
                        }
                        // The input has changed, need to process over again.
                        reprocess = true;
                        pindex = processedInput.size();
                        break;
                    }
                }
                pindex++;
            }
        }

        // Always add the input to the history, and add it now before the
        // command has a chance to throw an exception.
        addHistory(input);

        // The processed input should now consist of plain commands and
        // their arguments -- find the commands and invoke them.
        Iterator<String> commands = processedInput.iterator();
        while (commands.hasNext()) {
            String cinput = commands.next();
            CommandArguments args = new CommandArguments(cinput);
            PrintWriter writer = getOutput();
            String name = args.nextToken();
            Command command = null;
            try {
                // The first token is going to be the command name.
                command = findCommand(name);
            } catch (AmbiguousMatchException ame) {
                throw new CommandException(ame.getMessage());
            }
            if (command != null) {
                if (command.requiresArguments() && !args.hasMoreTokens()) {
                    throw new MissingArgumentsException();
                }
                Session session = SessionProvider.getCurrentSession();
                if (command.requiresDebuggee() && !session.isConnected()) {
                    throw new CommandException(NbBundle.getMessage(getClass(),
                            "ERR_NotConnected"));
                }
                CommandContext context = new CommandContext(session, writer, this);
                if (command.requiresThread()) {
                    DebuggingContext dc = context.getDebuggingContext();
                    ThreadReference tr = dc.getThread();
                    if (tr == null || !tr.isSuspended()) {
                        throw new CommandException(NbBundle.getMessage(
                                getClass(), "ERR_NoThread"));
                    }
                }
                try {
                    command.perform(context, args);
                } catch (MissingArgumentsException mae) {
                    throw mae;
                } catch (CommandException ce) {
                    throw ce;
                } catch (Exception e) {
                    // Everything else, report it and encapsulate it.
                    logger.log(Level.SEVERE, null, e);
                    throw new CommandException(e.toString());
                }
            } else {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "CTL_CommandParser_CommandNotFound", name));
            }
        }

        // Reset the current history now that we're done.
        resetCurrentHistory();
    }

    /**
     * Rebuilds the Command-related collections.
     */
    @SuppressWarnings("unchecked")
    private void rebuildCommands() {
        synchronized (commandsLock) {
            loadedCommands = (Collection<Command>) commandLookupResult.allInstances();
            commandMap = new HashMap<String, Command>();
            Iterator<Command> iter = loadedCommands.iterator();
            while (iter.hasNext()) {
                Command command = iter.next();
                String name = command.getName();
                commandMap.put(name, command);
            }
        }
    }

    /**
     * Rebuilds the InputProcessor-related collections.
     */
    @SuppressWarnings("unchecked")
    private void rebuildProcessors() {
        synchronized (inputProcessorsLock) {
            inputProcessors = (Collection<InputProcessor>) processorLookupResult.allInstances();
        }
    }

    /**
     * Listens to the commands lookup result for changes.
     *
     * @author  Nathan Fiedler
     */
    private class CommandLookupListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent event) {
            synchronized (commandsLock) {
                rebuildCommands();
            }
        }
    }

    /**
     * Listens to the input processors lookup result for changes.
     *
     * @author  Nathan Fiedler
     */
    private class InputProcessLookupListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent event) {
            synchronized (inputProcessorsLock) {
                rebuildProcessors();
            }
        }
    }
}
