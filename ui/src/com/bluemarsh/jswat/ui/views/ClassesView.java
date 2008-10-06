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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ClassesView.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Classes;
import com.bluemarsh.jswat.ui.components.FindPanel;
import com.bluemarsh.jswat.ui.components.Findable;
import com.bluemarsh.jswat.ui.nodes.BaseNode;
import com.bluemarsh.jswat.ui.nodes.ClassLoaderNode;
import com.bluemarsh.jswat.ui.nodes.ClassNode;
import com.bluemarsh.jswat.ui.nodes.PackageNode;
import com.bluemarsh.jswat.ui.nodes.ShowSourceAction;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
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
import javax.swing.Action;
import javax.swing.JScrollPane;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
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
    /** Name of the name property. */
    private static final String PROP_NAME = "name";
    /** The singleton instance of this class. */
    private static ClassesView theInstance;
    /** Preferred window system identifier for this window. */
    public static final String PREFERRED_ID = "classes";
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentTreeTableView nodeView;
    /** Array of actions for our nodes. */
    private Action[] nodeActions;
    /** Array of actions for the root node. */
    private Action[] rootActions;
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
        nodeActions = new Action[] {
            SystemAction.get(ShowSourceAction.class),
            SystemAction.get(FindAction.class),
            SystemAction.get(HotSwapAction.class),
            SystemAction.get(RefreshAction.class),
        };
        rootActions = new Action[] {
            SystemAction.get(FindAction.class),
            SystemAction.get(RefreshAction.class),
        };
        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the classes view.
        nodeView = new PersistentTreeTableView();
        nodeView.setRootVisible(false);
        columns = new Node.Property[] {
            // The Name column is always sorted, so disallow sortability.
            new Column(PROP_NAME, true, false, false),
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
        BaseNode rootNode = new BaseNode(kids);
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                ClassesView.class, "CTL_ClassesView_Column_Name_" + PROP_NAME));
        rootNode.setShortDescription(NbBundle.getMessage(
                ClassesView.class, "CTL_ClassesView_Column_Desc_" + PROP_NAME));
        rootNode.setActions(rootActions);
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
        Node rootNode = explorerManager.getRootContext();
        final List<String[]> expanded = getExpanded(nodeView, rootNode);
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
                    loaders.clear();
                    break;
                }
            }

            // Build the nodes for the class loaders.
            for (ClassLoaderReference clr : loaders) {
                Children ch = new PackageChildren(vm);
                Node node = createClassLoaderNode(ch, clr);
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
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Need to refetch the root in case it was replaced.
                Node rootNode = explorerManager.getRootContext();
                expandPaths(expanded, nodeView, rootNode);
            }
        });
    }

    public void closing(SessionEvent sevt) {
    }

    /**
     * Called only when top component was closed so that now it is closed
     * on all workspaces in the system.
     */
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

    /**
     * Called only when top component was closed on all workspaces before
     * and now is opened for the first time on some workspace.
     */
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

    public void connected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    /**
     * Creates a Node to represent the given class loader.
     *
     * @param  children  the children for the new Node.
     * @param  loader    the class loader.
     * @return  node for the loader.
     */
    private Node createClassLoaderNode(Children children,
            ClassLoaderReference loader) {
        BaseNode node = new ClassLoaderNode(children, loader);
        node.setActions(nodeActions);
        return node;
    }

    /**
     * Creates a Node to represent the given class.
     *
     * @param  type  the class in the debuggee.
     * @return  node for the class.
     */
    private Node createClassNode(ReferenceType type) {
        ClassNode node = new ClassNode(type);
        node.setActions(nodeActions);
        node.setPreferredAction(SystemAction.get(ShowSourceAction.class));
        return node;
    }

    /**
     * Creates a Node to represent a package by the given name.
     *
     * @param  children  the children for the new Node.
     * @param  name      the name of the package.
     * @param  loader    the class loader.
     * @return  node for the package.
     */
    private Node createPackageNode(PackageChildren children, String name,
            ClassLoaderReference loader) {
        BaseNode node = new PackageNode(children, name, loader);
        node.setActions(nodeActions);
        return node;
    }

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

    /**
     * Returns the display name for this component.
     *
     * @return  display name.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(ClassesView.class, "CTL_ClassesView_Name");
    }

    /**
     * Returns the explorer manager for this view.
     *
     * @return  explorer manager.
     */
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    /**
     * Get the help context for this component.
     *
     * @return  the help context.
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-classes-view");
    }

    /**
     * Returns the desired persistent type for this component.
     *
     * @return  desired persistence type.
     */
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    /**
     * Returns the display name for this component.
     *
     * @return  tooltip text.
     */
    public String getToolTipText() {
        return NbBundle.getMessage(ClassesView.class, "CTL_ClassesView_Tooltip");
    }

    public void opened(Session session) {
    }

    /**
     * Returns the unique identifier for this component.
     *
     * @return  unique component identifier.
     */
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        restoreColumns(in, columns);
        nodeView.setProperties(columns);
        nodeView.restoreColumnWidths(in);
    }

    public void resuming(SessionEvent sevt) {
    }

    public void run() {
        buildTree();
    }

    /**
     * Called when a Session has been added to the SessionManager.
     *
     * @param  e  session manager event.
     */
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
    }

    /**
     * Called when a Session has been removed from the SessionManager.
     *
     * @param  e  session manager event.
     */
    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        session.removeSessionListener(this);
    }

    /**
     * Called when a Session has been made the current session.
     *
     * @param  e  session manager event.
     */
    public void sessionSetCurrent(SessionManagerEvent e) {
        buildTree();
    }

    public void suspended(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

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
    private static class ClassComparator implements Comparator<ReferenceType> {

        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than,
         * equal to, or greater than the second.
         *
         * @param  o1  first ReferenceType.
         * @param  o2  second ReferenceType.
         */
        public int compare(ReferenceType o1, ReferenceType o2) {
            String n1 = o1.name();
            String n2 = o2.name();
            return n1.compareTo(n2);
        }
    }

    /**
     * A Comparator for ClassLoaderReference objects.
     *
     * @author  Nathan Fiedler
     */
    private static class LoaderComparator implements Comparator<ClassLoaderReference> {

        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than,
         * equal to, or greater than the second.
         *
         * @param  o1  first ClassLoaderReference.
         * @param  o2  second ClassLoaderReference.
         * @return  zero if equal, -1 if o1 less than o2, otherwise +1.
         */
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
     * Compares ClassNode and PackageNode objects for order.
     *
     * @author  Nathan Fiedler
     */
    private static class PackageComparator implements Comparator<Node> {

        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than,
         * equal to, or greater than the second.
         *
         * @param  o1  first ClassLoaderReference.
         * @param  o2  second ClassLoaderReference.
         */
        public int compare(Node o1, Node o2) {
            String n1 = o1.getDisplayName();
            String n2 = o2.getDisplayName();
            return n1.compareTo(n2);
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
        public Column(String key, boolean tree, boolean sortable, boolean hidden) {
            super(key, String.class,
                  NbBundle.getMessage(Column.class, "CTL_ClassesView_Column_Name_" + key),
                  NbBundle.getMessage(Column.class, "CTL_ClassesView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("ComparableColumnTTV", Boolean.valueOf(sortable));
            setValue("InvisibleInTreeTableView", Boolean.valueOf(hidden));
        }

        /**
         * Returns the column key value.
         *
         * @return  column value.
         */
        public Object getValue()
                throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }

    /**
     * Contains the children for a ClassLoaderNode or PackageNode.
     *
     * @author  Nathan Fiedler
     */
    private class PackageChildren extends Children.SortedArray {
        /** The virtual machine from which to get classes. */
        private VirtualMachine vm;

        /**
         * Constructs a new instance of PackageChildren.
         *
         * @param  vm  the virtual machine.
         */
        public PackageChildren(VirtualMachine vm) {
            super();
            this.vm = vm;
            setComparator(new PackageComparator());
        }

        protected void addNotify() {
            super.addNotify();
            Node parent = getNode();
            ClassLoaderReference clr = null;
            String prefix = null;
            if (parent instanceof ClassLoaderNode) {
                clr = ((ClassLoaderNode) parent).getLoader();
                prefix = "";
            } else if (parent instanceof PackageNode) {
                clr = ((PackageNode) parent).getLoader();
                prefix = ((PackageNode) parent).getFullName() + '.';
            }
            List<ReferenceType> classes = getClasses(vm);
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
            int ii = 0;
            for (String name : packages) {
                PackageChildren pc = new PackageChildren(vm);
                kids[ii++] = createPackageNode(pc, name, clr);
            }
            for (ReferenceType type : types) {
                Node node = createClassNode(type);
                kids[ii++] = node;
            }
            super.add(kids);
        }
    }

    /**
     * Implements the action of finding a class by substring.
     *
     * @author  Nathan Fiedler
     */
    public static class FindAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Panel, if it has been created. */
        private FindPanel panel;

        /**
         * Indicates if this action can be invoked on any thread.
         *
         * @return  true if asynchronous, false otherwise.
         */
        protected boolean asynchronous() {
            return false;
        }

        /**
         * Test whether the action should be enabled based on the currently
         * activated nodes.
         *
         * @param  activatedNodes  target nodes.
         * @return  true if enabled, false otherwise.
         */
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        /**
         * Get a help context for the action.
         *
         * @return  help context.
         */
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        /**
         * Get a human presentable name of the action.
         */
        public String getName() {
            return NbBundle.getMessage(FindAction.class,
                    "LBL_ClassesView_FindAction");
        }

        /**
         * Actually perform the action.
         *
         * @param  activatedNodes  activated nodes.
         */
        protected void performAction(Node[] activatedNodes) {
            ClassesView view = ClassesView.findInstance();
            if (panel == null) {
                ClassFindable cf = new ClassFindable(view);
                panel = new FindPanel(cf);
                cf.setFindPanel(panel);
            } else {
                // Remove the existing one, if it is still there.
                view.remove(panel);
            }
            view.add(panel, BorderLayout.SOUTH);
            view.revalidate();
            view.repaint();
        }
    }

    /**
     * Implements Findable over the classes view.
     *
     * @author  Nathan Fiedler
     */
    private static class ClassFindable implements Findable {
        /** View with which to interact. */
        private ClassesView view;
        /** The search interface. */
        private FindPanel panel;
        /** Index of the previous match, within the all-classes list. */
        private int previousMatchIndex;

        /**
         * Creates a new instance of ClassFindable.
         *
         * @param  view  classes view.
         */
        public ClassFindable(ClassesView view) {
            this.view = view;
        }

        public void dismiss() {
            view.remove(panel);
            view.revalidate();
            view.repaint();
        }

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

        /**
         * Sets the panel that this findable manages.
         *
         * @param  panel  interface for the findable.
         */
        public void setFindPanel(FindPanel panel) {
            this.panel = panel;
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

        /**
         * Indicates if this action can be invoked on any thread.
         *
         * @return  true if asynchronous, false otherwise.
         */
        protected boolean asynchronous() {
            return false;
        }

        /**
         * Test whether the action should be enabled based on the currently
         * activated nodes.
         *
         * @param  activatedNodes  target nodes.
         * @return  true if enabled, false otherwise.
         */
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        /**
         * Get a help context for the action.
         *
         * @return  help context.
         */
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        /**
         * Get a human presentable name of the action.
         */
        public String getName() {
            return NbBundle.getMessage(RefreshAction.class,
                    "LBL_ClassesView_RefreshAction");
        }

        /**
         * Actually perform the action.
         *
         * @param  activatedNodes  activated nodes.
         */
        protected void performAction(Node[] activatedNodes) {
            ClassesView view = ClassesView.findInstance();
            view.buildTree();
        }
    }

    /**
     * Implements the action of redefining the source code for a class.
     *
     * @author  Nathan Fiedler
     */
    public static class HotSwapAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Indicates if this action can be invoked on any thread.
         *
         * @return  true if asynchronous, false otherwise.
         */
        protected boolean asynchronous() {
            return false;
        }

        /**
         * Test whether the action should be enabled based on the currently
         * activated nodes.
         *
         * @param  activatedNodes  target nodes.
         * @return  true if enabled, false otherwise.
         */
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1 &&
                    activatedNodes[0] instanceof ClassNode) {
                return true;
            }
            return false;
        }

        /**
         * Get a help context for the action.
         *
         * @return  help context.
         */
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        /**
         * Get a human presentable name of the action.
         */
        public String getName() {
            return NbBundle.getMessage(HotSwapAction.class,
                    "LBL_ClassesView_HotSwapAction");
        }

        /**
         * Actually perform the action.
         *
         * @param  activatedNodes  activated nodes.
         */
        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1 &&
                    activatedNodes[0] instanceof ClassNode) {
                ClassNode cn = (ClassNode) activatedNodes[0];
                ReferenceType rt = cn.getReferenceType();
                Session session = SessionProvider.getCurrentSession();
                PathManager pm = PathProvider.getPathManager(session);
                StatusDisplayer sd = StatusDisplayer.getDefault();

                // Try to find the .class file.
                FileObject fo = pm.findByteCode(rt);
                if (fo == null) {
                    sd.setStatusText(NbBundle.getMessage(HotSwapAction.class,
                            "ERR_ClassesView_hotswap_ClassFileNotFound"));
                    return;
                }

                // Do the actual hotswap operation.
                String errorMsg = null;
                InputStream is = null;
                VirtualMachine vm = session.getConnection().getVM();
                try {
                    is = fo.getInputStream();
                    Classes.hotswap(rt, is, vm);
                } catch (UnsupportedOperationException uoe) {
                    if (!vm.canRedefineClasses()) {
                        errorMsg = NbBundle.getMessage(HotSwapAction.class,
                                "ERR_ClassesView_hotswap_CannotHotSwap");
                    } else if (!vm.canAddMethod()) {
                        errorMsg = NbBundle.getMessage(HotSwapAction.class,
                                "ERR_ClassesView_hotswap_CannotAddMethod");
                    } else if (!vm.canUnrestrictedlyRedefineClasses()) {
                        errorMsg = NbBundle.getMessage(HotSwapAction.class,
                                "ERR_ClassesView_hotswap_NotUnrestricted");
                    } else {
                        errorMsg = NbBundle.getMessage(HotSwapAction.class,
                                "ERR_ClassesView_hotswap_Unsupported");
                    }
                } catch (IOException ioe) {
                    errorMsg = NbBundle.getMessage(HotSwapAction.class,
                            "ERR_ClassesView_hotswap_IOException", ioe);
                } catch (NoClassDefFoundError ncdfe) {
                    errorMsg = NbBundle.getMessage(HotSwapAction.class,
                            "ERR_ClassesView_hotswap_WrongClass");
                } catch (VerifyError ve) {
                    errorMsg = NbBundle.getMessage(HotSwapAction.class,
                            "ERR_ClassesView_hotswap_VerifyError", ve);
                } catch (UnsupportedClassVersionError ucve) {
                    errorMsg = NbBundle.getMessage(HotSwapAction.class,
                            "ERR_ClassesView_hotswap_VersionError", ucve);
                } catch (ClassFormatError cfe) {
                    errorMsg = NbBundle.getMessage(HotSwapAction.class,
                            "ERR_ClassesView_hotswap_FormatError", cfe);
                } catch (ClassCircularityError cce) {
                    errorMsg = NbBundle.getMessage(HotSwapAction.class,
                            "ERR_ClassesView_hotswap_Circularity", cce);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ioe) { }
                    }
                }

                // Display the results.
                if (errorMsg != null) {
                    sd.setStatusText(errorMsg);
                } else {
                    sd.setStatusText(NbBundle.getMessage(HotSwapAction.class,
                            "CTL_ClassesView_hotswap_Success"));
                }
            }
        }
    }
}
