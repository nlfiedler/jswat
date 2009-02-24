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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.installer;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * Displays the welcome screen of the installer.
 *
 * @author  Nathan Fiedler
 */
public class WelcomePanel extends InstallerPanel {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates new form WelcomePanel.
     */
    public WelcomePanel() {
        initComponents();

        // Set up the text pane styles.
        StyledDocument doc = welcomeTextPane.getStyledDocument();
        addStylesToDocument(doc);

        // Populate the text pane with styled text.
        try {
            String msg = Bundle.getString("MSG_Welcome_Title");
            doc.insertString(doc.getLength(), msg, doc.getStyle("large"));
            msg = Bundle.getString("MSG_Welcome_Body");
            doc.insertString(doc.getLength(), msg, doc.getStyle("regular"));
        } catch (BadLocationException ble) {
            // This would be entirely unexpected.
            ble.printStackTrace();
        }
    }

    /**
     * Set up the styles for the styled document.
     *
     * @param  doc  styled document to prepare.
     */
    private void addStylesToDocument(StyledDocument doc) {
        Style def = StyleContext.getDefaultStyleContext().getStyle(
                StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("large", regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setFontSize(s, 16);
    }

    @Override
    public void doHide() {
    }

    @Override
    public void doShow() {
    }

    @Override
    public String getNext() {
        return "license";
    }

    @Override
    public String getPrevious() {
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        welcomeTextPane = new javax.swing.JTextPane();

        setLayout(new java.awt.BorderLayout());

        welcomeTextPane.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        welcomeTextPane.setEditable(false);
        welcomeTextPane.setFocusable(false);
        welcomeTextPane.setMargin(new java.awt.Insets(12, 12, 12, 12));
        add(welcomeTextPane, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane welcomeTextPane;
    // End of variables declaration//GEN-END:variables
}
