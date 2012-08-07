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
 * The Original Software is JSwat Installer. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.installer;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Displays the home directory selection panel.
 *
 * @author Nathan Fiedler
 */
public class HomePanel extends InstallerPanel {

    /**
     * silence compiler warnings
     */
    private static final long serialVersionUID = 1L;
    /**
     * True if home has been validated as okay, false otherwise.
     */
    private volatile boolean homeOkay;
    /**
     * List of paths where we should try to install.
     */
    private static final String[] CANDIDATE_PATH_LIST = {
        "C:\\Program Files",
        "D:\\Program Files",
        "/Applications",
        "/opt",
        "/usr/local",};

    /**
     * Creates new form HomePanel.
     */
    public HomePanel() {
        initComponents();
        // Use a sensible default that is guaranteed to always work.
        String home = System.getProperty("user.home");
        // But then try to find a better path.
        for (int ii = 0; ii < CANDIDATE_PATH_LIST.length; ii++) {
            File dir = new File(CANDIDATE_PATH_LIST[ii]);
            if (dir.exists()) {
                if (dir.isDirectory()) {
                    try {
                        File file = File.createTempFile("jswat", null, dir);
                        file.delete();
                        home = CANDIDATE_PATH_LIST[ii];
                        break;
                    } catch (Exception e) {
                        // I/O or security exceptions indicates we are
                        // not permitted to write this directory.
                    }
                }
            }
        }
        String version = getVersion();
        home += File.separator + "jswat-" + version;
        validateDirectory(home);
        homeTextField.setText(home);
        homeTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                String path = homeTextField.getText();
                validateDirectory(path);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String path = homeTextField.getText();
                validateDirectory(path);
            }
        });
        browseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                String home = homeTextField.getText();
                JFileChooser jfc = new JFileChooser(home);
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                Window window = SwingUtilities.getWindowAncestor(browseButton);
                int response = jfc.showOpenDialog(window);
                if (response == JFileChooser.APPROVE_OPTION) {
                    File dir = jfc.getSelectedFile();
                    String msg = validateDirectory(dir);
                    if (msg == null) {
                        homeTextField.setText(dir.getAbsolutePath());
                        messageLabel.setText("");
                    } else {
                        messageLabel.setText(msg);
                    }
                }
            }
        });
    }

    @Override
    public void doHide() {
        Controller.getDefault().setProperty("home", homeTextField.getText());
    }

    @Override
    public void doShow() {
        String path = homeTextField.getText();
        validateDirectory(path);
    }

    @Override
    public String getNext() {
        if (homeOkay) {
            return "review";
        } else {
            // When there is a message we cannot proceed.
            return null;
        }
    }

    @Override
    public String getPrevious() {
        return "jdk";
    }

    /**
     * Get the version number of the product from the properties file.
     *
     * @return version number, or null if file is missing.
     */
    private String getVersion() {
        String version = null;
        Properties props = new Properties();
        InputStream is = ClassLoader.getSystemResourceAsStream("version.properties");
        try {
            props.load(is);
            version = props.getProperty("version");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return version;
    }

    /**
     * Validate the given path and set the message label appropriately.
     *
     * @param path selected home location.
     */
    private void validateDirectory(String path) {
        String msg = validateDirectory(new File(path));
        if (msg != null) {
            messageLabel.setText(msg);
        } else {
            messageLabel.setText("");
        }
        // Force an update of the Next button.
        Controller.getDefault().markBusy(false);
    }

    /**
     * Validates the given directory and returns an error message if there is a
     * problem with the location.
     *
     * @param dir selected home directory.
     * @return error message, or null if dir is suitable.
     */
    private String validateDirectory(File dir) {
        String msg = null;
        File parent = dir.getParentFile();
        if (dir.exists()) {
            if (dir.isDirectory()) {
                try {
                    File file = File.createTempFile("jswat", null, dir);
                    file.delete();
                    // Ensure that the existing directory is empty.
                    String[] contents = dir.list();
                    if (contents != null && contents.length > 0) {
                        msg = Bundle.getString("MSG_Home_DirNotEmpty");
                    }
                } catch (Exception e) {
                    // I/O or security exceptions indicates we are
                    // not permitted to write this directory.
                    msg = Bundle.getString("MSG_Home_BadLocation");
                }
            } else {
                msg = Bundle.getString("MSG_Home_IsAFile");
            }
        } else if (parent == null) {
            msg = Bundle.getString("MSG_Home_BadLocation");
        } else {
            try {
                File file = File.createTempFile("jswat", null, parent);
                file.delete();
            } catch (Exception e) {
                // I/O or security exceptions indicates we are
                // not permitted to write this directory.
                msg = Bundle.getString("MSG_Home_BadLocation");
            }
        }
        homeOkay = msg == null;
        return msg;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        selectTextArea = new javax.swing.JTextArea();
        spacer1Panel = new javax.swing.JPanel();
        homeLabel = new javax.swing.JLabel();
        homeTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        spacer2Panel = new javax.swing.JPanel();
        messageLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        selectTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        selectTextArea.setLineWrap(true);
        selectTextArea.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("LBL_Home_Select_Text"));
        selectTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(selectTextArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.4;
        add(spacer1Panel, gridBagConstraints);

        homeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("KEY_Home_Home").charAt(0));
        homeLabel.setLabelFor(homeTextField);
        homeLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("LBL_Home_Home_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(homeLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(homeTextField, gridBagConstraints);

        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("KEY_Home_Browse").charAt(0));
        browseButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("LBL_Home_Browse_Button"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(browseButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.6;
        add(spacer2Panel, gridBagConstraints);

        messageLabel.setForeground(new java.awt.Color(102, 153, 255));
        messageLabel.setText("   ");
        messageLabel.setFocusable(false);
        messageLabel.setPreferredSize(new java.awt.Dimension(100, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(messageLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel homeLabel;
    private javax.swing.JTextField homeTextField;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JTextArea selectTextArea;
    private javax.swing.JPanel spacer1Panel;
    private javax.swing.JPanel spacer2Panel;
    // End of variables declaration//GEN-END:variables
}
