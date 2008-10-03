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
 * $Id: DefaultClassLoaderNode.java 24 2007-12-25 07:48:54Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.classes;

import com.bluemarsh.jswat.nodes.ReadOnlyProperty;
import com.sun.jdi.ClassLoaderReference;
import javax.swing.Action;
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
public class DefaultClassLoaderNode extends ClassLoaderNode {
    /** The class loader reference we represent (null = boot). */
    private ClassLoaderReference loader;
    /** The name of the class loader. */
    private String loaderName;
    /** The unique identifier of the class loader, or 0 if boot loader */
    private String loaderId;

    /**
     * Constructs a ClassLoaderNode to represent the given class loader.
     *
     * @param  kids    children of this node.
     * @param  loader  the class loader (may be null to represent the
     *                 boot classloader).
     */
    public DefaultClassLoaderNode(Children kids, ClassLoaderReference loader) {
        super(kids);
        this.loader = loader;
        if (loader != null) {
            loaderName = loader.referenceType().name();
            loaderId = String.valueOf(loader.uniqueID());
        } else {
            // For the node expansion support.
            loaderName = "boot";
            loaderId = "0";
        }
        setIconBaseWithExtension(
                "com/bluemarsh/jswat/nodes/resources/ClassLoader.gif");
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

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        if (loader == null) {
            String n = NbBundle.getMessage(ClassLoaderNode.class,
                    "CTL_ClassLoaderNode_SystemLoader_Name");
            set.put(createProperty(PROP_NAME, n));
        } else {
            set.put(createProperty(PROP_NAME, loaderName));
        }
        return sheet;
    }

    @Override
    public String getDisplayName() {
        if (loader == null) {
            return NbBundle.getMessage(ClassLoaderNode.class,
                    "CTL_ClassLoaderNode_SystemLoader_Name");
        } else {
            return NbBundle.getMessage(ClassLoaderNode.class,
                    "CTL_ClassLoaderNode_Loader_Name", loaderName, loaderId);
        }
    }

    public ClassLoaderReference getClassLoader() {
        return loader;
    }

    @Override
    public String getName() {
        // Make the name unique so NodeOp can find this node later.
        // For the boot class loader, it is unique enough already.
        if (loader == null) {
            return loaderName;
        } else {
            return loaderName + ':' + loaderId;
        }
    }

    protected Action[] getNodeActions() {
        return null;
    }
}
