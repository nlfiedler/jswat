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
 * are Copyright (C) 1999-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.core.PlatformProvider;
import com.bluemarsh.jswat.core.PlatformService;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class AbstractCommandParser provides a partial implementation of a
 * CommandParser, for use by concrete implementations.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractCommandParser implements CommandParser {
    /** Logger for gracefully reporting unexpected errors. */
    private static final Logger logger = Logger.getLogger(
            AbstractCommandParser.class.getName());
    /** Map of command aliases, keyed by their alias name. */
    private Map<String, String> aliasMap;
    /** List of commands executed by the user, stored in order. */
    private List<String> inputHistory;
    /** Maximum number of commands to store in history. */
    private int historySizeLimit;
    /** Index into history of entry currently under consideration by user. */
    private int currentHistory = -1;
    /** Writer to which messages are written. */
    private PrintWriter outputWriter;

    /**
     * Creates a new instance of AbstractCommandParser.
     */
    public AbstractCommandParser() {
        aliasMap = new HashMap<String, String>();
        inputHistory = new LinkedList<String>();
        historySizeLimit = 50;
    }

    /**
     * Adds the given input string to the end of the history.
     *
     * @param  input  command input to add to history.
     */
    protected void addHistory(String input) {
        // Sometimes there is no history to maintain.
        if (historySizeLimit > 0) {
            // Most recent goes to the front of the list.
            // Only add input which is different than the last one.
            if (inputHistory.size() > 0) {
                String last = inputHistory.get(0);
                if (!last.equals(input)) {
                    inputHistory.add(0, input);
                }
            } else {
                inputHistory.add(0, input);
            }
        }
        // Prune the history to the desired size.
        while (inputHistory.size() > historySizeLimit) {
            inputHistory.remove(historySizeLimit);
        }
    }

    @Override
    public String getAlias(String name) {
        return aliasMap.get(name);
    }

    @Override
    public Iterator<String> getAliases() {
        return aliasMap.keySet().iterator();
    }

    @Override
    public Iterator<String> getHistory(boolean reverse) {
        if (reverse) {
            // List is already in reverse order.
            return inputHistory.iterator();
        } else {
            // We have to create a reversed list.
            List<String> reversed = new LinkedList<String>(inputHistory);
            Collections.reverse(reversed);
            return reversed.iterator();
        }
    }

    @Override
    public String getHistoryNext() {
        if (currentHistory > 0) {
            currentHistory--;
            return inputHistory.get(currentHistory);
        } else {
            // Reached the most recent entry.
            currentHistory = -1;
            return null;
        }
    }

    @Override
    public String getHistoryPrev() {
        if (currentHistory < (inputHistory.size() - 1)) {
            currentHistory++;
            return inputHistory.get(currentHistory);
        } else {
            // Reached the oldest entry.
            // Subtract one from the size since we do not want to see the
            // oldest entry twice.
            currentHistory = inputHistory.size() - 1;
            return null;
        }
    }

    @Override
    public PrintWriter getOutput() {
        return outputWriter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadSettings() {
        XMLDecoder decoder = null;
        try {
            PlatformService platform = PlatformProvider.getPlatformService();
            String name = "commands.xml";
            InputStream is = platform.readFile(name);
            decoder = new XMLDecoder(is);
            decoder.setExceptionListener(new ExceptionListener() {
                @Override
                public void exceptionThrown(Exception e) {
                    logger.log(Level.SEVERE, null, e);
                }
            });
            aliasMap = (Map<String, String>) decoder.readObject();
            inputHistory = (List<String>) decoder.readObject();
            Integer size = (Integer) decoder.readObject();
            setHistorySize(size.intValue());
        } catch (FileNotFoundException e) {
            // Do not report this error, it's normal.
        } catch (Exception e) {
            // Parser, I/O, and various runtime exceptions may occur,
            // need to report them and gracefully recover.
            logger.log(Level.SEVERE, null, e);
        } finally {
            if (decoder != null) {
                decoder.close();
            }
        }
    }

    @Override
    public void saveSettings() {
        String name = "commands.xml";
        PlatformService platform = PlatformProvider.getPlatformService();
        try {
            OutputStream os = platform.writeFile(name);
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(aliasMap);
            encoder.writeObject(inputHistory);
            encoder.writeObject(new Integer(historySizeLimit));
            encoder.close();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
        } finally {
            platform.releaseLock(name);
        }
    }

    @Override
    public void setAlias(String name, String cmnd) {
        if (cmnd == null) {
            aliasMap.remove(name);
        } else {
            aliasMap.put(name, cmnd);
        }
    }

    @Override
    public Command findCommand(String input) throws AmbiguousMatchException {
        throw new UnsupportedOperationException();
    }

    /**
     * Resets the current history index to the end of the history.
     */
    protected void resetCurrentHistory() {
        currentHistory = -1;
    }

    @Override
    public void setHistorySize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        historySizeLimit = size;
    }

    @Override
    public void setOutput(PrintWriter writer) {
        outputWriter = writer;
    }
}
