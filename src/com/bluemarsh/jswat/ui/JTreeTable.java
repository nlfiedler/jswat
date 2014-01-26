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
 * $Id: JTreeTable.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * JTreeTable uses a JTree as the cell renderer for a JTable. The first
 * column of the table is drawn using tree nodes, while other columns
 * display additional data. Expanding the nodes in the tree will display
 * additional rows in the table.
 *
 * <p>This code is based on the tree-table examples provided by the
 * people of The Swing Connection. Thank you Philip Milne, Scott
 * Violet, and Kathy Walrath.</p>
 *
 * @author  Nathan Fiedler
 */
public class JTreeTable extends JTable {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The table cell renderer. */
    private TreeTableCellRenderer cellRenderer;
    /** The tree-table model, the real model of this component. */
    private TreeTableModel treeTableModel;

    /**
     * Constructs a JTreeTable with the given model data.
     *
     * @param  treeTableModel  model for the tree-table.
     */
    public JTreeTable(TreeTableModel treeTableModel) {
        super();
        this.treeTableModel = treeTableModel;

        // Create the cell renderer and editor.
        cellRenderer = new TreeTableCellRenderer(treeTableModel);
        setModel(new TreeTableModelAdapter(treeTableModel, cellRenderer));
        setDefaultRenderer(TreeTableModel.class, cellRenderer);
        setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());

        // Adapt the table selection to the tree, and vice versa.
        ListToTreeSelectionAdapter adapter = new ListToTreeSelectionAdapter();
        cellRenderer.setSelectionModel(adapter);
        setSelectionModel(adapter.getListSelectionModel());

        // Eliminate the gap between the cells. Without this, the rows
        // may actually intersect and that looks bad.
        setIntercellSpacing(new Dimension(0, 0));

        // Make our table look like a tree.
        setShowGrid(false);
    } // JTreeTable

    /**
     * The tree column cannot be edited, so return -1 in that case.
     *
     * @return  row being edited, or -1 if column is the tree column.
     */
    public int getEditingRow() {
        return (TreeTableModel.class.isAssignableFrom(
                    getColumnClass(editingColumn))) ? -1 : editingRow;
    } // getEditingRow

    /**
     * Returns the tree that is our renderer. Callers use this to listen
     * for tree expansion events and get the selected path.
     *
     * @return  tree cell renderer.
     */
    public JTree getTree() {
        return cellRenderer;
    } // getTree

    /**
     * Returns the tree-table model that this model delegates to.
     *
     * @return  the delegate tree-table model.
     */
    public TreeTableModel getTreeTableModel() {
        return treeTableModel;
    } // getTreeTableModel

    /**
     * Pass the new rowHeight on to the tree so the table and tree are
     * kept in sync.
     *
     * @param  rowHeight  new row height.
     */
    public void setRowHeight(int rowHeight) {
        super.setRowHeight(rowHeight);
        // Avoid making a loop with the renderer.
        if (cellRenderer != null && cellRenderer.getRowHeight() != rowHeight) {
            cellRenderer.setRowHeight(getRowHeight());
        }
    } // setRowHeight

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

    /**
     * Called when the look and feel is changing. Forward the call to
     * the cell renderer since it is not in the component tree.
     */
    public void updateUI() {
        super.updateUI();
        if (cellRenderer != null) {
            cellRenderer.updateUI();
        }
        // Make our table look like a tree.
        LookAndFeel.installColorsAndFont(this, "Tree.background",
                                         "Tree.foreground", "Tree.font");
    } // updateUI

    /**
     * A TableCellRenderer that renders the cells using a JTree instance.
     */
    public class TreeTableCellRenderer extends JTree
        implements TableCellRenderer {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Last tree-table row asked to renderer. */
        private int visibleRow;

        /**
         * Constructs a TreeTableCellRenderer with the given model.
         *
         * @param  model  tree model.
         */
        public TreeTableCellRenderer(TreeModel model) {
            super(model);
        } // TreeTableCellRenderer

        /**
         * Set the background color and update the visible row of the
         * JTree renderer.
         *
         * @param  table       tree-table being rendered.
         * @param  value       value of the cell being rendered.
         * @param  isSelected  true if cell is selected.
         * @param  hasFocus    true if cell has focus.
         * @param  row         table row.
         * @param  column      table column.
         * @return  rendering component.
         */
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            visibleRow = row;
            return this;
        } // getTableCellRendererComponent

        /**
         * Translate the graphics such that the visible row will be
         * drawn at the origin.
         *
         * @param  g  graphics context.
         */
        public void paint(Graphics g) {
            g.translate(0, -visibleRow * getRowHeight());
            super.paint(g);
        } // paint

        /**
         * Make sure our height matches that of the JTable, otherwise we
         * are shrunk down to the size of a cell.
         *
         * @param  x  x position.
         * @param  y  y position.
         * @param  w  width.
         * @param  h  height.
         */
        public void setBounds(int x, int y, int w, int h) {
            super.setBounds(x, 0, w, JTreeTable.this.getHeight());
        } // setBounds

        /**
         * Sets the row height of the tree, and forwards the row height
         * to the table.
         *
         * @param  rowHeight  new row height.
         */
        public void setRowHeight(int rowHeight) {
            if (rowHeight > 0) {
                super.setRowHeight(rowHeight);
                // Avoid making a loop with the table.
                if (JTreeTable.this != null
                    && JTreeTable.this.getRowHeight() != rowHeight) {
                    JTreeTable.this.setRowHeight(getRowHeight());
                }
            }
        } // setRowHeight

        /**
         * Make our colors look like those of the table, otherwise the
         * selection highlighter will be the same as the background.
         */
        public void updateUI() {
            super.updateUI();
            TreeCellRenderer tcr = getCellRenderer();
            if (tcr instanceof DefaultTreeCellRenderer) {
                DefaultTreeCellRenderer dtcr = (DefaultTreeCellRenderer) tcr;
                // Our tree cells do not have a selection border.
                dtcr.setBorderSelectionColor(null);
                dtcr.setTextSelectionColor(
                    UIManager.getColor("Table.selectionForeground"));
                dtcr.setBackgroundSelectionColor(
                    UIManager.getColor("Table.selectionBackground"));
                // Kill all of those nasty icons.
                dtcr.setClosedIcon(null);
                dtcr.setLeafIcon(null);
                dtcr.setOpenIcon(null);
            }
        } // updateUI
    } // TreeTableCellRenderer

    /**
     * Specialized table cell editor that ensures that mouse events are
     * forwarded to the JTree so nodes can be expanded and collapsed.
     */
    public class TreeTableCellEditor extends AbstractCellEditor
        implements TableCellEditor {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Returns the cell renderer.
         *
         * @param  table       table being edited.
         * @param  value       value being edited.
         * @param  isSelected  true if cell is selected.
         * @param  row         table row.
         * @param  column      table column.
         * @return  editor component.
         */
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row, int column) {
            return cellRenderer;
        } // getTableCellEditorComponent

        /**
         * Generally return false unless the event is a mouse event, in
         * which case it is forwarded to the tree. By returning false
         * the tree will never be editable.
         *
         * @param  e  event objectd.
         * @return  true if editable, false otherwise.
         */
        public boolean isCellEditable(EventObject e) {
            if (e instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) e;
                for (int ii = getColumnCount() - 1; ii >= 0; ii--) {
                    if (TreeTableModel.class.isAssignableFrom(
                            getColumnClass(ii))) {
                        // Has to be adjusted for each column.
                        MouseEvent newME = new MouseEvent(
                            cellRenderer, me.getID(), me.getWhen(),
                            me.getModifiers(),
                            me.getX() - getCellRect(0, ii, true).x,
                            me.getY(), me.getClickCount(),
                            me.isPopupTrigger());
                        cellRenderer.dispatchEvent(newME);
                        break;
                    }
                }
            }
            return false;
        } // isCellEditable

        /**
         * Returns the cell editor value.
         *
         * @return  always null, as we do not allow editing.
         */
        public Object getCellEditorValue() {
            return null;
        } // getCellEditorValue
    } // TreeTableCellEditor

    /**
     * ListToTreeSelectionAdapter extends DefaultTreeSelectionModel to
     * listen for changes in the ListSelectionModel it maintains. Once a
     * change in the ListSelectionModel happens, the paths are updated
     * in the DefaultTreeSelectionModel.
     */
    class ListToTreeSelectionAdapter extends DefaultTreeSelectionModel
        implements ListSelectionListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** True if the selection model is being updated. No, this is
         * not the same thing as a lock. Otherwise an update could
         * occur that would clear previous changes. */
        private boolean updatingModel;

        /**
         * Constructs a ListToTreeSelectionAdapter.
         */
        public ListToTreeSelectionAdapter() {
            super();
            // Add the selection listener that keeps the selections in sync.
            getListSelectionModel().addListSelectionListener(this);
        } // ListToTreeSelectionAdapter

        /**
         * Returns the list selection model.
         *
         * @return  list selection model.
         */
        ListSelectionModel getListSelectionModel() {
            return listSelectionModel;
        } // getListSelectionModel

        /**
         * Updates this object's mapping from tree paths to rows. This
         * should be invoked when the mapping from tree paths to rows
         * has changed (for example, a node has been expanded).
         */
        public void resetRowSelection() {
            if (!updatingModel) {
                updatingModel = true;
                try {
                    // Do this only when we are not updating the selections
                    // of the tree.
                    super.resetRowSelection();
                } finally {
                    updatingModel = false;
                }
            }
        } // resetRowSelection

        /**
         * Reset the selected paths to match the selected rows.
         */
        protected void updateSelectedPaths() {
            if (!updatingModel) {
                updatingModel = true;
                try {
                    int min = listSelectionModel.getMinSelectionIndex();
                    int max = listSelectionModel.getMaxSelectionIndex();
                    clearSelection();
                    if (min != -1 && max != -1) {
                        // Map the list selection to the tree paths.
                        for (int ii = min; ii <= max; ii++) {
                            if (listSelectionModel.isSelectedIndex(ii)) {
                                TreePath path = cellRenderer.getPathForRow(ii);
                                if (path != null) {
                                    addSelectionPath(path);
                                }
                            }
                        }
                    }
                } finally {
                    updatingModel = false;
                }
            }
        } // updateSelectedPaths

        /**
         * The list selection has changed. Update the tree selection.
         *
         * @param  e  list selection event.
         */
        public void valueChanged(ListSelectionEvent e) {
            updateSelectedPaths();
        } // valueChanged
    } // ListToTreeSelectionAdapter
} // JTreeTable
