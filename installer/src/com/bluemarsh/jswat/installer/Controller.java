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

import java.awt.Cursor;
import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manages the flow of the installer panels.
 *
 * @author Nathan Fiedler
 */
public class Controller {
    /** Our singleton instance. */
    private static final Controller instance;
    /** The main window. */
    private MainFrame mainFrame;
    /** Map of InstallerPanel instances, keyed by String name. */
    private Map<String, InstallerPanel> panelNames;
    /** The currently visible panel. */
    private InstallerPanel currentPanel;
    /** Installation properties. */
    private Properties installProps;

    static {
        instance = new Controller();
    }

    /**
     * Creates a new instance of Controller.
     */
    private Controller() {
        panelNames = new HashMap<String, InstallerPanel>();
        installProps = new Properties();
    }

    /**
     * Moves to the previous panel.
     */
    public void back() {
        String name = currentPanel.getPrevious();
        switchPanels(name);
    }

    /**
     * Set the enabled state of the control flow buttons according to what
     * the current panel will allow.
     */
    private void enableButtons() {
        boolean enable = currentPanel.getNext() != null;
        mainFrame.setNextEnabled(enable);
        enable = currentPanel.getPrevious() != null;
        mainFrame.setBackEnabled(enable);
    }

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return  singleton instance.
     */
    public static Controller getDefault() {
        return instance;
    }

    /**
     * Retrieves the instance of the named panel.
     *
     * @param  name  name of panel to acquire.
     * @return  the panel, or null if error.
     */
    private InstallerPanel getPanel(String name) {
        InstallerPanel panel = panelNames.get(name);
        if (panel == null) {
            // Locate panel class and instantiate it.
            if (name.equals("welcome")) {
                panel = new WelcomePanel();
            } else if (name.equals("license")) {
                panel = new LicensePanel();
            } else if (name.equals("jdk")) {
                panel = new JdkPanel();
            } else if (name.equals("home")) {
                panel = new HomePanel();
            } else if (name.equals("review")) {
                panel = new ReviewPanel();
            } else if (name.equals("progress")) {
                panel = new ProgressPanel();
            } else if (name.equals("summary")) {
                panel = new SummaryPanel();
            }
            panelNames.put(name, panel);
            panel.setName(name);
            mainFrame.addPanel(panel);
        }
        return panel;
    }

    /**
     * Retrieves the named property value.
     *
     * @param  key  name of the property.
     * @return  property value, or null if not found.
     */
    public String getProperty(String key) {
        return installProps.getProperty(key);
    }

    /**
     * Indicate whether the current panel is busy. Changes the mouse pointer
     * and the enabled state of the control flow buttons appropriately.
     *
     * @param  busy  true if busy, false if no longer busy.
     */
    public void markBusy(boolean busy) {
        if (busy) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            mainFrame.setCursor(cursor);
            mainFrame.setBackEnabled(false);
            mainFrame.setNextEnabled(false);
            mainFrame.setExitEnabled(false);
        } else {
            Cursor cursor = Cursor.getDefaultCursor();
            mainFrame.setCursor(cursor);
            enableButtons();
            mainFrame.setExitEnabled(true);
        }
    }

    /**
     * Indicate that the installation has finished by disabling the back
     * and next buttons, enabling the exit button, and setting its label
     * to "Finish".
     */
    public void markCompleted() {
        mainFrame.setBackEnabled(false);
        mainFrame.setNextEnabled(false);
        mainFrame.setExitLabel(Bundle.getString("LBL_Finish_Button"));
        mainFrame.setExitEnabled(true);
    }

    /**
     * Moves to the next panel.
     */
    public void next() {
        if (currentPanel.canProceed()) {
            String name = currentPanel.getNext();
            switchPanels(name);
        }
    }

    /**
     * Sets a property with the given name and value.
     *
     * @param  key    name of the property.
     * @param  value  the property value (null to delete).
     */
    public void setProperty(String key, String value) {
        installProps.setProperty(key, value);
    }

    /**
     * Begins the flow of the installer panels, starting with showing the
     * main window and setting up its initial state.
     */
    public void start() {
        mainFrame = new MainFrame();
        currentPanel = getPanel("welcome");
        mainFrame.showPanel(currentPanel);
        enableButtons();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainFrame.setLocationRelativeTo(null);
                mainFrame.setVisible(true);
            }
        });
    }

    /**
     * Closes the main window and exits the installer.
     */
    public void stop() {
        currentPanel.cancelInstall();
        mainFrame.dispose();
        System.exit(0);
    }

    /**
     * Hide the current panel and show the named panel, if found.
     *
     * @param  name  name of the panel to show.
     */
    private void switchPanels(String name) {
        InstallerPanel panel = getPanel(name);
        if (panel != null) {
            currentPanel.doHide();
            currentPanel = panel;
            mainFrame.showPanel(panel);
            panel.doShow();
            enableButtons();
        }
    }
}
