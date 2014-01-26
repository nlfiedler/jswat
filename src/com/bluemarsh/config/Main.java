/*********************************************************************
 *
 *	Copyright (C) 1999-2005 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id: Main.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Class Main acts as a test wrapper to the JConfigure class.
 *
 * @author  Nathan Fiedler
 * @version 1.2  4/7/01
 */
public class Main implements ConfigureListener {
    /** JConfigure object. */
    protected JConfigure config;
    /** Main window. */
    protected JFrame mainFrame;

    /**
     * Main constructor.
     */
    public Main() {
        mainFrame = new JFrame("JConfigure Test");
        config = new JConfigure();
        config.addListener(this);
        addButtons();
        // Make sure we exit when the user closes the main window.
        mainFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        mainFrame.pack();
        // Center the window on the screen so the preferences dialog
        // is also centered.
        Rectangle windowBounds = mainFrame.getBounds();
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (ss.width - windowBounds.width) / 2;
        int y = (ss.height - windowBounds.height) / 2;
        mainFrame.setLocation(x, y);
    } // Main

    /**
     * Adds a couple of buttons to the window.
     */
    public void addButtons() {
        JButton b = new JButton("Settings (non-modal)");
        Container pane = mainFrame.getContentPane();
        pane.add(b, "North");
        b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Show the preferences dialog.
                    showDialog(false);
                }
            });
        b = new JButton("Settings (modal)");
        pane.add(b, "Center");
        b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showDialog(true);
                }
            });
        b = new JButton("Popup");
        pane.add(b, "South");
        b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog
                        (mainFrame, "Hello world!", "Hello!",
                         JOptionPane.INFORMATION_MESSAGE);
                }
            });
    } // addButtons

    /**
     * Called whenever the configuration has been changed.
     */
    public void configurationChanged() {
        System.out.println("Preferences accepted.");
    } // configurationChanged

    /**
     * Show the preferences dialog.
     *
     * @param  modal  True if preferences dialog is to be modal.
     */
    protected void showDialog(boolean modal) {
        // Show the preferences dialog.
        config.showPreferences(mainFrame, modal);
    } // showDialog

    /**
     * Test-wrapper for this class.
     *
     * @param  args  Command-line arguments.
     *               If none given, loads 'test.properties' file.
     *               If one given, loads that file as a preferences file.
     *               If two given, tests merging the two with the first
     *               one being the user prefs and the second the new one.
     */
    public static void main(String[] args) {
        Main main = new Main();
        String filename;
        if ((args.length > 0) && (args[0].length() > 0)) {
            filename = args[0];
        } else {
            filename = "test.properties";
        }

        if ((args.length > 1) && (args[1].length() > 0)) {
            // Test upgrading the user preferences.
            try {
                System.out.println("Upgrading " + filename +
                                   " to new " + args[1] + "...");
                main.config.upgrade(filename, args[1]);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.exit(1);
            }
        } else {
            // Open the properties file and load the settings.
            try {
                System.out.println("Reading from " + filename + "...");
                if (!main.config.loadSettings(filename)) {
                    System.out.println("ERROR: reading settings file: " +
                                       filename);
                    System.exit(1);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.exit(1);
            }
        }

        main.mainFrame.setVisible(true);

        main.showDialog(false);
    } // main
} // Main
