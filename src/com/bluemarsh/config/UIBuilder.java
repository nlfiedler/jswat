/*********************************************************************
 *
 *	Copyright (C) 1999-2001 Nathan Fiedler
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
 * FILE:        UIBuilder.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/24/99        Initial version
 *      nf      11/26/00        Moved the listener code to DialogPresenter
 *      nf      08/22/01        Make group elements left aligned
 *      nf      11/10/01        Fixed bug 277
 *
 * DESCRIPTION:
 *      This file defines the class responsible for building out the
 *      user interface of JConfigure.
 *
 * $Id: UIBuilder.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * Class UIBuilder builds the user interface for JConfigure. It uses
 * the element tree to build out the appropriate user interface
 * widgets. The entire interface will be placed inside a dialog box,
 * with a tree on the left and the options on the right.
 *<p>
 * UIBuilder will act as a controller to the dialog elements. It
 * ensures the appropriate group panel is displayed when a group is
 * selected in the tree.
 *
 * @author  Nathan Fiedler
 */
class UIBuilder {

    /**
     * Builds out the group tree nodes and options panels.
     *
     * @param  group  GroupElement of the options tree.
     * @param  panel  Panel to which groups are added.
     * @return  New group tree node.
     */
    protected static MutableTreeNode buildGroup(GroupElement group,
                                                JPanel panel) {
        // Create the options panel and set it in the group element
        // before doing anything else.
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        // All components are laid out the same for now.
        // Each component is on a row to itself.
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        // Each component is aligned to the left and top.
        gbc.anchor = GridBagConstraints.NORTHWEST;
        // Components are spaced at least 10 pixels apart.
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        JPanel groupPanel = new JPanel(gbl);
        group.setPanel(groupPanel);
        // xxx - this casting is ugly
        GroupTreeNode node = (GroupTreeNode) group.getUI();

        // Add new panel to the right panel manager.
        panel.add(groupPanel, group.getName());

        // Add a group panel label object.
        GradientTextLabel gtl = new GradientTextLabel(group.getLabel());
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbl.setConstraints(gtl, gbc);
        groupPanel.add(gtl);
        gbc.fill = GridBagConstraints.NONE;

        // Find direct option element children and add to panel.
        Element e = group.getChild();
        while (e != null) {
            if (e instanceof OptionElement) {
                OptionElement oe = (OptionElement) e;
                // xxx - this casting is ugly
                Component c = (Component) oe.getUI();
                gbl.setConstraints(c, gbc);
                groupPanel.add(c);
            }
            if (e instanceof GroupElement) {
                // Recurse the group tree.
                node.add(buildGroup((GroupElement) e, panel));
            }
            e = e.getSibling();
        }

        return node;
    } // buildGroup

    /**
     * Builds out the JTree nodes to mirror the options tree.
     * Only the root and group elements are mirrored.
     *
     * @param  root   RootElement of the options tree.
     * @param  panel  Panel to which groups are added.
     * @return  New top-most tree node.
     */
    protected static MutableTreeNode buildTree(RootElement root,
                                               JPanel panel) {
        // Build the tree node for this element.
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getUI();

        // Add in this elements children, if they are GroupElements.
        // We process any of the group elements recursively.
        if (root.hasChild()) {
            Element child = root.getChild();
            do {
                if (child instanceof GroupElement) {
                    node.add(buildGroup((GroupElement) child, panel));
                }
                child = child.getSibling();
            } while (child != null);
        }
        return node;
    } // buildTree

    /**
     * Builds out the user interface components to mirror the
     * preferences element tree.
     *
     * @param  root   Root element of the options tree. Uses the root
     *                element's label to set the dialog title.
     * @param  owner  Owning frame for the new dialog.
     * @param  modal  True if dialog is to be modal.
     * @return  Dialog information object.
     */
    public static DialogInfo buildUI(RootElement root, Frame owner,
                                     boolean modal) {
        // Build these first, since we need them around.
        CardLayout panelLayout = new CardLayout();
        JPanel rightPanel = new JPanel(panelLayout);

        // Build the group tree and options panels.
        ConfigTreeModel model = new ConfigTreeModel
            (buildTree(root, rightPanel));
        JTree groupTree = new JTree(model);
        // Don't need to see the useless root node.
        groupTree.setRootVisible(false);
        groupTree.setShowsRootHandles(true);
        JScrollPane scroller = new JScrollPane(groupTree);

        // Set up the tree node to panel mediator.
        new TreeNodeToPanelMediator(groupTree, rightPanel, panelLayout);

        // Build the dialog.
        JDialog dialog = new JDialog(owner, root.getLabel(), modal);
        dialog.setDefaultCloseOperation
            (WindowConstants.DISPOSE_ON_CLOSE);
        Container pane = dialog.getContentPane();
        GridBagLayout gbl = new GridBagLayout();
        pane.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbl.setConstraints(scroller, gbc);
        pane.add(scroller);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(rightPanel, gbc);
        pane.add(rightPanel);

        // Add the Ok button.
        JButton okButton = new JButton("OK");
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(okButton, gbc);
        pane.add(okButton);

        // Add the Cancel button.
        JButton cancelButton = new JButton("Cancel");
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(cancelButton, gbc);
        pane.add(cancelButton);

        dialog.pack();
        // Need to make the dialog not resizable so the user won't
        // cause the components to fall on top of each other.
        dialog.setResizable(false);

        return new DialogInfo(dialog, okButton, cancelButton);
    } // buildUI
} // UIBuilder
