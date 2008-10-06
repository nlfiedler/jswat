/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 * $Id: Classes.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.breakpoint.AmbiguousMethodException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Class Classes provides a set of utility functions for dealing with
 * classes and their members.
 *
 * @author  Nathan Fiedler
 */
public class Classes {
    /** An empty List object. */
    private static final List EMPTY_LIST = new LinkedList();
    /** Our Preferences node. */
    private static Preferences preferences;

    static {
        preferences = Preferences.userRoot().node("com/bluemarsh/jswat/util");
    }

    /**
     * Call the <code>toString()</code> method on the given object.
     * <em>Note:</em> This method invalidates any
     * <code>StackFrame</code> instances for the given thread. The
     * caller will have to retrieve a new instance of
     * <code>StackFrame</code>, as needed.
     *
     * @param  obj     object on which to call toString().
     * @param  thread  thread on which to call toString() on obj.
     * @return  Return value from <code>obj.toString()</code>, or
     *          null if there was any problem.
     * @throws  Exception
     *          just about anything could go wrong.
     */
    public static String callToString(ObjectReference obj,
                                      ThreadReference thread)
        throws Exception {
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
            Object o = invokeMethod(obj, type, thread, method, EMPTY_LIST);
            String s = o == null ? "null" : o.toString();
            return Strings.trimQuotes(s);
        } catch (IncompatibleThreadStateException itse) {
            // This may happen if the thread is not suspended by JDI.
            return null;
        }
    } // callToString

    /**
     * Return a list of classes and interfaces whose names match the
     * given pattern. The pattern syntax is a fully-qualified class name
     * in which the first part or last part may optionally be a "*"
     * character, to match any sequence of characters.
     *
     * @param  vm       debuggee virtual machine.
     * @param  pattern  Classname pattern, optionally prefixed or
     *                  suffixed with "*" to match anything.
     * @return  List of ReferenceType objects.
     */
    public static List findClassesByPattern(VirtualMachine vm,
                                            String pattern) {

        List result = new ArrayList();
        if (pattern.indexOf('*') == -1) {
            // It's just a class name, try to find it.
            return vm.classesByName(pattern);
        } else {
            // Wild card exists, have to search manually.
            boolean head = true;
            if (pattern.startsWith("*")) {
                pattern = pattern.substring(1);
            } else if (pattern.endsWith("*")) {
                pattern = pattern.substring(0, pattern.length() - 1);
                head = false;
            }
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
     * Attempt an unambiguous match of the method name and argument
     * specification to a method. If no arguments are specified, the
     * method must not be overloaded. Otherwise, the argument types must
     * match exactly.
     *
     * @param  clazz          class in which to find method.
     * @param  methodName     name of method to find.
     * @param  argumentTypes  list of method argument types in JNI form.
     * @param  fuzzySearch    true to allow widening numbers to find a
     *                        match, and to equate wrapper classes with
     *                        equivalent primitive types.
     * @return  desired method, or null if not found.
     * @throws  AmbiguousMethodException
     *          if the method is overloaded.
     * @throws  ClassNotLoadedException
     *          if an argument type has not been loaded.
     * @throws  InvalidTypeException
     *          if an argument type was not recognized.
     * @throws  NoSuchMethodException
     *          if the method could not be found.
     */
    public static Method findMethod(ReferenceType clazz, String methodName,
                                    List argumentTypes, boolean fuzzySearch)
        throws AmbiguousMethodException,
               ClassNotLoadedException,
               InvalidTypeException,
               NoSuchMethodException {

        VirtualMachine vm = clazz.virtualMachine();
        List methods = clazz.methodsByName(methodName);
        // The 'best' matching method found (and how many).
        Method bestMethod = null;
        int bestScore = -1;
        int bestCount = 0;
        Iterator iter = methods.iterator();
        while (iter.hasNext()) {
            Method candidate = (Method) iter.next();
            List candidateTypes = candidate.argumentTypes();
            if (candidateTypes.size() != argumentTypes.size()) {
                continue;
            }
            int score = 0;
            for (int ii = 0; ii < candidateTypes.size(); ii++) {
                String givenSig = (String) argumentTypes.get(ii);
                if (givenSig.equals("*")) {
                    // Allow '*' to match any argument type for breakpoints.
                    score += 5;
                    continue;
                }
                Type expectedType = (Type) candidateTypes.get(ii);
                if (expectedType instanceof ReferenceType
                    && givenSig.equals("<null>")) {
                    // Allow null to match any class.
                    score += 1;
                    continue;
                }

                // Compare the argument types for similarity.
                Type givenType = Types.signatureToType(givenSig, vm);
                if (givenType == null) {
                    throw new InvalidTypeException(givenSig);
                }
                String primSig = Types.wrapperToPrimitive(givenSig);
                String expectedSig = expectedType.signature();
                if (givenSig.equals(expectedSig)) {
                    score += 5;
                } else if (fuzzySearch && primSig.equals(expectedSig)) {
                    score += 4;
                } else if (givenType instanceof ReferenceType
                           && Types.isCompatible(
                               expectedSig, (ReferenceType) givenType)) {
                    score += 3;
                } else if (fuzzySearch
                           && Types.canWiden(expectedSig, givenType)) {
                    score += 2;
                } else {
                    // They don't match at all, skip this method.
                    score = Integer.MIN_VALUE;
                    break;
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestMethod = candidate;
                bestCount = 1;
            } else if (score == bestScore) {
                bestCount++;
            }
        }

        if (bestCount > 1) {
            throw new AmbiguousMethodException(methodName);
        } else if (bestCount == 1) {
            return bestMethod;
        } else if (argumentTypes.size() == 0 && methods.size() > 0) {
            if (methods.size() == 1) {
                return (Method) methods.get(0);
            } else {
                throw new AmbiguousMethodException(methodName);
            }
        } else {
            throw new NoSuchMethodException(methodName);
        }
    } // findMethod

    /**
     * Perform the hotswap operation on the given class. If anything
     * goes wrong, it will be reported as an exception.
     *
     * @param  clazz  the class to be hotswapped.
     * @param  code   the bytecode to replace the class.
     * @param  vm     virtual machine.
     * @throws  ClassCircularityError
     *          if the class dependencies are circular.
     * @throws  ClassFormatError
     *          if the class format is wrong.
     * @throws  IOException
     *          if there was an error reading the input stream.
     * @throws  NoClassDefFoundError
     *          if the class definition was not found.
     * @throws  UnsupportedClassVersionError
     *          if the class is the wrong version.
     * @throws  UnsupportedOperationException
     *          if the hotswap operation is not supported.
     * @throws  VerifyError
     *          if the bytecode verfication failed.
     */
    public static void hotswap(ReferenceType clazz, InputStream code,
                               VirtualMachine vm)
        throws ClassCircularityError,
               ClassFormatError,
               IOException,
               NoClassDefFoundError,
               UnsupportedClassVersionError,
               UnsupportedOperationException,
               VerifyError {

        // Load the byte-code from the class file.
        byte[] byteCode = new byte[1024];
        int totalBytesRead = 0;
        int length = byteCode.length;
        while (true) {
            int bytesRead = code.read(byteCode, totalBytesRead, length);
            if (bytesRead == -1) {
                break;
            }

            totalBytesRead += bytesRead;
            if (totalBytesRead >= byteCode.length) {
                byte[] temp = new byte[totalBytesRead * 2];
                System.arraycopy(byteCode, 0, temp, 0, byteCode.length);
                byteCode = temp;
            }
            length = byteCode.length - totalBytesRead;
        }
        byte[] temp = new byte[totalBytesRead];
        System.arraycopy(byteCode, 0, temp, 0, temp.length);
        byteCode = temp;

        // Do the actual hotswap operation.
        Map map = new HashMap();
        map.put(clazz, byteCode);
        vm.redefineClasses(map);
    } // hotswap

    /**
     * Invokes the given method on the class or object. If the method
     * call times out, this method returns <code>null</code>. Uses the
     * timeout value defined by the user.
     *
     * @param  objref      object reference, if non-static method.
     * @param  reftype     reference type, if static method.
     * @param  thread      thread on which to invoke method.
     * @param  method      method to be invoked.
     * @param  methodArgs  arguments to the method.
     * @return  the return value of the method.
     * @throws  Exception
     *          if something goes wrong.
     */
    public static Object invokeMethod(ObjectReference objref,
                                      ReferenceType reftype,
                                      ThreadReference thread,
                                      Method method,
                                      List methodArgs) throws Exception {
        int timeout = preferences.getInt("invocationTimeout",
                                         Defaults.INVOCATION_TIMEOUT);
        return invokeMethod(objref, reftype, thread, method, methodArgs,
                            timeout);
    } // invokeMethod

    /**
     * Invokes the given method on the class or object. If the method
     * call times out, this method returns <code>null</code>.
     *
     * @param  objref      object reference, if non-static method.
     * @param  reftype     reference type, if static method.
     * @param  thread      thread on which to invoke method.
     * @param  method      method to be invoked.
     * @param  methodArgs  arguments to the method.
     * @param  timeout     milliseconds to wait for method to return.
     * @return  the return value of the method.
     * @throws  Exception
     *          if something goes wrong.
     */
    public static Object invokeMethod(ObjectReference objref,
                                      ReferenceType reftype,
                                      ThreadReference thread,
                                      Method method,
                                      List methodArgs,
                                      int timeout) throws Exception {
        return invokeMethod(objref, reftype, thread, method, methodArgs,
                            timeout, ObjectReference.INVOKE_SINGLE_THREADED);
    } // invokeMethod

    /**
     * Invokes the given method on the class or object. If the method
     * call times out, this method returns <code>null</code>.
     *
     * @param  objref      object reference, if non-static method.
     * @param  reftype     reference type, if static method.
     * @param  thread      thread on which to invoke method.
     * @param  method      method to be invoked.
     * @param  methodArgs  arguments to the method.
     * @param  timeout     milliseconds to wait for method to return.
     * @param  flags       flags to pass to JPDA's invokeMethod().
     * @return  the return value of the method.
     * @throws  Exception
     *          if something goes wrong.
     */
    public static Object invokeMethod(ObjectReference objref,
                                      ReferenceType reftype,
                                      ThreadReference thread,
                                      Method method, List methodArgs,
                                      int timeout, int flags)
        throws Exception {

        Invoker invoker = new Invoker(objref, reftype, thread, method,
                                      methodArgs, flags);
        Thread th = new Thread(invoker);
        th.start();
        synchronized (invoker) {
            try {
                invoker.wait(timeout);
            } catch (InterruptedException ie) {
                // ignored
            }
        }
        Exception excp = invoker.getException();
        if (excp != null) {
            throw excp;
        } else {
            return invoker.getValue();
        }
    } // invokeMethod

    /**
     * Invokes a method and returns the result.
     */
    protected static class Invoker implements Runnable {
        /** Return value from invoked method. */
        private Value value;
        /** The exception thrown, if any. */
        private Exception exception;
        /** Object reference, if non-static method. */
        private ObjectReference objref;
        /** Reference type, if static method. */
        private ReferenceType reftype;
        /** Thread on which to invoke method. */
        private ThreadReference thread;
        /** Method to be invoked. */
        private Method method;
        /** Arguments to the method. */
        private List methodArgs;
        /** Flags to pass to invokeMethod(). */
        private int flags;

        /**
         * Constructs an Invoker to invoke the given method.
         *
         * @param  objref      object reference, if non-static method.
         * @param  reftype     reference type, if static method.
         * @param  thread      thread on which to invoke method.
         * @param  method      method to be invoked.
         * @param  methodArgs  arguments to the method.
         * @param  flags       flags to pass to invokeMethod().
         */
        public Invoker(ObjectReference objref, ReferenceType reftype,
                       ThreadReference thread, Method method,
                       List methodArgs, int flags) {
            this.objref = objref;
            this.reftype = reftype;
            this.thread = thread;
            this.method = method;
            this.methodArgs = methodArgs;
            this.flags = flags;
        } // Invoker

        /**
         * Returns the exception that occurred while invoking the
         * method, or null if no exception occurred.
         *
         * @return  thrown exception.
         */
        public Exception getException() {
            return exception;
        } // getException

        /**
         * Returns the return value of the invoked method.
         *
         * @return  method return value.
         */
        public Value getValue() {
            return value;
        } // getValue

        /**
         * Invoke the desired method.
         */
        public void run() {
            // Clear these in case we are called more than once.
            value = null;
            exception = null;
            try {
                if (objref == null) {
                    // Static method invocation.
                    if (reftype instanceof ClassType) {
                        ClassType clazz = (ClassType) reftype;
                        value = clazz.invokeMethod(thread, method,
                                                   methodArgs, flags);
                    } else {
                        exception = new IllegalArgumentException(
                            "Specified class is an array or interface.");
                    }

                } else {
                    // Non-static method invocation.
                    value = objref.invokeMethod(thread, method, methodArgs,
                                                flags);
                }
                synchronized (this) {
                    notify();
                }
            } catch (Exception e) {
                exception = e;
            }
        } // run
    } // Invoker
} // Classes
