/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: PathEditorPanel.java 15 2007-06-03 00:01:17Z nfiedler $
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
public class PathEditorPanel extends javax.swing.JPanel
    implements ActionListener, ListSelectionListener {
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

    /**
     * Invoked when an action occurs.
     *
     * @param  event  action event.
     */
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
     * Set the list of path elements that this editor will be editing.
     *
     * @param  path  list of path elements, in order of appearance.
     */
    public void setPath(List<String> path) {
        listModel.clear();
        for (String entry : path) {
            listModel.add(listModel.size(), entry);
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

    /**
     * Called whenever the value of the selection changes.
     *
     * @param  event  list selection event.
     */
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

        /**
         * Test if the given file or directory is acceptable.
         *
         * @param  f  file to consider.
         * @return  true if file is acceptable.
         */
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            } else {
                String name = f.getName().toLowerCase();
                return name.endsWith(".zip") || name.endsWith(".jar");
            }
        }

        /**
         * Returns the description of this file filter.
         *
         * @return  String description of this filter.
         */
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

        fileChooser.setDialogTitle(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_FileChooser"));
        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);

        pathListScrollPane.setMinimumSize(new java.awt.Dimension(200, 100));
        pathList.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_PathList"));
        pathListScrollPane.setViewportView(pathList);

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_PathEditor_Add").charAt(0));
        addButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_AddButton"));
        addButton.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_AddButton"));

        removeButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_PathEditor_Remove").charAt(0));
        removeButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_RemoveButton"));
        removeButton.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_RemoveButton"));

        moveUpButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_PathEditor_MoveUp").charAt(0));
        moveUpButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_MoveUpButton"));
        moveUpButton.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_MoveUpButton"));

        moveDownButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_PathEditor_MoveDown").charAt(0));
        moveDownButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_MoveDownButton"));
        moveDownButton.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_MoveDownButton"));

        bulkAddButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("KEY_PathEditor_BulkAdd").charAt(0));
        bulkAddButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_Paths_BulkAdd"));
        bulkAddButton.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("TIP_Paths_BulkAdd"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(pathListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, moveUpButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, moveDownButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, bulkAddButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(moveUpButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(moveDownButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bulkAddButton))
                    .add(pathListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
                .addContainerGap())
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
