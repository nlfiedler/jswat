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
 * $Id: ViewTreeTableModel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.ui.AbstractTreeTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * TreeTableModel implementation for displaying generic object data. The
 * model is synchronized for thread safety.
 *
 * @author  Nathan Fiedler
 */
class ViewTreeTableModel extends AbstractTreeTableModel {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Names of the columns, in order. Set in the constructor. */
    private String[] columnNames;
    /** Types of the column data, in order. Set in the constructor. */
    private Class[] columnTypes;
    /** The total number of columns, including invisible ones. */
    private int numberOfColumns;
    /** If non-null, indicates which columns are editable. */
    private boolean[] editableColumns;

    /**
     * Constructs an empty ViewTreeTableModel object. The number of
     * columns in the table is determined by the length of the
     * column names array.
     *
     * @param  colNames  names for the columns.
     * @param  colTypes  types for the columns.
     */
    public ViewTreeTableModel(String[] colNames, Class[] colTypes) {
        this(colNames, colNames.length, colTypes, null);
    } // ViewTreeTableModel

    /**
     * Constructs an empty ViewTreeTableModel object. The number of
     * columns in the table is determined by the length of the
     * column names array.
     *
     * @param  colNames  names for the columns.
     * @param  numCols   number of columns, including invisibles.
     * @param  colTypes  types for the columns.
     */
    public ViewTreeTableModel(String[] colNames, int numCols,
                              Class[] colTypes) {
        this(colNames, numCols, colTypes, null);
    } // ViewTreeTableModel

    /**
     * Constructs an empty ViewTreeTableModel object. The number of
     * columns in the table is determined by the length of the
     * column names array.
     *
     * @param  colNames      names for the columns.
     * @param  colTypes      types for the columns.
     * @param  editableCols  Indicates which columns are editable.
     *                       Each entry corresponds to a column. A true
     *                       value means the column is editable.
     */
    public ViewTreeTableModel(String[] colNames, Class[] colTypes,
                              boolean[] editableCols) {
        this(colNames, colNames.length, colTypes, editableCols);
    } // ViewTreeTableModel

    /**
     * Constructs an empty ViewTreeTableModel object. The number of
     * columns in the table is determined by the length of the
     * column names array.
     *
     * @param  colNames      names for the columns.
     * @param  numCols       number of columns, including invisibles.
     * @param  colTypes      types for the columns.
     * @param  editableCols  Indicates which columns are editable.
     *                       Each entry corresponds to a column. A true
     *                       value means the column is editable.
     */
    public ViewTreeTableModel(String[] colNames, int numCols,
                              Class[] colTypes, boolean[] editableCols) {
        super();
        setRoot(new Node());
        columnNames = colNames;
        columnTypes = colTypes;
        numberOfColumns = numCols;
        assert numberOfColumns > 0 : "number of columns must be > 0";
        editableColumns = editableCols;
    } // ViewTreeTableModel

    /**
     * Clears the entire tree of all data. This fires a tree change
     * event.
     */
    public void clear() {
        TreeNode root = (TreeNode) getRoot();
        setRoot(new Node());
        nodeStructureChanged(root);
    } // clear

    /**
     * Get the type of the data in the given column. This default
     * implementation returns <code>Object.class</code> for all columns.
     *
     * @param  column  column for which to get type.
     * @return  type of the data in the column.
     */
    public Class getColumnClass(int column) {
        return columnTypes[column];
    } // getColumnClass

    /**
     * Returns the number of columns managed by the data source object.
     *
     * @return Always returns two.
     */
    public int getColumnCount() {
        return columnNames.length;
    } // getColumnCount

    /**
     *  Return name for the column.
     *
     * @param  column  Column to retreive name.
     * @return  Name of column.
     */
    public String getColumnName(int column) {
        return columnNames[column];
    } // getColumnName

    /**
     * Gets the value for the record in the identified cell.
     *
     * @param  node    the row whose value is to be changed.
     * @param  column  the column whose value is to be changed.
     * @return  cell value, possibly null.
     */
    public Object getValueAt(Object node, int column) {
        return ((Node) node).getData(column);
    } // getValueAt

    /**
     * Returns true if the identified cell is editable.
     *
     * @param  node    the row whose value is to be changed.
     * @param  column  the column whose value is to be changed.
     * @return  true if the cell is editable.
     */
    public boolean isCellEditable(Object node, int column) {
        if (editableColumns == null) {
            return super.isCellEditable(node, column);
        } else {
            return editableColumns[column];
        }
    } // isCellEditable

    /**
     * Remove the given node, and prune any empty parents.
     *
     * @param  node  node to be removed.
     */
    public void removeAndPrune(MutableTreeNode node) {
        MutableTreeNode parent = (MutableTreeNode) node.getParent();
        node.removeFromParent();
        while (parent != null && parent.getChildCount() == 0) {
            node = parent;
            parent = (MutableTreeNode) node.getParent();
            node.removeFromParent();
        }
        if (parent != null) {
            nodeStructureChanged(parent);
        }
    } // removeAndPrune

    /**
     * Sets the value for <code>node</code> in column <code>column</code>.
     * This notifies listeners that the node data has changed.
     *
     * @param  aValue  the new value.
     * @param  node    the row whose value is to be changed.
     * @param  column  the column whose value is to be changed.
     */
    public void setValueAt(Object aValue, Object node, int column) {
        setValueNoEvent(aValue, node, column);
        int[] childIndices = new int[1];
        TreeNode parent = ((TreeNode) node).getParent();
        childIndices[0] = getIndexOfChild(parent, node);
        nodesChanged(parent, childIndices);
    } // setValueAt

    /**
     * Sets an attribute value for the record in the cell at 'col'
     * and 'row'. The 'val' is the new value. This does not notify
     * listeners that the cell data has changed.
     *
     * @param  aValue  the new value.
     * @param  node    the row whose value is to be changed.
     * @param  column  the column whose value is to be changed.
     */
    public void setValueNoEvent(Object aValue, Object node, int column) {
        ((Node) node).setData(column, aValue);
    } // setValueNoEvent

    /**
     * Class Node is a default, mutable tree node with extra user data.
     *
     * <p>Note that the equals() and hashCode() methods ensure that the
     * default tree model will consider similars node as being equal.
     * This is required for keeping expanded nodes expanded even after
     * the entire model has been rebuilt.</p>
     */
    protected class Node extends DefaultMutableTreeNode {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** The extra data. */
        private Object[] extraData;

        /**
         * Creates a Node.
         */
        public Node() {
            this(null, true);
        } // Node

        /**
         * Creates a Node with the given user object.
         *
         * @param  userObject  user object.
         */
        public Node(Object userObject) {
            this(userObject, true);
        } // Node

        /**
         * Creates a Node with the given user object.
         *
         * @param  userObject      user object.
         * @param  allowsChildren  true if allows children, false otherwise.
         */
        public Node(Object userObject, boolean allowsChildren) {
            super(userObject, allowsChildren);
            extraData = new Object[numberOfColumns];
        } // Node

        /**
         * Tests two Nodes for equality, returning true only if they are
         * equivalent. We compare on the first column of extra data,
         * assuming that it is our key.
         *
         * @param  o  object to compare <code>this</code> to.
         * @return  true if equal, false otherwise.
         */
        public boolean equals(Object o) {
            if (o instanceof Node) {
                Node n = (Node) o;
                if (extraData.length > 0
                    && extraData.length == n.extraData.length) {
                    Object o1 = extraData[0];
                    Object o2 = n.extraData[0];
                    if (o1 == o2) {
                        return true;
                    }
                    if (o1 != null && o2 != null) {
                        return o1.equals(o2);
                    }
                }
            }
            return false;
        } // equals

        /**
         * Retrieve the child whose value at the given column equals
         * the given value.
         *
         * @param  column  column of data to compare.
         * @param  value   value to compare to.
         * @return  child, or null if not found.
         */
        public TreeNode getChild(int column, Object value) {
            if (children != null) {
                synchronized (children) {
                    int count = children.size();
                    for (int ii = 0; ii < count; ii++) {
                        Node child = (Node) children.get(ii);
                        if (child != null) {
                            Object o = child.getData(column);
                            if (o != null && o.equals(value)) {
                                return child;
                            }
                        }
                    }
                }
            }
            return null;
        } // getChild

        /**
         * Retrieve the extra data for the given column.
         *
         * @param  column  column within table model.
         * @return  data for that column, possibly null.
         */
        public Object getData(int column) {
            return extraData[column];
        } // getData

        /**
         * Computes the hash value for this object. The hash code is
         * based on the element stored in the first column of extra
         * data. If this element is null, 1 is returned.
         *
         * @return  hash value, suitable for hash tables.
         */
        public int hashCode() {
            if (extraData.length > 0 && extraData[0] != null) {
                return extraData[0].hashCode();
            }
            return 1;
        } // equals

        /**
         * Replaces the element at the specified position with the given
         * data.
         *
         * @param  column  column within table model.
         * @param  data    data to be stored.
         */
        public void setData(int column, Object data) {
            extraData[column] = data;
        } // setData

        /**
         * Return our string representation. This just happens to be
         * the string value of whatever is stored in the first column.
         *
         * @return  string representation.
         */
        public String toString() {
            if (extraData.length > 0) {
                return extraData[0] == null ? "" : extraData[0].toString();
            } else {
                return "(error)";
            }
        } // toString
    } // Node
} // ViewTreeTableModel
