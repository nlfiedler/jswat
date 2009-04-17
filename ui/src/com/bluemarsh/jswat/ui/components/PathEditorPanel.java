/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.components;

import com.bluemarsh.jswat.core.CoreSettings;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Class PathEditorPanel constructs a panel for editing a classpath-type
 * property, made up of source folders and archive files.
 *
 * @author  Nathan Fiedler
 */
public class PathEditorPanel extends JPanel implements ActionListener,
        ListSelectionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The directory that was last selected by the user. Initially this
     * is null and the file chooser will open to a default location. */
    private static File lastOpenedDirectory;
    /** The path list model. */
    private DefaultListModel listModel;
    /** True if this editor allows the user to modify the path. */
    private boolean writable = true;

    /**
     * Creates new form PathEditorPanel; by default it is writable.
     */
    public PathEditorPanel() {
        initComponents();
        pathList.addListSelectionListener(this);
        listModel = new DefaultListModel();
        pathList.setModel(listModel);
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        moveUpButton.addActionListener(this);
        moveDownButton.addActionListener(this);
        bulkAddButton.addActionListener(this);
        // Allow the user to choose directories and .zip/.jar archives.
        fileChooser.setFileFilter(new PathFilter());
        // Prevent user from adding random files to the path as that
        // causes exceptions in the path manager (bug 978).
        fileChooser.setAcceptAllFileFilterUsed(false);
        CoreSettings cs = CoreSettings.getDefault();
        boolean hideFiles = !cs.getShowHiddenFiles();
        fileChooser.setFileHidingEnabled(hideFiles);
        updateButtons();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == addButton) {
            Frame frame = WindowManager.getDefault().getMainWindow();
            // Needs to happen every time, as the value may change if there
            // are multiple instances of this editor opened at the same time.
            fileChooser.setCurrentDirectory(lastOpenedDirectory);
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                for (File file : files) {
                    listModel.add(listModel.size(), file.getAbsolutePath());
                }
                if (listModel.size() == 1) {
                    pathList.setSelectedIndex(0);
                }
                lastOpenedDirectory = fileChooser.getCurrentDirectory();
            }

        } else if (src == bulkAddButton) {
            PathAdderPanel pap = new PathAdderPanel();
            if (pap.display()) {
                List<String> paths = pap.getPaths();
                if (paths != null) {
                    for (String path : paths) {
                        listModel.add(listModel.size(), path);
                    }
                }
            }

        } else if (src == removeButton) {
            int[] rows = pathList.getSelectedIndices();
            if (rows.length > 0) {
                // Must process these in reverse order.
                for (int ii = rows.length - 1; ii >= 0; ii--) {
                    listModel.remove(rows[ii]);
                }
                int size = listModel.size();
                if (size > 0) {
                    if (size > rows[rows.length - 1]) {
                        pathList.setSelectedIndex(rows[rows.length - 1]);
                    } else {
                        pathList.setSelectedIndex(size - 1);
                    }
                }
            }

        } else if (src == moveUpButton) {
            int[] rows = pathList.getSelectedIndices();
            if (rows.length > 0) {
                if (rows[0] >= 1) {
                    // Move from top to bottom.
                    for (int ii = 0; ii < rows.length; ii++) {
                        Object a = listModel.get(rows[ii]);
                        Object b = listModel.get(rows[ii] - 1);
                        listModel.set(rows[ii] - 1, a);
                        listModel.set(rows[ii], b);
                        // Adjust so later we can set the selected indices.
                        rows[ii]--;
                    }
                    pathList.setSelectedIndices(rows);
                }
            }

        } else if (src == moveDownButton) {
            int[] rows = pathList.getSelectedIndices();
            if (rows.length > 0) {
                if (rows[rows.length - 1] < (listModel.size() - 1)) {
                    // Move from bottom to top.
                    for (int ii = rows.length - 1; ii >= 0; ii--) {
                        Object a = listModel.get(rows[ii]);
                        Object b = listModel.get(rows[ii] + 1);
                        listModel.set(rows[ii] + 1, a);
                        listModel.set(rows[ii], b);
                        // Adjust so later we can set the selected indices.
                        rows[ii]++;
                    }
                    pathList.setSelectedIndices(rows);
                }
            }
        }

        updateButtons();
    }

    /**
     * Adds a list data listener to the list model this panel manages.
     *
     * @param  l  list data listener.
     */
    public void addListDataListener(ListDataListener l) {
        listModel.addListDataListener(l);
    }

    /**
     * Returns the current list of path elements, in the order in which they
     * appear in the list.
     *
     * @return  list of path elements, may be empty, but never null.
     */
    public List<String> getPath() {
        List<String> list = new LinkedList<String>();
        Enumeration entries = listModel.elements();
        while (entries.hasMoreElements()) {
            Object entry = entries.nextElement();
            list.add(entry.toString());
        }
        return list;
    }

    /**
     * Removes the list data listener from the list model this panel manages.
     *
     * @param  l  list data listener.
     */
    public void removeListDataListener(ListDataListener l) {
        listModel.removeListDataListener(l);
    }

    /**
     * Set the list of path elements that this editor will be editing.
     *
     * @param  path  list of path elements, in order of appearance.
     */
    public void setPath(List<String> path) {
        listModel.clear();
        if (path != null) {
            for (String entry : path) {
                listModel.add(listModel.size(), entry);
            }
        }
        if (writable) {
            pathList.setSelectedIndex(0);
            updateButtons();
        }
    }

    /**
     * Set the path editor to writable or read-only state.
     *
     * @param  writable  true to allow user to edit the path.
     */
    public void setWritable(boolean writable) {
        if (this.writable != writable) {
            // Do this only if there is a change in the writability.
            if (writable) {
                addButton.addActionListener(this);
                removeButton.addActionListener(this);
                moveUpButton.addActionListener(this);
                moveDownButton.addActionListener(this);
                bulkAddButton.addActionListener(this);
            } else {
                addButton.removeActionListener(this);
                removeButton.removeActionListener(this);
                moveUpButton.removeActionListener(this);
                moveDownButton.removeActionListener(this);
                bulkAddButton.removeActionListener(this);
            }
            this.writable = writable;
            updateButtons();
        }
    }

    /**
     * Update the enabled state of the buttons according to the list
     * contents, the selected list index, and the writability.
     */
    private void updateButtons() {
        if (writable) {
            // Add buttons should always be enabled in this case.
            addButton.setEnabled(true);
            bulkAddButton.setEnabled(true);

            // Control the enabled state of the remove button.
            if (listModel.size() == 0) {
                removeButton.setEnabled(false);
            } else {
                removeButton.setEnabled(true);
            }

            // Update the move up/down buttons.
            int[] rows = pathList.getSelectedIndices();
            if (rows.length == 0) {
                moveUpButton.setEnabled(false);
                moveDownButton.setEnabled(false);
                removeButton.setEnabled(false);
            } else {
                int listSize = listModel.size();
                moveUpButton.setEnabled(rows[0] >= 1);
                moveDownButton.setEnabled(rows[rows.length - 1] < (listSize - 1));
            }
        } else {
            addButton.setEnabled(false);
            removeButton.setEnabled(false);
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
            bulkAddButton.setEnabled(false);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
        // Enable/disable the move buttons depending on the selection.
        if (event == null || !event.getValueIsAdjusting() && writable) {
            updateButtons();
        }
    }

    /**
     * Class PathFilter implements a FileFilter that only accepts directories
     * and jar or zip files.
     */
    private static class PathFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            } else {
                String name = f.getName().toLowerCase();
                return name.endsWith(".zip") || name.endsWith(".jar");
            }
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(PathEditorPanel.class, "LBL_PathFilter");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        pathListScrollPane = new javax.swing.JScrollPane();
        pathList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        bulkAddButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms"); // NOI18N
        fileChooser.setDialogTitle(bundle.getString("LBL_FileChooser")); // NOI18N
        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);

        pathListScrollPane.setMinimumSize(new java.awt.Dimension(200, 100));

        pathList.setToolTipText(bundle.getString("TIP_PathList")); // NOI18N
        pathListScrollPane.setViewportView(pathList);

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_PathEditor_Add").charAt(0));
        addButton.setText(bundle.getString("LBL_AddButton")); // NOI18N
        addButton.setToolTipText(bundle.getString("TIP_AddButton")); // NOI18N

        removeButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_PathEditor_Remove").charAt(0));
        removeButton.setText(bundle.getString("LBL_RemoveButton")); // NOI18N
        removeButton.setToolTipText(bundle.getString("TIP_RemoveButton")); // NOI18N

        moveUpButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_PathEditor_MoveUp").charAt(0));
        moveUpButton.setText(bundle.getString("LBL_MoveUpButton")); // NOI18N
        moveUpButton.setToolTipText(bundle.getString("TIP_MoveUpButton")); // NOI18N

        moveDownButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_PathEditor_MoveDown").charAt(0));
        moveDownButton.setText(bundle.getString("LBL_MoveDownButton")); // NOI18N
        moveDownButton.setToolTipText(bundle.getString("TIP_MoveDownButton")); // NOI18N

        bulkAddButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_PathEditor_BulkAdd").charAt(0));
        bulkAddButton.setText(bundle.getString("LBL_Paths_BulkAdd")); // NOI18N
        bulkAddButton.setToolTipText(bundle.getString("TIP_Paths_BulkAdd")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(pathListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(moveUpButton)
                    .addComponent(moveDownButton)
                    .addComponent(bulkAddButton))
                .addGap(0, 0, 0))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, bulkAddButton, moveDownButton, moveUpButton, removeButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveUpButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveDownButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bulkAddButton))
                    .addComponent(pathListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton bulkAddButton;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JList pathList;
    private javax.swing.JScrollPane pathListScrollPane;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
