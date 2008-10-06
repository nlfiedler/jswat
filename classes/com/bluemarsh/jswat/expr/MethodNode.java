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
 * $Id: MethodNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.breakpoint.AmbiguousMethodException;
import com.bluemarsh.jswat.parser.java.node.Token;
import com.bluemarsh.jswat.util.Classes;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.util.Types;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;


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
    private Method theMethod;

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
                Bundle.getString("error.method.thread.set"));
        }
        int frameIdx = context.getFrame();
        VirtualMachine vm = thread.virtualMachine();

        // Get the object or class on which to invoke the method.
        ObjectReference objref = null;
        ReferenceType reftype = null;
        if (classOrObject == null) {
            // No object or class was given, default to the class containing
            // the current location.
            try {
                if (thread.frameCount() == 0) {
                    throw new EvaluationException(
                        Bundle.getString("error.method.thread.stack"));
                }
                StackFrame frame = thread.frame(frameIdx);
                objref = frame.thisObject();

                if (objref == null) {
                    Location location = context.getLocation();
                    if (location == null) {
                        throw new EvaluationException(
                            Bundle.getString("error.method.location"));
                    }
                    reftype = location.declaringType();
                    if (reftype == null) {
                        throw new EvaluationException(
                            Bundle.getString("error.method.location"));
                    }
                } else {
                    reftype = objref.referenceType();
                }
            } catch (IncompatibleThreadStateException itse) {
                throw new EvaluationException(
                    Bundle.getString("error.method.thread.state"));
            }

        } else {
            // Evaluate the class or object node and determine what it is.
            Object coo = classOrObject.evaluate(context);
            if (coo instanceof ObjectReference) {
                objref = (ObjectReference) coo;
                reftype = objref.referenceType();
            } else if (coo instanceof ReferenceType) {
                reftype = (ReferenceType) coo;
            }
            // else: reftype will be null, and...
            if (reftype == null) {
                String msg = Bundle.getString(
                    "error.method.name", coo);
                throw new EvaluationException(msg);
            }
        }

        // Get the list of method arguments and their types.
        int count = childCount();
        List argumentValues = new ArrayList(count);
        List argumentTypes = new ArrayList(count);
        for (int ii = 0; ii < count; ii++) {
            Node n = getChild(ii);
            Object o = n.evaluate(context);
            argumentValues.add(o);
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
            theMethod = Classes.findMethod(
                reftype, methodName, argumentTypes, true);
        } catch (AmbiguousMethodException ame) {
            throw new EvaluationException(Bundle.getString(
                "error.method.ambiguous", methodName,
                Strings.listToString(argumentTypes)));
        } catch (ClassNotLoadedException cnle) {
            throw new EvaluationException(Bundle.getString(
                "error.method.class", cnle.className()));
        } catch (InvalidTypeException ite) {
            throw new EvaluationException(Bundle.getString(
                "error.method.argument", ite.getMessage()));
        } catch (NoSuchMethodException nsme) {
            throw new EvaluationException(Bundle.getString(
                "error.method.method", methodName,
                Strings.listToString(argumentTypes)));
        }

        // Convert the arguments to JDI objects.
        for (int ii = 0; ii < argumentValues.size(); ii++) {
            Object o = argumentValues.get(ii);
            o = Types.mirrorOf(o, vm);
            argumentValues.set(ii, o);
        }

        // Invoke the method and return the results.
        try {
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/util");
            int timeout = prefs.getInt("invocationTimeout",
                                       Defaults.INVOCATION_TIMEOUT);
            return Classes.invokeMethod(objref, reftype, thread,
                                        theMethod, argumentValues, timeout);
        } catch (Exception e) {
            throw new EvaluationException(e);
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
            return theMethod.returnType().signature();
        } catch (ClassNotLoadedException cnle) {
            throw new EvaluationException(Bundle.getString(
                "error.method.class", cnle.className()));
        }
    }
}
