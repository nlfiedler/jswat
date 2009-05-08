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
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.path.PathEntry;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Names;
import com.sun.jdi.Location;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Displays source code for the given class or file.
 *
 * @author Nathan Fiedler
 */
public class ViewCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "view";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter writer = context.getWriter();
        Session session = context.getSession();
        PathManager pm = PathProvider.getPathManager(session);

        int line = 0;
        int pc = 0;
        int count = 10;
        String name = null;

        // Get the argument values.
        while (arguments.hasMoreTokens()) {
            String token = arguments.nextToken();
            try {
                if (token.startsWith("@") && token.length() > 1) {
                    token = token.substring(1);
                    line = Integer.parseInt(token);
                } else if (token.startsWith("+") && token.length() > 1) {
                    token = token.substring(1);
                    count = Integer.parseInt(token);
                } else {
                    name = token;
                }
            } catch (NumberFormatException nfe) {
                throw new CommandException(NbBundle.getMessage(
                        ViewCommand.class, "ERR_InvalidNumber", token));
            }
        }

        InputStream is = null;
        try {
            if (name != null) {
                // If it looks like a class name, try finding its source.
                if (Names.isValidClassname(name, false)) {
                    PathEntry pe = pm.findSource(name);
                    if (pe != null) {
                        is = pe.getInputStream();
                    }
                }
                // Otherwise, see if it's a file we can find.
                if (is == null) {
                    File file = new File(name);
                    if (file.exists()) {
                        is = new FileInputStream(file);
                    } else {
                        // Could it be a relative path then?
                        PathEntry pe = pm.findFile(name);
                        if (pe != null) {
                            is = pe.getInputStream();
                        }
                    }
                }
            } else {
                // Use the current location information.
                DebuggingContext dc = ContextProvider.getContext(session);
                Location loc = dc.getLocation();
                if (loc == null) {
                    throw new CommandException(NbBundle.getMessage(
                            ViewCommand.class, "ERR_view_NoContext"));
                }
                PathEntry pe = pm.findSource(loc);
                if (pe == null) {
                    throw new CommandException(NbBundle.getMessage(
                            ViewCommand.class, "ERR_view_NotFound"));
                }
                is = pe.getInputStream();
                pc = loc.lineNumber();
                if (line == 0) {
                    // Default to showing the lines near the current location.
                    line = pc - 5;
                }
            }
        } catch (FileNotFoundException fnfe) {
            throw new CommandException(NbBundle.getMessage(
                    ViewCommand.class, "ERR_view_MissingFile", name));
        } catch (IOException ioe) {
            throw new CommandException(NbBundle.getMessage(
                    ViewCommand.class, "ERR_view_IOError"), ioe);
        }

        if (is == null) {
            throw new CommandException(NbBundle.getMessage(
                    ViewCommand.class, "ERR_view_MissingFile", name));
        }

        // Read the file contents into memory. Let the OS handle the caching.
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<String> lines = new ArrayList<String>(128);
        try {
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

        // Perform bounds checking on the line and count values.
        if (line <= 0) {
            line = 1;
        }
        if (count <= 0) {
            count = 1;
        }
        int max = Math.min(line + count, lines.size() + 1);

        // Display the lines of source code.
        StringBuilder sb = new StringBuilder();
        for (int ii = line; ii < max; ii++) {
            String ln = lines.get(ii - 1);
            if (ii == pc) {
                sb.append(String.format("%d:=>%s\n", ii, ln));
            } else {
                sb.append(String.format("%d:  %s\n", ii, ln));
            }
        }
        writer.print(sb.toString());
    }
}
