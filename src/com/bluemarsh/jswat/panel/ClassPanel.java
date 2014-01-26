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
 * $Id: ClassPanel.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Class ClassPanel is responsible for displaying a class tree
 * in a navigable tree component.
 *
 * @author  Nathan Fiedler
 */
public class ClassPanel extends JSwatPanel implements Runnable, VMEventListener {
    /** Tree that displays classes. */
    protected ClassTree tree;
    /** Our UI component - scrollable pane. */
    protected JScrollPane uicomp;
    /** Default root node name. */
    protected String defaultRootName;
    /** Class tree data model. Holds all the elements of the tree. */
    protected ClassTreeModel treeModel;
    /** Session that owns us. */
    protected Session session;
    /** Class prepare request we created. */
    protected ClassPrepareRequest prepareRequest;
    /** Class unload request we created. */
    protected ClassUnloadRequest unloadRequest;
    /** True if panel is waiting to be called by AWT event thread
     * in order to update the panel. */
    protected volatile boolean awaitingUpdate;

    /**
     * Constructs a new ClassPanel with the default tree.
     */
    public ClassPanel() {
        defaultRootName = Bundle.getString("Class.empty");
        tree = new ClassTree(new ClassTreeModel(
            new ClassTreeNode(defaultRootName)));
        ToolTipManager.sharedInstance().registerComponent(tree);
        uicomp = new JScrollPane(tree);

        // Set up a mouse listener to open the source file of the
        // corresponding class in the tree, when double-clicked on.
        MouseListener ml = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        TreePath selPath = tree.getPathForLocation(e.getX(),
                                                                   e.getY());
                        if (selPath != null) {
                            pathSelected(selPath);
                        }
                    }
                }
            };
        tree.addMouseListener(ml);
    } // ClassPanel

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        VirtualMachine vm = session.getVM();
        // Listen to all class prepare and unload events.
        EventRequestManager erm = vm.eventRequestManager();
        prepareRequest = erm.createClassPrepareRequest();
        prepareRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        prepareRequest.enable();
        unloadRequest = erm.createClassUnloadRequest();
        unloadRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        unloadRequest.enable();
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.addListener(ClassPrepareEvent.class, this,
                           VMEventListener.PRIORITY_DEFAULT);
        vmeman.addListener(ClassUnloadEvent.class, this,
                           VMEventListener.PRIORITY_DEFAULT);
        // Call this now in case we're connecting to a running VM
        // (in which case there might not be class prepare events).
        buildTree(vm);
    } // activate

    /**
     * Builds out the class tree.
     *
     * @param  vm  Virtual machine, or null if deactivating.
     */
    protected void buildTree(VirtualMachine vm) {
        // Start with a clean tree model.
        treeModel = new ClassTreeModel(new ClassTreeNode(defaultRootName));
        if (vm == null) {
            // Nothing to do if no VM.
            tree.setModel(treeModel);
            return;
        }

        // Get list of classes from VM.
        boolean reschedule = false;
        try {
            vm.suspend();
            List classList = vm.allClasses();
            vm.resume();
            Iterator iter = classList.iterator();
            // For each class, add it to the tree.
            while (iter.hasNext()) {
                ReferenceType clazz = (ReferenceType) iter.next();
                // Add the class name to the tree. We count on the
                // fact that the class name is fully-qualified and
                // elements are delimited by periods.
                String name = clazz.name();
                ClassLoaderReference cl = clazz.classLoader();
                if (cl != null) {
                    // Add the class loader identifier.
                    name += " (" + cl.uniqueID() + ")";
                }
                treeModel.addPath(name, cl);
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

        if (reschedule) {
            // A problem occurred, update the panel later.
            vm.resume();
            awaitingUpdate = true;
            SwingUtilities.invokeLater(this);
        } else {
            tree.setModel(treeModel);
        }
    } // buildTree


    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Panels are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
        prepareRequest = null;
        unloadRequest = null;
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.removeListener(ClassPrepareEvent.class, this);
        vmeman.removeListener(ClassUnloadEvent.class, this);
        buildTree(null);
    } // deactivate

    /**
     * Invoked when a VM event has occurred.
     *
     * @param  e  VM event
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e) {
        EventRequest er = e.request();
        if (!er.equals(prepareRequest) && !er.equals(unloadRequest)) {
            // We didn't ask for this event.
            return true;
        }

        // We are only registered for ClassPrepareEvents and
        // ClassUnloadEvents, and we don't need to check which it is.
        if (!awaitingUpdate) {
            // Let ourselves know that we need an update at some point.
            awaitingUpdate = true;
            SwingUtilities.invokeLater(this);
        }
        return true;
    } // eventOccurred

    /**
     * Converts the TreePath into a String object using the user
     * object names of the path nodes. Only works on leaf nodes.
     *
     * @param  tpath  TreePath
     * @return  Name of user objects in tree path, separated by periods,
     *          or null if path does not denote a leaf node.
     */
    public static String getPathName(TreePath tpath) {
        Object[] path = tpath.getPath();
        if (path.length <= 1) {
            // Nothing to do if only the root is selected.
            return null;
        }

        ClassTreeNode node = (ClassTreeNode) tpath.getLastPathComponent();
        if (!node.isLeaf()) {
            // Last node in the selection path is not a leaf.
            // That means the selection is a package, not a class.
            return null;
        }

        // Reconstruct the claspath of the selected element.
        node = (ClassTreeNode) path[1];
        StringBuffer pathname = new StringBuffer(node.getName());
        for (int i = 2; i < path.length; i++) {
            pathname.append(".");
            node = (ClassTreeNode) path[i];
            pathname.append(node.getName());
        }
        String name = pathname.toString();
        int cli = name.indexOf(" (");
        if (cli > -1) {
            // Remove the class loader identifier.
            name = name.substring(0, cli);
        }
        return name;
    } // getPathName

    /**
     * Returns a reference to the peer UI component. In many
     * cases this is a JList, JTree, or JTable, depending on
     * the type of data being displayed in the panel.
     *
     * @return  peer ui component object
     */
    public JComponent getPeer() {
        return tree;
    } // getPeer

    /**
     * Returns a reference to the UI component.
     *
     * @return  ui component object
     */
    public JComponent getUI() {
        return uicomp;
    } // getUI

    /**
     * Called when the Session is ready to initialize this panel,
     * generally just after the panel has been added to the Session.
     *
     * @param  session  Session initializing this panel.
     */
    public void init(Session session) {
        this.session = session;
    } // init

    /**
     * Called whenever a node in the tree has been double-clicked on.
     *
     * @param  tpath  TreePath.
     */
    protected void pathSelected(TreePath tpath) {
        String pathname = getPathName(tpath);
        if (pathname == null) {
            return;
        }

        // Map the class path to a source file, and try to view it.
        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        SourceSource source = null;
        try {
            source = pathman.mapSource(pathname);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        UIAdapter adapter = session.getUIAdapter();
        if (source == null) {
            session.getStatusLog().writeln
                (swat.getResourceString("couldntMapSrcFile"));
        } else if (!adapter.showFile(source, 0, 0)) {
            session.getStatusLog().writeln
                (swat.getResourceString("couldntOpenFileMsg"));
        }
    } // pathSelected

    /**
     * Builds the class tree from scratch.
     */
    public void run() {
        // Set this false first so we limit the number of events
        // that we may miss during the processing.
        awaitingUpdate = false;
        buildTree(session.getVM());
    } // run

    /**
     * Class ClassTreeModel represents a class tree.
     *
     * @author  Nathan Fiedler
     */
    class ClassTreeModel extends DefaultTreeModel {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a new ClassTreeModel with a root node of the
         * given name.
         *
         * @param  root  root node
         */
        public ClassTreeModel(ClassTreeNode root) {
            super(root);
        } // ClassTreeModel

        /**
         * Add the given path (delimited by periods (.)) to the tree
         * in the form of tree nodes.
         *
         * @param  path  period delimited path.
         * @param  cl    class loader.
         */
        public void addPath(String path, ClassLoaderReference cl) {
            StringTokenizer tokenizer = new StringTokenizer(path, ".");
            ClassTreeNode node, next;
            node = (ClassTreeNode) getRoot();
            // Go through the path elements, ensuring that there are
            // tree nodes for each one.
            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken();
                // Find a node by this name.
                next = node.getChild(name);
                if (next == null) {
                    // Node doesn't exist, create a new one.
                    next = new ClassTreeNode(name, cl);
                    int index = node.getChildCount();
                    // Insert the new node with the tree, without events.
                    node.insert(next, index == 0 ? 0 : index - 1);
                }
                node = next;
            }
        } // addPath
    } // ClassTreeModel

    /**
     * Node in our class tree.
     *
     * @author  Nathan Fiedler
     */
    class ClassTreeNode extends DefaultMutableTreeNode {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Class loader of the class. */
        ClassLoaderReference classLoader;

        /**
         * Constructs a new ClassTreeNode with the given name.
         *
         * @param  name  name of new node.
         */
        public ClassTreeNode(String name) {
            super(name);
        } // ClassTreeNode

        /**
         * Constructs a new ClassTreeNode with the given name.
         *
         * @param  name  name of new node.
         * @param  cl    class loader.
         */
        public ClassTreeNode(String name, ClassLoaderReference cl) {
            this(name);
            classLoader = cl;
        } // ClassTreeNode

        /**
         * Returns this path's class loader reference, if any.
         *
         * @return  class loader reference.
         */
        public ClassLoaderReference getClassLoader() {
            return classLoader;
        } // getClassLoader

        /**
         * Return the name of this node.
         *
         * @return  name of node
         */
        public String getName() {
            return (String) getUserObject();
        } // getName

        /**
         * Get the child of this node that has the given name.
         *
         * @param  name  get child with this name
         * @return  child with given name; null if none
         */
        public ClassTreeNode getChild(String name) {
            int size = getChildCount();
            for (int i = 0; i < size; i++) {
                ClassTreeNode node = (ClassTreeNode) getChildAt(i);
                if (node.getName().equals(name)) {
                    return node;
                }
            }
            // didn't find a matching child
            return null;
        } // getChild
    } // ClassTreeNode

    /**
     * Custom JTree to show custom tooltip.
     *
     * @author  Nathan Fiedler
     */
    class ClassTree extends JTree {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Default tooltip text. */
        protected String defaultTip;

        /**
         * Constructs a new ClassTree with the specified tree model.
         *
         * @param  model  TreeModel to use.
         */
        public ClassTree(TreeModel model) {
            super(model);
            defaultTip = Bundle.getString("Class.tooltip");
        } // ClassTree

        /**
         * Returns custom tooltip text for this tree.
         *
         * @param  e  Mouse event.
         * @return  Custom tooltip text.
         */
        public String getToolTipText(MouseEvent e) {
            TreePath selPath = getPathForLocation(e.getX(), e.getY());
            String name = null;
            if (selPath != null) {
                name = ClassPanel.getPathName(selPath);
            }
            if (name == null) {
                return defaultTip;
            } else {
                // Show the class loader information.
                ClassTreeNode node = (ClassTreeNode)
                    selPath.getLastPathComponent();
                Object clr = node.getClassLoader();
                if (clr != null) {
                    name = "<html><font size=\"-1\">" + name +
                        "<br>class loader: " + clr.toString() +
                        "</font></html>";
                }
                return name;
            }
        } // getToolTipText
    } // ClassTree
} // ClassPanel
