/*********************************************************************
 *
 *      Copyright (C) 2000-2005 David Lum
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
 * $Id: LocalsPanel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.event.ContextChangeEvent;
import com.bluemarsh.jswat.event.ContextListener;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.ui.SmartPopupMenu;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.VariableValue;
import com.bluemarsh.jswat.util.Variables;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InconsistentDebugInfoException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VMDisconnectedException;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Class LocalsPanel is responsible for displaying a tree of local
 * variables in the current thread.
 *
 * @author  David Lum
 * @author  Nathan Fiedler
 */
public class LocalsPanel extends AbstractPanel
    implements ContextListener, MouseListener {
    /** JTree containing local variables. */
    private LocalsTree localsTree;
    /** The tree model. Saved here so we don't have to rebuild it from
     * scratch when the stepping action occurs (VM is resumed and then
     * setMessage() clears the model, which means the next time around the
     * variables are all new and thus bold). */
    private DefaultTreeModel treeModel;
    /** Our UI component - scrollable panel */
    private JScrollPane uicomp;
    /** Our preferences node. */
    private Preferences preferences;
    /** List of expanded nodes from before the model was replaced with an
     * error or status message. Used to restore the expanded state of the
     * nodes once the variable tree is redisplayed. */
    private List expandedPaths;

    /**
     * Creates a LocalsPanel with the default tree.
     */
    public LocalsPanel() {
        preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/panel");

        treeModel = new DefaultTreeModel(new LocalsTreeNode());
        localsTree = new LocalsTree(treeModel);
        localsTree.setRootVisible(false);
        localsTree.setShowsRootHandles(true);
        localsTree.setCellRenderer(new LocalsRenderer());
        ToolTipManager.sharedInstance().registerComponent(localsTree);

        // Set up a mouse listener to react to the user double-clicking
        // on the "..." node within an array node.
        localsTree.addMouseListener(this);

        uicomp = new JScrollPane(localsTree);
    } // LocalsPanel

    /**
     * Called when the Session has activated. This occurs when the debuggee
     * has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        // Add ourselves as a context change listener.
        ContextManager ctxtMgr = (ContextManager)
            sevt.getSession().getManager(ContextManager.class);
        ctxtMgr.addContextListener(this);
        refreshLater();
    } // activated

    /**
     * Merge and consolidate the nodes in the existing tree with the new
     * variable information objects in the map.
     *
     * @param  rootNode  root node of the existing tree.
     * @param  allVars   map of new variable information objects.
     */
    protected void consolidateNodes(LocalsTreeNode rootNode, Map allVars) {
        // Convert the map entries into a sorted list of names.
        List sortedKeys = new ArrayList(allVars.keySet());
        Collections.sort(sortedKeys);

        // Now use the fact that the current root node child list and
        // the new variables list are sorted to merge them together in
        // a single pass.
        int childIndex = 0;
        int varsCount = allVars.size();
        int varsIndex = 0;
        // Manually walk both lists to add the new variables, remove
        // the out-of-scope, and update the remaining ones.
        while ((childIndex < rootNode.getChildCount())
               && (varsIndex < varsCount)) {
            Variable newVar = (Variable) allVars.get(
                sortedKeys.get(varsIndex));
            Variable oldVar = (Variable) rootNode.getChildAt(childIndex);
            // Assume variable is unchanged for now.
            oldVar.markChanged(false);

            int result = oldVar.compareTo(newVar);
            if (result < 0) {
                // If the current root node child is less than the
                // current new var, remove it from the root node.
                rootNode.remove(childIndex);

            } else if (result > 0) {
                // If the current root node child is greater than the
                // current new var, insert the new var before the child.
                rootNode.insert(newVar, childIndex);
                // This is a new variable, hence is is changed.
                newVar.markChanged(true);
                childIndex++;
                varsIndex++;

            } else {
                // Variables match, determine how to update.
                rootNode.updateChild(oldVar, newVar, childIndex);
                childIndex++;
                varsIndex++;
            }
        }

        // Remove left over old variables.
        while (childIndex < rootNode.getChildCount()) {
            rootNode.remove(childIndex);
        }

        // Add left over new variables.
        while (varsIndex < varsCount) {
            Variable newVar = (Variable) allVars.get(
                sortedKeys.get(varsIndex));
            rootNode.insert(newVar, rootNode.getChildCount());
            // This is a new variable, hence is is changed.
            newVar.markChanged(true);
            varsIndex++;
        }
    } // consolidateNodes

    /**
     * Invoked when the current context has changed. The context change
     * event identifies which aspect of the context has changed.
     *
     * @param  cce  context change event
     */
    public void contextChanged(ContextChangeEvent cce) {
        if (!cce.isBrief()) {
            // Not a brief event, refresh the display.
            refreshLater();
        }
    } // contextChanged

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
        // Remove ourselves as a context change listener.
        ContextManager ctxtMgr = (ContextManager)
            sevt.getSession().getManager(ContextManager.class);
        ctxtMgr.removeContextListener(this);
        setMessage(null);
    } // deactivated

    /**
     * Converts the TreePath into a String object using the user object
     * names of the path nodes. Only works on leaf nodes.
     *
     * @param  tpath  TreePath
     * @return  Name of user objects in tree path, separated by periods,
     *          or null if path points to root node.
     */
    public static String getPathName(TreePath tpath) {
        Object[] path = tpath.getPath();
        if (path.length <= 1) {
            // Nothing to do if it is only the root.
            return null;
        }

        // Construct the path of the selected element.
        LocalsTreeNode node = (LocalsTreeNode) tpath.getLastPathComponent();
        node = (LocalsTreeNode) path[1];
        StringBuffer pathname = new StringBuffer(node.getName());
        for (int i = 2; i < path.length; i++) {
            if (!(node instanceof ArrayVariable)) {
                pathname.append('.');
            }
            node = (LocalsTreeNode) path[i];
            pathname.append(node.getName());
        }
        return pathname.toString();
    } // getPathName

    /**
     * Returns a reference to the peer UI component. In many cases this is
     * a JList, JTree, or JTable, depending on the type of data being
     * displayed in the panel.
     *
     * @return  peer ui component object
     */
    public JComponent getPeer() {
        return localsTree;
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
     * Invoked when the mouse button has been clicked (pressed and
     * released) on a component.
     *
     * @param  e  mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            TreePath selPath =
                localsTree.getPathForLocation(e.getX(), e.getY());
            if (selPath != null) {
                // See if the tree node is a LocalsTreeNode
                // with a user object of '...'.
                Object last = selPath.getLastPathComponent();
                if (last instanceof LocalsTreeNode) {
                    LocalsTreeNode node = (LocalsTreeNode) last;
                    String str = (String) node.getUserObject();
                    if ((str != null) && str.equals("...")) {
                        // If so, get the parent array node and
                        // tell it to show all of the elements.
                        ArrayVariable arr = (ArrayVariable) node.getParent();
                        arr.showAll();
                        treeModel.reload(arr);
                    }
                }
            }
        }
    } // mouseClicked

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
     * Read the visible field variables from the given stack frame,
     * creating Variable objects and adding them to the given map.
     *
     * @param  frame    stack frame to get variables from.
     * @param  allVars  map to add variable info to.
     */
    protected void readFieldVars(StackFrame frame, Map allVars) {
        // (split from refresh() to reduce method size)
        ReferenceType clazz = frame.location().declaringType();
        ListIterator iter = clazz.visibleFields().listIterator();

        boolean hideThis = preferences.getBoolean(
            "locals.hideThis", Defaults.LOCALS_HIDE_THIS);
        boolean showFinals = preferences.getBoolean(
            "locals.showFinals", Defaults.LOCALS_SHOW_FINALS);
        ObjectReference thisObj = frame.thisObject();
        if (!hideThis || thisObj == null) {
            // Show the static fields if not hiding or if we are
            // in a static method.
            while (iter.hasNext()) {
                Field field = (Field) iter.next();
                if (field.isStatic()) {
                    // Skip over constants, which are boring.
                    if (!field.isFinal() || showFinals) {
                        // Get static values from the ReferenceType.
                        allVars.put(
                            field.name(),
                            Variable.create(field, clazz.getValue(field)));
                    }
                }
            }
        }
        // Else the statics will be shown under 'this'.

        if (thisObj != null) {
            // Save a reference to 'this'.
            if (hideThis) {
                // Add the 'this' fields under a node called 'this'.
                allVars.put("this", Variable.create(
                                "this", thisObj.type().name(), thisObj));
            } else {
                // Add the 'this' fields directly to the root node.
                allVars.put("this", Variable.createThis(thisObj));
                while (iter.hasPrevious()) {
                    Field field = (Field) iter.previous();
                    if (!field.isStatic()) {
                        // Get non-static values from the ObjectReference.
                        allVars.put(field.name(),
                                    Variable.create(
                                        field, thisObj.getValue(field)));
                    }
                }
            }
        }
    } // readFieldVars

    /**
     * Update the display on the screen. Use the given Session to fetch the
     * desired data. This must be run on the AWT event dispatching thread.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
        // Get the list of visible local variables.
        ContextManager conman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = conman.getCurrentThread();
        if (thread == null) {
            if (session.isActive()) {
                setMessage("error.threadNotSet");
            } else {
                setMessage(null);
            }
            return;
        }

        // Get the stack frame.
        StackFrame frame;
        try {
            frame = thread.frame(conman.getCurrentFrame());
            if (frame == null) {
                setMessage("error.noStackFrame");
                return;
            }
        } catch (IncompatibleThreadStateException itse) {
            setMessage("error.threadRunning");
            return;
        } catch (IndexOutOfBoundsException ioobe) {
            // This happens when the thread has no frames at all.
            setMessage("error.noStackFrame");
            return;
        } catch (NativeMethodException nme) {
            setMessage("error.nativeMethod");
            return;
        } catch (VMDisconnectedException vmde) {
            setMessage(null);
            return;
        }

        // Create a sorted map in which to store all of the variable names
        // and values. This helps us deal with shadowed variables.
        Map allVars = new TreeMap();

        // Get the field variable values (both static and non-static).
        // Try-catch the whole darn thing -- never know when those nasty
        // exceptions will be thrown.
        try {
            readFieldVars(frame, allVars);

            // Get the visible local variables in this frame.
            ListIterator iter = frame.visibleVariables().listIterator();
            // Put the LocalVariable elements into big map. Any field
            // variables of the same name will be appropriately shadowed.
            while (iter.hasNext()) {
                LocalVariable var = (LocalVariable) iter.next();
                try {
                    Value val = frame.getValue(var);
                    allVars.put(var.name(), Variable.create(var, val));
                } catch (InconsistentDebugInfoException idie) {
                    // JDK BUG #4349534
                }
            }
        } catch (AbsentInformationException aie) {
            if (allVars.size() == 0) {
                // When there's nothing to show and we get this exception,
                // indicate that the variable information is missing.
                setMessage("error.noDebugInfo");
                return;
            }
        } catch (InvalidStackFrameException isfe) {
            setMessage("error.invalidFrame");
            reschedule(session);
            return;
        } catch (NativeMethodException nme) {
            setMessage("error.nativeMethod");
            return;
        } catch (VMDisconnectedException vmde) {
            setMessage(null);
            return;
        }

        // See if we have anything to say.
        if (allVars.size() == 0) {
            setMessage("Locals.noVariables");
            return;
        }

        if (localsTree.getModel() != treeModel) {
            // Restore the true tree model.
            localsTree.setModel(treeModel);
            if (expandedPaths != null) {
                // Expand the previously expanded nodes.
                for (int i = 0; i < expandedPaths.size(); i++) {
                    TreePath path = (TreePath) expandedPaths.get(i);
                    localsTree.expandPath(path);
                }
            }
        }

        // Remember the expanded tree paths.
        LocalsTreeNode rootNode = (LocalsTreeNode) treeModel.getRoot();
        TreePath rootPath = new TreePath(rootNode);
        Enumeration expaths = localsTree.getExpandedDescendants(rootPath);
        // Copy these to be safe in case the enumeration chokes when
        // the backing data source changes.
        List expandedPathsLocal = null;
        if (expaths != null) {
            expandedPathsLocal = new ArrayList();
            while (expaths.hasMoreElements()) {
                expandedPathsLocal.add(expaths.nextElement());
            }
        }

        consolidateNodes(rootNode, allVars);

        // Cause the tree to update the display.
        localsTree.setRootVisible(false);
        treeModel.reload();

        // Expand all the previously expanded paths.
        if (expandedPathsLocal != null) {
            for (int i = 0; i < expandedPathsLocal.size(); i++) {
                TreePath path = (TreePath) expandedPathsLocal.get(i);
                localsTree.expandPath(path);
            }
        }
    } // refresh

    /**
     * Call the refresh() method in a short time.
     *
     * @param  session  owning Session.
     */
    protected void reschedule(final Session session) {
        javax.swing.Timer t = new javax.swing.Timer(
            100, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // Maybe we can refresh without a problem now.
                        try {
                            // This is happening on the AWT event
                            // dispatching thread.
                            refresh(session);
                        } catch (VMDisconnectedException vmde) {
                            // This is to be expected. Do not remove this
                            // catch unless refresh() is now catching it.
                            // Otherwise the AWT event thread will die.
                        }
                    }
                });
        t.setRepeats(false);
        t.start();
    } // reschedule

    /**
     * Sets the tree to show only a message as referenced by the given
     * resource name.
     *
     * @param  name  Name of string resource to display.
     */
    protected void setMessage(String name) {
        // Remember the expanded tree paths.
        LocalsTreeNode rootNode = (LocalsTreeNode) treeModel.getRoot();
        TreePath rootPath = new TreePath(rootNode);
        Enumeration expaths = localsTree.getExpandedDescendants(rootPath);
        // Copy these to be safe in case the enumeration chokes when the
        // backing data source changes.
        expandedPaths = null;
        if (expaths != null) {
            expandedPaths = new ArrayList();
            while (expaths.hasMoreElements()) {
                expandedPaths.add(expaths.nextElement());
            }
        }

        TreeNode root;
        boolean visibleRoot;
        if (name != null) {
            root = new LocalsTreeNode(Bundle.getString(name));
            visibleRoot = true;
        } else {
            root = new LocalsTreeNode();
            visibleRoot = false;
        }
        // Displaces the normal tree model temporarily.
        localsTree.setModel(new DefaultTreeModel(root));
        // Make sure the root node (message) is visible.
        localsTree.setRootVisible(visibleRoot);
    } // setMessage

    /**
     * Custom JTree to show custom tooltip, and set the empty selection
     * model.
     */
    protected class LocalsTree extends JTree {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Default tooltip text. */
        private String defaultTip;

        /**
         * Constructs a new LocalsTree with the specified tree model.
         *
         * @param  model  TreeModel to use.
         */
        public LocalsTree(TreeModel model) {
            super(model);
            defaultTip = Bundle.getString("Locals.tooltip");
            setSelectionModel(null);

            // Add the popup menu.
            LocalsPopup popup = new LocalsPopup();
            addMouseListener(popup);
        } // LocalsTree

        /**
         * Returns custom tooltip text for this tree.
         *
         * @param  me  Mouse event.
         * @return  Custom tooltip text.
         */
        public String getToolTipText(MouseEvent me) {
            TreePath selPath = getPathForLocation(me.getX(), me.getY());
            if (selPath == null) {
                return defaultTip;
            }
            String name = LocalsPanel.getPathName(selPath);
            if (name == null) {
                // Just the root node -- maybe it's an error message.
                return selPath.getLastPathComponent().toString();
            } else {
                // Show the detailed variable information.
                ContextManager conman = (ContextManager)
                    owningSession.getManager(ContextManager.class);
                ThreadReference thread = conman.getCurrentThread();
                if (thread == null) {
                    return "error.threadNotSet";
                }
                int frame = conman.getCurrentFrame();
                VariableValue fav = null;
                try {
                    fav = Variables.getField(name, thread, frame);
                } catch (Exception e) {
                    return "Locals.error.valueError";
                }

                StringBuffer sb = new StringBuffer(64);
                sb.append("<html><small><strong>");
                sb.append(name);
                sb.append("</strong>");
                if (fav.field() != null) {
                    sb.append("<br>field");
                    if (fav.field().isPublic()) {
                        sb.append("<br>public");
                    }
                    if (fav.field().isProtected()) {
                        sb.append("<br>protected");
                    }
                    if (fav.field().isPrivate()) {
                        sb.append("<br>private");
                    }
                    if (fav.field().isStatic()) {
                        sb.append("<br>static");
                    }
                    if (fav.field().isTransient()) {
                        sb.append("<br>transient");
                    }
                    if (fav.field().isVolatile()) {
                        sb.append("<br>volatile");
                    }
                    if (fav.field().isSynthetic()) {
                        sb.append("<br>synthetic");
                    }
                    if (fav.field().isFinal()) {
                        sb.append("<br>final");
                    }

                } else if (fav.localVariable() != null) {
                    // 'this' is not a field or a local var
                    if (fav.localVariable().isArgument()) {
                        sb.append("<br>argument");
                    } else {
                        sb.append("<br>local");
                    }
                }

                Type type = fav.type();
                Value value = fav.value();
                if (value != null) {
                    sb.append("<br><code>");
                    sb.append(value.type().name());
                    sb.append("</code>");
                }
                sb.append("</small></html>");
                return sb.toString();
            }
        } // getToolTipText

        /**
         * Called when the look and feel is changing. Forward the call to
         * the cell renderer since it is not in the component tree.
         */
        public void updateUI() {
            super.updateUI();
            Object renderer = getCellRenderer();
            if (renderer instanceof JComponent) {
                ((JComponent) renderer).updateUI();
            }
        } // updateUI
    } // LocalsTree

    /**
     * Locals panel popup menu.
     */
    protected class LocalsPopup extends SmartPopupMenu
        implements ActionListener, ItemListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** X position where mouse was clicked to invoke popup menu. */
        private int mousePositionX;
        /** Y position where mouse was clicked to invoke popup menu. */
        private int mousePositionY;
        /** Command for show source action. */
        private static final String SHOWSOURCE_ACTION = "showSource";
        /** Command for copy action. */
        private static final String COPY_ACTION = "copy";

        /**
         * Constructs the locals panel popup menu.
         */
        public LocalsPopup() {
            super(Bundle.getString("Locals.menu.title"));
            String label = Bundle.getString("Locals.menu.hideThis");
            JCheckBoxMenuItem cbitem = new JCheckBoxMenuItem(label);
            cbitem.setActionCommand("locals.hideThis");
            add(cbitem);
            cbitem.addItemListener(this);
            cbitem.setSelected(
                preferences.getBoolean(
                    "locals.hideThis", Defaults.LOCALS_HIDE_THIS));

            label = Bundle.getString("Locals.menu.showFinals");
            cbitem = new JCheckBoxMenuItem(label);
            cbitem.setActionCommand("locals.showFinals");
            add(cbitem);
            cbitem.addItemListener(this);
            cbitem.setSelected(
                preferences.getBoolean(
                    "locals.showFinals", Defaults.LOCALS_SHOW_FINALS));

            label = Bundle.getString("Locals.menu.showSource");
            JMenuItem item = new JMenuItem(label);
            item.setActionCommand(SHOWSOURCE_ACTION);
            add(item);
            item.addActionListener(this);

            label = Bundle.getString("Locals.copyLabel");
            item = new JMenuItem(label);
            item.setActionCommand(COPY_ACTION);
            add(item);
            item.addActionListener(this);
        } // LocalsPopup

        /**
         * Invoked when a menu item has been selected.
         *
         * @param  event  an <code>ActionEvent</code> value.
         */
        public void actionPerformed(ActionEvent event) {
            TreePath path = localsTree.getPathForLocation(
                mousePositionX, mousePositionY);
            if (path == null) {
                return;
            }

            String action = event.getActionCommand();
            if (action == SHOWSOURCE_ACTION) {
                Variable var = (Variable) path.getLastPathComponent();
                Value val = var.getValue();
                PathManager pathman = (PathManager)
                    owningSession.getManager(PathManager.class);
                SourceSource src = null;
                if (val instanceof ObjectReference) {
                    ObjectReference or = (ObjectReference) val;
                    try {
                        src = pathman.mapSource(or.referenceType());
                    } catch (IOException ioe) {
                        // This case is handled below.
                    }
                } else {
                    try {
                        src = pathman.mapSource(var.getTypeName());
                    } catch (IOException ioe) {
                        // This case is handled below.
                    }
                }

                if (src != null) {
                    UIAdapter uia = owningSession.getUIAdapter();
                    if (!uia.showFile(src, 0, 0)) {
                        String msg = MessageFormat.format(
                            Bundle.getString("couldntOpenFileMsg"),
                            new Object[] { src.getName() });
                        owningSession.getUIAdapter().showMessage(
                            UIAdapter.MESSAGE_ERROR, msg);
                    }
                } else {
                    owningSession.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING,
                        Bundle.getString("Locals.error.findingSource"));
                }

            } else if (action == COPY_ACTION) {
                Variable var = (Variable) path.getLastPathComponent();
                Value val = var.getValue();
                ContextManager conman = (ContextManager)
                    owningSession.getManager(ContextManager.class);
                ThreadReference thread = conman.getCurrentThread();
                String str = "";
                try {
                    str = Variables.printValue(val, thread, "\n");
                } catch (Exception e) {
                    str = e.toString();
                }
                StringSelection strsel  = new StringSelection(str);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    strsel, strsel);
            }
        } // actionPerformed

        /**
         * Invoked when a checkbox menu item has been selected.
         *
         * @param  e  Indicates which item was selected.
         */
        public void itemStateChanged(ItemEvent e) {
            JCheckBoxMenuItem cb = (JCheckBoxMenuItem) e.getSource();
            String name = cb.getActionCommand();
            preferences.putBoolean(name, cb.isSelected());
            if (owningSession != null) {
                // This is happening on the AWT event dispatching thread.
                refresh(owningSession);
            }
        } // itemStateChanged

        /**
         * Show the popup menu.
         *
         * @param  e  mouse event.
         */
        protected void showPopup(MouseEvent e) {
            mousePositionX = e.getX();
            mousePositionY = e.getY();
            show(e.getComponent(), mousePositionX, mousePositionY);
        } // showPopup
    } // LocalsPopup

    /**
     * Class LocalsRenderer renders the display of variables in the locals
     * panel. The recently changed variables are painted in a different
     * font style so they stand out from the other variables.
     */
    protected class LocalsRenderer extends DefaultTreeCellRenderer {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** True if the text should be drawn with an underline. */
        private boolean underlineText;

        /**
         * Returns a reference to this component after setting the font
         * style based on whether this row corresponds to a recently
         * changed variable. Other rows are drawn in the default style.
         *
         * @param  tree      the tree.
         * @param  value     node value.
         * @param  selected  true if selected.
         * @param  expanded  true if expanded.
         * @param  leaf      true if leaf.
         * @param  row       row of tree.
         * @param  hasFocus  true if focused.
         * @return  component renderer.
         */
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean selected,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            super.getTreeCellRendererComponent(
                tree, value, selected, expanded, leaf, row, hasFocus);
            // Have to reset this every time so when we draw a message,
            // it is not drawn with an underline.
            underlineText = false;
            Font font = getFont();
            if (!font.isPlain()) {
                // Reset the font back to plain style.
                setFont(font.deriveFont(Font.PLAIN));
            }
            if (value != null) {
                if (value instanceof Variable) {
                    // Did the variable recently change?
                    Variable var = (Variable) value;
                    if (var.isChanged()) {
                        setFont(font.deriveFont(Font.BOLD));
                    }
                    // We'll underline the text later.
                    underlineText = var.isStatic();
                }
            }
            LocalsTreeNode node = (LocalsTreeNode) value;
            setIcon(node.getIcon(expanded));
            return this;
        } // getTreeCellRendererComponent

        /**
         * Paints this component.
         *
         * @param  g  graphics context for drawing.
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (underlineText) {
                // We are to underline the text.
                FontMetrics fontMetrics = g.getFontMetrics();
                int y = fontMetrics.getMaxAscent() + 1;
                int width = fontMetrics.stringWidth(getText());
                g.setColor(getForeground());
                g.drawLine(0, y, width, y);
            }
        } // paintComponent

        /**
         * Called when the look and feel is changing.
         */
        public void updateUI() {
            super.updateUI();
            // Hmm, why doesn't it do this automatically?
            setOpenIcon(getDefaultOpenIcon());
            setClosedIcon(getDefaultClosedIcon());
            setLeafIcon(getDefaultLeafIcon());
            setTextSelectionColor(UIManager.getColor(
                                      "Tree.selectionForeground"));
            setTextNonSelectionColor(UIManager.getColor(
                                         "Tree.textForeground"));
            setBackgroundSelectionColor(UIManager.getColor(
                                            "Tree.selectionBackground"));
            setBackgroundNonSelectionColor(UIManager.getColor(
                                               "Tree.textBackground"));
            setBorderSelectionColor(UIManager.getColor(
                                        "Tree.selectionBorderColor"));
        } // updateUI
    } // LocalsRenderer
} // LocalsPanel
