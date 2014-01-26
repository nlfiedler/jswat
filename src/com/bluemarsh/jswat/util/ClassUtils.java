/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Nathan Fiedler
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
 * MODULE:      Utilities
 * FILE:        ClassUtils.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/19/01        Initial version
 *      nf      03/08/02        Added findMethod() method
 *      nf      03/18/02        Added callToString() method
 *      nf      03/20/02        Added isPrimitive() method
 *      nf      03/22/02        Added fuzzy-logic to findMethod()
 *
 * DESCRIPTION:
 *      This file defines a Class utility class.
 *
 * $Id: ClassUtils.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.bluemarsh.config.ConfigureListener;
import com.bluemarsh.config.JConfigure;
import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.breakpoint.AmbiguousClassSpecException;
import com.bluemarsh.jswat.breakpoint.AmbiguousMethodException;
import com.sun.jdi.*;
import java.util.*;

/**
 * Class ClassUtils provides a set of utility functions for dealing
 * with classes.
 *
 * @author  Nathan Fiedler
 */
public class ClassUtils implements ConfigureListener {
    /** Reference to the single instance of this class. */
    protected static ClassUtils instance;
    /** Indicates whether we shoud trim class names or not. */
    protected static boolean trimClassNames;

    /**
     * Constructor that sets up the instance to listen for configuration
     * changes.
     */
    protected ClassUtils() {
        JConfigure config = JSwat.instanceOf().getJConfigure();
        config.addListener(this);
        configurationChanged();
    } // ClassUtils

    /**
     * Call the <code>toString()</code> method on the given object.
     *
     * @param  obj     object on which to call toString().
     * @param  thread  thread on which to call toString() on obj.
     * @return  Return value from <code>obj.toString()</code>, or
     *          null if there was any problem.
     */
    public static String callToString(ObjectReference obj,
                                      ThreadReference thread) {
        try {
            ReferenceType type = obj.referenceType();
            // Find the zero-arg toString() method that returns a String.
            List methods = type.methodsByName(
                "toString", "()Ljava/lang/String;");
            // First one better be the right one.
            Method method = (Method) methods.get(0);
            // Must invoke single-threaded or other threads will run
            // briefly and strange things will seem to happen from
            // the debugger user's point of view.
            Value retval = obj.invokeMethod(
                thread, method, new LinkedList(),
                ObjectReference.INVOKE_SINGLE_THREADED);
            return retval == null ? "null" : retval.toString();
        } catch (IncompatibleThreadStateException itse) {
            // This may happen if the thread is not suspended by JDI.
            itse.printStackTrace();
            return null;
        } catch (InvalidTypeException ite) {
            // This cannot happen because there are no arguments.
            ite.printStackTrace();
            return null;
        } catch (ClassNotLoadedException cnle) {
            // This cannot happen.
            cnle.printStackTrace();
            return null;
        } catch (InvocationException ie) {
            // This is unlikely for toString().
            ie.printStackTrace();
            return null;
        }
    } // callToString

    /**
     * Invoked when the configuration has been accepted by the user.
     */
    public void configurationChanged() {
        JConfigure config = JSwat.instanceOf().getJConfigure();
        trimClassNames = config.getBooleanProperty
            ("appearance.trimClassNames", true);
    } // configurationChanged

    /**
     * Attempt an unambiguous match of the method name and argument
     * specification to a method. If no arguments are specified, the
     * method must not be overloaded. Otherwise, the argument types
     * must match exactly.
     *
     * @param  clazz       class in which to find method.
     * @param  methodId    name of method to find.
     * @param  methodArgs  list of method argument types.
     * @return  Method if found, null otherwise.
     * @exception  AmbiguousClassSpecException
     *             Thrown if the given pattern matches more than one class.
     * @exception  AmbiguousMethodException
     *             Thrown if the method is overloaded.
     * @exception  NoSuchMethodException
     *             Thrown if the method could not be found.
     */
    public static Method findMethod(ReferenceType clazz,
                                    String methodId,
                                    List methodArgs)
        throws AmbiguousClassSpecException,
               AmbiguousMethodException,
               NoSuchMethodException {

        // Normalize the argument list before comparing.
        List argTypeNames = null;
        if (methodArgs != null) {
            argTypeNames = new ArrayList(methodArgs.size());
            int size = methodArgs.size();
            for (int i = 0; i < size; i++) {
                String name = (String) methodArgs.get(i);
                if (!isPrimitive(name)) {
                    name = normalizeArgTypeName(name,
                                                clazz.virtualMachine());
                }
                argTypeNames.add(name);
            }
        }

        // Examine each method in the class.
        int size = clazz.methods().size();
        // A method with matching name (and possibly signature).
        Method match = null;
        // The method has the same name and signature.
        boolean exact = false;
        // More than one means the method is overloaded.
        int matchCount = 0;
        for (int i = 0; i < size; i++) {
            Method candidate = (Method) clazz.methods().get(i);
            if (candidate.name().equals(methodId)) {
                matchCount++;
                match = candidate;
                // Check if argument types are the same, if any are given.
                if (argTypeNames != null) {
                    List candidateNames = candidate.argumentTypeNames();

                    // Check that the number of arguments is the same.
                    if (candidateNames.size() == argTypeNames.size()) {
                        // Assume the arguments will match.
                        boolean argsMatch = true;
                        // Compare the list of arguments by type name.
                        for (int j = 0; j < candidateNames.size(); j++) {
                            String arg1 = (String) candidateNames.get(j);
                            String arg2 = (String) argTypeNames.get(j);
                            if (!arg1.equals(arg2)) {
                                // See if we can match the arguments using
                                // fuzzy logic. First try the primitives
                                // (e.g. "int" == "java.lang.Integer"),
                                // then try up-casting the types.
                                if (!tryPrimitives(arg1, arg2) &&
                                    !tryUpCasting(arg1, arg2)) {
                                    argsMatch = false;
                                    break;
                                }
                            }
                        }
                        // Found a matching method.
                        if (argsMatch) {
                            exact = true;
                            break;
                        }
                    }
                }
            }
        }

        if (exact) {
            return match;
        } else if ((argTypeNames == null) && (matchCount > 0)) {
            if (matchCount == 1) {
                // There's only one match, good enough for us.
                return match;
            } else {
                // No args were given to make an exact match.
                throw new AmbiguousMethodException();
            }
        } else {
            throw new NoSuchMethodException(methodId);
        }
    } // findMethod

    /**
     * Find the ReferenceType object for a given class.
     *
     * @param  cname  Class identifier for which to get reference.
     * @param  vm     Virtual machine from which to get reference.
     * @return  ReferenceType for this class, or null if none.
     * @exception  AmbiguousClassSpecException
     *             Thrown if the given pattern matches more than one class.
     */
    protected static ReferenceType getReferenceFromName(String cname,
                                                        VirtualMachine vm)
        throws AmbiguousClassSpecException {
        ReferenceType cls = null;
        if (cname.startsWith("*.")) {
            // It's a pattern match if ID starts with '*'.
            // The first loaded class whose name matches is selected.
            cname = cname.substring(1);
            vm.suspend();
            List classes = vm.allClasses();
            vm.resume();

            // Go through all of the classes looking for one that
            // matches what we were given.
            Iterator iter = classes.iterator();
            while (iter.hasNext()) {
                ReferenceType type = ((ReferenceType) iter.next());
                // Check only the end of the ID, for pattern match.
                if (type.name().endsWith(cname)) {
                    cls = type;
                    break;
                }
            }
        } else {
            // It's the whole class name, that's easy.
            List classes = vm.classesByName(cname);
            // We don't care if this is an interface or array.
            if (classes.size() > 0) {
                if (classes.size() > 1) {
                    // Oops, more than one class matched.
                    throw new AmbiguousClassSpecException();
                }
                cls = (ReferenceType) classes.get(0);
            }
        }
        return cls;
    } // getReferenceFromName

    /**
     * Returns the reference to the single instance of this class.
     * If an instance does not exist it will be created.
     *
     * @return  the instance of this class.
     */
    public static ClassUtils instanceOf() {
        // Yeah yeah, double-checked locking isn't perfect.
        // If you can find a better solution, tell the world.
        if (instance == null) {
            synchronized (ClassUtils.class) {
                if (instance == null) {
                    instance = new ClassUtils();
                }
            }
        }
        return instance;
    } // instanceOf

    /**
     * Determine if the given string is a valid Java identifier.
     *
     * @param  s  string to validate.
     * @return  true if string is a valid Java identifier.
     */
    public static boolean isJavaIdentifier(String s) {
        if (s.length() == 0) {
            return false;
        }
        // First character of identifier is a special case.
        if (!Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        // Now check all other characters of the identifier.
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    } // isJavaIdentifier

    /**
     * Returns true if the passed string is a primitive type.
     *
     * @param  s  string suspected of naming a primitive type.
     * @return  true if 's' names a primitive, false otherwise.
     */
    public static boolean isPrimitive(String s) {
        if (s.equals("boolean") ||
            s.equals("byte") ||
            s.equals("char") ||
            s.equals("double") ||
            s.equals("float") ||
            s.equals("int") ||
            s.equals("long") ||
            s.equals("short")) {
            return true;
        } else {
            return false;
        }
    } // isPrimitive

    /**
     * Returns just the name of the class, without the package name.
     *
     * @param  cname  Name of class, possibly fully-qualified.
     * @return  Just the class name.
     */
    public static String justTheName(String cname) {
        // Make sure we're instantiated and values filled in.
        instanceOf();
        if (trimClassNames) {
            int i = cname.lastIndexOf('.');
            if (i > 0) {
                return cname.substring(i + 1);
            }
        }
        return cname;
    } // justTheName

    /**
     * Remove unneeded spaces and expand class names to fully 
     * qualified names, if necessary and possible.
     *
     * @param  name  Name of class.
     * @param  vm    Virtual machine to normalize against.
     * @return  Fully qualified name of class.
     * @exception  AmbiguousClassSpecException
     *             Thrown if the given pattern matches more than one class.
     */
    protected static String normalizeArgTypeName(String name,
                                                 VirtualMachine vm)
        throws AmbiguousClassSpecException {
        name = name.trim();
        // Separate type name and any array modifiers.
        StringBuffer className = new StringBuffer();
        StringBuffer arrayPart = new StringBuffer();

        // Get the name of the class (the part before whitespace or [).
        int i = 0;
        while (i < name.length()) {
            char c = name.charAt(i);
            if (Character.isWhitespace(c) || (c == '[')) {
                break;
            }
            className.append(c);
            i++;
        }

        // Get the array portion of the name (just [] with whitespace).
        while (i < name.length()) {
            char c = name.charAt(i);
            if ((c == '[') || (c == ']')) {
                arrayPart.append(c);
            } else if (!Character.isWhitespace(c)) {
                throw new IllegalArgumentException("Invalid array type");
            }
            i++;
        }

        // If there's no package name given, try to make the class
        // name fully qualified.
        name = className.toString();
        if ((name.indexOf('.') == -1) || name.startsWith("*.")) {
            ReferenceType argClass = getReferenceFromName(name, vm);
            if (argClass != null) {
                name = argClass.name();
            }
        }
        return name + arrayPart.toString();
    } // normalizeArgTypeName

    /**
     * Compare the named types to determine if they are compatible by
     * one being a class type representing the primitive form of the
     * other. That is, if one argument is a primitive type and the
     * other is the class type that represents that primitive, return
     * true. Otherwise, return false.
     *
     * @param  candidate  type we are trying to match.
     * @param  given      type the user specified, which may be up-cast.
     * @return  true if types are equivalent, false otherwise.
     */
    protected static boolean tryPrimitives(String candidate, String given) {
        if (candidate.equals("byte") && given.equals("java.lang.Byte") ||
            candidate.equals("short") && given.equals("java.lang.Short") ||
            candidate.equals("int") && given.equals("java.lang.Integer") ||
            candidate.equals("long") && given.equals("java.lang.Long") ||
            candidate.equals("float") && given.equals("java.lang.Float") ||
            candidate.equals("double") && given.equals("java.lang.Double") ||
            candidate.equals("boolean") && given.equals("java.lang.Boolean") ||
            candidate.equals("char") && given.equals("java.lang.Character")) {
            return true;
        } else {
            return false;
        }
    } // tryPrimitives

    /**
     * Compare the named types to determine if up-casting is possible.
     * Note that this only works with class types that represent primitive
     * numeric types (byte, short, integer, long, float, double).
     *
     * @param  candidate  type we are trying to match.
     * @param  given      type the user specified, which may be up-cast.
     * @return  true if up-cast is possible, false otherwise.
     */
    protected static boolean tryUpCasting(String candidate, String given) {
        // Granted, string comparisons aren't exactly quick, but then
        // we hardly expect this to be invoked very often.
        if (candidate.equals("java.lang.Short") ||
            candidate.equals("short")) {
            if (given.equals("java.lang.Byte") ||
                given.equals("byte")) {
                return true;
            }
        } else if (candidate.equals("java.lang.Integer") ||
                   candidate.equals("int")) {
            if (given.equals("java.lang.Byte") ||
                given.equals("java.lang.Short") ||
                given.equals("byte") ||
                given.equals("short")) {
                return true;
            }
        } else if (candidate.equals("java.lang.Long") ||
                   candidate.equals("long")) {
            if (given.equals("java.lang.Byte") ||
                given.equals("java.lang.Short") ||
                given.equals("java.lang.Integer") ||
                given.equals("byte") ||
                given.equals("short") ||
                given.equals("integer")) {
                return true;
            }
        } else if (candidate.equals("java.lang.Double") ||
                   candidate.equals("double")) {
            if (given.equals("java.lang.Float") ||
                given.equals("float")) {
                return true;
            }
        }
        return false;
    } // tryUpCasting
} // ClassUtils
