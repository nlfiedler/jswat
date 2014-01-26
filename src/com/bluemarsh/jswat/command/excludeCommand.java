/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Taavo Raykoff
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
 * PROJECT:     JSwat
 * MODULE:      JSwat Commands
 * FILE:        excludeCommand.java
 *
 * AUTHOR:      Taavo Raykoff
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      tr      12/08/01        Initial version
 *      nf      03/08/02        Use new ClassUtils method
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'exclude' command.
 *
 * $Id: excludeCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.CommandManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ClassUtils;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.util.StringTokenizer;

/**
 * Defines the class that handles the 'exclude' command.
 *
 * @author  Taavo Raykoff
 */
public class excludeCommand extends JSwatCommand {

    /**
     * Gets the exclusion set property from the Session.
     *
     * @param  session  current Session.
     * @return  exclusion set value.
     */
    public static String[] getExclusionSet(Session session) {
        return StringUtils.tokenize(session.getProperty("excludes"));
    } // getExclusionSet

    /**
     * Determines if the given class exclusion filter is valid or not.
     *
     * @param  filter  class exclusion filter to examine.
     * @return  true if valid, false otherwise.
     */
    private boolean isLegalExclusionFilter(String filter) {
        // Check for an invalid case of two periods together.
        if (filter.indexOf("..") != -1) {
            return false;
        }
        // Remove the optional wildcards.
        if (filter.charAt(0) == '*') {
            filter = filter.substring(1);
        }
        if (filter.charAt(filter.length() - 1) == '*') {
            filter = filter.substring(0, filter.length() - 1);
        }
        // Remove the periods that were likely a part of the wildcards.
        if (filter.charAt(0) == '.') {
            filter = filter.substring(1);
        }
        if (filter.charAt(filter.length() - 1) == '.') {
            filter = filter.substring(0, filter.length() - 1);
        }
        // Verify that each non-wildcard piece is a Java identifier.
        StringTokenizer pieces  = new StringTokenizer(filter, ".");
        while (pieces.hasMoreElements()) {
            String piece = pieces.nextToken();
            if (!ClassUtils.isJavaIdentifier(piece)) {
                return false;
            }
        }
        return true;
    } // isLegalExclusionFilter

    /**
     *  Perform the 'exclude' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        String exPkgs[] = StringUtils.tokenize(
            session.getProperty("excludes"));

        String arg1 = args.hasMoreElements() ? args.nextToken() : null;
        String arg2 = args.hasMoreElements() ? args.nextToken() : null;

        if (arg1 == null) {
            StringBuffer msg  = new StringBuffer(
                Bundle.getString("exclude.list"));
            msg.append('\n');
            if (exPkgs.length == 0) {
                msg.append(Bundle.getString("exclude.none"));
                msg.append('\n');
            } else {
                for (int i = 0; i < exPkgs.length; i++) {
                    msg.append(exPkgs[i]);
                    msg.append('\n');
                }
            }
            out.write(msg.toString());
        } else if (arg2 == null) {
            if (isLegalExclusionFilter(arg1)) {
                StringBuffer newPkgs  = new StringBuffer();
                for (int i = 0; i < exPkgs.length; i++) {
                    // Skip excludes already in our list.
                    if (!exPkgs[i].equals(arg1)) {
                        newPkgs.append(exPkgs[i]);
                        newPkgs.append(' ');
                    }
                }
                newPkgs.append(arg1);
                session.setProperty("excludes", newPkgs.toString());
                out.writeln(arg1 + ' ' + Bundle.getString("exclude.added"));
            } else {
                out.writeln(Bundle.getString("incorrectSyntax"));
                help(out);
            }
        } else if (arg1.equals("-")) {
            boolean gotIt = false;
            StringBuffer newPkgs = new StringBuffer();
            for (int i = 0; i < exPkgs.length; i++) {
                if (exPkgs[i].equals(arg2)) {
                    gotIt = true;
                } else {
                    newPkgs.append(exPkgs[i]);
                    newPkgs.append(' ');
                }
            }
            if (gotIt) {
                session.setProperty("excludes", newPkgs.toString());
                out.writeln(arg2 + ' ' + Bundle.getString("exclude.removed"));
            } else {
                out.writeln(arg2 + ' ' + Bundle.getString("exclude.notfound"));
            }
        } else {
            out.writeln(Bundle.getString("incorrectSyntax"));
            help(out);
        }
    } // perform
} // excludeCommand
