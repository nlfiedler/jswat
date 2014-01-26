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
 * $Id: BreakPanel.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.JSwat;
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
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.sun.jdi.Location;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
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
public class BreakPanel extends JSwatPanel implements BreakpointListener, GroupListener {
    /** Tree representing the breakoint groups and breakpoints. */
    protected JTree uiTree;
    /** Containing component object. */
    protected JComponent uiComponent;
    /** Session that we are listening to. */
    protected Session owningSession;

    /**
     * Constructs a BreakPanel.
     */
    public BreakPanel() {
        // Don't try to build out anything just now. Wait for the
        // call to the init() method.
        BasicTreeNode root = new BasicTreeNode("junk");
        uiTree = new JTree(new DefaultTreeModel(root));
        uiTree.setCellRenderer(new DefaultTreeCellRenderer() {
                /** silence the compiler warnings */
                private static final long serialVersionUID = 1L;
                public Component getTreeCellRendererComponent
                    (JTree tree, Object value,
                     boolean isSelected, boolean isExpanded,
                     boolean leaf, int row, boolean hasFocus) {
                    // Get the default renderer first.
                    Component renderer = super.getTreeCellRendererComponent
                        (tree, value, isSelected, isExpanded,
                         leaf, row, hasFocus);
                    BasicTreeNode node = (BasicTreeNode) value;
                    setIcon(node.getIcon());
                    return renderer;
                }
            });

        // Add a mouse listener to show breakpoint properties.
        uiTree.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        TreePath selPath = uiTree.getPathForLocation
                            (e.getX(), e.getY());
                        if (selPath != null) {
                            Object[] path = selPath.getPath();
                            if (path.length > 1) {
                                BasicTreeNode node = (BasicTreeNode)
                                    selPath.getLastPathComponent();
                                if (node.isLeaf()) {
                                    // See if it is a breakpoint.
                                    if (node instanceof BreakpointNode) {
                                        Breakpoint bp = (Breakpoint)
                                            node.getUserObject();
                                        editBreakpoint(bp);
                                    }
                                }
                            }
                        }
                    }
                }
            });

        uiComponent = buildUI(new JScrollPane(uiTree));
    } // BreakPanel

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
    } // activate

    /**
     * Invoked when a breakpoint has been added.
     *
     * @param  e  breakpoint change event
     */
    public void breakpointAdded(BreakpointEvent e) {
        refresh(owningSession);
    } // breakpointAdded

    /**
     * Invoked when a breakpoint has been modified.
     *
     * @param  e  breakpoint change event
     */
    public void breakpointModified(BreakpointEvent e) {
        refresh(owningSession);
    } // breakpointModified

    /**
     * Invoked when a breakpoint has been removed.
     *
     * @param  e  breakpoint change event
     */
    public void breakpointRemoved(BreakpointEvent e) {
        // We're notified before the breakpoint is actually removed,
        // so call the refresh() method later.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    refresh(owningSession);
                }
            });
    } // breakpointRemoved

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
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridheight = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.0;

        // Buttons are spaced at least 2 pixels apart.
        gbc.insets = new Insets(1, 1, 1, 1);

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
        button.addActionListener(ActionTable.getAction("setBreak"));
        gbl.setConstraints(button, gbc);
        newComp.add(button);

        // Add a button to allow adding breakpoint groups.
        button = new JButton(Bundle.getString("Break.addGroupLabel"));
        button.addActionListener(ActionTable.getAction("addBreakGroup"));
        gbl.setConstraints(button, gbc);
        newComp.add(button);

        return newComp;
    } // buildUI

    /**
     * Fill in the given tree node with the breakpoint's contents.
     *
     * @param  brkpgrp  breakpoint group to build out.
     * @param  node     tree node to fill in.
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
    } // buildGroupNode

    /**
     * Called when the Session is closing down this panel, generally
     * just after the panel has been removed from the Session.
     *
     * @param  session  Session closing the panel.
     */
    public void close(Session session) {
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        brkman.removeBreakListener(this);
        brkman.removeGroupListener(this);
    } // close

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Panels are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
    } // deactivate

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
    } // deleteSelections

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
    } // disableSelections

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
                    refresh(owningSession);
                }
            });
        editor.setVisible(true);
    } // editBreakpoint

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
    } // enableSelections

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
    } // getSelectedBreakpoint

    /**
     * Returns a reference to the UI component. This is the object
     * that is placed within the program user interface. It wraps
     * the peer component inside a scrollable view.
     *
     * @return  ui component object
     */
    public JComponent getUI() {
        return uiComponent;
    } // getUI

    /**
     * Invoked when a group has been added.
     *
     * @param  event  group change event
     */
    public void groupAdded(GroupEvent event) {
        refresh(owningSession);
    } // groupAdded

    /**
     * Invoked when a group has been disabled.
     *
     * @param  event  group change event
     */
    public void groupDisabled(GroupEvent event) {
        refresh(owningSession);
    } // groupDisabled

    /**
     * Invoked when a group has been enabled.
     *
     * @param  event  group change event
     */
    public void groupEnabled(GroupEvent event) {
        refresh(owningSession);
    } // groupEnabled

    /**
     * Invoked when a group has been removed.
     *
     * @param  event  group change event
     */
    public void groupRemoved(GroupEvent event) {
        refresh(owningSession);
    } // groupRemoved

    /**
     * Called when the Session is ready to initialize this panel,
     * generally just after the panel has been added to the Session.
     *
     * @param  session  Session initializing this panel.
     */
    public void init(Session session) {
        owningSession = session;
        // Need to build out the tree as soon as possible.
        refresh(session);
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        brkman.addBreakListener(this);
        brkman.addGroupListener(this);
    } // init

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
            refresh(owningSession);
        }
    } // processSelections

    /**
     * Update the display on the screen. Use the given Session
     * to fetch the desired data.
     *
     * @param  session  debugging Session object.
     */
    public void refresh(Session session) {
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);

        // Create a new tree model with the default breakpoint
        // group as the tree root.
        BreakpointGroup group = brkman.getDefaultGroup();
        GroupNode groupNode = new GroupNode(group);
        DefaultTreeModel model = new DefaultTreeModel(groupNode);

        // Recursively build out the rest of the tree.
        buildGroupNode(group, groupNode);

        // Set the model now that it has been built out.
        uiTree.setModel(model);

        // Expand all of the rows in the tree.
        int row = 0;
        while (row < uiTree.getRowCount()) {
            uiTree.expandRow(row++);
        }
    } // refresh

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

        // Try the way that is most likely to succeed first.
        LocatableBreakpoint lbp = (LocatableBreakpoint) bp;
        PathManager pathman = (PathManager)
            owningSession.getManager(PathManager.class);
        // Won't work if the class name is pattern.
        SourceSource src = null;
        try {
            src = pathman.mapSource(lbp.getClassName());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        int line = lbp.getLineNumber();

        if (line < 0) {
            // That didn't work, try the resolved location.
            Location loc = lbp.getLocation();
            if (loc == null) {
                JOptionPane.showMessageDialog(
                    null,
                    Bundle.getString("Break.bpHasNoLocation"),
                    Bundle.getString("Break.errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                src = pathman.mapSource(loc.declaringType());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            line = loc.lineNumber();
        }

        if ((src != null) && (line > 0)) {
            // Open a source view to that line.
            UIAdapter adapter = owningSession.getUIAdapter();
            if (!adapter.showFile(src, line, 0)) {
                owningSession.getStatusLog().writeln(
                    Bundle.getString("Break.openFileFailed"));
            }
        } else {
            owningSession.getStatusLog().writeln(
                Bundle.getString("Break.showFailed1") + " " +
                lbp.getClassName() +
                "\n" + Bundle.getString("Break.showFailed2"));
        }
    } // showSource

    /**
     * Class BasicTreeNode is the base class for both breakpoint
     * nodes and breakpoint group nodes.
     */
    protected class BasicTreeNode extends DefaultMutableTreeNode {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Icon for disabled breakpoints. */
        protected Icon disabledIcon;
        /** Icon for resolved breakpoints. */
        protected Icon resolvedIcon;

        /**
         * Constructs a BasicTreeNode.
         *
         * @param  userObject  user object.
         */
        public BasicTreeNode(Object userObject) {
            super(userObject);
        } // BasicTreeNode

        /**
         * Constructs a BasicTreeNode.
         *
         * @param  userObject      user object.
         * @param  allowsChildren  true if node allows children.
         */
        public BasicTreeNode(Object userObject, boolean allowsChildren) {
            super(userObject, allowsChildren);
        } // BasicTreeNode

        /**
         * Returns the icon appropriate for this node.
         *
         * @return  icon appropriate for this node.
         */
        public Icon getIcon() {
            if (disabledIcon == null) {
                JSwat swat = JSwat.instanceOf();
                URL url = swat.getResource("brkDisabledImage");
                disabledIcon = new ImageIcon(url);
                url = swat.getResource("brkResolvedImage");
                resolvedIcon = new ImageIcon(url);
            }

            Object o = getUserObject();
            if (o instanceof Breakpoint) {
                Breakpoint bp = (Breakpoint) o;
                if (!bp.isEnabled()) {
                    // Breakpoint is disabled.
                    return disabledIcon;
                } if (bp.isResolved()) {
                    // Breakpoint is resolved.
                    return resolvedIcon;
                }
            } if (o instanceof BreakpointGroup) {
                BreakpointGroup grp = (BreakpointGroup) o;
                if (!grp.isEnabled()) {
                    // Breakpoint group is disabled.
                    return disabledIcon;
                }
            }
            return null;
        } // getIcon
    } // BasicTreeNode

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
        } // BreakpointNode

        /**
         * Returns the name of the breakpoint group.
         *
         * @return  name of the breakpoint group.
         */
        public String toString() {
            Breakpoint bp = (Breakpoint) getUserObject();
            // Get the terse breakpoint description.
            return bp.toString(true);
        } // toString
    } // BreakpointNode

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
        } // GroupNode

        /**
         * Returns the name of the breakpoint group.
         *
         * @return  name of the breakpoint group.
         */
        public String toString() {
            BreakpointGroup brkgrp = (BreakpointGroup) getUserObject();
            return brkgrp.getName();
        } // toString
    } // GroupNode

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
        public boolean processNode(BreakpointManager brkman, Object o);
    } // NodeProcessor
} // BreakPanel
