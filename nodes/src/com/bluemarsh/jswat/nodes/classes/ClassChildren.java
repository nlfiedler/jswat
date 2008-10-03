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
 * are Copyright (C) 2007-2008. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ClassChildren.java 25 2008-06-29 15:37:23Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.classes;

import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Strings;
import com.bluemarsh.jswat.nodes.ReadOnlyProperty;
import com.bluemarsh.jswat.nodes.ShowSourceAction;
import com.bluemarsh.jswat.nodes.ShowSourceCookie;
import com.bluemarsh.jswat.ui.editor.EditorSupport;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Contains the children for a ClassNode.
 *
 * @author Nathan Fiedler
 */
public class ClassChildren extends Children.SortedArray {
    /** The reference type from which to get class members. */
    private ReferenceType type;

    /**
     * Constructs a new instance of ClassChildren.
     *
     * @param  type  the reference type.
     */
    public ClassChildren(ReferenceType type) {
        super();
        this.type = type;
        setComparator(new MethodComparator());
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        List<Method> methods = type.methods();
        Node[] kids = new Node[methods.size()];
        int ii = 0;
        for (Method method : methods) {
            Node node = new DefaultMethodNode(method);
            kids[ii++] = node;
        }
        add(kids);
    }

    /**
     * Compares MethodNode objects for order.
     *
     * @author  Nathan Fiedler
     */
    private static class MethodComparator implements Comparator<Node> {

        public int compare(Node o1, Node o2) {
            String n1 = o1.getDisplayName();
            String n2 = o2.getDisplayName();
            return n1.compareTo(n2);
        }
    }

    /**
     * Represents a method (class member) in the classes view.
     *
     * @author  Nathan Fiedler
     */
    private static class DefaultMethodNode extends MethodNode
            implements ShowSourceCookie {
// XXX org.openide.src is gone, find the alternative
//        private static final String INITIALIZER =
//                "org/openide/src/resources/initializer.gif";
//        private static final String INITIALIZER_ST =
//                "org/openide/src/resources/initializerSt.gif";
//        private static final String CONSTRUCTOR_PUBLIC =
//                "org/openide/src/resources/constructorPublic.gif";
//        private static final String CONSTRUCTOR_PROTECTED =
//                "org/openide/src/resources/constructorProtected.gif";
//        private static final String CONSTRUCTOR_PRIVATE =
//                "org/openide/src/resources/constructorPrivate.gif";
//        private static final String CONSTRUCTOR_PACKAGE =
//                "org/openide/src/resources/constructorPackage.gif";
//        private static final String METHOD_PUBLIC =
//                "org/openide/src/resources/methodPublic.gif";
//        private static final String METHOD_PROTECTED =
//                "org/openide/src/resources/methodProtected.gif";
//        private static final String METHOD_PRIVATE =
//                "org/openide/src/resources/methodPrivate.gif";
//        private static final String METHOD_PACKAGE =
//                "org/openide/src/resources/methodPackage.gif";
//        private static final String METHOD_ST_PUBLIC =
//                "org/openide/src/resources/methodStPublic.gif";
//        private static final String METHOD_ST_PROTECTED =
//                "org/openide/src/resources/methodStProtected.gif";
//        private static final String METHOD_ST_PRIVATE =
//                "org/openide/src/resources/methodStPrivate.gif";
//        private static final String METHOD_ST_PACKAGE =
//                "org/openide/src/resources/methodStPackage.gif";
        /** The method we represent. */
        private Method method;
        /** The descriptive name for this method. */
        private String longName;

        /**
         * Creates a new instance of DefaultMethodNode.
         *
         * @param  method  the class method.
         */
        public DefaultMethodNode(Method method) {
            super();
            this.method = method;
            getLookupContent().add(this);

            // Build out the long description of this method.
            StringBuilder sb = new StringBuilder();
            if (method.isConstructor()) {
                // The instance initializer appears as a constructor in JDI.
                sb.append(method.declaringType().name());
            } else if (method.isStaticInitializer()) {
                sb.append(NbBundle.getMessage(DefaultMethodNode.class,
                        "LBL_MethodNode_staticinit"));
            } else {
                sb.append(method.name());
            }
            sb.append('(');
            List<String> typeNames = new ArrayList<String>(
                    method.argumentTypeNames());
            ListIterator<String> tniter = typeNames.listIterator();
            while (tniter.hasNext()) {
                tniter.set(Names.getShortClassName(tniter.next()));
            }
            String args = Strings.listToString(typeNames, ",");
            sb.append(args);
            sb.append("):");
            sb.append(method.returnTypeName());
            longName = sb.toString();

            // Define the icon for this method.
// XXX: org.openide.src is gone, need to find the icons again
//            if (method.isStaticInitializer()) {
//                setIconBaseWithExtension(INITIALIZER_ST);
//            } else if (method.isConstructor()) {
//                if (method.isPrivate()) {
//                    setIconBaseWithExtension(CONSTRUCTOR_PRIVATE);
//                } else if (method.isProtected()) {
//                    setIconBaseWithExtension(CONSTRUCTOR_PROTECTED);
//                } else if (method.isPublic()) {
//                    setIconBaseWithExtension(CONSTRUCTOR_PUBLIC);
//                } else {
//                    setIconBaseWithExtension(CONSTRUCTOR_PACKAGE);
//                }
//            } else if (method.isStatic()) {
//                if (method.isPrivate()) {
//                    setIconBaseWithExtension(METHOD_ST_PRIVATE);
//                } else if (method.isProtected()) {
//                    setIconBaseWithExtension(METHOD_ST_PROTECTED);
//                } else if (method.isPublic()) {
//                    setIconBaseWithExtension(METHOD_ST_PUBLIC);
//                } else {
//                    setIconBaseWithExtension(METHOD_ST_PACKAGE);
//                }
//            } else {
//                if (method.isPrivate()) {
//                    setIconBaseWithExtension(METHOD_PRIVATE);
//                } else if (method.isProtected()) {
//                    setIconBaseWithExtension(METHOD_PROTECTED);
//                } else if (method.isPublic()) {
//                    setIconBaseWithExtension(METHOD_PUBLIC);
//                } else {
//                    setIconBaseWithExtension(METHOD_PACKAGE);
//                }
//            }
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
                    MethodNode.class, "CTL_MethodProperty_Desc_" + key);
            String name = NbBundle.getMessage(
                    MethodNode.class, "CTL_MethodProperty_Name_" + key);
            return new ReadOnlyProperty(key, String.class, name, desc, value);
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = Sheet.createDefault();
            Sheet.Set set = sheet.get(Sheet.PROPERTIES);
            set.put(createProperty(PROP_NAME, method.name()));
            return sheet;
        }

        @Override
        public String getDisplayName() {
            return longName;
        }

        public Method getMethod() {
            return method;
        }

        @Override
        public String getName() {
            return method.name();
        }

        protected Action[] getNodeActions() {
            return new Action[] {
                SystemAction.get(ShowSourceAction.class),
                SystemAction.get(MethodBreakpointAction.class),
            };
        }

        public void showSource() {
            Session session = SessionProvider.getCurrentSession();
            PathManager pm = PathProvider.getPathManager(session);
            ReferenceType type = method.declaringType();
            FileObject src = pm.findSource(type);
            if (src != null) {
                try {
                    String url = src.getURL().toString();
                    int line = method.location().lineNumber();
                    EditorSupport.getDefault().showSource(url, line);
                } catch (FileStateInvalidException fsie) {
                    ErrorManager.getDefault().notify(fsie);
                }
            } else {
                String msg = NbBundle.getMessage(DefaultMethodNode.class,
                        "ERR_SourceMissing", type.name());
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                        msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
}
