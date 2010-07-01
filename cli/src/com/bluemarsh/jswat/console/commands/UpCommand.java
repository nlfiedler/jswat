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
 * $Id: UpCommand.java $
 */

package com.bluemarsh.jswat.console.commands;

import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.command.commands.FrameUpCommand;

/**
 * Extends the "up" command to issue a 1-frame stack trace. <p>
 *
 * Strictly speaking this isn't JDB-compatible -- JDB issues no message after
 * up/down commands, although the prompt changes to reflect the new active
 * frame.  JDB's behavior is essentially useless, as both users and
 * wrapper-tools such as Emacs must issue a "where" command after every
 * "up"/"down" just to see where the current frame is.  If we send just 1
 * frame it's a far superior console experience.
 *
 * @author Steve Yegge
 */
public class UpCommand extends FrameUpCommand {

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {
        // This will throw if we fail to change the current frame.
        super.perform(context, arguments);

        // Our output is identical one frame of "where".
        new WhereCommand().displayCurrentFrame(context);
    }
}
