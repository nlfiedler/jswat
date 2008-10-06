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
 * $Id: ViewTableModel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for displaying generic object data. The model is
 * synchronized for thread safety.
 *
 * @author  Nathan Fiedler
 */
class ViewTableModel extends AbstractTableModel {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Names of the columns, in order. Set in the constructor. */
    private String[] columnNames;
    /** The total number of columns, including invisible ones. */
    private int numberOfColumns;
    /** Array of row values. Each entry is a ArrayList of column values.
     * Requesting rowValues.get(2).get(3) will get the value for the cell
     * at row 3, column 4. */
    private ArrayList rowValues;
    /** If non-null, indicates which columns are editable. */
    private boolean[] editableColumns;

    /**
     * Constructs an empty ViewTableModel object. The number of
     * columns in the table is determined by the length of the
     * column names array.
     *
     * @param  colNames  names for the columns.
     */
    public ViewTableModel(String[] colNames) {
        this(colNames, colNames.length, null);
    } // ViewTableModel

    /**
     * Constructs an empty ViewTableModel object. The number of
     * columns in the table is determined by the length of the
     * column names array.
     *
     * @param  colNames  names for the columns.
     * @param  numCols   number of columns, including invisibles.
     */
    public ViewTableModel(String[] colNames, int numCols) {
        this(colNames, numCols, null);
    } // ViewTableModel

    /**
     * Constructs an empty ViewTableModel object. The number of
     * columns in the table is determined by the length of the
     * column names array.
     *
     * @param  colNames      names for the columns.
     * @param  editableCols  Indicates which columns are editable.
     *                       Each entry corresponds to a column. A true
     *                       value means the column is editable.
     */
    public ViewTableModel(String[] colNames, boolean[] editableCols) {
        this(colNames, colNames.length, editableCols);
    } // ViewTableModel

    /**
     * Constructs an empty ViewTableModel object. The number of
     * columns in the table is determined by the length of the
     * column names array.
     *
     * @param  colNames      names for the columns.
     * @param  numCols       number of columns, including invisibles.
     * @param  editableCols  Indicates which columns are editable.
     *                       Each entry corresponds to a column. A true
     *                       value means the column is editable.
     */
    public ViewTableModel(String[] colNames, int numCols,
                          boolean[] editableCols) {
        columnNames = colNames;
        rowValues = new ArrayList();
        numberOfColumns = numCols;
        editableColumns = editableCols;
    } // ViewTableModel

    /**
     * Adds a new row to the table and returns the new row index.
     * This does not fire a table change event.
     *
     * @return  Zero-based index of newly created row.
     */
    public int addRow() {
        // Create a new, empty row object.
        ArrayList rowValue = new ArrayList();
        int add = numberOfColumns;
        while (add > 0) {
            // Add null elements until we reach the required size.
            rowValue.add(null);
            add--;
        }
        synchronized (rowValues) {
            rowValues.add(rowValue);
            // Do not fire a change yet, let the caller do that.
            return rowValues.size() - 1;
        }
    } // addRow

    /**
     * Clears the entire table of all data.
     * This fires a table change event.
     */
    public void clear() {
        int size;
        synchronized (rowValues) {
            size = rowValues.size();
            rowValues.clear();
        }
        if (size > 0) {
            fireTableRowsDeleted(0, size - 1);
        }
    } // clear

    /**
     * Increases the capacity of this ViewTableModel instance, if
     * necessary, to ensure that it can hold at least the number of
     * elements specified by the minimum capacity argument.
     *
     * @param  minCapacity  the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity) {
        rowValues.ensureCapacity(minCapacity);
    } // ensureCapacity

    /**
     * Finds the row number that contains the given value in the
     * specified column.
     *
     * @param  val  Value to search for.
     * @param  col  Column in which to find the value.
     * @return  Row number of found value, or -1 if not found.
     */
    protected int findRow(Object val, int col) {
        // Scan all the rows.
        int rowIndex = -1;
        synchronized (rowValues) {
            int rowCount = rowValues.size();
            for (int i = 0; i < rowCount; i++) {
                ArrayList rowValue = (ArrayList) rowValues.get(i);
                // For each row, check the given column for a value
                // equal to val.
                Object o = rowValue.get(col);
                if ((o != null) && (o.equals(val))) {
                    // Found an equal value, we'll delete this row.
                    rowIndex = i;
                    break;
                }
            }
        }
        return rowIndex;
    } // findRow

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
        if (column < getColumnCount()) {
            return columnNames[column];
        } else {
            return "(error)";
        }
    } // getColumnName

    /**
     * Returns the number of rows managed by the data source object.
     *
     * @return  Number of table rows.
     */
    public int getRowCount() {
        return rowValues.size();
    } // getRowCount

    /**
     * Gets the value for the record in the cell at columnIndex
     * and rowIndex.
     *
     * @param  rowIndex     the row whose value is to be changed
     * @param  columnIndex  the column whose value is to be changed
     * @return  Cell value, possibly null.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        // Let's not throw an exception, since the model gets out
        // of sync with the view asynchronously and the view ends
        // up asking for a row that is no longer available.
        if ((rowIndex < 0) || (rowIndex >= rowValues.size())) {
            return null;
        }
        if ((columnIndex < 0) || (columnIndex >= numberOfColumns)) {
            return null;
        }
        ArrayList rowValue = (ArrayList) rowValues.get(rowIndex);
        if (rowValue == null) {
            throw new NullPointerException("rowValue null: get("
                                           + rowIndex + "), size = "
                                           + rowValues.size());
        }
        return rowValue.get(columnIndex);
    } // getValueAt

    /**
     * Returns true if the cell at <code>rowIndex</code> and
     * <code>columnIndex</code> is editable.
     *
     * @param  rowIndex     the row whose value to be queried
     * @param  columnIndex  the column whose value to be queried
     * @return  true if the cell is editable
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (editableColumns != null) {
            rangeCheck(rowIndex, columnIndex);
            return editableColumns[columnIndex];
        } else {
            return false;
        }
    } // isCellEditable

    /**
     * Checks both the row index and column index for out of bounds.
     *
     * @param  row     Row number to validation.
     * @param  column  Column number to validation.
     */
    protected void rangeCheck(int row, int column) {
        if ((row < 0) || (row >= rowValues.size())) {
            throw new ArrayIndexOutOfBoundsException
                ("row out of bounds: " + row);
        }
        if ((column < 0) || (column >= numberOfColumns)) {
            throw new ArrayIndexOutOfBoundsException
                ("column out of bounds: " + column);
        }
    } // rangeCheck

    /**
     * Removes a row from the table model by searching for the given
     * value in the given column. Notifies the model listeners. The
     * row count is decremented by one.
     *
     * @param  val  Value to search for.
     * @param  col  Column in which to find the value.
     */
    public void removeRow(Object val, int col) {
        synchronized (rowValues) {
            int rowIndex = findRow(val, col);
            if (rowIndex >= 0) {
                rowValues.remove(rowIndex);
                fireTableRowsDeleted(rowIndex, rowIndex);
            }
        }
    } // removeRow

    /**
     * Removes the given row from the model. Notifies the model listeners.
     * The row count is decremented by one.
     *
     * @param  row  Row to be removed.
     */
    public void removeRow(int row) {
        rangeCheck(row, 0);
        synchronized (rowValues) {
            rowValues.remove(row);
            fireTableRowsDeleted(row, row);
        }
    } // removeRow

    /**
     * Determines if a row exists in the table model by searching for
     * the given value in the specified column. Compares the values
     * with the Object.equals() method.
     *
     * @param  val  Value to search for.
     * @param  col  Column in which to find the value.
     * @return  True if a row was found, false otherwise.
     */
    public boolean rowExists(Object val, int col) {
        return findRow(val, col) >= 0;
    } // rowExists

    /**
     * Sets the table to one row and sets the 0,0 cell to the given
     * string message. It is better to keep the message short.
     * Notifies listeners that the table data has changed.
     *
     * @param  message  Message to display.
     */
    public void setMessage(String message) {
        setMessage(message, 0);
    } // setMessage

    /**
     * Sets the table to one row and sets the 0,0 cell to the given
     * string message. It is better to keep the message short.
     * Notifies listeners that the table data has changed.
     *
     * @param  message  Message to display.
     * @param  column   Column to show message in.
     */
    public void setMessage(String message, int column) {
        synchronized (rowValues) {
            clear();
            setValueNoEvent(message, addRow(), column);
            fireTableDataChanged();
        }
    } // setMessage

    /**
     * Sets an attribute value for the record in the cell at 'col'
     * and 'row'. The 'val' is the new value. This notifies
     * listeners that the cell data has changed.
     *
     * @param  val  the new value
     * @param  row  the row whose value is to be changed
     * @param  col  the column whose value is to be changed
     */
    public void setValueAt(Object val, int row, int col) {
        setValueNoEvent(val, row, col);
        fireTableCellUpdated(row, col);
    } // setValueAt

    /**
     * Sets an attribute value for the record in the cell at 'col'
     * and 'row'. The 'val' is the new value. This does not notify
     * listeners that the cell data has changed.
     *
     * @param  val  the new value
     * @param  row  the row whose value is to be changed
     * @param  col  the column whose value is to be changed
     */
    public void setValueNoEvent(Object val, int row, int col) {
        rangeCheck(row, col);
        synchronized (rowValues) {
            ArrayList rowValue = (ArrayList) rowValues.get(row);
            rowValue.set(col, val);
        }
    } // setValueNoEvent
} // ViewTableModel
