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
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionFactory;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.io.PrintWriter;
import java.util.Iterator;
import org.openide.util.NbBundle;

/**
 * Manages session lifecycle.
 *
 * @author Nathan Fiedler
 */
public class SessionCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "session";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter writer = context.getWriter();
        Session current = context.getSession();
        SessionManager manager = SessionProvider.getSessionManager();
        SessionFactory factory = SessionProvider.getSessionFactory();

        if (arguments.hasMoreTokens()) {
            String cmd = arguments.nextToken();
            if (cmd.equals("create")) {
                String name = null;
                if (arguments.hasMoreTokens()) {
                    arguments.returnAsIs(true);
                    name = arguments.rest();
                }
                String id = manager.generateIdentifier();
                Session session = factory.createSession(id);
                if (name != null && !name.isEmpty()) {
                    session.setProperty(Session.PROP_SESSION_NAME, name);
                }
                manager.add(session);
            } else if (cmd.equals("copy")) {
                String id = current.getIdentifier();
                if (arguments.hasMoreTokens()) {
                    id = arguments.nextToken();
                }
                Session session = manager.findById(id);
                if (session == null) {
                    throw new CommandException(NbBundle.getMessage(
                            SessionCommand.class, "ERR_session_Unknown", id));
                }
                manager.copy(session, null);
            } else if (cmd.equals("delete")) {
                if (arguments.hasMoreTokens()) {
                    String id = arguments.nextToken();
                    Session session = manager.findById(id);
                    if (session == null) {
                        throw new CommandException(NbBundle.getMessage(
                                SessionCommand.class, "ERR_session_Unknown", id));
                    }
                    if (current.equals(session)) {
                        throw new CommandException(NbBundle.getMessage(
                                SessionCommand.class, "ERR_session_DeleteCurrent"));
                    }
                    manager.remove(session);
                } else {
                    throw new MissingArgumentsException();
                }
            } else if (cmd.equals("rename")) {
                if (arguments.hasMoreTokens()) {
                    String id = arguments.nextToken();
                    Session session = manager.findById(id);
                    if (session == null) {
                        throw new CommandException(NbBundle.getMessage(
                                SessionCommand.class, "ERR_session_Unknown", id));
                    }
                    if (!arguments.hasMoreTokens()) {
                        throw new MissingArgumentsException();
                    }
                    String name = arguments.nextToken();
                    session.setProperty(Session.PROP_SESSION_NAME, name);
                } else {
                    throw new MissingArgumentsException();
                }
            } else if (cmd.equals("prop")) {
                if (arguments.hasMoreTokens()) {
                    String id = arguments.nextToken();
                    Session session = manager.findById(id);
                    if (session == null) {
                        throw new CommandException(NbBundle.getMessage(
                                SessionCommand.class, "ERR_session_Unknown", id));
                    }
                    if (arguments.hasMoreTokens()) {
                        String name = arguments.nextToken();
                        if (arguments.hasMoreTokens()) {
                            String value = arguments.nextToken();
                            if (value.equals("null")) {
                                value = null;
                            }
                            session.setProperty(name, value);
                        } else {
                            writer.println(String.format("%s = %s", name,
                                    session.getProperty(name)));
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        Iterator<String> names = session.propertyNames();
                        while (names.hasNext()) {
                            String name = names.next();
                            String value = session.getProperty(name);
                            sb.append(name);
                            sb.append(" = ");
                            sb.append(value);
                            sb.append('\n');
                        }
                        writer.print(sb.toString());
                    }
                } else {
                    throw new MissingArgumentsException();
                }
            } else if (cmd.equals("use")) {
                if (arguments.hasMoreTokens()) {
                    String id = arguments.nextToken();
                    Session session = manager.findById(id);
                    if (session == null) {
                        throw new CommandException(NbBundle.getMessage(
                                SessionCommand.class, "ERR_session_Unknown", id));
                    }
                    manager.setCurrent(session);
                } else {
                    throw new MissingArgumentsException();
                }
            } else {
                throw new CommandException(NbBundle.getMessage(
                        SessionCommand.class, "ERR_session_Subcommand", cmd));
            }
        } else {
            Iterator<Session> sessions = manager.iterateSessions();
            StringBuilder list = new StringBuilder();
            while (sessions.hasNext()) {
                Session session = sessions.next();
                if (current.equals(session)) {
                    list.append("* [");
                } else {
                    list.append("  [");
                }
                list.append(session.getIdentifier());
                list.append(", ");
                list.append(session.getProperty(Session.PROP_SESSION_NAME));
                list.append("] <");
                list.append(session.getState());
                list.append("> (");
                list.append(session.getStratum());
                list.append(")");
                if (session.isConnected()) {
                    list.append(" @");
                    list.append(session.getAddress());
                }
                list.append('\n');
            }
            writer.print(list.toString());
        }
    }
}
