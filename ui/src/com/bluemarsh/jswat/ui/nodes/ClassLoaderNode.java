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
 * $Id: ClassLoaderNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.sun.jdi.ClassLoaderReference;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;

/**
 * Represents a class loader in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class ClassLoaderNode extends BaseNode {
    /** The class loader reference we represent (null = boot). */
    private ClassLoaderReference loader;
    /** The name of the class loader. */
    private String name;

    /**
     * Constructs a ClassLoaderNode to represent the given class loader.
     *
     * @param  kids    children of this node.
     * @param  loader  the class loader (may be null to represent the
     *                 boot classloader).
     */
    public ClassLoaderNode(Children kids, ClassLoaderReference loader) {
        super(kids);
        this.loader = loader;
        if (loader != null) {
            name = loader.referenceType().name();
        } else {
            // For the node expansion support.
            name = "boot";
        }
        setIconBaseWithExtension(
                "com/bluemarsh/jswat/ui/resources/nodes/ClassLoader.gif");
    }

    /**
     * Creates a node property of the given key (same as the column keys).
     *
     * @param  key    property name (same as matching column).
     * @param  value  display value.
     * @return  new property.
     */
    private Node.Property createProperty(String key, String value) {
        String desc = NbBundle.getMessage(
                ClassLoaderNode.class, "CTL_ClassProperty_Desc_" + key);
        String name = NbBundle.getMessage(
                ClassLoaderNode.class, "CTL_ClassProperty_Name_" + key);
        return new ReadOnlyProperty(key, String.class, name, desc, value);
    }

    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        if (loader == null) {
            String n = NbBundle.getMessage(ClassLoaderNode.class,
                    "CTL_ClassLoaderNode_SystemLoader_Name");
            set.put(createProperty(PROP_NAME, n));
        }  else {
            set.put(createProperty(PROP_NAME, name));
        }
        return sheet;
    }

    public String getDisplayName() {
        if (loader == null) {
            return NbBundle.getMessage(ClassLoaderNode.class,
                    "CTL_ClassLoaderNode_SystemLoader_Name");
        }  else {
            return NbBundle.getMessage(ClassLoaderNode.class,
                    "CTL_ClassLoaderNode_Loader_Name", name);
        }
    }

    /**
     * Returns the ClassLoaderReference this node represents.
     *
     * @return  class loader.
     */
    public ClassLoaderReference getLoader() {
        return loader;
    }

    public String getName() {
        // Make the name unique so NodeOp can find this node later.
        // For the boot class loader, it is unique enough already.
        if (loader == null) {
            return name;
        } else {
            return name + ':' + loader.uniqueID();
        }
    }
}
