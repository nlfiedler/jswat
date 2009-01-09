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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes.variables;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;

/**
 * Represents a ClassObjectReference variable.
 *
 * @author Nathan Fiedler
 */
public class ClassObjectNode extends VariableNode {
    /** The class object reference. */
    private ClassObjectReference cref;

    /**
     * Creates a new instance of ClassObjectNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  cref  the ClassObjectReference.
     */
    public ClassObjectNode(String name, String type, VariableNode.Kind kind,
            ClassObjectReference cref) {
        super(new ClassObjectChildren(cref), name, type, kind);
        this.cref = cref;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_VALUE, cref.reflectedType().name()));
//        set.put(createProperty(PROP_STRING, cref.toString()));
        return sheet;
    }

    /**
     * Represents the children of an ObjectNode.
     *
     * @author  Nathan Fiedler
     */
    private static class ClassObjectChildren extends Children.SortedArray {
        /** The object reference. */
        private ClassObjectReference cref;

        /**
         * Creates a new instance of ClassObjectChildren.
         *
         * @param  cref  the ClassObjectReference.
         */
        public ClassObjectChildren(ClassObjectReference cref) {
            this.cref = cref;
        }

        protected void addNotify() {
            super.addNotify();
            try {
                VariableFactory vf = VariableFactory.getDefault();
                Set<Node> kids = new HashSet<Node>();

                // This could be an interface or a class.
                ReferenceType type = cref.reflectedType();
                // Find the set of static fields and convert them to nodes.
                List<Field> fields = type.fields();
                for (Field field : fields) {
                    if (field.isStatic()) {
                        Value value = type.getValue(field);
                        VariableNode vn = vf.create(field, value, null);
                        vn.setObjectReference(cref);
                        kids.add(vn);
                    }
                }

                // Add the children to our own set (which should be empty).
                Node[] kidsArray = kids.toArray(new Node[kids.size()]);
                super.add(kidsArray);
            } catch (Exception e) {
                // In most cases, debuggee has resumed, just do nothing.
            }
        }
    }
}
