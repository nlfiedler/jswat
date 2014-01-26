/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: ClassPanel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.ui.SmartPopupMenu;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Classes;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Class ClassPanel is responsible for displaying a class tree in a
 * navigable tree component.
 *
 * @author  Nathan Fiedler
 */
public class ClassPanel extends AbstractPanel
    implements MouseListener, TreeWillExpandListener, VMEventListener {
    /** Tree that displays classes. */
    private ClassTree uitree;
    /** Our UI component - scrollable pane. */
    private JScrollPane uicomp;
    /** Class prepare request we created. */
    private ClassPrepareRequest prepareRequest;
    /** Class unload request we created. */
    private ClassUnloadRequest unloadRequest;
    /** True if we are automatically refreshing the tree. */
    private boolean autoRefreshing;
    /** Our user preferences node. */
    private Preferences preferences;
    /** Names of loaded classes, sorted by name. */
    private List currentClassesList;

    /**
     * Constructs a new ClassPanel with the default tree.
     */
    public ClassPanel() {
        currentClassesList = new ArrayList();
        preferences = Preferences.userRoot().node("com/bluemarsh/jswat/panel");

        uitree = new ClassTree(new ClassTreeModel(new BasicNode()));
        uitree.setRootVisible(false);
        uitree.setShowsRootHandles(true);
        ToolTipManager.sharedInstance().registerComponent(uitree);
        uicomp = new JScrollPane(uitree);

        // Set up a mouse listener to open the source file of the
        // corresponding class in the tree, when double-clicked on.
        uitree.addMouseListener(this);
        uitree.addTreeWillExpandListener(this);

        TreeCellRenderer renderer = uitree.getCellRenderer();
        if (renderer instanceof DefaultTreeCellRenderer) {
            DefaultTreeCellRenderer dtcr = (DefaultTreeCellRenderer) renderer;
            // Kill all of those nasty icons.
            dtcr.setClosedIcon(null);
            dtcr.setLeafIcon(null);
            dtcr.setOpenIcon(null);
        }
    }

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        // Now is the safest time to do this.
        currentClassesList.clear();
        // Listen to all class prepare and unload events.
        enableAutoRefresh(preferences.getBoolean("classes_autoRefresh", true));
        // Update the tree now in case we're connecting to a running VM
        // (in which case there might not be class prepare events).
        SwingUtilities.invokeLater(this);
    }

    /**
     * Merge the nodes in the existing tree with the new class list.
     *
     * @param  model    existing tree model to be updated.
     * @param  classes  list of all loaded classes.
     */
    protected void consolidateNodes(ClassTreeModel model, List classes) {
        BasicNode treeRoot = (BasicNode) model.getRoot();

        // Use the fact that everything is sorted to merge in one pass.
        int indexOld = 0;
        int sizeOld = currentClassesList.size();
        int indexNew = 0;
        int sizeNew = classes == null ? 0 : classes.size();
        while (indexOld < sizeOld && indexNew < sizeNew) {
            String oldClass = (String) currentClassesList.get(indexOld);
            ReferenceType clazz = (ReferenceType) classes.get(indexNew);
            String newClass = clazz.name();

            int result = oldClass.compareTo(newClass);
            if (result < 0) {
                // Old class no longer exists.
                BasicNode oldNode = findNode(treeRoot, oldClass);
                if (oldNode != null && oldNode != treeRoot) {
                    BasicNode parent = (BasicNode) oldNode.getParent();
                    parent.remove(oldNode);
                }
                indexOld++;

            } else if (result > 0) {
                // New class to be inserted into tree.
                model.addPath(clazz);
                indexNew++;

            } else {
                // Matching class names.
                indexOld++;
                indexNew++;
            }
        }

        // Remove left over old classes.
        while (indexOld < sizeOld) {
            String oldClass = (String) currentClassesList.get(indexOld);
            BasicNode oldNode = findNode(treeRoot, oldClass);
            if (oldNode != null && oldNode != treeRoot) {
                BasicNode parent = (BasicNode) oldNode.getParent();
                parent.remove(oldNode);
            }
            indexOld++;
        }

        // Add left over new classes.
        while (indexNew < sizeNew) {
            ReferenceType clazz = (ReferenceType) classes.get(indexNew);
            model.addPath(clazz);
            indexNew++;
        }
    }

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
        prepareRequest = null;
        unloadRequest = null;
        enableAutoRefresh(false);
        // The only safe way to make sure the tree is wiped out, and not
        // rebuilt if this method is called during a refresh.
        uitree.setModel(new ClassTreeModel(new BasicNode()));
    }

    /**
     * Enable or disable the automatic refresh feature.
     *
     * @param  auto  true to automatically refresh; false otherwise.
     */
    protected void enableAutoRefresh(boolean auto) {
        if (auto && !autoRefreshing) {
            VirtualMachine vm = owningSession.getVM();
            if (vm == null) {
                // Wait until we are activated.
                return;
            }
            EventRequestManager erm = vm.eventRequestManager();
            prepareRequest = erm.createClassPrepareRequest();
            prepareRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            prepareRequest.enable();
            unloadRequest = erm.createClassUnloadRequest();
            unloadRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            unloadRequest.enable();
            VMEventManager vmeman = (VMEventManager)
                owningSession.getManager(VMEventManager.class);
            vmeman.addListener(ClassPrepareEvent.class, this,
                               VMEventListener.PRIORITY_DEFAULT);
            vmeman.addListener(ClassUnloadEvent.class, this,
                               VMEventListener.PRIORITY_DEFAULT);

        } else if (!auto && autoRefreshing) {
            VMEventManager vmeman = (VMEventManager)
                owningSession.getManager(VMEventManager.class);
            vmeman.removeListener(ClassPrepareEvent.class, this);
            vmeman.removeListener(ClassUnloadEvent.class, this);
        }
        autoRefreshing = auto;
    }

    /**
     * Invoked when a VM event has occurred.
     *
     * @param  e  VM event
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e) {
        EventRequest er = e.request();
        if (er.equals(prepareRequest) || er.equals(unloadRequest)
            && autoRefreshing) {
            refreshLater();
        }
        return true;
    }

    /**
     * Finds the tree node matching the given class name. The name must
     * be fully-qualified.
     *
     * @param  root  root of tree.
     * @param  path  name of class.
     * @return  matching node in tree; root if path is empty; null if
     *          path was not found.
     */
    protected static BasicNode findNode(BasicNode root, String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, ".");
        BasicNode target = root;
        while (target != null && tokenizer.hasMoreTokens()) {
            target = target.getChild(tokenizer.nextToken());
        }
        return target;
    }

    /**
     * Converts the TreePath into a String object using the user object
     * names of the path nodes. This is used to convert a tree path to
     * a fully-qualified class name. If the path specifies only a
     * package, null is returned. If the path refers to a method node,
     * then that method's class is used. Otherwise, the node is a
     * class and it's name is returned.
     *
     * @param  tpath  TreePath
     * @return  Name of user objects in tree path, separated by periods,
     *          or null if path does not denote a leaf node.
     */
    protected static String getPathName(TreePath tpath) {
        if (tpath.getPathCount() <= 1) {
            // Nothing to do if only the root is selected.
            return null;
        }

        BasicNode node = (BasicNode) tpath.getLastPathComponent();
        if (node instanceof PackageNode) {
            return null;
        } else if (node instanceof MethodNode) {
            // Ignore method nodes and get their parent instead.
            tpath = tpath.getParentPath();
        }

        // Reconstruct the fully-qualified name of the selected node.
        node = (BasicNode) tpath.getPathComponent(1);
        StringBuffer pathname = new StringBuffer(node.getName());
        for (int ii = 2; ii < tpath.getPathCount(); ii++) {
            pathname.append('.');
            node = (BasicNode) tpath.getPathComponent(ii);
            pathname.append(node.getName());
        }
        String name = pathname.toString();
        int cli = name.indexOf(" (");
        if (cli > -1) {
            // Remove the class loader identifier.
            name = name.substring(0, cli);
        }
        return name;
    }

    /**
     * Returns a reference to the peer UI component. In many cases this
     * is a JList, JTree, or JTable, depending on the type of data being
     * displayed in the panel.
     *
     * @return  peer ui component object
     */
    public JComponent getPeer() {
        return uitree;
    }

    /**
     * Returns a reference to the UI component.
     *
     * @return  ui component object
     */
    public JComponent getUI() {
        return uicomp;
    }

    /**
     * Returns the last component of the fully-qualified class name.
     *
     * @param  cname  fully-qualified class name.
     * @return  last part of class name.
     */
    protected static String justTheName(String cname) {
        return cname.substring(cname.lastIndexOf('.') + 1);
    }

    /**
     * Called whenever a node in the tree has been double-clicked on.
     *
     * @param  tpath  TreePath.
     */
    protected void pathSelected(TreePath tpath) {
        BasicNode node = (BasicNode) tpath.getLastPathComponent();
        Location location = null;
        String pathname = null;
        if (node instanceof MethodNode) {
            MethodNode mnode = (MethodNode) node;
            location = mnode.getLocation();
        } else {
            pathname = getPathName(tpath);
            if (pathname == null) {
                return;
            }
        }

        PathManager pathman = (PathManager)
            owningSession.getManager(PathManager.class);
        SourceSource source = null;
        try {
            if (location == null) {
                source = pathman.mapSource(pathname);
            } else {
                source = pathman.mapSource(location);
            }
        } catch (IOException ioe) {
            // This case is handled below.
        }
        UIAdapter adapter = owningSession.getUIAdapter();
        if (source == null || !source.exists()) {
            owningSession.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR,
                Bundle.getString("couldntMapSrcFile"));
        } else if (!adapter.showFile(source, 0, 0)) {
            String msg = MessageFormat.format(
                Bundle.getString("couldntOpenFileMsg"),
                new Object[] { source.getName() });
            owningSession.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR, msg);
        }
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and
     * released) on a component.
     *
     * @param  e  mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            TreePath selPath = uitree.getPathForLocation(e.getX(), e.getY());
            if (selPath != null) {
                e.consume();
                pathSelected(selPath);
            }
        }
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param  e  mouse event.
     */
    public void mousePressed(MouseEvent e) { }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param  e  mouse event.
     */
    public void mouseReleased(MouseEvent e) { }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param  e  mouse event.
     */
    public void mouseEntered(MouseEvent e) { }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param  e  mouse event.
     */
    public void mouseExited(MouseEvent e) { }

    /**
     * Update the display on the screen. Use the given Session to fetch
     * the desired data. This must be run on the AWT event dispatching
     * thread.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
        // Remember the expanded tree paths.
        ClassTreeModel treeModel = (ClassTreeModel) uitree.getModel();
        BasicNode rootNode = (BasicNode) treeModel.getRoot();
        TreePath rootPath = new TreePath(rootNode);
        Enumeration expaths = uitree.getExpandedDescendants(rootPath);
        List expandedPaths = null;
        if (expaths != null) {
            expandedPaths = new ArrayList();
            while (expaths.hasMoreElements()) {
                expandedPaths.add(expaths.nextElement());
            }
        }

        VirtualMachine vm = session.getVM();
        boolean reschedule = false;
        List classList = null;
        List classListNames = new ArrayList();
        if (vm != null) {

            try {
                // Get the list of classes from the VM.
                vm.suspend();
                List tempList = vm.allClasses();
                vm.resume();
                classList = new ArrayList(tempList);
                Collections.sort(classList);
                boolean hide = preferences.getBoolean(
                    "classes_hideCoreClasses", true);
                ListIterator liter = classList.listIterator();
                // Trim out the undesirable entries.
                while (liter.hasNext()) {
                    ReferenceType clazz = (ReferenceType) liter.next();
                    String name = clazz.name();
                    if (hide && (name.startsWith("java.")
                                 || name.startsWith("javax.")
                                 || name.startsWith("sun.")
                                 || name.startsWith("com.sun.")
                                 || (clazz instanceof ArrayType))) {
                        liter.remove();
                    } else {
                        // Save the desirable names for later.
                        classListNames.add(name);
                    }
                }
            } catch (NoSuchElementException nsee) {
                // This happens sometimes, so let's get the classes later.
                reschedule = true;
            } catch (ConcurrentModificationException cme) {
                // This happens sometimes, so let's get the classes later.
                reschedule = true;
            } catch (VMDisconnectedException vmde) {
                // This can certainly happen, with the asynchronous updates.
            }
        }

        if (reschedule) {
            // A problem occurred, update the panel later.
            vm.resume();
            refreshLater();
        } else {
            // Consolidate the tree model and new class list.
            consolidateNodes(treeModel, classList);

            // Update the tree display.
            treeModel.reload();

            // Expand all the previously expanded paths.
            if (expandedPaths != null) {
                for (int ii = 0; ii < expandedPaths.size(); ii++) {
                    TreePath path = (TreePath) expandedPaths.get(ii);
                    uitree.expandPath(path);
                }
            }

            currentClassesList = classListNames;
        }
    }

    /**
     * Invoked whenever a node in the tree is about to be expanded.
     *
     * @param  event  expansion event.
     * @throws  ExpandVetoException
     *          never thrown.
     */
    public void treeWillExpand(TreeExpansionEvent event)
        throws ExpandVetoException {

        TreePath path = event.getPath();
        BasicNode node = (BasicNode) path.getLastPathComponent();
        if (node instanceof ClassNode) {
            // Build out the method nodes of the class node.
            ClassNode cn = (ClassNode) node;
            if (cn.getChildCount() == 0) {
                // We assume class nodes with no children must not have
                // been built out yet.
                cn.populateMethods();
            }
        }
    }

    /**
     * Invoked whenever a node in the tree is about to be collapsed.
     *
     * @param  event  expansion event.
     * @throws  ExpandVetoException
     *          never thrown.
     */
    public void treeWillCollapse(TreeExpansionEvent event)
        throws ExpandVetoException {
        // We couldn't care less.
    }

    /**
     * Class ClassTreeModel represents a class tree.
     *
     * @author  Nathan Fiedler
     */
    protected class ClassTreeModel extends DefaultTreeModel {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a new ClassTreeModel with a root node of the given
         * name.
         *
         * @param  root  root node
         */
        public ClassTreeModel(BasicNode root) {
            super(root);
        }

        /**
         * Add the path of this class, and the class node itself, to the
         * tree. The name of the class is tokenized by periods and each
         * part makes up a node in the tree.
         *
         * @param  clazz  the class.
         */
        public void addPath(ReferenceType clazz) {
            String path = clazz.name();
            StringTokenizer tokenizer = new StringTokenizer(path, ".");
            BasicNode node = (BasicNode) getRoot();
            // Go through the path elements, ensuring that there are
            // tree nodes for each one.
            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken();
                // Find a node by this name.
                BasicNode next = node.getChild(name);
                if (next == null) {
                    // Node doesn't exist, create a new one.
                    if (tokenizer.hasMoreTokens()) {
                        next = new PackageNode(name);
                    } else {
                        next = new ClassNode(clazz);
                    }
                    int index = node.getChildCount();
                    // Insert the new node with the tree, without events.
                    node.insert(next, index == 0 ? 0 : index - 1);
                }
                node = next;
            }
        }
    }

    /**
     * Implements the basic tree node behavior.
     *
     * @author  Nathan Fiedler
     */
    protected class BasicNode extends DefaultMutableTreeNode {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a BasicNode with no name.
         */
        public BasicNode() {
            super();
        }

        /**
         * Constructs a BasicNode with the given name.
         *
         * @param  name  name for this node.
         */
        public BasicNode(String name) {
            super(name);
        }

        /**
         * Get the child of this node that has the given name.
         *
         * @param  name  get child with this name.
         * @return  child with given name; null if none.
         */
        public BasicNode getChild(String name) {
            for (int ii = getChildCount() - 1; ii >= 0; ii--) {
                BasicNode node = (BasicNode) getChildAt(ii);
                if (node.getName().equals(name)) {
                    return node;
                }
            }
            // didn't find a matching child
            return null;
        }

        /**
         * Return the name of this node (the user object, assumed to be
         * a String).
         *
         * @return  name of this node.
         */
        public String getName() {
            return (String) getUserObject();
        }
    }

    /**
     * Represents a package node, which contains other package and/or
     * class nodes.
     *
     * @author  Nathan Fiedler
     */
    protected class PackageNode extends BasicNode {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a PackageNode with the given name.
         *
         * @param  name  name for this node.
         */
        public PackageNode(String name) {
            super(name);
        }
    }

    /**
     * Node in our class tree.
     *
     * @author  Nathan Fiedler
     */
    protected class ClassNode extends BasicNode {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** The class this node reprsents. */
        private ReferenceType classRef;
        /** Class loader of the class. */
        private ClassLoaderReference classLoader;
        /** Our string representation of this node. */
        private String ourString;

        /**
         * Constructs a ClassNode to represent the given class.
         *
         * @param  clazz  the class.
         */
        public ClassNode(ReferenceType clazz) {
            super(justTheName(clazz.name()));
            classRef = clazz;
            classLoader = clazz.classLoader();
            if (classLoader != null) {
                ourString = getName() + " (" + classLoader.uniqueID() + ')';
            } else {
                ourString = getName();
            }
        }

        /**
         * Returns this path's class loader reference, if any.
         *
         * @return  class loader reference.
         */
        public ClassLoaderReference getClassLoader() {
            return classLoader;
        }

        /**
         * Returns the fully qualified name of the class represented by
         * this node.
         *
         * @return  fully qualified class name.
         */
        public String getFQClassName() {
            return classRef.name();
        }

        /**
         * Returns true if this node is a leaf, false otherwise.
         *
         * @return  true if array type, false otherwise.
         */
        public boolean isLeaf() {
            if (classRef instanceof ArrayType) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Build out the method nodes for this class.
         */
        public void populateMethods() {
            List methods = classRef.methods();
            Iterator iter = methods.iterator();
            List methodChildren = new ArrayList();
            while (iter.hasNext()) {
                Method method = (Method) iter.next();
                if (method.isStaticInitializer()) {
                    // Skip static initializers, they're not methods.
                    continue;
                }
                methodChildren.add(new MethodNode(method));
            }

            // Sort the methods and add them to the class node.
            Collections.sort(methodChildren);
            iter = methodChildren.iterator();
            while (iter.hasNext()) {
                add((MethodNode) iter.next());
            }
        }

        /**
         * Returns the string representation of this.
         *
         * @return  string representation of this.
         */
        public String toString() {
            return ourString;
        }
    }

    /**
     * Represents a method node.
     *
     * @author  Nathan Fiedler
     */
    protected class MethodNode extends BasicNode implements Comparable {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Location of the method. */
        private Location location;

        /**
         * Constructs a MethodNode from the given Method instance.
         *
         * @param  method  method to be displayed.
         */
        public MethodNode(Method method) {
            super();
            StringBuffer buffer = new StringBuffer(128);
            // Get the method name.
            if (method.isConstructor()) {
                ReferenceType clazz = method.declaringType();
                String name = justTheName(clazz.name());
                buffer.append(name);
            } else {
                buffer.append(method.name());
            }
            buffer.append('(');

            // Get the method arguments.
            List args = method.argumentTypeNames();
            for (int ii = 0; ii < args.size(); ii++) {
                String arg = (String) args.get(ii);
                buffer.append(justTheName(arg));
                if (ii < (args.size() - 1)) {
                    buffer.append(", ");
                }
            }
            buffer.append(')');
            setUserObject(buffer.toString());

            // Get the method line number.
            location = method.location();
        }

        /**
         * Compares this object with the specified object for order.
         * Returns a negative integer, zero, or a positive integer as
         * this object is less than, equal to, or greater than the
         * specified object.
         *
         * @param  o  the Object to be compared.
         * @return  a negative integer, zero, or a positive integer as
         *          this object is less than, equal to, or greater than
         *          the specified object.
         */
        public int compareTo(Object o) {
            MethodNode m = (MethodNode) o;
            return getName().compareTo(m.getName());
        }

        /**
         * Returns the associated location of this method.
         *
         * @return  location of method.
         */
        public Location getLocation() {
            return location;
        }
    }

    /**
     * Custom JTree to show custom tooltip, and set the empty selection
     * model.
     *
     * @author  Nathan Fiedler
     */
    protected class ClassTree extends JTree {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Default tooltip text. */
        private String defaultTip;

        /**
         * Constructs a new ClassTree with the specified tree model.
         *
         * @param  model  TreeModel to use.
         */
        public ClassTree(TreeModel model) {
            super(model);
            defaultTip = Bundle.getString("Class.tooltip");
            setSelectionModel(null);

            // Add the popup menu.
            ClassesPopup popup = new ClassesPopup();
            addMouseListener(popup);
        }

        /**
         * Returns custom tooltip text for this tree.
         *
         * @param  e  Mouse event.
         * @return  Custom tooltip text.
         */
        public String getToolTipText(MouseEvent e) {
            TreePath selPath = getPathForLocation(e.getX(), e.getY());
            String tip = defaultTip;
            if (selPath != null) {
                // Show the class name and loader information.
                BasicNode node = (BasicNode) selPath.getLastPathComponent();
                if (node instanceof ClassNode) {
                    Object clr = ((ClassNode) node).getClassLoader();
                    String name = ClassPanel.getPathName(selPath);
                    tip = "<html><small>" + name
                        + "<br>class loader: " + clr + "</small></html>";
                }
            }
            return tip;
        }
    }

    /**
     * Class ClassesPopup is a popup menu that allows the user to clear
     * all of the entries from the watchpoints list.
     */
    protected class ClassesPopup extends SmartPopupMenu
        implements ActionListener, ItemListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** The hide menu item. */
        private JCheckBoxMenuItem hideMenuItem;
        /** The auto-refresh menu item. */
        private JCheckBoxMenuItem autoRefreshMenuItem;
        /** Menu item to hotswap a class. */
        private JMenuItem hotswapItem;
        /** Name of the class that was selected by the user. */
        private String selectedClass;

        /**
         * Constructs an ClassesPopup that interacts with the panel.
         */
        ClassesPopup() {
            super(Bundle.getString("Class.menu.label"));
            hideMenuItem = new JCheckBoxMenuItem(
                Bundle.getString("Class.menu.hideLabel"),
                preferences.getBoolean("classes_hideCoreClasses", true));
            hideMenuItem.addItemListener(this);
            add(hideMenuItem);
            autoRefreshMenuItem = new JCheckBoxMenuItem(
                Bundle.getString("Class.menu.autoRefreshLabel"),
                preferences.getBoolean("classes_autoRefresh", true));
            autoRefreshMenuItem.addItemListener(this);
            add(autoRefreshMenuItem);

            hotswapItem = new JMenuItem("N/A");
            hotswapItem.addActionListener(this);
        }

        /**
         * Invoked when a menu item has been selected.
         *
         * @param  e  action event.
         */
        public void actionPerformed(ActionEvent e) {
            Object evtsrc = e.getSource();
            if (evtsrc == hotswapItem) {
                // Hotswap the selected class.

                // Find the ReferenceType for this class.
                VirtualMachine vm = owningSession.getConnection().getVM();
                List classes = vm.classesByName(selectedClass);
                // We assume the list will be non-empty since the class
                // must clearly be loaded in order to get here.
                ReferenceType clazz = (ReferenceType) classes.get(0);

                // Try to find the .class file.
                PathManager pathman = (PathManager)
                    owningSession.getManager(PathManager.class);
                SourceSource src = pathman.mapClass(clazz);
                if (src == null) {
                    owningSession.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_ERROR,
                        Bundle.getString("Class.classFileNotFound"));
                    return;
                }
                InputStream is = src.getInputStream();

                // Do the actual hotswap operation.
                String errorMsg = null;
                try {
                    Classes.hotswap(clazz, is, vm);
                } catch (UnsupportedOperationException uoe) {
                    if (!vm.canRedefineClasses()) {
                        errorMsg = Bundle.getString("Class.hotswap.noHotSwap");
                    } else if (!vm.canAddMethod()) {
                        errorMsg = Bundle.getString(
                            "Class.hotswap.noAddMethod");
                    } else if (!vm.canUnrestrictedlyRedefineClasses()) {
                        errorMsg = Bundle.getString(
                            "Class.hotswap.noUnrestricted");
                    } else {
                        errorMsg = Bundle.getString(
                            "Class.hotswap.unsupported");
                    }
                } catch (IOException ioe) {
                    errorMsg = Bundle.getString(
                        "Class.hotswap.errorReadingFile") + ' ' + ioe;
                } catch (NoClassDefFoundError ncdfe) {
                    errorMsg = Bundle.getString("Class.hotswap.wrongClass");
                } catch (VerifyError ve) {
                    errorMsg = Bundle.getString("Class.hotswap.verifyError")
                        + ' ' + ve;
                } catch (UnsupportedClassVersionError ucve) {
                    errorMsg = Bundle.getString("Class.hotswap.versionError")
                        + ' ' + ucve;
                } catch (ClassFormatError cfe) {
                    errorMsg = Bundle.getString("Class.hotswap.formatError")
                        + ' ' + cfe;
                } catch (ClassCircularityError cce) {
                    errorMsg = Bundle.getString("Class.hotswap.circularity")
                        + ' ' + cce;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ioe) { }
                    }
                }

                if (errorMsg != null) {
                    owningSession.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_ERROR, errorMsg);
                } else {
                    owningSession.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_NOTICE,
                        Bundle.getString("Class.hotswap.success"));
                }
            }
        }

        /**
         * Invoked when an item has been selected or deselected by the user.
         *
         * @param  e  Indicates which item was selected.
         */
        public void itemStateChanged(ItemEvent e) {
            Object src = e.getSource();
            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            if (src == hideMenuItem) {
                // Show or hide the core classes.
                preferences.putBoolean("classes_hideCoreClasses", selected);
                SwingUtilities.invokeLater(ClassPanel.this);

            } else if (src == autoRefreshMenuItem) {
                // Enable or disable auto refresh mode.
                preferences.putBoolean("classes_autoRefresh", selected);
                enableAutoRefresh(selected);
            }
        }

        /**
         * Set the popup menu items enabled or disabled depending on
         * which line of the source view area the mouse button has
         * been pressed.
         *
         * @param  e  mouse event.
         */
        protected void setMenuItemsForEvent(MouseEvent e) {
            // Reset the popup menu by removing all children.
            removeAll();

            // Add the checkbox menu items first.
            add(hideMenuItem);
            add(autoRefreshMenuItem);

            // Use mouse position to determine the class.
            TreePath path = uitree.getClosestPathForLocation(
                e.getX(), e.getY());
            ClassNode cnode = null;
            if (path != null) {
                BasicNode node = (BasicNode) path.getLastPathComponent();
                if (node instanceof ClassNode) {
                    cnode = (ClassNode) node;
                } else if (node instanceof MethodNode) {
                    ClassNode cn = (ClassNode)
                        path.getParentPath().getLastPathComponent();
                    cnode = cn;
                }
            }

            // Add the actionable menu items next.
            if (cnode != null) {
                selectedClass = cnode.getFQClassName();
                hotswapItem.setText(
                    Bundle.getString("Class.menu.hotswap") + ' '
                        + cnode.getName());
                add(hotswapItem);
            } else {
                selectedClass = null;
            }
        }

        /**
         * Show the popup menu.
         *
         * @param  e  mouse event.
         */
        protected void showPopup(MouseEvent e) {
            setMenuItemsForEvent(e);
            show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
