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
 * FILE:        TreeTableModel.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      11/26/02        Initial version
 *
 * $Id: TreeTableModel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import javax.swing.tree.TreeModel;

/**
 * TreeTableModel defines the model used by a JTreeTable. It includes
 * methods for dealing with the table columns.
 *
 * @author Nathan Fiedler
 */
public interface TreeTableModel extends TreeModel {

    /**
     * Returns the type for given column.
     *
     * @param  column  column for which to retrieve the type.
     * @return  type of the column.
     */
    Class getColumnClass(int column);

    /**
     * Returns the number of columns.
     *
     * @return  number of columns.
     */
    int getColumnCount();

    /**
     * Returns the name for given column.
     *
     * @param  column  column for which to retrieve the name.
     * @return  name of the column.
     */
    String getColumnName(int column);

    /**
     * Returns the value to be displayed for the node in the given
     * column.
     *
     * @param  node    node corresponding to a row in the table.
     * @param  column  column from which to get value.
     * @return  value at given node and column.
     */
    Object getValueAt(Object node, int column);

    /**
     * Indicates whether the the value for the node at in the given
     * column number is editable.
     *
     * @param  node    node corresponding to a row in the table.
     * @param  column  column from which to get editable state.
     * @return  true if editable, false otherwise.
     */
    boolean isCellEditable(Object node, int column);

    /**
     * Sets the value for the node at the given column.
     *
     * @param  aValue  new value.
     * @param  node    node corresponding to a row in the table.
     * @param  column  column from which to get editable state.
     */
    void setValueAt(Object aValue, Object node, int column);
} // TreeTableModel
