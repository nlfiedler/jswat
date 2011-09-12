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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Steve Yegge, Nathan L. Fiedler.
 *
 * $Id: WhereCommand.java 284 2010-11-20 22:35:20Z nathanfiedler $
 */
package com.bluemarsh.jswat.console.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.command.commands.ViewCommand;
import com.bluemarsh.jswat.command.commands.WhereCommand;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.path.PathEntry;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Implements JDB's "list" command to show lines around current
 * single-step location.  Has the same syntax as JDB's "list".
 *
 * @author Steve Yegge
 */
public class ListCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter writer = context.getWriter();
        Session session = context.getSession();
        PathManager pm = PathProvider.getPathManager(session);
        DebuggingContext dc = context.getDebuggingContext();
        ThreadReference thread = dc.getThread();
        StackFrame frame = thread == null ? null : getFrame(thread);
        Location loc = frame == null ? null : frame.location();
        if (loc == null) {
            throw new CommandException(getMessage("ERR_list_not_ready"));
        }
        if (loc.method().isNative()) {
            throw new CommandException(getMessage("ERR_NativeMethod"));
        }
        ReferenceType refType = loc.declaringType();
        int line = loc.lineNumber();

        if (arguments.hasMoreTokens()) {
            String token = arguments.nextToken();
            try {
                line = Integer.parseInt(token);
            } catch (NumberFormatException nfx) {
                List methods = refType.methodsByName(token);
                if (methods == null || methods.isEmpty()) {
                    throw new CommandException(getMessage("ERR_invalid_list_args",
                        token, refType.name()));
                } else if (methods.size() > 1) {
                    throw new CommandException(getMessage("ERR_ambiguous_method",
                        token, refType.name()));
                }
                loc = ((Method)methods.get(0)).location();
                line = loc.lineNumber();
            }
        }

        if (line < 0) {
            throw new CommandException(getMessage("ERR_no_line_info",
                Integer.toString(line)));
        }
        PathEntry pe = pm.findSource(loc);
        if (pe == null) {
            throw new CommandException(NbBundle.getMessage(
                ViewCommand.class, "ERR_view_NotFound"));
        }

        List<String> lines = new ArrayList<String>(128);
        try {
            InputStream is = pe.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String ln = br.readLine();
            while (ln != null) {
                lines.add(ln);
                ln = br.readLine();
            }
            br.close();
        } catch (IOException ioe) {
            throw new CommandException(NbBundle.getMessage(
                ViewCommand.class, "ERR_view_IOError"), ioe);
        }

        int startLine = Math.max(line - 5, 1);
        int endLine = startLine + 10;
        for (int i = startLine; i <= endLine; i++) {
            if (i >= lines.size()) {
                break;
            }
            String sourceLine = lines.get(i - 1);  // ArrayList is zero-indexed
            if (i == line) {
                writer.println(String.format("%d:=>%s", i, sourceLine));
            } else {
                writer.println(String.format("%d:  %s", i, sourceLine));
            }
        }
    }

    protected String getMessage(String key) {
        return NbBundle.getMessage(ListCommand.class, key);
    }

    protected String getMessage(String key, String arg) {
        return NbBundle.getMessage(ListCommand.class, key, arg);
    }

    protected String getMessage(String key, String arg1, String arg2) {
        return NbBundle.getMessage(ListCommand.class, key, arg1, arg2);
    }

    StackFrame getFrame(ThreadReference thread) throws CommandException {
        List<StackFrame> stack = null;
        try {
            stack = thread.frames();
        } catch (IncompatibleThreadStateException itse) {
            throw new CommandException(NbBundle.getMessage(WhereCommand.class,
                                                           "ERR_ThreadNotSuspended"));
        } catch (ObjectCollectedException oce) {
            throw new CommandException(NbBundle.getMessage(WhereCommand.class,
                                                           "ERR_ObjectCollected"));
        }
        if (stack == null) {
            throw new CommandException(NbBundle.getMessage(WhereCommand.class,
                                                           "ERR_IncompatibleThread"));
        }
        return stack.get(0);
    }
}
