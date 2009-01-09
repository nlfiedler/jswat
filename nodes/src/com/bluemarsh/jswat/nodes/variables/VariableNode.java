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
 * are Copyright (C) 2005-2008. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes.variables;

import com.bluemarsh.jswat.core.util.Arrays;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.nodes.ReadOnlyProperty;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import java.awt.Image;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Represents either a local variable or a field of an object.
 *
 * @author Nathan Fiedler
 */
public class VariableNode extends AbstractNode implements
        Comparable<Node>, Node.Cookie {
    /** Name of the type property. */
    public static final String PROP_TYPE = "type";
    /** Name of the fully-qualified typename property. */
    public static final String PROP_FULL_TYPE = "fullType";
    /** Name of the value property. */
    public static final String PROP_VALUE = "value";
    /** Name of the declaring type property. */
    public static final String PROP_DECLARING_TYPE = "declaringType";
    /** What kind of variable this is. */
    public static enum Kind {
        FIELD, LOCAL, STATIC_FIELD, THIS
    }
    /** The name of the variable. */
    private String name;
    /** The type of the variable (usually a fully-qualified class name). */
    private String type;
    /** The kind of variable this node represents. */
    private Kind kind;
    /** The Field this variable represents, if it is not a local variable. */
    private Field field;
    /** The object reference, if this node represents a field of an object. */
    private ObjectReference object;
    /** Array of actions for our node. */
    private Action[] nodeActions;

    /**
     * Creates a VariableNode to represent the given class loader.
     *
     * @param  kids   children of this node.
     * @param  name   name of the variable.
     * @param  type   type of the variable.
     * @param  kind   kind of variable.
     */
    public VariableNode(Children kids, String name, String type, Kind kind) {
        super(kids);
        this.name = name;
        this.type = type;
        this.kind = kind;
        nodeActions = new Action[] {
            SystemAction.get(WatchpointAction.class),
            SystemAction.get(AddWatchAction.class),
        };
        getCookieSet().add(this);
    }

    public int compareTo(Node o) {
        // Keep 'this' above the others, and 'referents' below.
        if (name.equals("this") || o.getName().equals(ReferentsNode.NAME)) {
            return -1;
        } else {
            return name.compareTo(o.getName());
        }
    }

    /**
     * Creates a node property of the given key (same as the column keys).
     *
     * @param  key    property name (same as matching column).
     * @param  value  display value.
     * @return  new property.
     */
    protected Node.Property createProperty(String key, String value) {
        String desc = NbBundle.getMessage(
                VariableNode.class, "CTL_VariableNode_Property_Desc_" + key);
        String name = NbBundle.getMessage(
                VariableNode.class, "CTL_VariableNode_Property_Name_" + key);
        return new ReadOnlyProperty(key, String.class, name, desc, value);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_NAME, name));
        // Shorten the class name for easier viewing.
        String shortType = Names.getShortClassName(type);
        set.put(createProperty(PROP_TYPE, shortType));
        if (type.length() > shortType.length()) {
            // But also have the long name for the properties sheet.
            set.put(createProperty(PROP_FULL_TYPE, type));
        }
        return sheet;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] retValue = super.getActions(context);
        retValue = (Action[]) Arrays.join(retValue, nodeActions);
        return retValue;
    }

    @Override
    public String getDisplayName() {
        // Just return the name of the variable.
        return name;
    }

    @Override
    public Image getIcon(int type) {
        String url = null;
        switch (kind) {
            case LOCAL:
                url = NbBundle.getMessage(VariableNode.class,
                        "IMG_VariableNode_LocalNode");
                break;
            case FIELD:
                url = NbBundle.getMessage(VariableNode.class,
                        "IMG_VariableNode_FieldNode");
                break;
            case STATIC_FIELD:
                url = NbBundle.getMessage(VariableNode.class,
                        "IMG_VariableNode_StaticFieldNode");
                break;
            case THIS:
                url = NbBundle.getMessage(VariableNode.class,
                        "IMG_VariableNode_ThisNode");
                break;
        }
        if (url != null) {
            return ImageUtilities.loadImage(url);
        } else {
            return null;
        }
    }

    /**
     * Returns the field this node represents, if it is not a local variable.
     *
     * @return  field, or null if local variable.
     */
    public Field getField() {
        return field;
    }

    /**
     * Returns the kind of variable this node represents.
     *
     * @return  kind of variable.
     * @see #Kind
     */
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the object reference this node is associated with.
     *
     * @return  object reference.
     */
    public ObjectReference getObjectReference() {
        return object;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    /**
     * Sets the field that this node represents.
     *
     * @param  field  field for this node.
     */
    public void setField(Field field) {
        this.field = field;
        // Get the declaring type of the field so we can show the user
        // in the properties sheet, which is quite useful if a class
        // has subclassed other classes, resulting in many fields, and
        // no easy way to discern which field comes from which class.
        Sheet sheet = getSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_DECLARING_TYPE,
                field.declaringType().name()));
    }

    /**
     * Sets the object reference this node is associated with.
     *
     * @param  object  object reference.
     */
    public void setObjectReference(ObjectReference object) {
        this.object = object;
    }
}
