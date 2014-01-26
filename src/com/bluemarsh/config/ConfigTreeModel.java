/*********************************************************************
 *
 *	Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: ConfigTreeModel.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Class ConfigTreeModel represents the options tree model.
 *
 * @author  Nathan Fiedler
 * @version 1.0  12/24/99
 */
class ConfigTreeModel extends DefaultTreeModel {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a ConfigTreeModel object with the given root node.
     *
     * @param  root  Root node of the tree.
     */
    public ConfigTreeModel(TreeNode root) {
        super(root);
    } // ConfigTreeModel
} // ConfigTreeModel
