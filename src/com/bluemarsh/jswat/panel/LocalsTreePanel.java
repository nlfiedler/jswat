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
 * $Id: LocalsTreePanel.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.ContextChangeEvent;
import com.bluemarsh.jswat.event.ContextListener;
import com.bluemarsh.jswat.util.FieldAndValue;
import com.bluemarsh.jswat.util.VariableUtils;
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
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Class LocalsTreePanel is responsible for displaying a tree of
 * local variables in the current thread.
 *
 * @author  David Lum
 * @author  Nathan Fiedler
 */
public class LocalsTreePanel extends JSwatPanel implements ContextListener {
    /** JTree containing local variables. */
    protected JTree localsTree;
    /** Our UI component - scrollable panel */
    protected JScrollPane uicomp;
    /** Reference of the Session object that calls our refresh method. */
    protected Session owningSession;

    /**
     * Creates a LocalsTreePanel with the default tree.
     */
    public LocalsTreePanel() {
        String msg = Bundle.getString("Locals.empty");
        TreeNode root = new BasicTreeNode(msg);
        localsTree = new LocalsTree(new DefaultTreeModel(root));
        localsTree.setCellRenderer(new DefaultTreeCellRenderer() {
                /** silence the compiler warnings */
                private static final long serialVersionUID = 1L;
                public Component getTreeCellRendererComponent
                    (JTree tree,
                     Object value,
                     boolean isSelected,
                     boolean isExpanded,
                     boolean leaf,
                     int row,
                     boolean hasFocus) {
                    Component renderer = super.getTreeCellRendererComponent
                        (tree, value, isSelected, isExpanded, leaf,
                         row, hasFocus);
                    BasicTreeNode node = (BasicTreeNode) value;
                    setIcon(node.getIcon(isExpanded));
                    return renderer;
                }
            });
        localsTree.setShowsRootHandles(true);
        ToolTipManager.sharedInstance().registerComponent(localsTree);

        // Set up a mouse listener to react to the user double-clicking
        // on the "..." node within an array node.
        MouseListener ml = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        TreePath selPath =
                            localsTree.getPathForLocation(e.getX(), e.getY());
                        if (selPath != null) {
                            // See if the tree node is a BasicTreeNode
                            // with a user object of '...'.
                            Object last = selPath.getLastPathComponent();
                            if (last instanceof BasicTreeNode) {
                                BasicTreeNode node = (BasicTreeNode) last;
                                String str = (String) node.getUserObject();
                                if ((str != null) && str.equals("...")) {
                                    // If so, get the parent array node and
                                    // tell it to show all of the elements.
                                    ArrayDbgVar arr = (ArrayDbgVar)
                                        node.getParent();
                                    arr.showAll();
                                    ((DefaultTreeModel)
                                     localsTree.getModel()).reload(arr);
                                }
                            }
                        }
                    }
                }
            };
        localsTree.addMouseListener(ml);

        uicomp = new JScrollPane(localsTree);
    } // LocalsTreePanel

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        // Add ourselves as a context change listener.
        ContextManager ctxtMgr = (ContextManager)
            session.getManager(ContextManager.class);
        ctxtMgr.addContextListener(this);
        refresh(session);
    } // activate

    /**
     * Invoked when the current context has changed. The context
     * change event identifies which aspect of the context has
     * changed.
     *
     * @param  cce  context change event
     */
    public void contextChanged(ContextChangeEvent cce) {
        refresh(owningSession);
    } // contextChanged

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Panels are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
        // Remove ourselves as a context change listener.
        ContextManager ctxtMgr = (ContextManager)
            session.getManager(ContextManager.class);
        ctxtMgr.removeContextListener(this);

        // Clear the tree model.
        setMessage("Locals.empty");
    } // deactivate

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

        // Reconstruct the claspath of the selected element.
        BasicTreeNode node = (BasicTreeNode) tpath.getLastPathComponent();
        node = (BasicTreeNode) path[1];
        StringBuffer pathname = new StringBuffer(node.getName());
        for (int i = 2; i < path.length; i++) {
            if (!(node instanceof ArrayDbgVar)) {
                pathname.append('.');
            }
            node = (BasicTreeNode) path[i];
            pathname.append(node.getName());
        }
        return pathname.toString();
    } // getPathName

    /**
     * Returns a reference to the peer UI component. In many
     * cases this is a JList, JTree, or JTable, depending on
     * the type of data being displayed in the panel.
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
     * Called when the Session is ready to initialize this panel,
     * generally just after the panel has been added to the Session.
     *
     * @param  session  Session initializing this panel.
     */
    public void init(Session session) {
        owningSession = session;
    } // init

    /**
     * Update the display on the screen. Use the given VM
     * to fetch the desired data.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
        // Get the list of visible local variables.
        ThreadReference thread = session.getCurrentThread();
        if (thread == null) {
            if (session.isActive()) {
                setMessage("nothreadInParen");
            } else {
                setMessage("Locals.empty");
            }
            return;
        }

        // Get the stack frame.
        StackFrame frame;
        try {
            ContextManager ctxtMgr = (ContextManager)
                session.getManager(ContextManager.class);
            frame = thread.frame(ctxtMgr.getCurrentFrame());
            if (frame == null) {
                setMessage("noframeInParen");
                return;
            }
        } catch (IncompatibleThreadStateException itse) {
            setMessage("threadRunningInParen");
            return;
        } catch (IndexOutOfBoundsException ioobe) {
            // This happens when the thread has no frames at all.
            setMessage("noframeInParen");
            return;
        } catch (NativeMethodException nme) {
            setMessage("nativeInParen");
            return;
        } catch (VMDisconnectedException vmde) {
            // Do nothing, just return.
            return;
        } catch (Exception e) {
            // All other exceptions result in an immediate abort.
            e.printStackTrace();
            setMessage("errorInParen");
            return;
        }

        // Create a sorted map in which to store all of the variable
        // names and values. This helps us deal with shadowed variables.
        Map allVars = new TreeMap();

        // Get the field variable values (both static and non-static).
        try {
            ReferenceType clazz = frame.location().declaringType();
            ListIterator iter = clazz.visibleFields().listIterator();
            while (iter.hasNext()) {
                Field field = (Field) iter.next();
                if (field.isStatic()) {
                    // Skip over constants, which are boring.
                    if (!field.isFinal()) {
                        // Get static values from the ReferenceType.
                        allVars.put(field.name(),
                                    DbgVar.create(field,
                                                  clazz.getValue(field)));
                    }
                }
            }

            ObjectReference thisObj = frame.thisObject();
            if (thisObj != null) {
                // Save a reference to 'this'.
                allVars.put("this", DbgVar.create(thisObj));
                while (iter.hasPrevious()) {
                    Field field = (Field) iter.previous();
                    if (!field.isStatic()) {
                        // Get non-static values from the ObjectReference.
                        allVars.put(field.name(),
                                    DbgVar.create(field,
                                                  thisObj.getValue(field)));
                    }
                }
            }
        } catch (InvalidStackFrameException isfe) {
            setMessage("invalidframeInParen");
            reschedule(session);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            setMessage("errorInParen");
            return;
        }

        // Get the visible local variables in this frame.
        try {
            ListIterator iter = frame.visibleVariables().listIterator();
            // Put the LocalVariable elements into big map.
            // Any field variables of the same name will be
            // appropriately shadowed.
            while (iter.hasNext()) {
                LocalVariable var = (LocalVariable) iter.next();
                try {
                    Value val = frame.getValue(var);
                    allVars.put(var.name(), DbgVar.create(var, val));
                } catch (InconsistentDebugInfoException idie) {
                    // Yuck, a JDK problem; just skip this variable.
                }
            }
        } catch (AbsentInformationException aie) {
            // Skip the local variables when not available.
        } catch (InvalidStackFrameException isfe) {
            setMessage("invalidframeInParen");
            reschedule(session);
            return;
        } catch (NativeMethodException nme) {
            setMessage("nativeInParen");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            setMessage("errorInParen");
            return;
        }

	// See if we have anything to say.
        int nvars = allVars.size();
        if (nvars == 0) {
            setMessage("noneInParen");
            return;
        }

        // Convert the map entries into a sorted list of names.
        List sortedKeys = new ArrayList(allVars.keySet());
        Collections.sort(sortedKeys);

        // Remember the expanded tree paths.
        TreePath rootPath = new TreePath(localsTree.getModel().getRoot());
        Enumeration enmr = localsTree.getExpandedDescendants(rootPath);
        // Copy these to be safe in case the enumeration chokes when
        // the backing data source changes.
        List expandedPaths = null;
        if (enmr != null) {
            expandedPaths = new ArrayList();
            while (enmr.hasMoreElements()) {
                expandedPaths.add(enmr.nextElement());
            }
        }

        // Now use the fact that the current root node child list
        // and the new variables list are sorted to merge them
        // together in a single pass.
        BasicTreeNode rootNode = (BasicTreeNode)
            localsTree.getModel().getRoot();
        int childIndex = 0;
        int varsCount = allVars.size();
        int varsIndex = 0;
        // Manually walk both lists to add the new variables,
        // remove the out-of-scope, and update the remaining ones.
        while ((childIndex < rootNode.getChildCount()) &&
               (varsIndex < varsCount)) {
            DbgVar newVar = (DbgVar) allVars.get(sortedKeys.get(varsIndex));
            DbgVar oldVar = (DbgVar) rootNode.getChildAt(childIndex);

            int result = oldVar.compareTo(newVar);
            if (result < 0) {
                // If the current root node child is less than the current
                // new var, remove it from the root node.
                rootNode.remove(childIndex);

            } else if (result > 0) {
                // If the current root node child is greater than the
                // current new var, insert the new var before the child.
                rootNode.insert(newVar, childIndex);
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
            DbgVar newVar = (DbgVar) allVars.get(sortedKeys.get(varsIndex));
            rootNode.insert(newVar, rootNode.getChildCount());
            varsIndex++;
        }

        // Cause the tree to update the display.
        localsTree.setRootVisible(false);
        DefaultTreeModel model = (DefaultTreeModel) localsTree.getModel();
        model.reload();

        // Expand all the previously expanded paths.
        if (expandedPaths != null) {
            for (int i = 0; i < expandedPaths.size(); i++) {
                TreePath path = (TreePath) expandedPaths.get(i);
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
        javax.swing.Timer t = new javax.swing.Timer
            (100, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // Maybe we can refresh without a problem now.
                        refresh(session);
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
        String msg = Bundle.getString(name);
        TreeNode root = new BasicTreeNode(msg);
        localsTree.setModel(new DefaultTreeModel(root));
        // Make sure the root node (message) is visible.
        localsTree.setRootVisible(true);
    } // setMessage

    /**
     * Custom JTree to show custom tooltip.
     *
     * @author  Nathan Fiedler
     */
    class LocalsTree extends JTree {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Default tooltip text. */
        protected String defaultTip;

        /**
         * Constructs a new LocalsTree with the specified tree model.
         *
         * @param  model  TreeModel to use.
         */
        public LocalsTree(TreeModel model) {
            super(model);
            defaultTip = Bundle.getString("Locals.tooltip");
        } // LocalsTree

        /**
         * Returns custom tooltip text for this tree.
         *
         * @param  e  Mouse event.
         * @return  Custom tooltip text.
         */
        public String getToolTipText(MouseEvent me) {
            TreePath selPath = getPathForLocation(me.getX(), me.getY());
            String name = null;
            if (selPath != null) {
                name = LocalsTreePanel.getPathName(selPath);
            }
            if (name == null) {
                return defaultTip;
            } else {
                // Show the detailed variable information.
                ContextManager conman = (ContextManager)
                    owningSession.getManager(ContextManager.class);
                ThreadReference thread = conman.getCurrentThread();
                if (thread == null) {
                    return defaultTip;
                }
                int frame = conman.getCurrentFrame();
                FieldAndValue fav = null;
                try {
                    // XXX - doesn't get array elements
                    fav = VariableUtils.getField(
                        name, thread, frame);
                } catch (Exception e) {
                    return defaultTip;
                }

                StringBuffer sb = new StringBuffer(64);
                sb.append("<html><font size=\"-1\"><strong>");
                sb.append(name);
                sb.append("</strong><br>");
                if (fav.field != null) {
                    sb.append("field<br>");
                    if (fav.field.isPublic()) {
                        sb.append("public<br>");
                    }
                    if (fav.field.isProtected()) {
                        sb.append("protected<br>");
                    }
                    if (fav.field.isPrivate()) {
                        sb.append("private<br>");
                    }
                    if (fav.field.isStatic()) {
                        sb.append("static<br>");
                    }
                    if (fav.field.isTransient()) {
                        sb.append("transient<br>");
                    }
                    if (fav.field.isVolatile()) {
                        sb.append("volatile<br>");
                    }
                    if (fav.field.isSynthetic()) {
                        sb.append("synthetic<br>");
                    }
                    if (fav.field.isFinal()) {
                        sb.append("final<br>");
                    }

                } else if (fav.localVar != null) {
                    // 'this' is not a field or a local var
                    if (fav.localVar.isArgument()) {
                        sb.append("argument<br>");
                    } else {
                        sb.append("local<br>");
                    }
                }

                sb.append("<code>");
                if (fav.value != null) {
                    sb.append(fav.value.type().name());
                } else if (fav.field != null) {
                    sb.append(fav.field.typeName());
                } else if (fav.localVar != null) {
                    sb.append(fav.localVar.typeName());
                }
                sb.append("</code>");
                sb.append("</font></html>");
                return sb.toString();
            }
        } // getToolTipText
    } // LocalsTree
} // LocalsTreePanel
