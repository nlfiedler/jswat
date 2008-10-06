/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2002-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: MethodNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import com.bluemarsh.jswat.core.util.AmbiguousMethodException;
import com.bluemarsh.jswat.core.util.Classes;
import com.bluemarsh.jswat.core.util.Strings;
import com.bluemarsh.jswat.core.util.Types;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.openide.util.NbBundle;

/**
 * Class MethodNode represents a method invocation. Its type is the return
 * type of the method being invoked.
 *
 * @author  Nathan Fiedler
 */
class MethodNode extends OperatorNode implements JoinableNode {
    /** The name of the method. */
    private String methodName;
    /** The class or object on which to invoke the method. */
    private Node classOrObject;
    /** The method that was invoked; used for determining this node's type. */
    private Method method;

    /**
     * Constructs a MethodNode associated with the given token and name.
     *
     * @param  node  lexical token.
     * @param  name  name of the method.
     */
    public MethodNode(Token node, String name) {
        super(node);
        methodName = name;
    }

    /**
     * Constructs a MethodNode associated with the given token, object or
     * class, and method name.
     *
     * @param  node    lexical token.
     * @param  object  the object or class on which to invoke the method.
     * @param  name    name of the method.
     */
    public MethodNode(Token node, Node object, String name) {
        super(node);
        classOrObject = object;
        methodName = name;
    }

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        // Get the preliminaries out of the way first.
        ThreadReference thread = context.getThread();
        if (thread == null) {
            throw new EvaluationException(
                NbBundle.getMessage(MethodNode.class, "error.method.thread.set"));
        }
        int frameIdx = context.getFrame();
        VirtualMachine vm = thread.virtualMachine();

        // Get the object or class on which to invoke the method.
        ObjectReference object = null;
        ReferenceType clazz = null;
        if (classOrObject == null) {
            // No object or class was given, default to the class containing
            // the current location.
            try {
                if (thread.frameCount() == 0) {
                    throw new EvaluationException(
                        NbBundle.getMessage(MethodNode.class, "error.method.thread.stack"));
                }
                StackFrame frame = thread.frame(frameIdx);
                object = frame.thisObject();

                if (object == null) {
                    Location location = context.getLocation();
                    if (location == null) {
                        throw new EvaluationException(
                            NbBundle.getMessage(MethodNode.class, "error.method.location"));
                    }
                    clazz = location.declaringType();
                    if (clazz == null) {
                        throw new EvaluationException(
                            NbBundle.getMessage(MethodNode.class, "error.method.location"));
                    }
                } else {
                    clazz = object.referenceType();
                }
            } catch (IncompatibleThreadStateException itse) {
                throw new EvaluationException(
                    NbBundle.getMessage(MethodNode.class, "error.thread.state"));
            }

        } else {
            // Evaluate the class or object node and determine what it is.
            Object coo = classOrObject.evaluate(context);
            if (coo instanceof ObjectReference) {
                object = (ObjectReference) coo;
                clazz = object.referenceType();
            } else if (coo instanceof ReferenceType) {
                clazz = (ReferenceType) coo;
            }
            // else: reftype will be null, and...
            if (clazz == null) {
                String msg = NbBundle.getMessage(
                    MethodNode.class, "error.method.name", coo);
                throw new EvaluationException(msg);
            }
        }

        // Get the list of method arguments and their types.
        int count = childCount();
        List<Object> argumentObjects = new ArrayList<Object>(count);
        List<String> argumentTypes = new ArrayList<String>(count);
        for (int ii = 0; ii < count; ii++) {
            Node n = getChild(ii);
            Object o = n.evaluate(context);
            argumentObjects.add(o);
            String t = n.getType(context);
            if (t == null) {
                // For method arguments, 'null' is a special type.
                argumentTypes.add("<null>");
            } else {
                argumentTypes.add(t);
            }
        }

        // Locate the named method in the resolved class.
        try {
            method = Classes.findMethod(
                clazz, methodName, argumentTypes, true);
        } catch (AmbiguousMethodException ame) {
            throw new EvaluationException(NbBundle.getMessage(
                MethodNode.class, "error.method.ambiguous", methodName,
                Strings.listToString(argumentTypes)));
        } catch (InvalidTypeException ite) {
            throw new EvaluationException(NbBundle.getMessage(
                MethodNode.class, "error.method.argument", ite.getMessage()));
        } catch (NoSuchMethodException nsme) {
            throw new EvaluationException(NbBundle.getMessage(
                MethodNode.class, "error.method.method", methodName,
                Strings.listToString(argumentTypes)));
        }

        // Convert the arguments to JDI objects.
        List<Value> arguments = new ArrayList<Value>(count);
        for (Object o : argumentObjects) {
            Value v = Types.mirrorOf(o, vm);
            arguments.add(v);
        }

        // Invoke the method and return the results.
        try {
            return Classes.invokeMethod(object, (ClassType) clazz, thread,
                    method, arguments);
        } catch (InterruptedException ie) {
            throw new EvaluationException(ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if (cause instanceof IncompatibleThreadStateException) {
                String msg = NbBundle.getMessage(MethodNode.class,
                        "error.thread.state");
                throw new EvaluationException(msg);
            }
            throw new EvaluationException(cause);
        } catch (TimeoutException te) {
            throw new EvaluationException(te);
        }
    }

    /**
     * Returns this operator's precedence value. The lower the value
     * the higher the precedence. The values are equivalent to those
     * described in the Java Language Reference book (2nd ed.), p 106.
     *
     * @return  precedence value.
     */
    public int precedence() {
        return 1;
    }

    /**
     * Returns the signature of the type this node represents. If the
     * type is void, or otherwise unrecognizable, an exception is
     * thrown.
     *
     * @param  context  evaluation context.
     * @return  type signature, or null if value is null.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected String type(EvaluationContext context)
        throws EvaluationException {

        // Force the evaluation to happen, if it hasn't already, so we
        // get the method reference.
        evaluate(context);
        try {
            return method.returnType().signature();
        } catch (ClassNotLoadedException cnle) {
            throw new EvaluationException(NbBundle.getMessage(
                MethodNode.class, "error.method.class", cnle.className()));
        }
    }
}
