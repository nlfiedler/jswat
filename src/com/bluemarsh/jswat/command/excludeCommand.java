/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Taavo Raykoff
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
 * $Id: excludeCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.Names;
import com.bluemarsh.jswat.util.Strings;
import java.util.StringTokenizer;

/**
 * Defines the class that handles the 'exclude' command.
 *
 * @author  Taavo Raykoff
 */
public class excludeCommand extends JSwatCommand {

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
            if (!Names.isJavaIdentifier(piece)) {
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
    public void perform(Session session, CommandArguments args, Log out) {
        String[] excludes = Strings.tokenize(
            session.getProperty("excludes"));

        String arg1 = args.hasMoreTokens() ? args.nextToken() : null;
        String arg2 = args.hasMoreTokens() ? args.nextToken() : null;

        if (arg1 == null) {
            StringBuffer msg  = new StringBuffer(
                Bundle.getString("exclude.list"));
            msg.append('\n');
            if (excludes.length == 0) {
                msg.append(Bundle.getString("exclude.none"));
                msg.append('\n');
            } else {
                for (int i = 0; i < excludes.length; i++) {
                    msg.append(excludes[i]);
                    msg.append('\n');
                }
            }
            out.write(msg.toString());

        } else if (arg2 == null) {
            if (isLegalExclusionFilter(arg1)) {
                StringBuffer newexcludes  = new StringBuffer();
                for (int i = 0; i < excludes.length; i++) {
                    // Skip excludes already in our list.
                    if (!excludes[i].equals(arg1)) {
                        newexcludes.append(excludes[i]);
                        newexcludes.append(' ');
                    }
                }
                newexcludes.append(arg1);
                session.setProperty("excludes", newexcludes.toString().trim());
                out.writeln(arg1 + ' ' + Bundle.getString("exclude.added"));
            } else {
                throw new CommandException(
                    Bundle.getString("incorrectSyntax"));
            }

        } else if (arg1.equals("-")) {
            boolean gotIt = false;
            StringBuffer newexcludes = new StringBuffer();
            for (int i = 0; i < excludes.length; i++) {
                if (excludes[i].equals(arg2)) {
                    gotIt = true;
                } else {
                    newexcludes.append(excludes[i]);
                    newexcludes.append(' ');
                }
            }
            if (gotIt) {
                session.setProperty("excludes", newexcludes.toString().trim());
                out.writeln(arg2 + ' ' + Bundle.getString("exclude.removed"));
            } else {
                throw new CommandException(
                    arg2 + ' ' + Bundle.getString("exclude.notfound"));
            }
        } else {
            throw new CommandException(Bundle.getString("incorrectSyntax"));
        }
    } // perform
} // excludeCommand
