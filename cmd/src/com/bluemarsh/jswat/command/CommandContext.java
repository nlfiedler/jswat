/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: CommandContext.java 15 2007-06-03 00:01:17Z nfiedler $
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
