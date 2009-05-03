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
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.IncompatibleThreadStateException;
import org.openide.util.NbBundle;

/**
 * Sets the current stack frame.
 *
 * @author Nathan Fiedler
 */
public class FrameJumpCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "frame";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        DebuggingContext dc = ContextProvider.getContext(session);

        // Get the value of the stack frame index.
        int frame;
        String token = arguments.nextToken();
        try {
            frame = Integer.parseInt(token);
        } catch (NumberFormatException nfe) {
            throw new CommandException(NbBundle.getMessage(
                    FrameJumpCommand.class, "ERR_InvalidNumber", token));
        }

        // Try to set the new current frame index.
        try {
            dc.setFrame(frame);
        } catch (IncompatibleThreadStateException itse) {
            throw new CommandException(NbBundle.getMessage(
                    FrameJumpCommand.class, "ERR_ThreadNotSuspended"));
        } catch (IndexOutOfBoundsException ioobe) {
            throw new CommandException(NbBundle.getMessage(
                    FrameJumpCommand.class, "ERR_InvalidStackFrame"));
        }
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

    @Override
    public boolean requiresDebuggee() {
        return true;
    }

    @Override
    public boolean requiresThread() {
        return true;
    }
}
