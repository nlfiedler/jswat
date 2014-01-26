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
 * $Id: hotswapCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.util.Classes;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the class that handles the 'hotswap' command.
 *
 * @author  Nathan Fiedler
 */
public class hotswapCommand extends JSwatCommand {

    /**
     * Perform the 'hotswap' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }

        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Name of class is required; name of .class file is optional.
        String cname = args.nextToken();
        String cfile = null;
        if (args.hasMoreTokens()) {
            cfile = args.nextToken();
        }

        // Find the ReferenceType for this class.
        VirtualMachine vm = session.getConnection().getVM();
        List classes = vm.classesByName(cname);
        if (classes.size() == 0) {
            throw new CommandException(
                Bundle.getString("hotswap.noSuchClass"));
        }
        ReferenceType clazz = (ReferenceType) classes.get(0);

        // Did the user give us a .class file?
        InputStream is = null;
        if (cfile == null) {
            // Try to find the .class file.
            PathManager pathman = (PathManager)
                session.getManager(PathManager.class);
            SourceSource src = pathman.mapClass(clazz);
            if (src == null) {
                throw new CommandException(
                    Bundle.getString("hotswap.fileNotFound"));
            }
            is = src.getInputStream();
        } else {
            // A filename was given, just open it.
            try {
                is = new FileInputStream(cfile);
            } catch (FileNotFoundException fnfe) {
                throw new CommandException(
                    Bundle.getString("hotswap.fileNotFound"));
            }
        }

        // Do the actual hotswap operation.
        try {
            Classes.hotswap(clazz, is, vm);
            out.writeln(Bundle.getString("hotswap.success"));
        } catch (UnsupportedOperationException uoe) {
            if (!vm.canRedefineClasses()) {
                throw new CommandException(
                    Bundle.getString("hotswap.noHotSwap"));
            } else if (!vm.canAddMethod()) {
                throw new CommandException(
                    Bundle.getString("hotswap.noAddMethod"));
            } else if (!vm.canUnrestrictedlyRedefineClasses()) {
                throw new CommandException(
                    Bundle.getString("hotswap.noUnrestricted"));
            } else {
                throw new CommandException(
                    Bundle.getString("hotswap.unsupported"));
            }
        } catch (IOException ioe) {
            throw new CommandException(
                Bundle.getString("hotswap.errorReadingFile") + ' ' + ioe);
        } catch (NoClassDefFoundError ncdfe) {
            throw new CommandException(Bundle.getString("hotswap.wrongClass"));
        } catch (VerifyError ve) {
            throw new CommandException(
                Bundle.getString("hotswap.verifyError") + ' ' + ve);
        } catch (UnsupportedClassVersionError ucve) {
            throw new CommandException(
                Bundle.getString("hotswap.versionError") + ' ' + ucve);
        } catch (ClassFormatError cfe) {
            throw new CommandException(
                Bundle.getString("hotswap.formatError") + ' ' + cfe);
        } catch (ClassCircularityError cce) {
            throw new CommandException(
                Bundle.getString("hotswap.circularity") + ' ' + cce);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) { }
            }
        }
    } // perform
} // hotswapCommand
