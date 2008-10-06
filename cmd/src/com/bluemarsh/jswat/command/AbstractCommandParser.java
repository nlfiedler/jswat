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
 * are Copyright (C) 1999-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AbstractCommandParser.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
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
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 * Class AbstractCommandParser provides a partial implementation of a
 * CommandParser, for use by concrete implementations.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractCommandParser implements CommandParser {
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

    public String getAlias(String name) {
        return aliasMap.get(name);
    }

    /**
     * Returns an iterator of the names of all the available command aliases.
     *
     * @return  iterator of alias names.
     */
    public Iterator<String> getAliases() {
        return aliasMap.keySet().iterator();
    }

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

    /**
     * Returns the writer to which commands send their output.
     *
     * @return  output writer.
     */
    protected PrintWriter getOutput() {
        return outputWriter;
    }

    @SuppressWarnings("unchecked")
    public void loadSettings() {
        XMLDecoder decoder = null;
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = "commands.xml";
            FileObject fo = fs.findResource(name);
            if (fo != null && fo.isData()) {
                InputStream is = fo.getInputStream();
                decoder = new XMLDecoder(is);
                decoder.setExceptionListener(new ExceptionListener() {
                    public void exceptionThrown(Exception e) {
                        ErrorManager.getDefault().notify(e);
                    }
                });
                aliasMap = (Map<String, String>) decoder.readObject();
                inputHistory = (List<String>) decoder.readObject();
                Integer size = (Integer) decoder.readObject();
                setHistorySize(size.intValue());
            }
        } catch (Exception e) {
            // Parser, I/O, and various runtime exceptions may occur,
            // need to report them and gracefully recover.
            ErrorManager.getDefault().notify(e);
        } finally {
            if (decoder != null) {
                decoder.close();
            }
        }
    }

    public void saveSettings() {
        FileLock lock = null;
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = "commands.xml";
            FileObject fo = fs.findResource(name);
            if (fo == null) {
                fo = fs.getRoot().createData(name);
            }
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(aliasMap);
            encoder.writeObject(inputHistory);
            encoder.writeObject(new Integer(historySizeLimit));
            encoder.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        } finally {
            if (lock != null) lock.releaseLock();
        }
    }

    public void setAlias(String name, String cmnd) {
        if (cmnd == null) {
            aliasMap.remove(name);
        } else {
            aliasMap.put(name, cmnd);
        }
    }

    /**
     * Resets the current history index to the end of the history.
     */
    protected void resetCurrentHistory() {
        currentHistory = -1;
    }

    public void setHistorySize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        historySizeLimit = size;
    }

    public void setOutput(PrintWriter writer) {
        outputWriter = writer;
    }
}
