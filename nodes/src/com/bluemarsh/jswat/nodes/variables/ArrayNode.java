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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Represents an ArrayReference variable.
 *
 * @author Nathan Fiedler
 */
public class ArrayNode extends VariableNode {
    /** The size of the array element groups. */
    private static final int GROUPING_SIZE = 100;
    /** The array reference. */
    private ArrayReference aref;

    /**
     * Creates a new instance of ArrayNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  aref  the ArrayReference.
     */
    public ArrayNode(String name, String type, VariableNode.Kind kind,
            ArrayReference aref) {
        super(new ArrayChildren(aref, 0, aref.length()), name, type, kind);
        this.aref = aref;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        String desc = NbBundle.getMessage(ArrayNode.class,
                "LBL_VariableFactory_Array", aref.uniqueID(), aref.length());
        set.put(createProperty(PROP_VALUE, desc));
        return sheet;
    }

    /**
     * Contains a small subset of the array element nodes, for the sake of
     * reducing the number of nodes created at any one time.
     *
     * @author  Nathan Fiedler
     */
    private static class SubNode extends VariableNode {
        /** The generated name for this node. */
        private String displayName;

        /**
         * Creates a new instance of SubNode.
         *
         * @param  aref    the array reference.
         * @param  offset  starting position in array.
         * @param  length  length of array subet.
         */
        public SubNode(ArrayReference aref, int offset, int length) {
            // Create a unique name for this node.
            super(new ArrayChildren(aref, offset, length), String.valueOf(offset),
                    aref.referenceType().name(), VariableNode.Kind.FIELD);
            if (length > 1) {
                // Minus one for conversion to relative indexing.
                displayName = offset + " - " + (offset + length - 1);
            } else {
                // One node does not make a range.
                displayName = String.valueOf(offset);
            }
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Represents the children of an ArrayNode.
     *
     * @author  Nathan Fiedler
     */
    private static class ArrayChildren extends Children.Array {
        /** The array reference. */
        private ArrayReference aref;
        /** Index into array of first element. */
        private int offset;
        /** Length of the array subset. */
        private int length;

        /**
         * Creates a new instance of ArrayChildren.
         *
         * @param  aref    the ArrayReference.
         * @param  offset  starting position in array.
         * @param  length  length of array subset.
         */
        public ArrayChildren(ArrayReference aref, int offset, int length) {
            this.aref = aref;
            this.offset = offset;
            this.length = length;
        }

        protected void addNotify() {
            super.addNotify();
            try {
                VariableFactory vf = VariableFactory.getDefault();
                ArrayType atype = (ArrayType) aref.referenceType();
                String arrayType = atype.componentTypeName();

                List<Node> kids = new ArrayList<Node>();
                if (length > GROUPING_SIZE) {
                    // Build subnodes to contain a subset of the array nodes.
                    int first = offset;
                    int last = offset + length;
                    while (first < last) {
                        int size = Math.min(last - first, GROUPING_SIZE);
                        Node n = new SubNode(aref, first, size);
                        kids.add(n);
                        first += GROUPING_SIZE;
                    }
                } else {
                    // No need to make subgroups.
                    for (int ii = offset; ii < offset + length; ii++) {
                        Value value = aref.getValue(ii);
                        String type = null;
                        if (value instanceof ObjectReference) {
                            // For arrays of Objects, show the actual element type.
                            type = ((ObjectReference) value).referenceType().name();
                        } else {
                            // Otherwise, just show the declared element type.
                            type = arrayType;
                        }

                        VariableNode vn = vf.create("[" + ii + "]", type,
                                value, VariableNode.Kind.FIELD, null);
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
