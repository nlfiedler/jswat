/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * FILE:        JSwatCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/23/99        Initial version
 *      nf      05/19/01        Fixed #114, add "this." support
 *
 * DESCRIPTION:
 *      This file defines the abstract class used to define commands
 *      that perform various debugging operations.
 *
 * $Id: JSwatCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.CommandManager;
import com.bluemarsh.jswat.FieldNotObjectException;
import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.report.Category;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.*;
import com.sun.jdi.request.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the abstract class for command classes within JSwat.
 *
 * @author  Nathan Fiedler
 */
public abstract class JSwatCommand {
    /** List of primitive type names. */
    protected static List primitiveTypeNames;
    /** Instance of JSwat. */
    protected static JSwat swat;
    /** Reporting Category for logging debugging events. */
    protected static Category logCategory;
    /** Name of this command, if it has been determined earlier.
     * Use getCommandName() to retrieve this value. */
    private String commandName;

    static {
        swat = JSwat.instanceOf();
        logCategory = Category.instanceOf("command");
        // Initialize the list of primitive type names.
        primitiveTypeNames = new ArrayList();
        primitiveTypeNames.add("boolean");
        primitiveTypeNames.add("byte");
        primitiveTypeNames.add("char");
        primitiveTypeNames.add("short");
        primitiveTypeNames.add("int");
        primitiveTypeNames.add("long");
        primitiveTypeNames.add("float");
        primitiveTypeNames.add("double");
    }

    /**
     * Tests if the two lists of arguments match in types.
     *
     * @param  argNames   Names of arguments.
     * @param  arguments  List of method arguments.
     * @return  True of two lists match, false otherwise.
     */
    protected static boolean argumentsMatch(List argNames, List arguments) {
        if (argNames.size() != arguments.size()) {
            return false;
        }
        for (int i = argNames.size() - 1; i >= 0; i--) {
            String argTypeName = (String)argNames.get(i);
            Value value = (Value)arguments.get(i);
            // For now we require an exact match.
            if (value == null) {
                // Null values can be passed to any non-primitive argument
                if (primitiveTypeNames.contains(argTypeName)) {
                    return false;
                }
            } else if (!argTypeName.equals(value.type().name())) {
                return false;
            }
        }
        return true;
    } // argumentsMatch

    /**
     * Creates an object of the given class in the debuggee VM.
     *
     * @param  refType    Class of object to create.
     * @param  arguments  List of argument values to pass to constructor.
     * @param  thread     Thread in which to create new object. Must be
     *                    suspended before calling this method.
     * @return  New object's reference.
     * @exception  ClassNotLoadedException
     *             Thrown if any argument type has not yet been loaded.
     * @exception  IncompatibleThreadStateException
     *             Thrown if the thread is not suspended.
     * @exception  InvalidTypeException
     *             Thrown if argument types are not assignment-compatible
     *             with the arguments in the constructor.
     * @exception  InvocationException
     *             Thrown if there's an error calling the class's constructor.
     */
    public static ObjectReference createObject(ReferenceType refType,
                                               List arguments,
                                               ThreadReference thread)
        throws ClassNotLoadedException,
               IncompatibleThreadStateException,
               InvalidTypeException,
               InvocationException {
        if (!(refType instanceof ClassType)) {
            throw new InvalidTypeException
                ("Cannot create instance of interface " + refType);
        }

        // Get the methods declared in this class. Find the ones
        // that are constructors, discarding the rest.
        ClassType classType = (ClassType) refType;
        List methods = new ArrayList(classType.methods());
        for (int i = methods.size() - 1; i >= 0; i--) {
            Method method = (Method)methods.get(i);
            if (!method.isConstructor()) {
                // Remove all non-constructors from our list.
                methods.remove(i);
            }
        }

        // Find the matching constructor.
        Method constructor = resolveOverload(methods, arguments);

        // Try to create a new instance in the debuggee VM.
        return classType.newInstance(thread, constructor, arguments, 0);
    } // createObject

    /**
     * Return a short, one-line description of this command.
     *
     * @return  One-line description of command.
     */
    public String description() {
        return Bundle.getString(getCommandName() + "Desc");
    } // description

    /**
     * Retrieves the name of this command (similar to the class name).
     *
     * @return  name of this command class.
     */
    public String getCommandName() {
        if (commandName == null) {
            commandName = getClass().getName();
            int dot = commandName.lastIndexOf('.');
            // Length of the "Command" suffix is 7.
            int end = commandName.length() - 7;
            commandName = commandName.substring(dot + 1, end);
        }
        return commandName;
    } // getCommandName

    /**
     * Return a list of classes and interfaces whose names match the
     * given pattern. The pattern syntax is a fully-qualified class
     * name in which the first part or last part may optionally be a
     * "*" character, to match any sequence of characters.
     *
     * @param  session  current Session.
     * @param  pattern  Classname pattern, optionally prefixed or
     *                  suffixed with "*" to match anything.
     * @return  List of ReferenceType objects.
     * @exception  NotActiveException
     *             Thrown if the session is not active.
     */
    public static List findClassesByPattern(Session session, String pattern)
        throws NotActiveException {
        if (!session.isActive()) {
            throw new NotActiveException();
        }

        List result = new ArrayList();
        if (pattern.indexOf('*') == -1) {
            // It's just a class name, try to find it.
            return session.getConnection().getVM().classesByName(pattern);
        } else {
            // Wild card exists, have to search manually.
            boolean head = true;
            if (pattern.startsWith("*")) {
                pattern = pattern.substring(1);
            } else if (pattern.endsWith("*")) {
                pattern = pattern.substring(0, pattern.length() - 1);
                head = false;
            }
            VirtualMachine vm = session.getConnection().getVM();
            vm.suspend();
            List classes = vm.allClasses();
            vm.resume();
            Iterator iter = classes.iterator();
            while (iter.hasNext()) {
                ReferenceType type = (ReferenceType) iter.next();
                if (head && type.name().endsWith(pattern)) {
                    result.add(type);
                } else if (!head && type.name().startsWith(pattern)) {
                    result.add(type);
                }
            }
            return result;
        }
    } // findClassesByPattern

    /**
     * Display helpful information about this command, including
     * the possible arguments and their interpretation. First
     * prints the command's description.
     *
     * @param  out  output to write help message to.
     */
    public void help(Log out) {
        // Call description to create the commandName if not known.
        StringBuffer buf = new StringBuffer(512);
        buf.append(description());
        buf.append('\n');
        help(out, buf);
    } // help

    /**
     * Display helpful information about this command, including
     * the possible arguments and their interpretation.
     *
     * @param  out  output to write help message to.
     * @param  buf  string buffer to use for printing.
     */
    protected void help(Log out, StringBuffer buf) {
        String commandName = getCommandName();
        // Use the command name to get the help strings. When a
        // help string is null, we stop trying to get more help.
        int i = 1;
        while (true) {
            String helpStr = Bundle.getString(commandName + "Help" + i);
            if ((helpStr == null) || (helpStr.length() == 0)) {
                break;
            }
            buf.append(helpStr);
            buf.append('\n');
            i++;
        }
        out.write(buf.toString());
    } // help

    /**
     * Display helpful information about this command because the
     * user failed to provide all of the necessary arguments.
     *
     * @param  out  output to write help message to.
     */
    public void missingArgs(Log out) {
        StringBuffer buf = new StringBuffer(512);
        buf.append(Bundle.getString("missingArguments"));
        buf.append('\n');
        help(out, buf);
    } // missingArgs

    /**
     * Perform the command using the given arguments. Any output
     * that needs to be displayed should go to the <code>out</code>
     * object.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public abstract void perform(Session session, StringTokenizer args,
                                 Log out);

    /**
     * Called by the CommandManager when new input has been received
     * from the user. This asynchronously follows a call to
     * <code>CommandManager.grabInput()</code>
     *
     * @param  session  JSwat session on which to operate.
     * @param  out      Output to write messages to.
     * @param  cmdman  CommandManager that's calling us.
     * @param  input   Input from user.
     */
    public void receiveInput(Session session, Log out,
                             CommandManager cmdman, String input) {
    } // receiveInput

    /** Called by the CommandManager to get the prompt to display when
     * input is being grabbed by this command.
     *
     * @return Prompt string to use while input is grabbed by this command.
     */
    public String getPromptString() {
        return getCommandName();
    } // getPromptString

    /**
     * Finds a method that matches the given arguments.
     *
     * @param  overloads  List of overloaded methods.
     * @param  arguments  List of arguments for method.
     * @return  Matching method, or null if none found.
     */
    protected static Method resolveOverload(List overloads, List arguments) {
        for (int i = overloads.size() - 1; i >= 0; i--) {
            Method method = (Method)overloads.get(i);
            List argNames = method.argumentTypeNames();
            if (argumentsMatch(argNames, arguments)) {
                return method;
            }
        }
        return null;
    } // resolveOverload
} // JSwatCommand
