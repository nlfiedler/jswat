/*********************************************************************
 *
 *	Copyright (C) 1999 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * PROJECT:     JConfigure
 * FILE:        TreeNodeToPanelMediator.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      NF      09/16/00        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class responsible for defining how the
 *      group tree node and group panel interact.
 *
 * $Id: TreeNodeToPanelMediator.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.awt.CardLayout;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 * Class TreeNodeToPanelMediator controls the interaction between
 * the group tree node and the group panel layout object. That is,
 * whenever a group tree node is selected, the matching group
 * panel is brought forward in the panel layout.
 *
 * @author  Nathan Fiedler
 * @version 1.0  9/16/00
 */
class TreeNodeToPanelMediator implements TreeSelectionListener {
    /** Panel parent for the group panels. This is managed by
     * the card layout. */
    protected JPanel parentPanel;
    /** Card layout manager for the option panels. */
    protected CardLayout panelLayout;

    /**
     * Constructs a new TreeNodeToPanelMediator.
     *
     * @param  tree    JTree to listen to for selection changes.
     * @param  parent  Parent panel for the group panels.
     * @param  layout  CardLayout managing the group panels.
     */
    public TreeNodeToPanelMediator(JTree tree, JPanel parent,
                                   CardLayout layout) {
        // Add ourselves as a group node selection listener.
        tree.addTreeSelectionListener(this);
        parentPanel = parent;
        panelLayout = layout;
    } // TreeNodeToPanelMediator

    /**
     * Called whenever the value of the selection changes.
     *
     * @param  e  Tree selection event.
     */
    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getPath();
        if (e.isAddedPath(path)) {
            // Is the selected path a group element?
            Object node = path.getLastPathComponent();
            if (node instanceof GroupTreeNode) {
                // If so, have the card layout bring the corresponding
                // panel forward.
                GroupTreeNode groupNode = (GroupTreeNode) node;
                panelLayout.show(parentPanel,
                                 groupNode.getElement().getName());
            }
        }
    } // valueChanged
} // TreeNodeToPanelMediator
