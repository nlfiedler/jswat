/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: VariableNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

/**
 * A node that represents some type of variable, either a local variable
 * or a field. It defines methods for retrieving the thing that contains
 * the variable. If the thing is a Field, it also provides access to the
 * thing (either ObjectReference or ReferenceType) that contains that field.
 *
 * @author  Nathan Fiedler
 */
public interface VariableNode extends Node {

    /**
     * Returns the thing the field is contained in, either an ObjectReference
     * or a ReferenceType.
     *
     * @param  context  evaluation context.
     * @return  object or class.
     * @throws  EvaluationException
     *          if there was an evaluation error.
     */
    Object getFieldContainer(EvaluationContext context)
        throws EvaluationException;

    /**
     * Returns the thing this node refers to rather than its value;
     * either a Field, LocalVariable, ObjectReference, or ReferenceType.
     *
     * @param  context  evaluation context.
     * @return  field, variable, object, or class.
     * @throws  EvaluationException
     *          if there was an evaluation error.
     */
    Object getValueContainer(EvaluationContext context)
        throws EvaluationException;
}
