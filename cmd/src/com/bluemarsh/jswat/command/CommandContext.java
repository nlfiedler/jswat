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
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: CommandContext.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import java.io.PrintWriter;

/**
 * Encapsulates the information required for commands to perform their task.
 *
 * @author Nathan Fiedler
 */
public class CommandContext {
    /** Session in which the command is invoked. */
    private Session session;
    /** DebuggingContext for the Session. */
    private DebuggingContext debuggingContext;
    /** Where command output is to be written. */
    private PrintWriter writer;
    /** The parser that is invoking the command. */
    private CommandParser parser;

    /**
     * Creates a new instance of CommandContext.
     *
     * @param  session  current Session.
     * @param  writer   where to write messages.
     * @param  parser   command parser.
     */
    public CommandContext(Session session, PrintWriter writer, CommandParser parser) {
        this.session = session;
        debuggingContext = ContextProvider.getContext(session);
        this.writer = writer;
        this.parser = parser;
    }

    /**
     * Returns the DebuggingContext associated with this context's Session.
     *
     * @return  debugging context.
     */
    public DebuggingContext getDebuggingContext() {
        return debuggingContext;
    }

    /**
     * Returns the CommandParser invoking the command.
     *
     * @return  command parser.
     */
    public CommandParser getParser() {
        return parser;
    }

    /**
     * Returns the Session in which the command is invoked.
     *
     * @return  Session instance.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Returns the PrintWriter to which commands write their messages.
     *
     * @return  output writer.
     */
    public PrintWriter getWriter() {
        return writer;
    }
}
