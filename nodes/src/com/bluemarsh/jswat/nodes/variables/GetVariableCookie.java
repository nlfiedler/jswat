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
 * are Copyright (C) 2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.nodes.variables;

import com.bluemarsh.jswat.nodes.variables.VariableNode.Kind;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import org.openide.nodes.Node;

/**
 * Cookie for getting the JDI variable information associated with a node.
 *
 * @author Nathan Fiedler
 */
public interface GetVariableCookie extends Node.Cookie {

    /**
     * Returns the field this node represents, if it is not a local variable.
     *
     * @return  field, or null if local variable.
     */
    Field getField();

    /**
     * Returns the kind of variable this node represents.
     *
     * @return  kind of variable.
     * @see #Kind
     */
    Kind getKind();

    /**
     * Returns the object reference this node is associated with.
     *
     * @return  object reference.
     */
    ObjectReference getObjectReference();
}
