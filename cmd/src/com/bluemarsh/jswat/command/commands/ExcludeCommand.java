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
import com.bluemarsh.jswat.core.CoreSettings;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Strings;
import java.io.PrintWriter;
import java.util.List;
import java.util.StringTokenizer;
import org.openide.util.NbBundle;

/**
 * Manages the set of single stepping exclusions.
 *
 * @author Nathan Fiedler
 */
public class ExcludeCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "exclude";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter writer = context.getWriter();
        CoreSettings settings = CoreSettings.getDefault();
        if (arguments.hasMoreTokens()) {
            String token = arguments.rest();
            if (token.equals("none")) {
                // Remove all exclusions.
                settings.setSteppingExcludes(null);
            } else {
                // Comma-separated list of package/class patterns.
                List<String> excludes = Strings.stringToList(token);
                // Validate the patterns to ensure they will work.
                for (String excl : excludes) {
                    // For some bizarre reason, StringTokenizer does not
                    // return the empty tokens between delimiters.
                    if (excl.indexOf("..") >= 0) {
                        throw new CommandException(NbBundle.getMessage(
                                ExcludeCommand.class,
                                "ERR_exclude_InvalidPattern", excl));
                    }
                    StringTokenizer tokenizer = new StringTokenizer(excl, ".");
                    while (tokenizer.hasMoreTokens()) {
                        String part = tokenizer.nextToken();
                        if (!part.equals("*") && !Names.isJavaIdentifier(part)) {
                            throw new CommandException(NbBundle.getMessage(
                                    ExcludeCommand.class,
                                    "ERR_exclude_InvalidPattern", excl));
                        }
                    }
                }
                // Save the excludes in the settings.
                settings.setSteppingExcludes(excludes);
            }
        } else {
            List<String> list = settings.getSteppingExcludes();
            String excludes = Strings.listToString(list);
            if (excludes == null || excludes.length() == 0) {
                excludes = NbBundle.getMessage(ExcludeCommand.class,
                        "CTL_excludes_NoExclusions");
            }
            writer.println(excludes);
        }
    }
}
