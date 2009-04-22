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
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Strings;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Displays or sets the classpath for the current session.
 *
 * @author Nathan Fiedler
 */
public class ClasspathCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "classpath";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter writer = context.getWriter();
        Session session = context.getSession();
        PathManager pm = PathProvider.getPathManager(session);
        if (arguments.hasMoreTokens()) {
            String path = arguments.rest();
            List<String> roots = Strings.stringToList(path, File.pathSeparator);
            pm.setClassPath(roots);
        } else {
            List<String> roots = pm.getClassPath();
            String path = Strings.listToString(roots, File.pathSeparator);
            if (path == null || path.length() == 0) {
                path = NbBundle.getMessage(ClasspathCommand.class,
                        "CTL_classpath_Nopath");
            }
            writer.println(path);
        }
    }
}
