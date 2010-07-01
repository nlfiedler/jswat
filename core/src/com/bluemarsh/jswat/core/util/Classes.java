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
 * The Original Software is the JSwat Core Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2001-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.util;

import com.bluemarsh.jswat.core.CoreSettings;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Class Classes provides a set of utility functions for dealing with
 * classes and their members.
 *
 * @author  Nathan Fiedler
 */
public class Classes {

    /**
     * Creates a new instance of Classes.
     */
    private Classes() {
    }

    /**
     * Return a list of classes and interfaces whose names match the given
     * pattern. The pattern syntax is a fully-qualified class name in which
     * the first part or last part may optionally be a "*" character, to
     * match any sequence of characters.
     *
     * @param  vm       debuggee virtual machine.
     * @param  pattern  Classname pattern, optionally prefixed or
     *                  suffixed with "*" to match anything.
     * @return  List of ReferenceType objects.
     */
    public static List<ReferenceType> findClasses(
            VirtualMachine vm, String pattern) {

        if (pattern.indexOf('*') == -1) {
            // It's just a class name, try to find it.
            return vm.classesByName(pattern);
        } else {
            // Wild card exists, have to search manually.
            List<ReferenceType> result = new ArrayList<ReferenceType>();
            boolean head = true;
            if (pattern.startsWith("*")) {
                pattern = pattern.substring(1);
            } else if (pattern.endsWith("*")) {
                pattern = pattern.substring(0, pattern.length() - 1);
                head = false;
            } else {
                throw new IllegalArgumentException("embedded wildcard not allowed");
            }
            List<ReferenceType> classes = vm.allClasses();
            for (ReferenceType type : classes) {
                if (head && type.name().endsWith(pattern)) {
                    result.add(type);
                } else if (!head && type.name().startsWith(pattern)) {
                    result.add(type);
                }
            }
            return result;
        }
    }

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
     * @throws  InvalidTypeException
     *          if an argument type was not recognized.
     * @throws  NoSuchMethodException
     *          if the method could not be found.
     */
    public static Method findMethod(ReferenceType clazz, String methodName,
            List<String> argumentTypes, boolean fuzzySearch)
            throws AmbiguousMethodException,
            InvalidTypeException,
            NoSuchMethodException {

        VirtualMachine vm = clazz.virtualMachine();
        boolean constructor = false;
        if (methodName.equals(Names.getShortClassName(clazz.name()))) {
            // Special case for what appears to be a constructor.
            constructor = true;
        }
        // Need to perform our own method name matching since JDI is broken
        // (does not differentiate constructors from initializers).
        List<Method> methods = clazz.methods();
        // The 'best' matching method found (and how many).
        Method bestMethod = null;
        int bestScore = -1;
        int bestCount = 0;
        Iterator<Method> iter = methods.iterator();
        while (iter.hasNext()) {
            Method candidate = iter.next();
            if (constructor) {
                // User specified a constructor, don't bother checking the
                // name since it may be <init> instead of the class name.
                if (!candidate.isConstructor()) {
                    // But the method has to be a constructor.
                    continue;
                }
            } else if (!methodName.equals(candidate.name())) {
                // Method name does not match. This will pass the <init>
                // initializer methods, since they are not constructors
                // and the user must specifically enter "<init>".
                continue;
            }
            List<String> candidateTypes = candidate.argumentTypeNames();
            if (candidateTypes.size() != argumentTypes.size()) {
                continue;
            }

            boolean haveCandidateTypes = true;
            try {
                // Just asking for them will throw if any aren't loaded.
                List<Type> actualCandidateTypes = candidate.argumentTypes();
            } catch (ClassNotLoadedException cnlx) {
                // This is thrown when there is _any_ parameter to this method
                // whose type is a reference type (class, interface, array)
                // and it has not been loaded by the declaring type's ClassLoader.
                // In this situation we can only use type names for scoring.
                // See http://java.sun.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/ClassNotLoadedException.html
                // for more info (including how _not_ to solve it.)
                // Note that if no ClassNotLoadedException is thrown, the arg
                // types may still have problems.  In particular a given arg type
                // maybe loaded but not yet prepared (see JVM specification 5.4),
                // in which case we can perform certain operations, but others,
                // for instance fields(), will throw a ClassNotPreparedException.
                // I don't think we use any of those operations here, so we don't
                // check for that case.
                haveCandidateTypes = false;
            }

            int score = 0;
            for (int ii = 0; ii < candidateTypes.size(); ii++) {
                String givenSig = argumentTypes.get(ii);
                if (givenSig.equals("*")) {
                    // Allow '*' to match any argument type for breakpoints.
                    score += 5;
                    continue;
                }
                String expectedType = candidateTypes.get(ii);
                String expectedSig = Types.typeNameToJNI(expectedType);
                if (expectedSig != null && (expectedSig.charAt(0) == 'L'
                        || expectedSig.charAt(0) == '[')
                        && givenSig.equals("<null>")) {
                    // Allow null to match any class.
                    score += 1;
                    continue;
                }

                Type givenType = Types.signatureToType(givenSig, vm);
                if (givenType == null && haveCandidateTypes) {
                    // Don't throw if the arg types weren't loaded.
                    // It's a rare edge case (see comments above), and when
                    // it happens we should still try to find a match.
                    throw new InvalidTypeException(givenSig);
                }

                // Compare the argument types for similarity.
                String primSig = Types.wrapperToPrimitive(givenSig);
                if (givenSig.equals(expectedSig)) {
                    score += 5;
                } else if (fuzzySearch && primSig.equals(expectedSig)) {
                    score += 4;
                } else if (haveCandidateTypes
                        && givenType instanceof ReferenceType
                        && Types.isCompatible(
                        expectedSig, (ReferenceType) givenType)) {
                    score += 3;
                } else if (fuzzySearch
                        && haveCandidateTypes
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

        if (bestCount == 1) {
            return bestMethod;
        } else if (bestCount > 1) {
            throw new AmbiguousMethodException(methodName);
        } else {
            throw new NoSuchMethodException(methodName);
        }
    }

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
        int bytesRead = 0;
        do {
            bytesRead = code.read(byteCode, totalBytesRead,
                    byteCode.length - totalBytesRead);
            if (bytesRead > 0) {
                totalBytesRead += bytesRead;
                if (totalBytesRead >= byteCode.length) {
                    byte[] temp = new byte[totalBytesRead * 2];
                    System.arraycopy(byteCode, 0, temp, 0, byteCode.length);
                    byteCode = temp;
                }
            }
        } while (bytesRead != -1);
        byte[] temp = new byte[totalBytesRead];
        System.arraycopy(byteCode, 0, temp, 0, temp.length);
        byteCode = temp;

        // Do the actual hotswap operation.
        Map<ReferenceType, byte[]> map = new HashMap<ReferenceType, byte[]>();
        map.put(clazz, byteCode);
        vm.redefineClasses(map);
    }

    /**
     * Invokes a method and returns its value.
     *
     * @param  object     the object on which to invoke the method, or null
     *                    if the method is static.
     * @param  clazz      the class on which to invoke the method, may be
     *                    null if the object parameter is non-null.
     * @param  thread     the thread on which to invoke the method.
     * @param  method     the method to invoke.
     * @param  arguments  the method parameters.
     * @return  the return value of the invoked method.
     * @throws  ExecutionException
     *          if a runtime exception occurs.
     */
    public static Value invokeMethod(ObjectReference object, ClassType clazz,
            ThreadReference thread, Method method, List<Value> arguments) throws
            ExecutionException {
        if (thread == null) {
            throw new IllegalArgumentException("thread must not be null");
        }
        if (method == null) {
            throw new IllegalArgumentException("method must not be null");
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null");
        }

        Invoker invoker = new Invoker(object, clazz, thread, method, arguments);
        CoreSettings cs = CoreSettings.getDefault();
        int timeout = cs.getInvocationTimeout();
        Future<Value> future = Threads.getThreadPool().submit(invoker);
        Value v = null;
        // Disable all of the breakpoints to avoid hitting them while
        // invoking the target method.
        Session session = SessionProvider.getCurrentSession();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        BreakpointGroup bg = bm.getDefaultGroup();
        boolean enabled = bg.isEnabled();
        bg.setEnabled(false);
        try {
            v = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            future.cancel(true);
        } catch (TimeoutException te) {
            future.cancel(true);
        } finally {
            // Re-enable all of the breakpoints, regardless if an
            // exception has occurred or not.
            bg.setEnabled(enabled);
        }
        return v;
    }

    /**
     * Invokes a method and returns the result.
     *
     * @author  Nathan Fiedler
     */
    private static class Invoker implements Callable<Value> {

        /** Object reference, if non-static method. */
        private ObjectReference object;
        /** Reference type, if static method. */
        private ClassType clazz;
        /** Thread on which to invoke method. */
        private ThreadReference thread;
        /** Method to be invoked. */
        private Method method;
        /** Arguments to the method. */
        private List<Value> arguments;

        /**
         * Constructs an Invoker to invoke the given method.
         *
         * @param  object     object reference, if non-static method.
         * @param  clazz      reference type, if static method.
         * @param  thread     thread on which to invoke method.
         * @param  method     method to be invoked.
         * @param  arguments  arguments to the method.
         */
        public Invoker(ObjectReference object, ClassType clazz,
                ThreadReference thread, Method method,
                List<Value> arguments) {
            this.object = object;
            this.clazz = clazz;
            this.thread = thread;
            this.method = method;
            this.arguments = arguments;
        }

        @Override
        public Value call() throws Exception {
            // It is not safe to invoke methods with the single-threaded
            // bit flag, so let JDI resume all of the threads normally.
            if (object == null) {
                return clazz.invokeMethod(thread, method, arguments, 0);
            } else {
                return object.invokeMethod(thread, method, arguments, 0);
            }
        }
    }
}
