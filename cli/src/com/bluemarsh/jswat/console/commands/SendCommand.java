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

package com.bluemarsh.jswat.console.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.console.PipeProvider;
import com.bluemarsh.jswat.core.session.Session;
import java.io.IOException;
import java.io.PipedWriter;
import org.openide.util.NbBundle;

/**
 * Sends input to the debuggee through its input stream.
 *
 * @author Nathan Fiedler
 */
public class SendCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "send";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PipedWriter pw = PipeProvider.getPipedWriter(session);
        try {
            if (arguments.hasMoreTokens()) {
                arguments.returnAsIs(true);
                String input = arguments.rest();
                pw.write(input);
            }
            pw.write('\n');
            pw.flush();
        } catch (IOException ioe) {
            throw new CommandException(NbBundle.getMessage(SendCommand.class,
                    "ERR_send_Failed"), ioe);
        }
    }
}
