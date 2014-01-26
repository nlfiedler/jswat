/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * PROJECT:     JSwat
 * MODULE:      UI
 * FILE:        AbstractTreeTableModel.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      11/26/02        Initial version
 *
 * $Id: AbstractTreeTableModel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import javax.swing.tree.DefaultTreeModel;

/**
 * An abstract implementation of the TreeTableModel interface. Most of
 * the real implementation is handled by the superclass.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractTreeTableModel extends DefaultTreeModel
    implements TreeTableModel {

    /**
     * Constructs a AbstractTreeTableModel.
     */
    public AbstractTreeTableModel() {
        super(null);
    } // AbstractTreeTableModel

    /**
     * Get the type of the data in the given column. This default
     * implementation returns <code>Object.class</code> for all columns.
     *
     * @param  column  column for which to get type.
     * @return  type of the data in the column.
     */
    public Class getColumnClass(int column) {
        return Object.class;
    } // getColumnClass

    /**
     * By default, make the column with the Tree in it the only editable
     * one. Making this column editable causes the JTable to forward
     * mouse and keyboard events in the Tree column to the underlying
     * JTree.
     *
     * @param  node    node to test.
     * @param  column  column to test.
     * @return  true if cell is editable, false otherwise.
     */
    public boolean isCellEditable(Object node, int column) {
        return TreeTableModel.class.isAssignableFrom(getColumnClass(column));
    } // isCellEditable

    /**
     * Set the value of the given node at the indicated column.
     *
     * @param  aValue  new cell value.
     * @param  node    tree node to take new value.
     * @param  column  column of cell being changed.
     */
    public void setValueAt(Object aValue, Object node, int column) {
        // to be overridden
    } // setValueAt
} // AbstractTreeTableModel
