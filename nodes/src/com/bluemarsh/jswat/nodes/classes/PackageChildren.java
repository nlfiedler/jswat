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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes.classes;

import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.nodes.BaseNode;
import com.bluemarsh.jswat.nodes.NodeFactory;
import com.bluemarsh.jswat.nodes.ReadOnlyProperty;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Action;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Contains the children for a ClassLoaderNode, representing the packages
 * containing classes in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class PackageChildren extends Children.SortedArray {
    /** The virtual machine we are associated with. */
    private VirtualMachine vm;

    /**
     * Creates a new instance of PackageChildren.
     *
     * @param  vm  virtual machine.
     */
    public PackageChildren(VirtualMachine vm) {
        super();
        this.vm = vm;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        Node parent = getNode();
        ClassLoaderReference clr = null;
        String prefix = null;
        if (parent instanceof ClassLoaderNode) {
            clr = ((ClassLoaderNode) parent).getClassLoader();
            prefix = "";
        } else if (parent instanceof PackageNode) {
            clr = ((PackageNode) parent).getClassLoader();
            prefix = ((PackageNode) parent).getFullName()  + '.';
        }
        List<ReferenceType> classes = vm.allClasses();
        Set<String> packages = new TreeSet<String>();
        Set<ReferenceType> types = new TreeSet<ReferenceType>(
                new ClassComparator());
        for (ReferenceType clazz : classes) {
            // Because boot classloader is null, we have to do all this
            // extra work scanning all classes and checking for equality.
            if (!(clazz instanceof ArrayType) && clazz.classLoader() == clr) {
                String name = clazz.name();
                if (name.startsWith(prefix)) {
                    int start = prefix.length();
                    int end = name.indexOf('.', start);
                    if (end < 0) {
                        // This is a reference type.
                        types.add(clazz);
                    } else {
                        name = name.substring(0, end);
                        packages.add(name);
                    }
                }
            }
        }

        // Construct the children, with packages before classes.
        Node[] kids = new Node[packages.size() + types.size()];
        NodeFactory factory = NodeFactory.getDefault();
        int ii = 0;
        for (String name : packages) {
            kids[ii++] = new PackageNode(name, vm, clr);
        }
        for (ReferenceType type : types) {
            Node node = factory.createClassNode(type);
            kids[ii++] = node;
        }
        super.add(kids);
    }

    /**
     * A Comparator for ClassLoaderReference objects.
     *
     * @author  Nathan Fiedler
     */
    private static class ClassComparator implements Comparator<ReferenceType> {

        @Override
        public int compare(ReferenceType o1, ReferenceType o2) {
            String n1 = o1.name();
            String n2 = o2.name();
            return n1.compareTo(n2);
        }
    }

    @Override
    public Comparator<? super Node> getComparator() {
        return new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                // Sort package nodes before class nodes.
                if (o1 instanceof PackageNode && o2 instanceof ClassNode) {
                    return -1;
                } else if (o1 instanceof ClassNode && o2 instanceof PackageNode) {
                    return 1;
                }
                // Else sort by name alone.
                String n1 = o1.getDisplayName();
                String n2 = o2.getDisplayName();
                return n1.compareTo(n2);
            }
        };
    }

    /**
     * Represents a package in the classes view.
     *
     * @author  Nathan Fiedler
     */
    private static class PackageNode extends BaseNode {
        /** The class loader reference we represent (null = boot). */
        private ClassLoaderReference loader;
        /** The full name of the package. */
        private String longName;
        /** The short name of the package. */
        private String shortName;

        /**
         * Creates a new instance of PackageNode.
         *
         * @param  name    full name of the package.
         * @param  vm      virtual machine.
         * @param  loader  the class loader (null is boot classloader).
         */
        public PackageNode(String name, VirtualMachine vm,
                ClassLoaderReference loader) {
            super(new PackageChildren(vm));
            this.longName = name;
            this.loader = loader;
            shortName = Names.getShortClassName(name);
            setIconBaseWithExtension(
                    "com/bluemarsh/jswat/nodes/resources/Folder.gif");
        }

        /**
         * Creates a node property of the given key (same as the column keys).
         *
         * @param  key   property name (same as matching column).
         * @param  value  display value.
         * @return  new property.
         */
        private Node.Property createProperty(String key, String value) {
            String desc = NbBundle.getMessage(
                    PackageNode.class, "CTL_ClassProperty_Desc_" + key);
            String name = NbBundle.getMessage(
                    PackageNode.class, "CTL_ClassProperty_Name_" + key);
            return new ReadOnlyProperty(key, String.class, name, desc, value);
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = Sheet.createDefault();
            Sheet.Set set = sheet.get(Sheet.PROPERTIES);
            set.put(createProperty(PROP_NAME, longName));
            return sheet;
        }

        @Override
        public String getDisplayName() {
            return shortName;
        }

        /**
         * Gets the full name of the package.
         *
         * @return  full package name.
         */
        public String getFullName() {
            return longName;
        }

        /**
         * Returns the ClassLoaderReference this node belongs to.
         *
         * @return  class loader.
         */
        public ClassLoaderReference getClassLoader() {
            return loader;
        }

        @Override
        public String getName() {
            return shortName;
        }

        @Override
        protected Action[] getNodeActions() {
            return null;
        }
    }
}
