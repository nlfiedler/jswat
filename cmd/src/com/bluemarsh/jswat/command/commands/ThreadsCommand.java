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
 * are Copyright (C) 2003-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ThreadsCommand.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;

/**
 * Displays all of the threads in the debuggee.
 *
 * @author Nathan Fiedler
 */
public class ThreadsCommand extends AbstractCommand {

    public String getName() {
        return "threads";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();
        DebuggingContext dc = context.getDebuggingContext();
        ThreadReference current = dc.getThread();

        if (arguments.hasMoreTokens()) {
            long tid = -1;
            String name = arguments.nextToken();
            // Is it a thread group name or unique ID?
            try {
                tid = Long.parseLong(name);
            } catch (NumberFormatException nfe) {
                // It's not a thread id then.
            }

            List<ThreadReference> threadsList = null;
            // Look for a thread group with this name or id.
            Iterator iter = new ThreadGroupIterator(
                vm.topLevelThreadGroups());
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
                threadsList = new ArrayList<ThreadReference>();
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
                writer.println(NbBundle.getMessage(getClass(),
                        "CTL_threads_noThreadsInGroup"));
            } else if (threadsList.size() > 0) {
                writer.println(printThreads(threadsList.iterator(), "  ", current));
            }

        } else {
            // Print all of the thread groups and their threads.
            List topGroups = vm.topLevelThreadGroups();
            if (topGroups == null || topGroups.size() == 0) {
                writer.println(NbBundle.getMessage(getClass(),
                        "CTL_threads_noThreads"));
            } else if (topGroups.size() > 0) {
                Iterator iter = topGroups.iterator();
                while (iter.hasNext()) {
                    ThreadGroupReference group =
                        (ThreadGroupReference) iter.next();
                    printGroup(group, current, writer, "");
                }
            }
        }
    }


    /**
     * Print the thread group to the output with each line prefixed
     * by the given string.
     *
     * @param  group    thread group to print.
     * @param  current  current thread.
     * @param  writer   writer to print to.
     * @param  prefix   string to display before each line.
     */
    protected void printGroup(ThreadGroupReference group, ThreadReference current,
                              PrintWriter writer, String prefix) {
        ReferenceType clazz = group.referenceType();
        String id = String.valueOf(group.uniqueID());
        if (clazz == null) {
            writer.println(prefix + id + ' ' + group.name());
        } else {
            writer.println(prefix + id + ' ' + group.name() + " (" + clazz.name() + ')');
        }

        // Now traverse this group's subgroups.
        List<ThreadGroupReference> groups = group.threadGroups();
        Iterator<ThreadGroupReference> iter = groups.iterator();
        while (iter.hasNext()) {
            ThreadGroupReference subgrp = iter.next();
            printGroup(subgrp, current, writer, prefix + "  ");
        }

        // Print this threadgroup's threads.
        List<ThreadReference> threads = group.threads();
        writer.print(printThreads(threads.iterator(), prefix + "  ", current));
    }

    /**
     * Print the threads in the given iterator. Indicate which thread is
     * the current one by comparing to the given current.
     *
     * @param  iter     threads iterator.
     * @param  prefix   prefix for each output line.
     * @param  current  current thread.
     * @return  output from printing the threads.
     */
    protected String printThreads(Iterator<ThreadReference> iter,
            String prefix, ThreadReference current) {
        StringBuilder sb = new StringBuilder(256);
        String starfix = prefix.substring(1);
        while (iter.hasNext()) {
            ThreadReference thrd = iter.next();
            if (thrd.equals(current)) {
                sb.append('*');
                sb.append(starfix);
            } else {
                sb.append(prefix);
            }
            sb.append(thrd.uniqueID());
            sb.append(' ');
            sb.append(thrd.name());
            sb.append(": ");
            sb.append(Threads.threadStatus(thrd));
            sb.append('\n');
        }
        return sb.toString();
    }

    public boolean requiresDebuggee() {
        return true;
    }

    /**
     * Class ThreadGroupIterator has special functionality for iterating
     * over a list of thread group references. Since thread groups are
     * often assembled in trees, this iterator uses a stack to traverse
     * that tree in depth-first order.
     *
     * @author  Nathan Fiedler
     */
    private static class ThreadGroupIterator implements Iterator {
        /** Stack of thread group iterators. */
        private Stack<Iterator<ThreadGroupReference>> stack =
                new Stack<Iterator<ThreadGroupReference>>();

        /**
         * Constructs a new ThreadGroupIterator with an initial set
         * of thread group iterators.
         *
         * @param  groups  ThreadGroup list.
         */
        public ThreadGroupIterator(List<ThreadGroupReference> groups) {
            push(groups);
        }

        /**
         * Constructs a new ThreadGroupIterator with an initial set
         * of thread groups.
         *
         * @param  group  ThreadGroup
         */
        public ThreadGroupIterator(ThreadGroupReference group) {
            List<ThreadGroupReference> groups =
                    new ArrayList<ThreadGroupReference>();
            groups.add(group);
            push(groups);
        }

        /**
         * Returns true if there are more iterators to be examined.
         *
         * @return  True if there are more iterators.
         */
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        /**
         * Returns the next element in the interation.
         *
         * @return  Next object in iteration.
         */
        public Object next() {
            // Ask the top iterator for the next thread group reference.
            ThreadGroupReference group = (ThreadGroupReference) peek().next();
            // If this group has more groups, add them to the stack.
            push(group.threadGroups());
            // Return the thread group.
            return group;
        }

        /**
         * Looks at the object at the top of this stack without removing
         * it from the stack.
         *
         * @return  First iterator on the stack, or null if none.
         */
        private Iterator peek() {
            try {
                return (Iterator) stack.peek();
            } catch (EmptyStackException ese) {
                return null;
            }
        }

        /**
         * Push the given list of thread group iterators onto the stack.
         *
         * @param  groups  List of ThreadGroup iterators.
         */
        private void push(List<ThreadGroupReference> groups) {
            // Add this list's iterator to the stack.
            stack.push(groups.iterator());
            // While the top iterator is empty, pop it off the stack.
            // This ensures that the top iterator has something to iterate.
            while (!stack.isEmpty() && !peek().hasNext()) {
                stack.pop();
            }
        }

        /**
         * Remove is not supported on this iterator.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
