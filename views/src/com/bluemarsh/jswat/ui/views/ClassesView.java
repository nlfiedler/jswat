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
 * are Copyright (C) 2005-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.nodes.NodeFactory;
import com.bluemarsh.jswat.ui.components.FindPanel;
import com.bluemarsh.jswat.ui.components.Findable;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Class ClassesView displays the class loaders and their defined classes.
 *
 * @author  Nathan Fiedler
 */
public class ClassesView extends AbstractView
        implements ExplorerManager.Provider, Runnable, SessionListener,
        SessionManagerListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The singleton instance of this class. */
    private static ClassesView theInstance;
    /** Preferred window system identifier for this window. */
    public static final String PREFERRED_ID = "classes";
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentOutlineView nodeView;
    /** Lists of classes, keyed by the VirtualMachine. */
    private Map<VirtualMachine, WeakReference<List<ReferenceType>>> classesCache;
    /** Columns for the tree-table view. */
    private transient Node.Property[] columns;

    /**
     * Constructs a new instance of ClassesView. Clients should not construct
     * this class but rather use the findInstance() method to get the single
     * instance from the window system.
     */
    public ClassesView() {
        classesCache = new HashMap<VirtualMachine,
                WeakReference<List<ReferenceType>>>();
        explorerManager = new ExplorerManager();
        ActionMap map = getActionMap();
        CallbackSystemAction globalFindAction =
                (CallbackSystemAction) SystemAction.get(FindAction.class);
        Object findKey = globalFindAction.getActionMapKey();
        map.put(findKey, new ClassesFindAction(this));
        associateLookup(ExplorerUtils.createLookup(explorerManager, map));
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = (KeyStroke) globalFindAction.getValue(Action.ACCELERATOR_KEY);
        if (key == null) {
            key = KeyStroke.getKeyStroke("control F");
        }
        keys.put(key, findKey);

        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the classes view.
        nodeView = new PersistentOutlineView();
        nodeView.getOutline().setRootVisible(false);
        columns = new Node.Property[] {
            // The Name column is always sorted, so disallow sortability.
            new Column(Node.PROP_NAME, true, false, false),
        };
        nodeView.setProperties(columns);
        // This, oddly enough, enables the column hiding feature.
        nodeView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setLayout(new BorderLayout());
        add(nodeView, BorderLayout.CENTER);
    }

    /**
     * Build a new root node and set it to be the explorer's root context.
     *
     * @param  kids  root node's children, or Children.LEAF if none.
     */
    private void buildRoot(Children kids) {
        // Use a simple root node for which we can set the display name;
        // otherwise the logical root's properties affect the table headers.
        Node rootNode = new AbstractNode(kids) {
            @Override
            public Action[] getActions(boolean b) {
                return new Action[] {
                    SystemAction.get(RefreshAction.class),
                };
            }
        };
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                ClassesView.class, "CTL_ClassesView_Column_Name_" + Node.PROP_NAME));
        rootNode.setShortDescription(NbBundle.getMessage(
                ClassesView.class, "CTL_ClassesView_Column_Desc_" + Node.PROP_NAME));
        explorerManager.setRootContext(rootNode);
    }

    /**
     * Builds the node tree for the current session.
     */
    private void buildTree() {
        // Ensure that this method runs on the AWT thread to avoid slowing
        // the event dispatching thread, and the session connect process.
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(this);
            return;
        }

        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        List<Node> list = new LinkedList<Node>();
//        Node rootNode = explorerManager.getRootContext();
//        final List<String[]> expanded = getExpanded(nodeView, rootNode);
        if (session.isConnected()) {
            VirtualMachine vm = session.getConnection().getVM();
            // Remove any previously fetched list in order to get the latest.
            classesCache.remove(vm);
            // Now fetch the class list and cache it in case our inner
            // classes need access to them for building out the tree.
            List<ReferenceType> classes = getClasses(vm);

            // Build the set of class loaders.
            Set<ClassLoaderReference> loaders =
                    new TreeSet<ClassLoaderReference>(new LoaderComparator());
            for (ReferenceType clazz : classes) {
                try {
                    ClassLoaderReference clr = clazz.classLoader();
                    if (clr == null) {
                        // Boot classloader is null for some unknown reason.
                        loaders.add(null);
                    } else if (clr.referenceType().name().equals(
                            "sun.reflect.DelegatingClassLoader")) {
                        // This one is not interesting.
                        continue;
                    } else {
                        loaders.add(clr);
                    }
                } catch (ObjectCollectedException oce) {
                    // Yes, this can happen with a fresh list of classes.
                    // Just ignore that particular class and keep going.
                } catch (VMDisconnectedException vmde) {
                    // Likely to happen since we are running asynchronously.
                    loaders.clear();
                    break;
                }
            }

            // Build the nodes for the class loaders.
            NodeFactory factory = NodeFactory.getDefault();
            for (ClassLoaderReference clr : loaders) {
                Node node = factory.createClassLoaderNode(vm, clr);
                list.add(node);
            }
        }
        if (list.size() > 0) {
            Children children = new Children.Array();
            Node[] nodes = list.toArray(new Node[list.size()]);
            children.add(nodes);
            buildRoot(children);
        } else {
            buildRoot(Children.LEAF);
        }

        // Must expand the nodes on the AWT event thread.
// TODO: get tree expansion working
//        EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                // Need to refetch the root in case it was replaced.
//                Node rootNode = explorerManager.getRootContext();
//                expandPaths(expanded, nodeView, rootNode);
//            }
//        });
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        // Clear the tree to release resources.
        buildRoot(Children.LEAF);
        // Stop listening to everything that affects our tree.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.removeSessionManagerListener(this);
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            session.removeSessionListener(this);
        }
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        // Build out the tree.
        buildTree();
        // Start listening to everything that affects our tree.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.addSessionManagerListener(this);
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            session.addSessionListener(this);
        }
    }

    @Override
    public void connected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    /**
     * Obtain the window instance, first by looking for it in the window
     * system, then if not found, creating the instance.
     *
     * @return  the window instance.
     */
    public static synchronized ClassesView findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(
                PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Cannot find '" + PREFERRED_ID +
                    "' component in the window system");
            return getDefault();
        }
        if (win instanceof ClassesView) {
            return (ClassesView) win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '" +
                PREFERRED_ID + "' ID, this a potential source of errors");
        return getDefault();
    }

    /**
     * Retrieves the cached list of classes for the given virtual machine,
     * if available and not already garbage collected.
     *
     * @param  vm  virtual machine.
     * @return  all classes from vm, or empty if disconnected.
     */
    protected List<ReferenceType> getClasses(VirtualMachine vm) {
        WeakReference<List<ReferenceType>> ref = classesCache.get(vm);
        List<ReferenceType> classes = ref == null ? null : ref.get();
        if (classes == null) {
            try {
                classes = vm.allClasses();
                classesCache.put(vm, new WeakReference<List<ReferenceType>>(classes));
            } catch (VMDisconnectedException vmde) {
                classes = Collections.emptyList();
            }
        }
        return classes;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     * Clients should not call this method, but instead use findInstance().
     *
     * @return  instance of this class.
     */
    public static synchronized ClassesView getDefault() {
        if (theInstance == null) {
            theInstance = new ClassesView();
        }
        return theInstance;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ClassesView.class, "CTL_ClassesView_Name");
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-classes-view");
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(ClassesView.class, "CTL_ClassesView_Tooltip");
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        restoreColumns(in, columns);
        nodeView.setProperties(columns);
        nodeView.restoreColumnWidths(in);
    }

    @Override
    public void resuming(SessionEvent sevt) {
    }

    @Override
    public void run() {
        buildTree();
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        session.removeSessionListener(this);
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
        buildTree();
    }

    @Override
    public void suspended(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        saveColumns(out, columns);
        nodeView.saveColumnWidths(out);
    }

    /**
     * A Comparator for ClassLoaderReference objects.
     *
     * @author  Nathan Fiedler
     */
    private static class LoaderComparator implements Comparator<ClassLoaderReference> {

        @Override
        public int compare(ClassLoaderReference o1, ClassLoaderReference o2) {
            // Boot classloader is represented as a null.
            if (o1 == null) {
                if (o2 == null) {
                    return 0;
                }
                // Put the boot class loader before all others.
                return -1;
            } else if (o2 == null) {
                return 1;
            } else {
                // First sort by the class loader class name.
                String n1 = o1.referenceType().name();
                String n2 = o2.referenceType().name();
                int result = n1.compareTo(n2);
                if (result == 0) {
                    // If the names are the same, sort by unique ID.
                    long id1 = o1.uniqueID();
                    long id2 = o2.uniqueID();
                    if (id1 == id2) {
                        return 0;
                    } else if (id1 > id2) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    return result;
                }
            }
        }
    }

    /**
     * A column for the session table.
     *
     * @author  Nathan Fiedler
     */
    private class Column extends PropertySupport.ReadOnly {
        /** The keyword for this column. */
        private String key;

        /**
         * Constructs a new instance of Column.
         *
         * @param  key       keyword for this column.
         * @param  tree      true if this is the 'tree' column, false if 'table' column.
         * @param  sortable  true if this is sortable column, false otherwise.
         * @param  hidden    true to hide this column initially.
         */
        @SuppressWarnings("unchecked")
        public Column(String key, boolean tree, boolean sortable, boolean hidden) {
            super(key, String.class,
                  NbBundle.getMessage(Column.class, "CTL_ClassesView_Column_Name_" + key),
                  NbBundle.getMessage(Column.class, "CTL_ClassesView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("ComparableColumnTTV", Boolean.valueOf(sortable));
            setValue("InvisibleInTreeTableView", Boolean.valueOf(hidden));
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }

    /**
     * Implements the action of finding a class by substring.
     *
     * @author  Nathan Fiedler
     */
    private static class ClassesFindAction extends AbstractAction // TODO: needs a keyboard shortcut
            implements Findable {
        /** The associated view for this finder. */
        private ClassesView view;
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Panel, if it has been created. */
        private FindPanel panel;
        /** Index of the previous match, within the all-classes list. */
        private int previousMatchIndex;

        /**
         * Creates a new instance of ClassesFindAction.
         *
         * @param  view  the associated view.
         */
        public ClassesFindAction(ClassesView view) {
            this.view = view;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (panel == null) {
                panel = new FindPanel(this);
            } else {
                // Remove the existing one, if it is still there.
                view.remove(panel);
            }
            view.add(panel, BorderLayout.SOUTH);
            view.revalidate();
            view.repaint();
        }

        @Override
        public void dismiss() {
            view.remove(panel);
            view.revalidate();
            view.repaint();
        }

        @Override
        public boolean findNext(String query) {
            query = query.toLowerCase();
            Session session = SessionProvider.getSessionManager().getCurrent();
            JvmConnection jvmc = session.getConnection();
            if (jvmc != null) {
                VirtualMachine vm = jvmc.getVM();
                if (vm != null) {
                    List<ReferenceType> classes = view.getClasses(vm);
                    int index = 0;
                    int wrapCount = 0;
                    if (previousMatchIndex > 0) {
                        // Continue searching from previous result, taking
                        // care not to exceed the size of the list.
                        index = Math.min(previousMatchIndex + 1, classes.size());
                    } else {
                        // First time searching, don't wrap around.
                        wrapCount = 1;
                    }
                    while (wrapCount < 2) {
                        // Search list for classes matching query.
                        ReferenceType match = null;
                        ListIterator<ReferenceType> iter = classes.listIterator(index);
                        while (iter.hasNext()) {
                            ReferenceType clazz = iter.next();
                            // Ignore array types, they are not shown.
                            if (!(clazz instanceof ArrayType)) {
                                String name = clazz.name().toLowerCase();
                                if (name.contains(query)) {
                                    match = clazz;
                                    break;
                                }
                            }
                            index++;
                        }
                        if (match != null) {
                            // Save index of matching result.
                            previousMatchIndex = index;
                            // Locate the node and expand/scroll/select it.
                            Node node = locateNode(match);
                            if (node != null) {
                                view.nodeView.scrollAndSelectNode(node);
                                return true;
                            } else {
                                // Something bad happened, exit (or we loop forever).
                                wrapCount = 2;
                            }
                        } else {
                            index = 0;
                            wrapCount++;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean findPrevious(String query) {
            query = query.toLowerCase();
            Session session = SessionProvider.getSessionManager().getCurrent();
            JvmConnection jvmc = session.getConnection();
            if (jvmc != null) {
                VirtualMachine vm = jvmc.getVM();
                if (vm != null) {
                    List<ReferenceType> classes = view.getClasses(vm);
                    int index = classes.size();
                    int wrapCount = 0;
                    if (previousMatchIndex > 0) {
                        // Continue searching from previous result, taking
                        // care not to exceed the size of the list.
                        index = Math.min(previousMatchIndex, index);
                    } else {
                        // First time searching, don't wrap around.
                        wrapCount = 1;
                    }
                    while (wrapCount < 2) {
                        // Search list for classes matching query.
                        ReferenceType match = null;
                        ListIterator<ReferenceType> iter = classes.listIterator(index);
                        while (iter.hasPrevious()) {
                            ReferenceType clazz = iter.previous();
                            // Ignore array types, they are not shown.
                            if (!(clazz instanceof ArrayType)) {
                                String name = clazz.name().toLowerCase();
                                if (name.contains(query)) {
                                    match = clazz;
                                    break;
                                }
                            }
                            index--;
                        }
                        if (match != null) {
                            // Save index of matching result.
                            previousMatchIndex = index - 1;
                            // Locate the node and expand/scroll/select it.
                            Node node = locateNode(match);
                            if (node != null) {
                                view.nodeView.scrollAndSelectNode(node);
                                return true;
                            } else {
                                // Something bad happened, exit (or we loop forever).
                                wrapCount = 2;
                            }
                        } else {
                            index = classes.size();
                            wrapCount++;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Locates the Node that represents the given class.
         *
         * @param  clazz  reference type for which to find node.
         * @return  node for class, or null if none.
         */
        private Node locateNode(ReferenceType clazz) {
            ClassLoaderReference loader = clazz.classLoader();
            String cname = clazz.name();
            String[] cpath = cname.split("\\.");
            String[] path = new String[cpath.length + 1];
            if (loader == null) {
                path[0] = "boot";
            } else {
                path[0] = loader.referenceType().name() + ':' + loader.uniqueID();
            }
            System.arraycopy(cpath, 0, path, 1, cpath.length);
            try {
                return NodeOp.findPath(view.explorerManager.getRootContext(), path);
            } catch (NodeNotFoundException nnfe) {
                return null;
            }
        }
    }

    /**
     * Implements the action of refreshing the node tree.
     *
     * @author  Nathan Fiedler
     */
    public static class RefreshAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean asynchronous() {
            return false;
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(RefreshAction.class,
                    "LBL_RefreshAction_Name");
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            ClassesView view = ClassesView.findInstance();
            view.buildTree();
        }
    }
}
