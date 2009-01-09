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
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
