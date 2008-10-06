/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: CommandTestCase.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import junit.framework.TestCase;

/**
 * Base class for all command test cases.
 *
 * @author  Nathan Fiedler
 */
public class CommandTestCase extends TestCase {

    /**
     * Constructs a command test case with the given name.
     *
     * @param  name  name of test case.
     */
    public CommandTestCase(String name) {
        super(name);
    } // CommandTestCase

    /**
     * Resume the Session and wait for the Session to suspend again.
     * Used when a breakpoint has been set but not yet hit.
     *
     * @param  session  Session to resume.
     * @param  ssl      listener that notices Session stopping.
     */
    protected void resumeAndWait(Session session, SimpleSessionListener ssl) {
        session.resumeVM(this, false, true);
        waitForSuspend(ssl);
    } // resumeAndWait

    /**
     * Get the command and call its perform() method directly.
     *
     * @param  session  open session.
     * @param  input    command input to run.
     */
    protected void runCommand(Session session, String input) {
        CommandManager cm = (CommandManager)
            session.getManager(CommandManager.class);
        CommandArguments cargs = new CommandArguments(input);
        String cname = cargs.nextToken();
        JSwatCommand c = cm.getCommand(cname, false);
        assertNotNull(cname + " is not a command", c);
        c.perform(session, cargs, session.getStatusLog());
    } // runCommand

    /**
     * Runs the given input through the command manager.
     *
     * @param  session  open session.
     * @param  input    command input to run.
     */
    protected void runCommandMgr(Session session, String input) {
        CommandManager cm = (CommandManager)
            session.getManager(CommandManager.class);
        cm.handleInput(input);
    } // runCommandMgr

    /**
     * Wait for the Session to suspend again.
     *
     * @param  ssl  listener that notices Session stopping.
     */
    protected void waitForSuspend(SimpleSessionListener ssl) {
        synchronized (ssl) {
            int count = 0;
            // Give the session a second chance to finish resuming.
            while (ssl.isRunning() && count < 2) {
                try {
                    // wait for the session to suspend
                    ssl.wait();
                } catch (InterruptedException ie) {
                    // ignored
                }
                count++;
            }
            if (!ssl.isActive()) {
                fail("session is not active");
            }
            if (ssl.isRunning()) {
                fail("session is not suspended");
            }
        }
    } // waitForSuspend
} // CommandTestCase
