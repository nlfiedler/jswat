/*********************************************************************
 *
 *      Copyright (C) 1999-2003 Nathan Fiedler
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
 * $Id: threadsCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ThreadGroupIterator;
import com.bluemarsh.jswat.util.Threads;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines the class that handles the 'threads' command.
 *
 * @author  Nathan Fiedler
 */
public class threadsCommand extends JSwatCommand {

    /**
     * Perform the 'threads' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        // Get the list of all threads.
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }
        ContextManager conman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference current = conman.getCurrentThread();

        if (args.hasMoreTokens()) {
            long tid = -1;
            String name = args.nextToken();
            // Is it a thread group name or unique ID?
            try {
                tid = Long.parseLong(name);
            } catch (NumberFormatException nfe) {
                // It's not a thread id then.
            }

            List threadsList = null;
            // Look for a thread group with this name or id.
            Iterator iter = new ThreadGroupIterator(
                session.getVM().topLevelThreadGroups());
            if (tid > -1) {
                while (iter.hasNext()) {
                    ThreadGroupReference group =
                        (ThreadGroupReference) iter.next();
                    if (group.uniqueID() == tid) {
                        threadsList = group.threads();
                        break;
                    }
                }

            } else {
                threadsList = new ArrayList();
                Pattern patt = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
                while (iter.hasNext()) {
                    ThreadGroupReference group =
                        (ThreadGroupReference) iter.next();
                    Matcher matcher = patt.matcher(group.name());
                    if (matcher.find()) {
                        threadsList.addAll(group.threads());
                    } else {
                        String idstr = String.valueOf(group.uniqueID());
                        matcher = patt.matcher(idstr);
                        if (matcher.find()) {
                            threadsList.addAll(group.threads());
                        }
                    }
                }
            }

            if (threadsList == null || threadsList.size() == 0) {
                out.writeln(Bundle.getString("threads.noThreadsInGroup"));
            } else if (threadsList.size() > 0) {
                out.write(printThreads(threadsList.iterator(), "  ", current));
            }
        } else {
            // Else, print all of the threads with their parent
            // thread groups, too.
            List topGroups = session.getVM().topLevelThreadGroups();
            if (topGroups == null || topGroups.size() == 0) {
                out.writeln(Bundle.getString("threads.noThreads"));
            } else if (topGroups.size() > 0) {
                Iterator iter = topGroups.iterator();
                while (iter.hasNext()) {
                    ThreadGroupReference group =
                        (ThreadGroupReference) iter.next();
                    printGroup(group, current, out, "");
                }
            }
        }
    } // perform

    /**
     * Print the thread group to the output with each line prefixed
     * by the given string.
     *
     * @param  group    thread group to print.
     * @param  current  current thread.
     * @param  out      place to print to.
     * @param  prefix   string to display before each line.
     */
    protected void printGroup(ThreadGroupReference group,
                              ThreadReference current, Log out,
                              String prefix) {
        // See if the thread group has a class type.
        ReferenceType clazz = group.referenceType();
        String id = String.valueOf(group.uniqueID());
        if (clazz == null) {
            // No class type, show just the ID and group name.
            out.writeln(prefix + id + ' ' + group.name());
        } else {
            // Show ID, group name, and class name.
            out.writeln(prefix + id + ' ' + group.name()
                        + " (" + clazz.name() + ')');
        }

        // Now traverse this group's subgroups.
        List groups = group.threadGroups();
        Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            ThreadGroupReference subgrp = (ThreadGroupReference) iter.next();
            printGroup(subgrp, current, out, prefix + "  ");
        }

        // Print this threadgroup's threads.
        List threads = group.threads();
        out.write(printThreads(threads.iterator(), prefix + "  ", current));
    } // printGroup

    /**
     * Print the threads in the given iterator. Indicate which thread is
     * the current one by comparing to the given current.
     *
     * @param  iter     threads iterator.
     * @param  prefix   prefix for each output line.
     * @param  current  current thread.
     * @return  output from printing the threads.
     */
    protected String printThreads(Iterator iter, String prefix,
                                  ThreadReference current) {
        StringBuffer outbuf = new StringBuffer(256);
        String shortPrefix = prefix.substring(1);
        while (iter.hasNext()) {
            // For each thread in the list, get its status and name and
            // append those to the string buffer.
            ThreadReference thrd = (ThreadReference) iter.next();
            if (thrd.equals(current)) {
                outbuf.append('*');
                outbuf.append(shortPrefix);
            } else {
                outbuf.append(prefix);
            }
            outbuf.append(thrd.uniqueID());
            outbuf.append(' ');
            outbuf.append(thrd.name());
            outbuf.append(": ");
            outbuf.append(Threads.threadStatus(thrd));
            outbuf.append('\n');
        }
        return outbuf.toString();
    } // printThreads
} // threadsCommand
