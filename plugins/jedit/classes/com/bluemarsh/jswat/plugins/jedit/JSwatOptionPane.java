/*********************************************************************
 *
 *      Copyright (C) 2001-2004 David Taylor
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
 * $Id: JSwatOptionPane.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.plugins.jedit;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.action.PathBuilder;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Provides the interface for changing the plugin settings.
 *
 * @author  David Taylor
 * @author  Nathan Fiedler
 * @author  Dirk Moebius
 */
public class JSwatOptionPane extends AbstractOptionPane
    implements ActionListener {
    /** Button to build classpath. */
    private JButton buildClasspath;
    /** Button to build sourcepath. */
    private JButton buildSourcepath;
    /** Stop in main() method when launching. */
    private JCheckBox stopOnMain;
    /** Raise window upon debugging event. */
    private JCheckBox raiseWindow;
    /** Shorten class names. */
    private JCheckBox shortClassNames;
    /** Classpath field. */
    private JTextField classPath;
    /** Sourcepath field. */
    private JTextField sourcePath;

    /**
     * Constructs a JSwatOptionPane.
     */
    public JSwatOptionPane() {
        super(JSwatPlugin.NAME);
    } // JSwatOptionPane

    /**
     * A button has been pressed -- do something.
     *
     * @param  evt  action event.
     */
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        Component parentComp = getTopLevelAncestor();

        if (source == buildClasspath) {
            PathBuilder pathBuilder = new PathBuilder(false);
            pathBuilder.setPath(classPath.getText());
            pathBuilder.setFileFilter(new PathBuilder.ClasspathFilter());
            pathBuilder.setMultiSelectionEnabled(true);
            int response = JOptionPane.showOptionDialog(
                parentComp, pathBuilder,
                // LOCALIZE
                "Build Classpath",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response == JOptionPane.OK_OPTION) {
                classPath.setText(pathBuilder.getPath());
            }

        } else if (source == buildSourcepath) {
            PathBuilder pathBuilder = new PathBuilder(false);
            pathBuilder.setPath(sourcePath.getText());
            pathBuilder.setFileFilter(new PathBuilder.SourcepathFilter());
            pathBuilder.setMultiSelectionEnabled(true);
            int response = JOptionPane.showOptionDialog(
                parentComp, pathBuilder,
                // LOCALIZE
                "Build Sourcepath",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response == JOptionPane.OK_OPTION) {
                sourcePath.setText(pathBuilder.getPath());
            }
        }
    } // actionPerformed

    /**
     * Initialize the option pane.
     */
    public void _init() {
        GridBagConstraints constraints = new GridBagConstraints();
        int gridy = 0;

        Insets spaceAbove = new Insets(12, 0, 0, 0);
        Insets nullInsets = new Insets(0, 0, 0, 0);

        // Classpath
        JLabel label = new JLabel(
            jEdit.getProperty(JSwatPlugin.OPTION_PREFIX + "classpath.title"));
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        add(label, constraints);

        String classPathSetting = JSwatPlugin.getInstance().getSession()
            .getProperty("classpath");
        if (classPathSetting == null) {
            classPathSetting = "";
        }
        classPath = new JTextField(classPathSetting, 30);
        classPath.setMaximumSize(new Dimension(
            600, classPath.getPreferredSize().height));
        constraints.gridx = 0;
        constraints.gridy = gridy;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(classPath, constraints);

        // Classpath builder button
        constraints.gridx = 1;
        constraints.gridy = gridy++;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        buildClasspath = new JButton("...");
        buildClasspath.setPreferredSize(new Dimension(
            buildClasspath.getPreferredSize().width,
            classPath.getPreferredSize().height));
        add(buildClasspath, constraints);

        // Source path
        label = new JLabel(
            jEdit.getProperty(JSwatPlugin.OPTION_PREFIX + "sourcepath.title"));
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        constraints.weightx = 0.0;
        constraints.insets = spaceAbove;
        add(label, constraints);

        String sourcePathSetting = JSwatPlugin.getInstance().getSession()
            .getProperty("sourcepath");
        if (sourcePathSetting == null) {
            sourcePathSetting = "";
        }
        sourcePath = new JTextField(sourcePathSetting, 30);
        sourcePath.setMaximumSize(new Dimension(
            600, classPath.getPreferredSize().height));
        constraints.gridx = 0;
        constraints.gridy = gridy;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = nullInsets;
        add(sourcePath, constraints);

        // Sourcepath builder button
        constraints.gridx = 1;
        constraints.gridy = gridy++;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        buildSourcepath = new JButton("...");
        buildSourcepath.setPreferredSize(new Dimension(
            buildSourcepath.getPreferredSize().width,
            sourcePath.getPreferredSize().height));
        add(buildSourcepath, constraints);

        // "Options:" label
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        constraints.weightx = 1.0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = spaceAbove;
        label = new JLabel(
            jEdit.getProperty(JSwatPlugin.OPTION_PREFIX
                              + "otherOptions.title"));
        add(label, constraints);

        // Stop in main() method when launching
        constraints.gridy = gridy++;
        constraints.insets = nullInsets;
        stopOnMain = new JCheckBox(jEdit.getProperty(
            JSwatPlugin.OPTION_PREFIX + "stopOnMain.title"),
            Preferences.userRoot().node("com/bluemarsh/jswat/breakpoint")
                .getBoolean("stopOnMain", Defaults.STOP_ON_MAIN));
        add(stopOnMain, constraints);

        // Raise window upon debugging event
        constraints.gridy = gridy++;
        raiseWindow = new JCheckBox(jEdit.getProperty(
            JSwatPlugin.OPTION_PREFIX + "raiseWindow.title"),
            Preferences.userRoot().node("com/bluemarsh/jswat/Session")
                .getBoolean("raiseWindow", Defaults.RAISE_WINDOW));
        add(raiseWindow, constraints);

        // Shorten class names
        constraints.gridy = gridy++;
        shortClassNames = new JCheckBox(jEdit.getProperty(
            JSwatPlugin.OPTION_PREFIX + "shortClassNames.title"),
            Preferences.userRoot().node("com/bluemarsh/jswat/util")
                .getBoolean("shortClassNames", Defaults.SHORT_CLASS_NAMES));
        add(shortClassNames, constraints);

        buildClasspath.addActionListener(this);
        buildSourcepath.addActionListener(this);
    } // _init

    /**
     * Save the options.
     * Note that the option are saved in the JSwat settings, not the jEdit
     * settings.
     */
    public void _save() {
        JSwatPlugin.getInstance().getSession().setProperty("classpath",
            classPath.getText());
        JSwatPlugin.getInstance().getSession().setProperty("sourcepath",
            sourcePath.getText());
        Preferences.userRoot().node("com/bluemarsh/jswat/breakpoint")
            .putBoolean("stopOnMain", stopOnMain.isSelected());
        Preferences.userRoot().node("com/bluemarsh/jswat/Session")
            .putBoolean("raiseWindow", raiseWindow.isSelected());
        Preferences.userRoot().node("com/bluemarsh/jswat/util")
            .putBoolean("shortClassNames", shortClassNames.isSelected());
    } // _save
} // JSwatOptionPane
