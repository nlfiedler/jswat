/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: TreeTableModelAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 * Class TreeTableModelAdapter is a table model that provides the
 * mapping between the table and the tree. Most of the methods simply
 * delegate to either the tree-table model or the tree component.
 *
 * @author Nathan Fiedler
 */
class TreeTableModelAdapter extends AbstractTableModel implements Runnable {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The tree component. */
    protected JTree treeComponent;
    /** The tree-table model. */
    protected TreeTableModel treeTableModel;

    /**
     * Constructs a TreeTableModelAdapter for the given tree-table model
     * and the tree component.
     *
     * @param  treeTableModel  tree-table model.
     * @param  tree            tree component.
     */
    public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree) {
        treeComponent = tree;
        this.treeTableModel = treeTableModel;

        // Can't use fireTableRowsInserted() here because the selection
        // model would get updated twice.
        tree.addTreeExpansionListener(new TreeExpansionListener() {
                public void treeExpanded(TreeExpansionEvent event) {
                    fireTableDataChanged();
                }

                public void treeCollapsed(TreeExpansionEvent event) {
                    fireTableDataChanged();
                }
            });

        // Pass along the tree model events, but do so on the AWT thread.
        treeTableModel.addTreeModelListener(new TreeModelListener() {
                public void treeNodesChanged(TreeModelEvent e) {
                    SwingUtilities.invokeLater(TreeTableModelAdapter.this);
                }

                public void treeNodesInserted(TreeModelEvent e) {
                    SwingUtilities.invokeLater(TreeTableModelAdapter.this);
                }

                public void treeNodesRemoved(TreeModelEvent e) {
                    SwingUtilities.invokeLater(TreeTableModelAdapter.this);
                }

                public void treeStructureChanged(TreeModelEvent e) {
                    SwingUtilities.invokeLater(TreeTableModelAdapter.this);
                }
            });
    } // TreeTableModelAdapter

    /**
     * Returns the type of the given column.
     *
     * @param  column  column for which to retrieve type.
     * @return  type of column.
     */
    public Class getColumnClass(int column) {
        return treeTableModel.getColumnClass(column);
    } // getColumnClass

    /**
     * Returns the number of columns.
     *
     * @return  number of columns.
     */
    public int getColumnCount() {
        return treeTableModel.getColumnCount();
    } // getColumnCount

    /**
     * Returns the name of the given column.
     *
     * @param  column  column for which to retrieve name.
     * @return  name of column.
     */
    public String getColumnName(int column) {
        return treeTableModel.getColumnName(column);
    } // getColumnName

    /**
     * Returns the number of rows.
     *
     * @return  number of rows.
     */
    public int getRowCount() {
        return treeComponent.getRowCount();
    } // getRowCount

    /**
     * Returns value of cell at row and column.
     *
     * @param  row     row in table.
     * @param  column  column in table.
     * @return  value at the given cell.
     */
    public Object getValueAt(int row, int column) {
        TreePath treePath = treeComponent.getPathForRow(row);
        if (treePath == null) {
            return null;
        }
        Object node = treePath.getLastPathComponent();
        return treeTableModel.getValueAt(node, column);
    } // getValueAt

    /**
     * Determines if the given cell is editable or not.
     *
     * @param  row     row in table.
     * @param  column  column in table.
     * @return  true if cell is editable.
     */
    public boolean isCellEditable(int row, int column) {
        TreePath treePath = treeComponent.getPathForRow(row);
        Object node = treePath.getLastPathComponent();
        return treeTableModel.isCellEditable(node, column);
    } // isCellEditable

    /**
     * Notify the table listeners on the AWT thread.
     */
    public void run() {
        fireTableDataChanged();
    } // run

    /**
     * Sets the value at the given cell.
     *
     * @param  value   new value.
     * @param  row     row in table.
     * @param  column  column in table.
     */
    public void setValueAt(Object value, int row, int column) {
        TreePath treePath = treeComponent.getPathForRow(row);
        Object node = treePath.getLastPathComponent();
        treeTableModel.setValueAt(value, node, column);
    } // setValueAt
} // TreeTableModelAdapter
