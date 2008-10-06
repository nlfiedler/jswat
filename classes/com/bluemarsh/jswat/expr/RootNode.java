/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
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
 * $Id: RootNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

/**
 * Class RootNode is a parent node that has no parent.
 *
 * @author  Nathan Fiedler
 */
class RootNode extends ParentNode {

    /**
     * Constructs a RootNode.
     */
    public RootNode() {
        super(null);
    } // RootNode

    /**
     * Sets the parent node of this node.
     *
     * @param  parent  parent node.
     */
    public void setParent(Node parent) {
        throw new UnsupportedOperationException("root has no parent");
    } // setParent
} // RootNode
