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
 * $Id: BreakPanel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.action.ActionTable;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointEvent;
import com.bluemarsh.jswat.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.breakpoint.BreakpointListener;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.GroupEvent;
import com.bluemarsh.jswat.breakpoint.GroupListener;
import com.bluemarsh.jswat.breakpoint.LocatableBreakpoint;
import com.bluemarsh.jswat.breakpoint.ui.EditorDialog;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.sun.jdi.ReferenceType;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Class BreakPanel is responsible for displaying all known breakpoints
 * in a tree structure. The root node of the tree is the "Default"
 * breakpoint group, which may contain other groups and/or breakpoints.
 *
 * <p>It will keep itself up to date with changes in the breakpoints, by
 * listening to the breakpoint manager.</p>
 *
 * @author  Nathan Fiedler
 */
public class BreakPanel extends AbstractPanel
    implements BreakpointListener, GroupListener, MouseListener,
               PropertyChangeListener {
    /** Tree representing the breakoint groups and breakpoints. */
    private JTree uiTree;
    /** Containing component object. */
    private JComponent uiComponent;
    /** Our tree expansion listener for setting the groups as expanded. */
    private TreeExpansionListener treeExpansionListener;

    /**
     * Constructs a BreakPanel.
     */
    public BreakPanel() {
        // Don't try to build out anything just now. Wait for the
        // call to the opened() method.
        BasicTreeNode root = new BasicTreeNode("junk");
        uiTree = new JTree(new DefaultTreeModel(root));
        uiTree.setShowsRootHandles(true);
        uiTree.setCellRenderer(new BreakpointRenderer());
        uiTree.addMouseListener(this);
        uiComponent = buildUI(new JScrollPane(uiTree));
    }

    /**
     * Invoked when a breakpoint has been added.
     *
     * @param  e  breakpoint change event
     */
    public void breakpointAdded(BreakpointEvent e) {
        refreshLater();
    }

    /**
     * Invoked when a breakpoint has been modified.
     *
     * @param  e  breakpoint change event
     */
    public void breakpointModified(BreakpointEvent e) {
        refreshLater();
    }

    /**
     * Invoked when a breakpoint has been removed.
     *
     * @param  e  breakpoint change event
     */
    public void breakpointRemoved(BreakpointEvent e) {
        // We're notified before the breakpoint is actually removed,
        // so call the refresh() method later.
        refreshLater();
    }

    /**
     * Build the interface components for this panel (buttons mostly).
     *
     * @param  tree  breakpoints tree component.
     * @return  new containing component for all of the ui.
     */
    protected JComponent buildUI(JComponent tree) {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel newComp = new JPanel(gbl);

        // Make the tree take up all the space on the left.
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbl.setConstraints(tree, gbc);
        newComp.add(tree);

        // Reset the constraints.
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridheight = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.0;

        JButton button;

        // Add a button to edit the properties of the selected breakpoint.
        button = new JButton(Bundle.getString("Break.propertiesLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    Breakpoint bp = getSelectedBreakpoint();
                    if (bp != null) {
                        editBreakpoint(bp);
                    }
                }
            });
        gbl.setConstraints(button, gbc);
        newComp.add(button);

        gbc.gridy = GridBagConstraints.RELATIVE;

        // Add a button to show the breakpoint source code.
        button = new JButton(Bundle.getString("Break.viewcodeLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showSource();
                }
            });
        gbl.setConstraints(button, gbc);
        newComp.add(button);

        // Add a button to disable the selected breakpoints.
        button = new JButton(Bundle.getString("Break.disableLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    disableSelections();
                }
            });
        gbl.setConstraints(button, gbc);
        newComp.add(button);

        // Add a button to enable the selected breakpoints.
        button = new JButton(Bundle.getString("Break.enableLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    enableSelections();
                }
            });
        gbl.setConstraints(button, gbc);
        newComp.add(button);

        // Add a button to delete the selected breakpoints.
        button = new JButton(Bundle.getString("Break.deleteLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteSelections();
                }
            });
        gbl.setConstraints(button, gbc);
        newComp.add(button);

        // Add a button to allow adding breakpoints.
        button = new JButton(Bundle.getString("Break.addLabel"));
        try {
            button.addActionListener(ActionTable.getAction("setBreak"));
        } catch (Exception e) {
            button.setEnabled(false);
        }
        gbl.setConstraints(button, gbc);
        newComp.add(button);

        // Add a button to allow adding breakpoint groups.
        button = new JButton(Bundle.getString("Break.addGroupLabel"));
        try {
            button.addActionListener(ActionTable.getAction("addBreakGroup"));
        } catch (Exception e) {
            button.setEnabled(false);
        }
        gbl.setConstraints(button, gbc);
        newComp.add(button);

        return newComp;
    }

    /**
     * Fill in the given tree node with the breakpoint's contents.
     *
     * @param  brkgrp  breakpoint group to build out.
     * @param  node    tree node to fill in.
     */
    protected void buildGroupNode(BreakpointGroup brkgrp,
                                  MutableTreeNode node) {
        // Iterate over the breakpoint groups.
        Iterator iter = brkgrp.groups(false);
        while (iter.hasNext()) {
            BreakpointGroup subgroup = (BreakpointGroup) iter.next();
            // Create a GroupNode for each breakpoint group.
            GroupNode subnode = new GroupNode(subgroup);
            node.insert(subnode, node.getChildCount());
            buildGroupNode(subgroup, subnode);
        }

        // Iterate over the breakpoints.
        iter = brkgrp.breakpoints(false);
        while (iter.hasNext()) {
            // Create a BreakpointNode for each of the breakpoints.
            Breakpoint bp = (Breakpoint) iter.next();
            BreakpointNode brknode = new BreakpointNode(bp);
            node.insert(brknode, node.getChildCount());
        }
    }

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
        UIManager.removePropertyChangeListener(this);
        BreakpointManager brkman = (BreakpointManager)
            sevt.getSession().getManager(BreakpointManager.class);
        brkman.removeBreakListener(this);
        brkman.removeGroupListener(this);
        super.closing(sevt);
    }

    /**
     * Deletes the currently selected breakpoints and groups.
     * The selection is based on the nodes selected in the tree.
     */
    protected void deleteSelections() {
        processSelections(new NodeProcessor() {
                public boolean processNode(BreakpointManager brkman,
                                           Object o) {
                    if (o instanceof Breakpoint) {
                        brkman.removeBreakpoint((Breakpoint) o);
                        return true;
                    } else if (o instanceof BreakpointGroup) {
                        BreakpointGroup grp = (BreakpointGroup) o;
                        try {
                            brkman.removeBreakpointGroup(grp);
                            return true;
                        } catch (IllegalArgumentException iae) {
                            // who cares, just ignore it
                        }
                    }
                    return false;
                }
            });
    }

    /**
     * Disables the currently selected breakpoints and groups.
     * The selection is based on the nodes selected in the tree.
     */
    protected void disableSelections() {
        processSelections(new NodeProcessor() {
                public boolean processNode(BreakpointManager brkman,
                                           Object o) {
                    if (o instanceof Breakpoint) {
                        brkman.disableBreakpoint((Breakpoint) o);
                        return true;
                    } else if (o instanceof BreakpointGroup) {
                        BreakpointGroup grp = (BreakpointGroup) o;
                        brkman.disableBreakpointGroup(grp);
                        return true;
                    }
                    return false;
                }
            });
    }

    /**
     * Show the given breakpoint's properties.
     *
     * @param  bp  breakpoint to edit.
     */
    protected void editBreakpoint(Breakpoint bp) {
        Frame frame = SessionFrameMapper.getOwningFrame(uiComponent);
        EditorDialog editor = new EditorDialog(frame, bp);
        editor.setLocationRelativeTo(frame);
        editor.addWindowListener(new WindowAdapter() {
                // For some reason, windowClosing() isn't sent.
                public void windowClosed(WindowEvent we) {
                    // When the editor dialog closes, refresh
                    // the breakpoints panel.
                    refreshLater();
                }
            });
        editor.setVisible(true);
    }

    /**
     * Enables the currently selected breakpoints and groups.
     * The selection is based on the nodes selected in the tree.
     */
    protected void enableSelections() {
        processSelections(new NodeProcessor() {
                public boolean processNode(BreakpointManager brkman,
                                           Object o) {
                    if (o instanceof Breakpoint) {
                        brkman.enableBreakpoint((Breakpoint) o);
                        return true;
                    } else if (o instanceof BreakpointGroup) {
                        BreakpointGroup grp = (BreakpointGroup) o;
                        brkman.enableBreakpointGroup(grp);
                        return true;
                    }
                    return false;
                }
            });
    }

    /**
     * Returns the first currently selected breakpoint in the panel,
     * if any.
     *
     * @return  first selected breakpoint, or null if none selected.
     */
    public Breakpoint getSelectedBreakpoint() {
        TreePath path = uiTree.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                path.getLastPathComponent();
            if (node instanceof BreakpointNode) {
                Breakpoint bp = (Breakpoint) node.getUserObject();
                return bp;
            }
        }
        return null;
    }

    /**
     * Returns a reference to the UI component. This is the object
     * that is placed within the program user interface. It wraps
     * the peer component inside a scrollable view.
     *
     * @return  ui component object
     */
    public JComponent getUI() {
        return uiComponent;
    }

    /**
     * Invoked when a group has been added.
     *
     * @param  event  group change event
     */
    public void groupAdded(GroupEvent event) {
        refreshLater();
    }

    /**
     * Invoked when a group has been disabled.
     *
     * @param  event  group change event
     */
    public void groupDisabled(GroupEvent event) {
        refreshLater();
    }

    /**
     * Invoked when a group has been enabled.
     *
     * @param  event  group change event
     */
    public void groupEnabled(GroupEvent event) {
        refreshLater();
    }

    /**
     * Invoked when a group has been removed.
     *
     * @param  event  group change event
     */
    public void groupRemoved(GroupEvent event) {
        refreshLater();
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and
     * released) on a component.
     *
     * @param  e  mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            TreePath selPath = uiTree.getPathForLocation(e.getX(), e.getY());
            if (selPath != null) {
                Object[] path = selPath.getPath();
                if (path.length > 1) {
                    BasicTreeNode node = (BasicTreeNode)
                        selPath.getLastPathComponent();
                    if (node.isLeaf()) {
                        // See if it is a breakpoint.
                        if (node instanceof BreakpointNode) {
                            Breakpoint bp = (Breakpoint) node.getUserObject();
                            editBreakpoint(bp);
                        }
                    }
                }
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
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
        super.opened(session);
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        brkman.addBreakListener(this);
        brkman.addGroupListener(this);
        UIManager.addPropertyChangeListener(this);
    }

    /**
     * Perform some action on the set of selected breakpoints
     * and breakpoint groups.
     *
     * @param  processor  node processor.
     */
    protected void processSelections(NodeProcessor processor) {
        BreakpointManager brkman = (BreakpointManager)
            owningSession.getManager(BreakpointManager.class);

        // Find selected items in the JTree.
        TreePath[] paths = uiTree.getSelectionPaths();
        if (paths == null) {
            return;
        }

        // Stop listening to breakpoint events until we're done.
        brkman.removeBreakListener(this);
        brkman.removeGroupListener(this);

        // Flag to determine if the tree needs rebuilding.
        boolean rebuild = false;
        for (int i = 0; i < paths.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                paths[i].getLastPathComponent();
            // Process this node of the tree.
            rebuild |= processor.processNode(brkman, node.getUserObject());
        }

        // Start listening again.
        brkman.addBreakListener(this);
        brkman.addGroupListener(this);

        // Rebuild the tree if anything changed.
        if (rebuild) {
            refreshLater();
        }
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param  evt  a PropertyChangeEvent object describing the event source
     *              and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("lookAndFeel")) {
            Object renderer = uiTree.getCellRenderer();
            if (renderer instanceof JComponent) {
                ((JComponent) renderer).updateUI();
            }
        }
    }

    /**
     * Update the display on the screen. Use the given Session to fetch
     * the desired data. This must be run on the AWT event dispatching
     * thread.
     *
     * @param  session  debugging Session object.
     */
    public void refresh(Session session) {
        if (treeExpansionListener != null) {
            uiTree.removeTreeExpansionListener(treeExpansionListener);
        }

        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);

        // Create a new tree model with the default breakpoint
        // group as the tree root.
        BreakpointGroup group = brkman.getDefaultGroup();
        GroupNode groupNode = new GroupNode(group);
        DefaultTreeModel model = new DefaultTreeModel(groupNode);

        try {
            // Recursively build out the rest of the tree.
            buildGroupNode(group, groupNode);
        } catch (ConcurrentModificationException cme) {
            // Whoops, something changed in the breakpoints.
            // Let's bail and hope we get called again later.
            return;
        }

        // Set the model now that it has been built out.
        uiTree.setModel(model);

        // Set expansion state for all of the rows in the tree.
        Enumeration nodes = ((DefaultMutableTreeNode)
                model.getRoot()).depthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                nodes.nextElement();
            Object o = node.getUserObject();
            if (o instanceof BreakpointGroup) {
                TreePath path = new TreePath(model.getPathToRoot(node));
                BreakpointGroup grp = (BreakpointGroup) o;
                if (grp.isExpanded()) {
                    uiTree.expandPath(path);
                } else {
                    uiTree.collapsePath(path);
                }
            }
        }

        if (treeExpansionListener == null) {
            treeExpansionListener = new TreeExpansionListener() {
                public void treeExpanded(TreeExpansionEvent event) {
                    handlePathExpanded(event.getPath(), true);
                }

                public void treeCollapsed(TreeExpansionEvent event) {
                    handlePathExpanded(event.getPath(), false);
                }
            };
        }
        uiTree.addTreeExpansionListener(treeExpansionListener);
    }

    /**
     * Do something about the tree expansion.
     *
     * @param  path      path to expanded node.
     * @param  expanded  true if the node is expanded.
     */
    private void handlePathExpanded(TreePath path, boolean expanded) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
            path.getLastPathComponent();
        Object o = node.getUserObject();
        // It has to be BreakpointGroup, otherwise it should be an error.
        if (o instanceof BreakpointGroup) {
            BreakpointGroup grp = (BreakpointGroup) o;
            grp.setExpanded(expanded);
        }
    }

    /**
     * Show the source code for the first selected breakpoint,
     * if the breakpoint is indeed locatable.
     */
    protected void showSource() {
        // See if the breakpoint is locatable.
        Breakpoint bp = getSelectedBreakpoint();
        if ((bp == null) || !(bp instanceof LocatableBreakpoint)) {
            return;
        }

        // Use most accurate method first, then last accurate methods.
        LocatableBreakpoint lbp = (LocatableBreakpoint) bp;
        PathManager pathman = (PathManager)
            owningSession.getManager(PathManager.class);
        SourceSource src = null;
        ReferenceType clazz = lbp.getReferenceType();
        if (clazz != null) {
            try {
                src = pathman.mapSource(clazz);
            } catch (IOException ioe) {
                // ignore and fall through
            }
        }

        if (src == null) {
            // See if the source name and package are available.
            StringBuffer filepath = new StringBuffer();
            String path = lbp.getPackageName();
            if (path != null) {
                filepath.append(path.replace('.', File.separatorChar));
                filepath.append(File.separatorChar);
            }
            String name = lbp.getSourceName();
            if (name != null) {
                filepath.append(name);
            }
            if (filepath.length() > 0) {
                src = pathman.mapFile(filepath.toString());
            }
        }
        if (src == null) {
            // This won't work if the class name is wild-carded.
            try {
                src = pathman.mapSource(lbp.getClassName());
            } catch (IOException ioe) {
                // ignore and fall through
            }
        }

        int line = lbp.getLineNumber();
        if (src != null && src.exists() && line > 0) {
            // Open a source view to that line.
            UIAdapter adapter = owningSession.getUIAdapter();
            if (!adapter.showFile(src, line, 0)) {
                String msg = MessageFormat.format(
                    Bundle.getString("couldntOpenFileMsg"),
                    new Object[] { src.getName() });
                owningSession.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_ERROR, msg);
            }
        } else {
            String msg = MessageFormat.format(
                Bundle.getString("Break.showFailed"),
                new Object[] { lbp.getClassName() });
            owningSession.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR, msg);
        }
    }

    /**
     * Class BreakpointRenderer implements a tree cell renderer that
     * draws breakpoints in the breakpoint panel.
     */
    protected class BreakpointRenderer extends DefaultTreeCellRenderer {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a BreakpointRenderer.
         */
        public BreakpointRenderer() {
            super();
            // Kill all of those nasty icons.
            setClosedIcon(null);
            setLeafIcon(null);
            setOpenIcon(null);
        }

        /**
         * Return the component for rendering the identified cell.
         *
         * @param  tree        the tree.
         * @param  value       the node user object.
         * @param  isSelected  true if node is selected.
         * @param  isExpanded  true if node is expanded.
         * @param  leaf        true if node is a leaf.
         * @param  row         row of the tree.
         * @param  hasFocus    true if node has focus.
         * @return  rendering component.
         */
        public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean isSelected, boolean isExpanded,
            boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(
                tree, value, isSelected, isExpanded, leaf, row, hasFocus);
            BasicTreeNode node = (BasicTreeNode) value;
            Font font = getFont();
            if (!font.isPlain()) {
                font = font.deriveFont(Font.PLAIN);
            }
            setFont(node.getFont(font));
            return this;
        }

        /**
         * Called when the look and feel is changing.
         */
        public void updateUI() {
            super.updateUI();
            // Hmm, why doesn't it do this automatically?
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
        }
    }

    /**
     * Class BasicTreeNode is the base class for both breakpoint
     * nodes and breakpoint group nodes.
     */
    protected class BasicTreeNode extends DefaultMutableTreeNode {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a BasicTreeNode.
         *
         * @param  userObject  user object.
         */
        public BasicTreeNode(Object userObject) {
            super(userObject);
        }

        /**
         * Constructs a BasicTreeNode.
         *
         * @param  userObject      user object.
         * @param  allowsChildren  true if node allows children.
         */
        public BasicTreeNode(Object userObject, boolean allowsChildren) {
            super(userObject, allowsChildren);
        }

        /**
         * Returns the font appropriate for this node, based on the
         * given font.
         *
         * @param  font  font from which to derive a new one; the initial
         *               style is plain.
         * @return  font appropriate for this node.
         */
        public Font getFont(Font font) {
            return null;
        }
    }

    /**
     * Class BreakpointNode represents a breakpoint in the tree.
     */
    protected class BreakpointNode extends BasicTreeNode {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a BreakpointNode.
         *
         * @param  brk  breakpoint.
         */
        public BreakpointNode(Breakpoint brk) {
            super(brk, false);
        }

        /**
         * Returns the font appropriate for this node, based on the
         * given font.
         *
         * @param  font  font from which to derive a new one; the initial
         *               style is plain.
         * @return  font appropriate for this node.
         */
        public Font getFont(Font font) {
            Breakpoint bp = (Breakpoint) getUserObject();
            if (!bp.isEnabled()) {
                font = font.deriveFont(Font.ITALIC);
            }
            if (bp.isResolved()) {
                font = font.deriveFont(Font.BOLD | font.getStyle());
            }
            return font;
        }

        /**
         * Returns the name of the breakpoint group.
         *
         * @return  name of the breakpoint group.
         */
        public String toString() {
            Breakpoint bp = (Breakpoint) getUserObject();
            // Get the terse breakpoint description.
            return bp.toString(true);
        }
    }

    /**
     * Class GroupNode represents a breakpoint group in the tree.
     */
    protected class GroupNode extends BasicTreeNode {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a GroupNode.
         *
         * @param  brkgrp  breakpoint group.
         */
        public GroupNode(BreakpointGroup brkgrp) {
            super(brkgrp);
        }

        /**
         * Returns the font appropriate for this node, based on the
         * given font.
         *
         * @param  font  font from which to derive a new one; the initial
         *               style is plain.
         * @return  font appropriate for this node.
         */
        public Font getFont(Font font) {
            BreakpointGroup grp = (BreakpointGroup) getUserObject();
            if (!grp.isEnabled()) {
                font = font.deriveFont(Font.ITALIC);
            }
            return font;
        }

        /**
         * Returns the name of the breakpoint group.
         *
         * @return  name of the breakpoint group.
         */
        public String toString() {
            BreakpointGroup brkgrp = (BreakpointGroup) getUserObject();
            return brkgrp.getName();
        }
    }

    /**
     * Interface NodeProcessor defines methods necessary for a class
     * that wishes to process nodes in a tree.
     */
    protected interface NodeProcessor {

        /**
         * Process the given user object from some node of the tree.
         *
         * @param  brkman  BreakpointManager, for processing breakpoints.
         * @param  o  user object from a node of the tree.
         * @return  true if the tree should be rebuilt.
         */
        boolean processNode(BreakpointManager brkman, Object o);
    }
}
