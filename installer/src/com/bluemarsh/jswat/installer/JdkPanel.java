/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License 
 * Version 1.0 (the "License"); you may not use this file except in 
 * compliance with the License. A copy of the License is available at 
 * http://www.sun.com/
 *
 * The Original Code is JSwat Installer. The Initial Developer of the 
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: JdkPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.installer;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Displays the JDK selection panel.
 *
 * @author  Nathan Fiedler
 */
public class JdkPanel extends InstallerPanel implements
        ActionListener, DocumentListener, Runnable {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** True if JDK has been validated as okay, false otherwise. */
    private volatile boolean jdkOkay;

    /**
     * Creates new form JdkPanel.
     */
    public JdkPanel() {
        initComponents();
        String home = System.getProperty("java.home");
        if (home.endsWith("jre")) {
            // Trim the "/jre" part from the path.
            home = home.substring(0, home.length() - 4);
        }
        validateDirectory(home);
        homeTextField.setText(home);
        homeTextField.getDocument().addDocumentListener(this);
        browseButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == browseButton) {
            String home = homeTextField.getText();
            JFileChooser jfc = new JFileChooser(home);
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            Window window = SwingUtilities.getWindowAncestor(this);
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
    }

    public boolean canProceed() {
        if (!jdkOkay) {
            messageLabel.setText(Bundle.getString("MSG_Jdk_Verifying"));
            new Thread(this).start();
            return false;
        } else {
            return true;
        }
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void doHide() {
        Controller.getDefault().setProperty("jdk", homeTextField.getText());
    }

    public void doShow() {
        jdkOkay = false;
        String path = homeTextField.getText();
        validateDirectory(path);
    }

    public String getNext() {
        return "home";
    }

    public String getPrevious() {
        return "license";
    }

    public void insertUpdate(DocumentEvent e) {
        String path = homeTextField.getText();
        validateDirectory(path);
    }

    public void removeUpdate(DocumentEvent e) {
        String path = homeTextField.getText();
        validateDirectory(path);
    }

    public void run() {
        File dir = new File(homeTextField.getText());
        final JdkVerifier verifier = new JdkVerifier();
        verifier.scanPath(dir);
        jdkOkay = verifier.hasDebugInterface() && verifier.sufficientVersion();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (jdkOkay) {
                    messageLabel.setText("");
                } else {
                    if (!verifier.sufficientVersion()) {
                        showVersionWarning();
                    } else if (!verifier.hasDebugInterface()) {
                        showJdiWarning();
                    }
                }
            }
        });
        if (jdkOkay) {
            Controller.getDefault().next();
        }
    }

    /**
     * Displays a dialog asking the user if they want to revise the selected
     * JDK since it is apparently missing the JDI clases.
     */
    private void showJdiWarning() {
        String[] messages = new String[] {
            Bundle.getString("MSG_Jdk_MissingJDI_1"),
            Bundle.getString("MSG_Jdk_MissingJDI_2"),
            Bundle.getString("MSG_Jdk_MissingJDI_3"),
            Bundle.getString("MSG_Jdk_MissingJDI_4"),
            Bundle.getString("MSG_Jdk_MissingJDI_5"),
            Bundle.getString("MSG_Jdk_MissingJDI_6")
        };
        String title = Bundle.getString("LBL_Jdk_MissingJDI_Title");
        int opt = JOptionPane.showConfirmDialog(this, messages, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt == JOptionPane.NO_OPTION) {
            jdkOkay = true;
            Controller.getDefault().next();
        } else {
            messageLabel.setText("");
        }
    }

    /**
     * Displays a dialog asking the user if they want to revise the selected
     * JDK since it is apparently not a sufficient version for JSwat.
     */
    private void showVersionWarning() {
        String[] messages = new String[] {
            Bundle.getString("MSG_Jdk_LowVersion_1"),
            Bundle.getString("MSG_Jdk_LowVersion_2"),
            Bundle.getString("MSG_Jdk_LowVersion_3"),
            Bundle.getString("MSG_Jdk_LowVersion_4")
        };
        String title = Bundle.getString("MSG_Jdk_LowVersion_Title");
        int opt = JOptionPane.showConfirmDialog(this, messages, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt == JOptionPane.NO_OPTION) {
            jdkOkay = true;
            Controller.getDefault().next();
        } else {
            messageLabel.setText("");
        }
    }

    /**
     * Validate the given path and set the message label appropriately.
     *
     * @param  path  selected home location.
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
     * Validates the given directory and returns an error message if there
     * is a problem with the location.
     *
     * @param  dir  selected home directory.
     * @return  error message, or null if dir is suitable.
     */
    private String validateDirectory(File dir) {
        String msg = null;
        if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
            msg = Bundle.getString("MSG_Jdk_BadInstall");
        }
        return msg;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        selectTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        selectTextArea.setFont(new java.awt.Font("Dialog", 0, 12));
        selectTextArea.setLineWrap(true);
        selectTextArea.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("LBL_Jdk_Select_Text"));
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

        homeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("KEY_Jdk_Home").charAt(0));
        homeLabel.setLabelFor(homeTextField);
        homeLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("LBL_Jdk_Home_Label"));
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

        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("KEY_Jdk_Browse").charAt(0));
        browseButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("LBL_Jdk_Browse_Button"));
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

    }// </editor-fold>//GEN-END:initComponents
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
