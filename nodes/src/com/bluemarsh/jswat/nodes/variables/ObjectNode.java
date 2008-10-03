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
 * $Id: ObjectNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.variables;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;

/**
 * Represents an ObjectReference variable.
 *
 * @author Nathan Fiedler
 */
public class ObjectNode extends VariableNode {
    /** The object reference (keep separate from similar field in superclass,
     * which is used differently than this field. */
    private ObjectReference oref;

    /**
     * Creates a new instance of ObjectNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  oref  the ObjectReference.
     */
    public ObjectNode(String name, String type, VariableNode.Kind kind, ObjectReference oref) {
        super(new ObjectChildren(oref), name, type, kind);
        this.oref = oref;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_VALUE, "#" + oref.uniqueID()));
        //set.put(createProperty(PROP_STRING, oref.toString()));
        return sheet;
    }

    /**
     * Represents the children of an ObjectNode.
     *
     * @author  Nathan Fiedler
     */
    private static class ObjectChildren extends Children.SortedArray {
        /** The object reference. */
        private ObjectReference oref;

        /**
         * Creates a new instance of ObjectChildren.
         *
         * @param  oref  the ObjectReference.
         */
        public ObjectChildren(ObjectReference oref) {
            this.oref = oref;
        }

        protected void addNotify() {
            super.addNotify();
            try {
                VariableFactory vf = VariableFactory.getDefault();

                // Find the set of fields and convert them to nodes.
                Set<Node> kids = new HashSet<Node>();
                List<Field> fields = oref.referenceType().visibleFields();
                for (Field field : fields) {
                    Value value = oref.getValue(field);
                    VariableNode vn = vf.create(field, value, null);
                    vn.setObjectReference(oref);
                    kids.add(vn);
                }

                // Add the referents node.
                if (oref.virtualMachine().canGetInstanceInfo()) {
                    kids.add(new ReferentsNode(oref));
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
