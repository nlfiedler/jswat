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
 * $Id: JSwatTable.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Class JSwatTable extends JTable with some application-specific
 * behavior.
 *
 * @author  Nathan Fiedler
 */
public class JSwatTable extends JTable {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a default <code>JTable</code> that is initialized with
     * a default data model, a default column model, and a default
     * selection model.
     */
    public JSwatTable() {
        super();
    } // JSwatTable

    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, a default column model, and a
     * default selection model.
     *
     * @param  dm  the data model for the table.
     */
    public JSwatTable(TableModel dm) {
        super(dm);
    } // JSwatTable

    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, <code>cm</code> as the column
     * model, and a default selection model.
     *
     * @param  dm  the data model for the table.
     * @param  cm  the column model for the table.
     */
    public JSwatTable(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
    } // JSwatTable

    /**
     * Sets the row selection model for this table to <code>newModel</code>
     * and registers for listener notifications from the new selection
     * model.
     *
     * @param  newModel  the new selection model; if <code>null</code>,
     *                   no selections are permitted.
     * @see  #getSelectionModel
     */
    public void setSelectionModel(ListSelectionModel newModel) {
        if (newModel == null) {
            newModel = EmptyListSelectionModel.sharedInstance();
        }
        super.setSelectionModel(newModel);
    } // setSelectionModel
} // JSwatTable
