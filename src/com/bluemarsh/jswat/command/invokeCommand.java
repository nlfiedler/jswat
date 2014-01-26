/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: invokeCommand.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.AmbiguousClassSpecException;
import com.bluemarsh.jswat.breakpoint.AmbiguousMethodException;
import com.bluemarsh.jswat.util.ClassUtils;
import com.bluemarsh.jswat.util.VariableUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMMismatchException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the class that handles the 'invoke' command.
 *
 * @author  Nathan Fiedler
 */
public class invokeCommand extends JSwatCommand {

    /**
     * Returns a list of Strings representing the types of the
     * arguments in the list given.
     *
     * @param  arguments  list of arguments.
     * @return  list of argument types.
     */
    protected List argumentTypes(List arguments) {
        List types = new ArrayList(arguments.size());

        for (int ii = 0; ii < arguments.size(); ii++) {
            Object o = arguments.get(ii);
            if (o instanceof String) {
                types.add("java.lang.String");
            } else if (o instanceof Boolean) {
                types.add("java.lang.Boolean");
            } else if (o instanceof Character) {
                types.add("java.lang.Character");
            } else if (o instanceof Double) {
                types.add("java.lang.Double");
            } else if (o instanceof Float) {
                types.add("java.lang.Float");
            } else if (o instanceof Integer) {
                types.add("java.lang.Integer");
            } else if (o instanceof Long) {
                types.add("java.lang.Long");
            } else if (o instanceof Short) {
                types.add("java.lang.Short");
            } else if (o instanceof Byte) {
                types.add("java.lang.Byte");
            } else if (o instanceof Value) {
                Value v = (Value) o;
                types.add(v.type().signature());
            } else {
                types.add("java.lang.Object");
            }
        }

        return types;
    } // argumentTypes

    /**
     * Translate the given list of arguments into Value instances of
     * the appropriate type.
     */
    protected List mirrorArguments(VirtualMachine vm, List arguments) {
        for (int ii = 0; ii < arguments.size(); ii++) {
            Object o = arguments.get(ii);
            if (o instanceof String) {
                arguments.set(ii, vm.mirrorOf((String) o));
            } else if (o instanceof Boolean) {
                arguments.set(ii, vm.mirrorOf(((Boolean) o).booleanValue()));
            } else if (o instanceof Character) {
                arguments.set(ii, vm.mirrorOf(((Character) o).charValue()));
            } else if (o instanceof Double) {
                arguments.set(ii, vm.mirrorOf(((Double) o).doubleValue()));
            } else if (o instanceof Float) {
                arguments.set(ii, vm.mirrorOf(((Float) o).floatValue()));
            } else if (o instanceof Integer) {
                arguments.set(ii, vm.mirrorOf(((Integer) o).intValue()));
            } else if (o instanceof Long) {
                arguments.set(ii, vm.mirrorOf(((Long) o).longValue()));
            } else if (o instanceof Short) {
                arguments.set(ii, vm.mirrorOf(((Short) o).shortValue()));
            } else if (o instanceof Byte) {
                arguments.set(ii, vm.mirrorOf(((Byte) o).byteValue()));
            }
            // Else it's a null or variable Value.
        }
        return arguments;
    } // mirrorArguments

    /**
     * Parse the given string of comma-separated arguments and return
     * a list of Objects representing those argument values.
     *
     * @param  argStr  string of arguments.
     * @param  thread  current thread for looking up variables.
     * @return  List of objects.
     * @exception  IllegalArgumentException
     *             thrown if an argument was malformed (such as a
     *             character that was not really a character).
     * @exception  IndexOutOfBoundsException
     *             thrown if argument list is malformed (such as
     *             mismatched quotes).
     * @exception  UnknownExpressionException
     *             thrown if an argument was not understood.
     */
    protected List parseArguments(String argStr, ThreadReference thread) {
        List args = new ArrayList();
        // Look for commas that are not inside single or double-quotes.
        // Handle escaped quote characters.
        int strlen = argStr.length();
        int strlen1 = strlen - 1;
        int prevIdx = 0;
        for (int ii = 0; ii < strlen; ii++) {
            char ch = argStr.charAt(ii);
            if (ch == ',' || ii == strlen1) {
                String elem;
                if (ch == ',') {
                    elem = argStr.substring(prevIdx, ii);
                } else {
                    elem = argStr.substring(prevIdx, ii + 1);
                }
                elem = elem.trim();
                if (elem.length() > 0 && elem.charAt(0) == ',') {
                    // Remove preceding comma.
                    elem = elem.substring(1).trim();
                }
                prevIdx = ii + 1;

                // Is this thing a boolean or null literal?
                if (elem.equals("true")) {
                    args.add(Boolean.TRUE);
                } else if (elem.equals("false")) {
                    args.add(Boolean.FALSE);
                } else if (elem.equals("null")) {
                    args.add(null);
                } else {

                    // Is this thing a number?
                    try {
                        args.add(new Byte(elem));
                        continue;
                    } catch (NumberFormatException nfe) { }
                    try {
                        args.add(new Short(elem));
                        continue;
                    } catch (NumberFormatException nfe) { }
                    try {
                        args.add(new Integer(elem));
                        continue;
                    } catch (NumberFormatException nfe) { }
                    try {
                        args.add(new Long(elem));
                        continue;
                    } catch (NumberFormatException nfe) { }
                    try {
                        args.add(new Float(elem));
                        continue;
                    } catch (NumberFormatException nfe) { }
                    try {
                        args.add(new Double(elem));
                        continue;
                    } catch (NumberFormatException nfe) { }

                    // Is this thing a variable reference?
                    try {
                        Value var = VariableUtils.getValue(elem, thread, 0);
                        args.add(var);
                    } catch (Exception e) {
                        throw new UnknownExpressionException(
                            "unknown argument: " + elem);
                    }
                }

            } else if (ch == '\'') {
                // Start of a character.
                prevIdx = ii + 1;
                boolean okay = false;
                for (int jj = prevIdx; jj < strlen; jj++) {
                    ch = argStr.charAt(jj);
                    if (ch == '\\') {
                        // Skip over escaped things.
                        jj++;
                    } else if (ch == '\'') {
                        // End of character definition.
                        String elem = argStr.substring(prevIdx, jj);
                        args.add(translateChar(elem));
                        okay = true;
                        prevIdx = ii = jj + 1;
                        break;
                    }
                }
                if (!okay) {
                    throw new IllegalArgumentException(
                        "malformed character: " + argStr.substring(prevIdx));
                }

            } else if (ch == '"') {
                // Start of a string.
                prevIdx = ii + 1;
                boolean okay = false;
                for (int jj = prevIdx; jj < strlen; jj++) {
                    ch = argStr.charAt(jj);
                    if (ch == '\\') {
                        // Skip over escaped things.
                        jj++;
                    } else if (ch == '"') {
                        // End of string.
                        String elem = argStr.substring(prevIdx, jj);
                        args.add(translateString(elem));
                        okay = true;
                        prevIdx = ii = jj + 1;
                        break;
                    }
                }
                if (!okay) {
                    throw new IllegalArgumentException(
                        "malformed string: " + argStr.substring(prevIdx));
                }
            }
        }
        return args;
    } // parseArguments

    /**
     *  Perform the 'invoke' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        if (!args.hasMoreTokens()) {
            missingArgs(out);
            return;
        }

        // Get the current thread.
        ContextManager ctxtman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = ctxtman.getCurrentThread();
        if (thread == null) {
            out.writeln(Bundle.getString("noCurrentThread"));
            return;
        }

        // Get the rest of the line as the argument.
        String arg = args.restTrim();

        // Is the argument well-formed?
        int firstParen = arg.indexOf('(');
        int lastParen = arg.lastIndexOf(')');
        if (lastParen <= firstParen) {
            out.writeln(Bundle.getString("invoke.malformedMethod"));
            return;
        }

        ObjectReference objref = null;
        ReferenceType reftype = null;

        // Is first part a variable reference or a class name?
        // Is the first part even given at all?
        String firstPart = arg.substring(0, firstParen);
        int lastDot = firstPart.lastIndexOf('.');
        if (lastDot == -1) {
            // First part is not given, default to class containing
            // current location.
            try {
                if (thread.frameCount() == 0) {
                    out.writeln(Bundle.getString("threadNotRunning"));
                    return;
                }
                StackFrame frame = thread.frame(0);
                objref = frame.thisObject();
                if (objref == null) {
                    Location location = ctxtman.getCurrentLocation();
                    if (location == null) {
                        out.writeln(Bundle.getString(
                            "invoke.unknownLocation"));
                        return;
                    }
                    reftype = location.declaringType();
                    if (reftype == null) {
                        out.writeln(Bundle.getString(
                            "invoke.unknownLocation"));
                        return;
                    }
                } else {
                    reftype = objref.referenceType();
                }
            } catch (IncompatibleThreadStateException itse) {
                out.writeln(Bundle.getString("invoke.badThreadState"));
                return;
            }

        } else {
            String varOrClass = firstPart.substring(0, lastDot);
            try {
                Value variable = VariableUtils.getValue(varOrClass, thread, 0);
                objref = (ObjectReference) variable;
                reftype = objref.referenceType();
            } catch (Exception e) {
                // Maybe it's not a variable reference.
            }
            if (reftype == null) {
                // See if the argument is a class name.
                try {
                    List classes = findClassesByPattern(session, varOrClass);
                    if (classes == null || classes.size() == 0) {
                        out.writeln(Bundle.getString("invoke.badVarOrClass"));
                        return;
                    }
                    reftype = (ReferenceType) classes.get(0);
                } catch (NotActiveException nae) {
                    out.writeln(swat.getResourceString("noActiveSession"));
                    return;
                }
            }
        }

        // Evaluate the arguments and create the argument list.
        List methodArgs = null;
        try {
            String argslist = arg.substring(firstParen + 1, lastParen);
            methodArgs = parseArguments(argslist, thread);
        } catch (IndexOutOfBoundsException ioobe) {
            out.writeln(Bundle.getString("invoke.malformedArguments"));
            out.writeln(ioobe.toString());
            return;
        } catch (IllegalArgumentException iae) {
            out.writeln(Bundle.getString("invoke.malformedArgument"));
            out.writeln(iae.toString());
            return;
        } catch (UnknownExpressionException uee) {
            out.writeln(Bundle.getString("invoke.unknownArgument"));
            out.writeln(uee.getMessage());
            return;
        }

        String methodId = arg.substring(lastDot + 1, firstParen);

        // Make a list of argument types, based on the types of the
        // arguments passed from the user.
        List argTypes = argumentTypes(methodArgs);

        // Locate method in the resolved class. The ClassUtils method
        // will employ fuzzy logic to find a reasonable match. This
        // means it will treat primitives and their equivalent class
        // types as being the same; in addition, it considers the
        // possibility of up-casting numeric types.
        Method method = null;
        try {
            method = ClassUtils.findMethod(reftype, methodId, argTypes);
        } catch (AmbiguousClassSpecException acse) {
            out.writeln(Bundle.getString("invoke.ambiguousClass"));
            return;
        } catch (AmbiguousMethodException acse) {
            out.writeln(Bundle.getString("invoke.ambiguousMethod"));
            return;
        } catch (NoSuchMethodException nsme) {
            out.writeln(Bundle.getString("invoke.noSuchMethod"));
            return;
        }

        // Turn the user values into Mirrors in the debuggee.
        methodArgs = mirrorArguments(session.getVM(), methodArgs);

        // Invoke the method on the class or object.
        Value value = null;
        try {
            if (objref == null) {
                // Static method invocation.
                if (reftype instanceof ClassType) {
                    ClassType clazz = (ClassType) reftype;
                    value = clazz.invokeMethod(thread, method, methodArgs, 0);
                } else {
                    out.writeln(Bundle.getString("invoke.notaClass"));
                }
            } else {
                // Non-static method invocation.
                value = objref.invokeMethod(thread, method, methodArgs, 0);
            }

        } catch (IllegalArgumentException iae) {
            out.writeln(iae.toString());
        } catch (InvalidTypeException ite) {
            out.writeln(ite.toString());
        } catch (ClassNotLoadedException cnle) {
            out.writeln(cnle.toString());
        } catch (IncompatibleThreadStateException itse) {
            out.writeln(itse.toString());
        } catch (InvocationException ie) {
            out.writeln(ie.toString());
        } catch (ObjectCollectedException oce) {
            out.writeln(oce.toString());
        } catch (VMMismatchException vmme) {
            out.writeln(vmme.toString());
        }

        // Display the method return value.
        if (value != null) {
            out.writeln(value.toString());
        }
    } // perform

    /**
     * Translates the given string to a character. Handles character
     * escapes such as \r and Unicode escapes.
     *
     * @param  charStr  string representing a character.
     * @return  the Character.
     * @exception  IllegalArgumentException
     *             thrown if it is not a character.
     */
    protected Character translateChar(String charStr) {
        // May just be a single character.
        if (charStr.length() == 1) {
            return new Character(charStr.charAt(0));
        }
        if (charStr.charAt(0) == '\\') {
            char ch = charStr.charAt(1);
            if (ch == 'b') {
                return new Character('\b');
            } else if (ch == 'f') {
                return new Character('\f');
            } else if (ch == 't') {
                return new Character('\t');
            } else if (ch == 'n') {
                return new Character('\n');
            } else if (ch == 'r') {
                return new Character('\r');
            } else if (ch == 'u') {
                // Unicode character.
                String hex = charStr.substring(2);
                try {
                    int i = Integer.parseInt(hex, 16);
                    return new Character((char) i);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("invalid Unicode: " +
                                                       hex);
                }
            }
        } else {
            throw new IllegalArgumentException("not a character: " + charStr);
        }
        return null;
    } // translateChar

    /**
     * Processes the given string, looking for character escapes and
     * translating them to their actual values. Handles character
     * escapes such as \r and Unicode escapes.
     *
     * @param  str  string to be processed.
     * @return  processed string.
     * @exception  IllegalArgumentException
     *             thrown if an invalid character escape was found.
     * @exception  IndexOutOfBoundsException
     *             thrown if an invalid character escape was found.
     */
    protected String translateString(String str) {
        int strlen = str.length();
        StringBuffer buf = new StringBuffer(strlen);
        for (int ii = 0; ii < strlen; ii++) {
            char ch = str.charAt(ii);
            if (ch == '\\') {
                ii++;
                ch = str.charAt(ii);
                if (ch == 'b') {
                    buf.append('\b');
                } else if (ch == 'f') {
                    buf.append('\f');
                } else if (ch == 't') {
                    buf.append('\t');
                } else if (ch == 'n') {
                    buf.append('\n');
                } else if (ch == 'r') {
                    buf.append('\r');
                } else if (ch == 'u') {
                    // Unicode character.
                    ii++;
                    String hex = str.substring(ii, ii + 4);
                    try {
                        int i = Integer.parseInt(hex, 16);
                        buf.append((char) i);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException(
                            "invalid Unicode: " + hex);
                    }
                    ii += 3; // for loop will increment i again
                }
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    } // translateString

    /**
     * Signals that the parser did not understand the argument being
     * passed. Most likely the expression was not a number, string,
     * character, or variable reference.
     */
    protected class UnknownExpressionException extends RuntimeException {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs an UnknownExpressionException with the specified
         * detailed message.
         *
         * @param  s  the detail message
         */
        public UnknownExpressionException(String s) {
            super(s);
        } // UnknownExpressionException
    } // UnknownExpressionException
} // invokeCommand
