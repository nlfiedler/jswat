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
 * The Original Software is the JSwat Command Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2009-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointClearCommand.java 193 2009-05-14 01:02:10Z nathanfiedler $
 */
package com.bluemarsh.jswat.console.commands;

import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.command.commands.BreakpointClearCommand;
import com.bluemarsh.jswat.command.commands.BreakpointSetCommand;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.LocationBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.MethodBreakpoint;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.Location;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;

/**
 * Provides additional "clear" command syntaxes for JDB compatibility.
 * With no arguments, "clear" is a synonym for "break".
 * With an argument spec of the form {@code full.class.name:line}
 * or {@code full.class.name.method}, scans the breakpoint
 * list and deletes the first one that matches the spec exactly.
 *
 * @author Steve Yegge
 */
public class ClearCommand extends BreakpointClearCommand {

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {
        // "clear" by itself shows the breakpoint list
        if (!arguments.hasMoreTokens()) {
            new BreakpointSetCommand().displayBreakpointList(context);
            return;
        }

        String arg = arguments.peek();
        if (arg.matches(".+?:\\d+")) {  // matches <class:line>?
            clearLineBreak(context, arg);
        } else if (arg.matches(".*?[^\\d].*")) {  // contains a non-number?
            // assume for now that it's a method breakpoint
            clearMethodBreak(context, arg);
        } else {
            super.perform(context, arguments);
        }
    }

    private void clearLineBreak(CommandContext context, String arg)
            throws CommandException {
        String[] parts = arg.split(":");
        if (parts.length != 2) {
            barfNotFound(arg);
        }
        String clazz = parts[0];
        int line = Integer.parseInt(parts[1]);
        for (Breakpoint b : getBreakpoints(context)) {
            if (b instanceof LocationBreakpoint) {
                Location loc = ((LocationBreakpoint) b).getLocation();
                if (clazz.equals(loc.declaringType().name())
                        && line == loc.lineNumber()) {
                    deleteBreakpoint(context, b);
                    return;
                }
            } else if (b instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) b;
                String typename = classnameFromUrl(lb.getURL());
                if (line == lb.getLineNumber() && clazz.equals(typename)) {
                    deleteBreakpoint(context, lb);
                    return;
                }
            }
        }
        barfNotFound(arg);
    }

    private void clearMethodBreak(CommandContext context, String arg)
            throws CommandException {
        // <class id>.<method>[(argument_type,...)]
        String syntax = "^(.+)\\.([^(.]+)(?:\\((.+)\\))?$";
        Matcher m = Pattern.compile(syntax).matcher(arg);
        if (!m.matches()) {
            barfNotFound(arg);
        }
        String clazz = m.group(1), method = m.group(2), args = m.group(3);
        List<String> givenArgTypes = new ArrayList<String>();
        if (args != null) {
            givenArgTypes = Strings.stringToList(args);
        }

        for (Breakpoint b : getBreakpoints(context)) {
            if (b instanceof MethodBreakpoint) {
                MethodBreakpoint mb = (MethodBreakpoint) b;
                if (!(method.equals(mb.getMethodName())
                        && clazz.equals(mb.getClassName()))) {
                    continue;
                }
                if (givenArgTypes.equals(mb.getMethodParameters())) {
                    deleteBreakpoint(context, mb);
                    return;
                }
            }
        }
        barfNotFound(arg);
    }

    private void deleteBreakpoint(CommandContext cx, Breakpoint b) {
        BreakpointProvider.getBreakpointManager(
                cx.getSession()).removeBreakpoint(b);
        cx.getWriter().println(NbBundle.getMessage(ClearCommand.class,
                "CTL_Clear_Removed", b.getDescription()));
    }

    private void barfNotFound(String arg) throws CommandException {
        throw new CommandException(
                NbBundle.getMessage(ClearCommand.class,
                "ERR_Clear_NotFound", arg));
    }

    /**
     * Parse the internal {@code LineBreakpoint} {@code url} field
     * to retrieve the fully qualified type name.
     * @param url e.g. "file://root/com/bluemarsh/jswat/console/Main.java"
     * @return a typename such as {@code com.bluemarsh.jswat.console.Main}
     * Returns {@code null} if we could not parse out a type.
     */
    private static String classnameFromUrl(String url) {
        String prefix = "file://root/";
        if (!url.startsWith(prefix)) {
            return null;
        }
        String result = url.substring(prefix.length());
        if (result.endsWith(".java")) {
            result = result.substring(0, result.length() - ".java".length());
        }
        return result.replace('/', '.');
    }

    private List<Breakpoint> getBreakpoints(CommandContext context) {
        Session session = context.getSession();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        List<Breakpoint> result = new ArrayList<Breakpoint>();
        Iterator<Breakpoint> it = bm.getDefaultGroup().breakpoints(true);
        while (it.hasNext()) {
            result.add(it.next());
        }
        return result;
    }

    @Override
    public boolean requiresArguments() {
        return false;
    }
}
