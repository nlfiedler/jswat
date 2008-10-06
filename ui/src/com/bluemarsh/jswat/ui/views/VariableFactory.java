/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: VariableFactory.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.IntegerType;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongType;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Constructs instances of VariableNode.
 *
 * @author Nathan Fiedler
 */
public class VariableFactory {
    /** The singleton instance. */
    private static VariableFactory instance;

    static {
        instance = new VariableFactory();
    }

    /**
     * Creates a new instance of VariableFactory.
     */
    private VariableFactory() {
    }

    /**
     * Creates a VariableNode from a local variable and its value.
     *
     * @param  local  a local variable.
     * @param  value  its value.
     * @return  instance of VariableNode.
     */
    public VariableNode create(LocalVariable local, Value value) {
        return create(local, value, null);
    }

    /**
     * Creates a VariableNode from a local variable and its value.
     *
     * @param  local  a local variable.
     * @param  value  its value.
     * @param  mods   display modifiers.
     *
     * @return instance of VariableNode.
     */
    public VariableNode create(LocalVariable local, Value value, String mods) {
        // Value.type().name() returns actual type name.
        // LocalVariable.typeName() returns declared type name.
        String tname = (value != null) ? value.type().name() : local.typeName();
        return create(local.name(), tname, value, VariableNode.Kind.LOCAL, mods);
    }

    /**
     * Creates a VariableNode from a field and its value.
     *
     * @param  field  a field.
     * @param  value  its value.
     * @return  instance of VariableNode.
     */
    public VariableNode create(Field field, Value value) {
        return create(field, value, null);
    }

    /**
     * Creates a VariableNode from a field and its value.
     *
     * @param  field  a field.
     * @param  value  its value.
     * @param  mods   display modifiers.
     * @return  instance of VariableNode.
     */
    public VariableNode create(Field  field, Value value, String mods) {
        // Value.type().name() returns actual type name.
        // Field.typeName() returns declared type name.
        String tname = (value != null) ? value.type().name() : field.typeName();
        VariableNode.Kind kind = field.isStatic() ? VariableNode.Kind.STATIC_FIELD
                : VariableNode.Kind.FIELD;
        VariableNode node = create(field.name(), tname, value, kind, mods);
        node.setField(field);
        return node;
    }

    /**
     * Creates a VariableNode to represent 'this' object.
     *
     * @param  obj  the 'this' object.
     * @return  instance of VariableNode.
     */
    public VariableNode create(ObjectReference obj) {
        return create(obj, null);
    }

    /**
     * Creates a VariableNode to represent 'this' object.
     *
     * @param  obj   the 'this' object.
     * @param  mods  display modifiers.
     * @return instance of VariableNode.
     */
    public VariableNode create(ObjectReference obj, String mods) {
        String tname = obj.referenceType().name();
        return create("this", tname, obj, VariableNode.Kind.THIS, mods);
    }

    /**
     * Creates a VariableNode based on name, type name, and value.
     *
     * @param  name   name of the variable.
     * @param  type   type of the variable.
     * @param  value  value of the variable.
     * @param  kind   kind of variable.
     * @return  instance of VariableNode.
     */
    public VariableNode create(String name, String type, Value value,
            VariableNode.Kind kind) {
        return create(name, type, value, kind, null);
    }

    /**
     * Creates a VariableNode based on name, type name, and value.
     *
     * @param  name   name of the variable.
     * @param  type   type of the variable.
     * @param  value  value of the variable.
     * @param  kind   kind of variable.
     * @param  mods   display modifiers.
     * @return instance of VariableNode.
     */
    public VariableNode create(String name, String type, Value value,
            VariableNode.Kind kind, String mods) {
        // Shorten the class name to avoid ellipses.
        type = Names.getShortClassName(type);
        if(value instanceof StringReference) {
            return new StringNode(name, type, kind, (StringReference) value);
        } else if(value instanceof ClassObjectReference) {
            return new ClassObjectNode(name, type, kind, (ClassObjectReference) value);
        } else if(value instanceof ArrayReference) {
            return new ArrayNode(name, type, kind, (ArrayReference) value);
        } else if(value instanceof ObjectReference) {
            // Check what specific type of object this is and try to show
            // it in a more informative fashion.
            ObjectReference obj = (ObjectReference) value;
            String cname = obj.referenceType().name();
            if ((cname.equals("java.lang.StringBuffer") ||
                    cname.equals("java.lang.StringBuilder")) &&
                    StringBufferNode.isValid(obj)) {
                return new StringBufferNode(name, type, kind, obj);
            } else if (cname.equals("java.util.BitSet") &&
                    BitSetNode.isValid(obj)) {
                return new BitSetNode(name, type, kind, obj);
            } else {
                return new ObjectNode(name, type, kind, obj);
            }
        } else {
            // The value is either primitive, or null.
            return new PrimitiveNode(name, type, kind, (PrimitiveValue) value, mods);
        }
    }

    /**
     * Returns the singleton instance of this factory.
     *
     * @return  a VariableFactory instance.
     */
    public static VariableFactory getInstance() {
        return instance;
    }
}

/**
 * Represents an ArrayReference.
 *
 * @author Nathan Fiedler
 */
class ArrayNode extends VariableNode {
    /** The size of the array element groups. */
    private static final int GROUPING_SIZE = 100;
    /** The array reference. */
    private ArrayReference aref;

    /**
     * Constructs a new instance of ArrayNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  aref  the ArrayReference.
     */
    public ArrayNode(String name, String type, VariableNode.Kind kind,
            ArrayReference aref) {
        super(new ANChildren(aref, 0, aref.length()), name, type, kind);
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
         * Constructs a new instance of SubNode.
         *
         * @param  aref    the array reference.
         * @param  offset  starting position in array.
         * @param  length  length of array subet.
         */
        public SubNode(ArrayReference aref, int offset, int length) {
            // Create a unique name for this node.
            super(new ANChildren(aref, offset, length), String.valueOf(offset),
                    aref.referenceType().name(), VariableNode.Kind.FIELD);
            if (length > 1) {
                // Minus one for conversion to relative indexing.
                displayName = offset + " - " + (offset + length - 1);
            } else {
                // One node does not make a range.
                displayName = String.valueOf(offset);
            }
        }

        /**
         * Gets the localized display name of this feature.
         *
         * @return  display name.
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Represents the children of an ArrayNode.
     *
     * @author  Nathan Fiedler
     */
    private static class ANChildren extends Children.Array {
        /** The array reference. */
        private ArrayReference aref;
        /** Index into array of first element. */
        private int offset;
        /** Length of the array subset. */
        private int length;

        /**
         * Constructs a new instance of ANChildren.
         *
         * @param  aref    the ArrayReference.
         * @param  offset  starting position in array.
         * @param  length  length of array subset.
         */
        public ANChildren(ArrayReference aref, int offset, int length) {
            this.aref = aref;
            this.offset = offset;
            this.length = length;
        }

        protected void addNotify() {
            super.addNotify();
            try {
                VariableFactory vf = VariableFactory.getInstance();
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
                                value, VariableNode.Kind.FIELD);
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

/**
 * Represents a ClassObjectReference.
 *
 * @author Nathan Fiedler
 */
class ClassObjectNode extends VariableNode {
    /** The class object reference. */
    private ClassObjectReference cref;

    /**
     * Constructs a new instance of ClassObjectNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  cref  the ClassObjectReference.
     */
    public ClassObjectNode(String name, String type, VariableNode.Kind kind, ClassObjectReference cref) {
        super(new CNChildren(cref), name, type, kind);
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
    private static class CNChildren extends Children.SortedArray {
        /** The object reference. */
        private ClassObjectReference cref;

        /**
         * Constructs a new instance of CNChildren.
         *
         * @param  cref  the ClassObjectReference.
         */
        public CNChildren(ClassObjectReference cref) {
            this.cref = cref;
        }

        protected void addNotify() {
            super.addNotify();
            try {
                VariableFactory vf = VariableFactory.getInstance();
                Set<VariableNode> kids = new HashSet<VariableNode>();

                // This could be an interface or a class.
                ReferenceType type = cref.reflectedType();
                // Find the set of static fields and convert them to nodes.
                List<Field> fields = type.fields();
                for (Field field : fields) {
                    if (field.isStatic()) {
                        Value value = type.getValue(field);
                        VariableNode vn = vf.create(field, value);
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

/**
 * Represents an ObjectReference.
 *
 * @author Nathan Fiedler
 */
class ObjectNode extends VariableNode {
    /** The object reference (keep separate from similar field in superclass,
     * which is used differently than this field. */
    private ObjectReference oref;

    /**
     * Constructs a new instance of ObjectNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  oref  the ObjectReference.
     */
    public ObjectNode(String name, String type, VariableNode.Kind kind, ObjectReference oref) {
        super(new ONChildren(oref), name, type, kind);
        this.oref = oref;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_VALUE, "#" + oref.uniqueID()));
//        set.put(createProperty(PROP_STRING, oref.toString()));
        return sheet;
    }

    /**
     * Represents the children of an ObjectNode.
     *
     * @author  Nathan Fiedler
     */
    private static class ONChildren extends Children.SortedArray {
        /** The object reference. */
        private ObjectReference oref;

        /**
         * Constructs a new instance of ONChildren.
         *
         * @param  oref  the ObjectReference.
         */
        public ONChildren(ObjectReference oref) {
            this.oref = oref;
        }

        protected void addNotify() {
            super.addNotify();
            try {
                VariableFactory vf = VariableFactory.getInstance();

                // Find the set of fields and convert them to nodes.
                Set<VariableNode> kids = new HashSet<VariableNode>();
                List<Field> fields = oref.referenceType().visibleFields();
                for (Field field : fields) {
                    Value value = oref.getValue(field);
                    VariableNode vn = vf.create(field, value);
                    vn.setObjectReference(oref);
                    kids.add(vn);
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

/**
 * Represents a PrimitiveValue.
 *
 * @author Nathan Fiedler
 */
class PrimitiveNode extends VariableNode {
    /** The primitive value. */
    private PrimitiveValue pval;

    /**
     * Constructs a new instance of PrimitiveNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  pval  the PrimitiveValue (may be null).
     */
    public PrimitiveNode(String name, String type,
            VariableNode.Kind kind, PrimitiveValue pval) {
        this(name, type, kind, pval, null);
    }

    /**
     * Creates a new PrimitiveNode object.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  pval  the PrimitiveValue (may be null).
     * @param  mods  display modifiers.
     */
    public PrimitiveNode(String name, String type, VariableNode.Kind kind,
            PrimitiveValue pval, String mods) {
        super(Children.LEAF, name, type, kind, mods);
        this.pval = pval;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        String propvalue = "invalid";
        if (getModifiers() == null) {
            if (pval instanceof CharValue) {
                CharValue cv = (CharValue) pval;
                StringBuilder sb = new StringBuilder();
                if (!Character.isISOControl(cv.charValue())) {
                    // If it is a printable character, show it.
                    sb.append(pval.toString());
                    sb.append(' ');
                }
                // Always print the hexadecimal value of the character.
                sb.append("(\\u");
                sb.append(Strings.toHexString(cv.value()));
                sb.append(')');
                propvalue = sb.toString();
            } else if (pval != null) {
                propvalue = pval.toString();
                set.put(createProperty(PROP_VALUE,
                        pval.toString()));
            } else {
                propvalue = "null";
            }
        } else {
            propvalue = applyModifiers();
        }
        set.put(createProperty(PROP_VALUE, propvalue));
        return sheet;
    }

    /**
     * Apply the display modifiers to the value.
     *
     * @return  modified value.
     */
    private String applyModifiers() {
        if (pval != null) {
            boolean bInteger = false;
            long lValue = 0;
            String mods = getModifiers();
            if (pval instanceof CharValue) {
                CharValue cv = (CharValue) pval;
                if (mods.indexOf('u') >= 0 ||
                        Character.isISOControl(cv.charValue())) {
                    // Display Unicode for character.
                    return "(\\u" + Strings.toHexString(cv.value()) + ')';
                } else if (mods.indexOf('s') < 0) {
                    // 's' is of higher priority than any of 'xbo'.
                    lValue = cv.longValue();
                    bInteger = true;
                }
            } else if (pval instanceof ByteValue) {
                lValue = ((ByteValue) pval).longValue();
                bInteger = true;
            } else if (pval instanceof ShortValue) {
                lValue = ((ShortValue) pval).longValue();
                bInteger = true;
            } else if (pval instanceof IntegerValue) {
                lValue = ((IntegerValue) pval).longValue();
                bInteger = true;
            } else if (pval instanceof LongValue) {
                lValue = ((LongValue) pval).longValue();
                bInteger = true;
            }

            if (bInteger) {
                if (mods.indexOf('x') >= 0) {
                    return "0x" + Long.toHexString(lValue)
                    .toUpperCase();
                } else if (mods.indexOf('o') >= 0) {
                    return "0" + Long.toOctalString(lValue);
                } else if (mods.indexOf('b') >= 0) {
                    String s = Long.toBinaryString(lValue);
                    // Pad the value with zeros so it is obviously binary.
                    int b = Long.numberOfLeadingZeros(lValue);
                    if (b == 64) {
                        // The value was zero.
                        s = "00000000";
                    } else {
                        // Rather than padding to 64 characters, find
                        // the closest-fitting 8-bit boundary.
                        b = b % 8;
                        StringBuilder sb = new StringBuilder();
                        while (b > 0) {
                            sb.append('0');
                            b--;
                        }
                        s = sb.toString() + s;
                    }
                    return s;
                }
            }

            // Unknown or the 's' modifier.
            return pval.toString();
        } else {
            return "null";
        }
    }
}

/**
 * Represents a StringReference.
 *
 * @author Nathan Fiedler
 */
class StringNode extends VariableNode {
    /** The string reference. */
    private StringReference sref;

    /**
     * Constructs a new instance of StringNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  sref  the StringReference.
     */
    public StringNode(String name, String type, VariableNode.Kind kind,
            StringReference sref) {
        super(Children.LEAF, name, type, kind);
        this.sref = sref;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_VALUE, "\"" + sref.value() + "\""));
        return sheet;
    }
}

/**
 * Represents a StringBuffer or StringBuilder reference.
 *
 * @author Nathan Fiedler
 */
class StringBufferNode extends VariableNode {
    /** The object reference. */
    private ObjectReference oref;

    /**
     * Constructs a new instance of StringBufferNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  sref  the StringReference.
     */
    public StringBufferNode(String name, String type, VariableNode.Kind kind,
            ObjectReference oref) {
        super(Children.LEAF, name, type, kind);
        this.oref = oref;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        ReferenceType clazz = oref.referenceType();
        try {
            // The strategy is to avoid invoking methods and to get the
            // elements of the character array up to the count value.
            // This should work for StringBuilder as well.
            Field field = clazz.fieldByName("value");
            ArrayReference array = (ArrayReference) oref.getValue(field);
            field = clazz.fieldByName("count");
            IntegerValue count = (IntegerValue) oref.getValue(field);
            StringBuilder sb = new StringBuilder();
            for (int index = 0; index < count.value(); index++) {
                Value v = array.getValue(index);
                sb.append(v.toString());
            }
            set.put(createProperty(PROP_VALUE, "\"" + sb.toString() + "\""));
        } catch (RuntimeException re) {
            // Fall back to the safest possible operation.
            set.put(createProperty(PROP_VALUE, oref.toString()));
        }
//        set.put(createProperty(PROP_STRING, oref.toString()));
        return sheet;
    }

    /**
     * Indicates if the given object is a value that this class can display.
     *
     * @return  true if object okay, false if not appropriate type.
     */
    public static boolean isValid(ObjectReference oref) {
        boolean valid = false;
        ReferenceType clazz = oref.referenceType();
        try {
            // Check the availability and types of the fields.
            Field field = clazz.fieldByName("value");
            Type type = field.type();
            if (type instanceof ArrayType) {
                field = clazz.fieldByName("count");
                type = field.type();
                if (type instanceof IntegerType) {
                    valid = true;
                }
            }
        } catch (ClassNotLoadedException cnle) {
            // Then return false
        } catch (RuntimeException re) {
            // Then return false
        }
        return valid;
    }
}

/**
 * Represents a BitSet reference.
 *
 * @author Nathan Fiedler
 */
class BitSetNode extends VariableNode {
    /** The object reference. */
    private ObjectReference oref;

    /**
     * Constructs a new instance of BitSetNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  sref  the StringReference.
     */
    public BitSetNode(String name, String type, VariableNode.Kind kind,
            ObjectReference oref) {
        super(Children.LEAF, name, type, kind);
        this.oref = oref;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        ReferenceType clazz = oref.referenceType();
        try {
            // The strategy is to avoid invoking methods and to get the
            // elements of the long array up to the unitsInUse value.
            Field field = clazz.fieldByName("bits");
            ArrayReference array = (ArrayReference) oref.getValue(field);
            StringBuilder sb = new StringBuilder();
            for (int index = 0; index < array.length(); index++) {
                LongValue v = (LongValue) array.getValue(index);
                String s = Long.toBinaryString(v.value());
                sb.insert(0, s);
                if (s.length() < 64) {
                    char[] zeros = new char[64 - s.length()];
                    Arrays.fill(zeros, '0');
                    sb.insert(0, zeros);
                }
            }
            set.put(createProperty(PROP_VALUE, sb.toString()));
        } catch (RuntimeException re) {
            // Fall back to the safest possible operation.
            set.put(createProperty(PROP_VALUE, oref.toString()));
        }
//        set.put(createProperty(PROP_STRING, oref.toString()));
        return sheet;
    }

    /**
     * Indicates if the given object is a value that this class can display.
     *
     * @return  true if object okay, false if not appropriate type.
     */
    public static boolean isValid(ObjectReference oref) {
        boolean valid = false;
        ReferenceType clazz = oref.referenceType();
        try {
            // Check the availability and types of the fields.
            Field field = clazz.fieldByName("bits");
            Type type = field.type();
            if (type instanceof ArrayType) {
                ArrayType at = (ArrayType) type;
                if (at.componentType() instanceof LongType) {
                    valid = true;
                }
            }
        } catch (ClassNotLoadedException cnle) {
            // Then return false
        } catch (RuntimeException re) {
            // Then return false
        }
        return valid;
    }
}
