/*********************************************************************
 *
 *      Copyright (C) 2000-2005 Nathan Fiedler
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
 * $Id: PreferencesAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.view.ViewDesktopFactory;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Implements the preferences action used to allow the user to set the
 * application preferences.
 *
 * @author  Nathan Fiedler
 */
public class PreferencesAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new PreferencesAction object with the default action
     * command string of "preferences".
     */
    public PreferencesAction() {
        super("preferences");
    } // PreferencesAction

    /**
     * Performs the preferences action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Frame topFrame = getFrame(event);
        JDialog dialog = new PreferencesDialog(topFrame);
        dialog.pack();
        dialog.setLocationRelativeTo(topFrame);
        dialog.setResizable(false);
        dialog.setVisible(true);
    } // actionPerformed
} // PreferencesAction

/**
 * Dialog for changing user preferences.
 *
 * @author  Nathan Fiedler
 */
class PreferencesDialog extends JDialog implements ActionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Appearance options panel. */
    private AppearancePanel appearancePanel;
    /** Miscellaneous options panel. */
    private MiscPanel miscPanel;
    /** Keyboard shortcuts panel. */
    private ShortcutsPanel shortsPanel;
    /** Colors for the source view. */
    private SourceColorsPanel colorsPanel;
    /** Source view options panel. */
    private ViewPanel viewPanel;

    /**
     * Constructs a PreferencesDialog with the given frame.
     *
     * @param  owner  Frame owner.
     */
    public PreferencesDialog(Frame owner) {
        super(owner, Bundle.getString("Preferences.title"));

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        Container contentPane = getContentPane();
        contentPane.setLayout(gbl);
        JPanel allPanel = new JPanel(new GridBagLayout());
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbl.setConstraints(allPanel, gbc);
        contentPane.add(allPanel);
        gbl = (GridBagLayout) allPanel.getLayout();

        JTabbedPane tabbedPane = new JTabbedPane();
        gbc.insets = new Insets(3, 3, 10, 3);
        gbl.setConstraints(tabbedPane, gbc);
        allPanel.add(tabbedPane);

        appearancePanel = new AppearancePanel();
        tabbedPane.addTab(Bundle.getString("Preferences.appearance.tab"),
                          appearancePanel);

        colorsPanel = new SourceColorsPanel();
        tabbedPane.addTab(Bundle.getString("Preferences.colors.tab"),
                          colorsPanel);

        miscPanel = new MiscPanel();
        tabbedPane.addTab(Bundle.getString("Preferences.misc.tab"),
                          miscPanel);

        shortsPanel = new ShortcutsPanel();
        tabbedPane.addTab(Bundle.getString("Preferences.keys.tab"),
                          shortsPanel);

        viewPanel = new ViewPanel();
        tabbedPane.addTab(Bundle.getString("Preferences.view.tab"),
                          viewPanel);

        JButton button = new JButton(
            Bundle.getString("Preferences.saveButton"));
        button.setActionCommand("save");
        button.addActionListener(this);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(3, 10, 3, 10);
        gbc.weightx = 1.0;
        gbl.setConstraints(button, gbc);
        allPanel.add(button);

        button = new JButton(
            Bundle.getString("Preferences.cancelButton"));
        button.setActionCommand("cancel");
        button.addActionListener(this);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(button, gbc);
        allPanel.add(button);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    } // PreferencesDialog

    /**
     * Invoked when a button has been pressed.
     *
     * @param  e  action event.
     */
    public void actionPerformed(ActionEvent e) {
        AbstractButton src = (AbstractButton) e.getSource();
        if (src.getActionCommand().equals("save")) {
            // Save the new settings.
            appearancePanel.commitChanges();
            colorsPanel.commitChanges();
            miscPanel.commitChanges();
            shortsPanel.commitChanges();
            viewPanel.commitChanges();
        }
        dispose();
    } // actionPerformed

    /**
     * Builds the appearanceellaneous options panel.
     *
     * @author  Nathan Fiedler
     */
    protected class AppearancePanel extends JPanel implements ItemListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Spinner for entering the message lines maximum. */
        private JSpinner msgLinesSpinner;
        /** Spinner for entering the output lines maximum. */
        private JSpinner outLinesSpinner;
        /** The view desktop mode button group. */
        private ButtonGroup desktopButtonGroup;
        /** The maximize source view check box. */
        private JCheckBox maxViewCBox;
        /** Keep the source view tabs in a single row. */
        private JCheckBox oneRowTabsCBox;

        /**
         * Constructs a panel to represent appearance options.
         */
        public AppearancePanel() {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            setLayout(gbl);
            gbc.insets = new Insets(3, 3, 3, 3);

            JPanel panel = createOutputPanel();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbl.setConstraints(panel, gbc);
            add(panel);

            panel = createDesktopPanel();
            gbc.weighty = 1.0;
            gbl.setConstraints(panel, gbc);
            add(panel);
        } // AppearancePanel

        /**
         * Commit the values in the interface widgets to the
         * preferences nodes.
         */
        public void commitChanges() {
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/ui/graphical");
            Integer max = (Integer) msgLinesSpinner.getValue();
            prefs.putInt("messageLines", max.intValue());
            max = (Integer) outLinesSpinner.getValue();
            prefs.putInt("outputLines", max.intValue());

            ButtonModel bm = desktopButtonGroup.getSelection();
            String cmd = bm.getActionCommand();
            if (cmd.equals("tabbed")) {
                prefs.putInt("viewDesktopType",
                             ViewDesktopFactory.MODE_TABBED);
            } else if (cmd.equals("frames")) {
                prefs.putInt("viewDesktopType",
                             ViewDesktopFactory.MODE_IFRAMES);
            }

            prefs.putBoolean("maximizeView", maxViewCBox.isSelected());
            prefs.putBoolean("oneRowTabs", oneRowTabsCBox.isSelected());
        } // commitChanges

        /**
         * Creates the view desktop panel of options.
         *
         * @return  newly created panel.
         */
        protected JPanel createDesktopPanel() {
            GridBagLayout gbl = new GridBagLayout();
            JPanel panel = new JPanel(gbl);
            Border border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                Bundle.getString("Preferences.appearance.desktop"));
            panel.setBorder(border);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);

            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/ui/graphical");

            JLabel label = new JLabel(Bundle.getString(
                "Preferences.appearance.desktopMode"));
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbl.setConstraints(label, gbc);
            panel.add(label);

            // Create widgets for selecting the view desktop.
            int mode = prefs.getInt("viewDesktopType",
                                    Defaults.VIEW_DESKTOP_TYPE);
            desktopButtonGroup = new ButtonGroup();

            JRadioButton rbutton = new JRadioButton(
                Bundle.getString("Preferences.appearance.desktopTabbed"));
            desktopButtonGroup.add(rbutton);
            rbutton.setActionCommand("tabbed");
            if (mode == ViewDesktopFactory.MODE_TABBED) {
                rbutton.setSelected(true);
            }
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbl.setConstraints(rbutton, gbc);
            panel.add(rbutton);
            rbutton.addItemListener(this);

            rbutton = new JRadioButton(
                Bundle.getString("Preferences.appearance.desktopFrames"));
            desktopButtonGroup.add(rbutton);
            rbutton.setActionCommand("frames");
            if (mode == ViewDesktopFactory.MODE_IFRAMES) {
                rbutton.setSelected(true);
            }
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbl.setConstraints(rbutton, gbc);
            panel.add(rbutton);
            rbutton.addItemListener(this);

            maxViewCBox = new JCheckBox(
                Bundle.getString("Preferences.appearance.maximizeView"));
            maxViewCBox.setSelected(
                prefs.getBoolean("maximizeView", Defaults.VIEW_MAXIMIZE));
            maxViewCBox.setEnabled(mode == ViewDesktopFactory.MODE_IFRAMES);
            gbl.setConstraints(maxViewCBox, gbc);
            panel.add(maxViewCBox);

            oneRowTabsCBox = new JCheckBox(
                Bundle.getString("Preferences.appearance.oneRowTabs"));
            oneRowTabsCBox.setSelected(
                prefs.getBoolean("oneRowTabs", Defaults.VIEW_SINGLE_ROW_TABS));
            oneRowTabsCBox.setEnabled(mode == ViewDesktopFactory.MODE_TABBED);
            gbl.setConstraints(oneRowTabsCBox, gbc);
            panel.add(oneRowTabsCBox);

            return panel;
        } // createDesktopPanel

        /**
         * Creates the scrollable output panel of options.
         *
         * @return  newly created panel.
         */
        protected JPanel createOutputPanel() {
            GridBagLayout gbl = new GridBagLayout();
            JPanel panel = new JPanel(gbl);
            Border border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                Bundle.getString("Preferences.appearance.scroll"));
            panel.setBorder(border);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);

            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/ui/graphical");

            JLabel label = new JLabel(Bundle.getString(
                "Preferences.appearance.messageLines"));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbl.setConstraints(label, gbc);
            panel.add(label);

            int lines = prefs.getInt("messageLines",
                                     Defaults.MESSAGE_LINES_SAVE);
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
                lines, 100, 10000, 10);
            msgLinesSpinner = new JSpinner(spinnerModel);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            // Dumb spinner is too small.
            gbc.ipadx = 20;
            gbl.setConstraints(msgLinesSpinner, gbc);
            panel.add(msgLinesSpinner);

            label = new JLabel(Bundle.getString(
                "Preferences.appearance.outputLines"));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbc.ipadx = 00;
            gbl.setConstraints(label, gbc);
            panel.add(label);

            lines = prefs.getInt("outputLines", Defaults.OUTPUT_LINES_SAVE);
            spinnerModel = new SpinnerNumberModel(lines, 100, 10000, 10);
            outLinesSpinner = new JSpinner(spinnerModel);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            // Dumb spinner is too small.
            gbc.ipadx = 20;
            gbc.weightx = 1.0;
            gbl.setConstraints(outLinesSpinner, gbc);
            panel.add(outLinesSpinner);

            return panel;
        } // createOutputPanel

        /**
         * Invoked when an item changes state.
         *
         * @param  e  item event.
         */
        public void itemStateChanged(ItemEvent e) {
            Object src = e.getSource();
            if (src instanceof JRadioButton) {
                JRadioButton rbutton = (JRadioButton) src;
                String cmd = rbutton.getActionCommand();
                if (cmd.equals("tabbed")) {
                    maxViewCBox.setEnabled(false);
                    oneRowTabsCBox.setEnabled(true);
                } else if (cmd.equals("frames")) {
                    maxViewCBox.setEnabled(true);
                    oneRowTabsCBox.setEnabled(false);
                }
            }
        } // itemStateChanged
    } // AppearancePanel

    /**
     * Builds the miscellaneous options panel.
     *
     * @author  Nathan Fiedler
     */
    protected class MiscPanel extends JPanel {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Field for entering the default file extension. */
        private JTextField fileExtField;
        /** Field for entering the default source stratum. */
        private JTextField stratumField;
        /** Spinner for specifying the mro list size. */
        private JSpinner mroListSpinner;
        /** Spinner for specifying the invocation timeout. */
        private JSpinner rmiTimeoutSpinner;

        /**
         * Constructs a panel to represent miscellaneous options.
         */
        public MiscPanel() {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            setLayout(gbl);
            gbc.insets = new Insets(3, 3, 3, 3);

            JPanel panel = createMapperPanel();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbl.setConstraints(panel, gbc);
            add(panel);

            panel = createFileMenuPanel();
            gbl.setConstraints(panel, gbc);
            add(panel);

            panel = createEvalPanel();
            gbc.weighty = 1.0;
            gbl.setConstraints(panel, gbc);
            add(panel);
        } // MiscPanel

        /**
         * Commit the values in the interface widgets to the
         * preferences nodes.
         */
        public void commitChanges() {
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat");
            prefs.put("defaultFileExtension", fileExtField.getText());

            prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/Session");
            prefs.put("defaultStratum", stratumField.getText());

            prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/ui/mro");
            Integer max = (Integer) mroListSpinner.getValue();
            prefs.putInt("maxListSize", max.intValue());

            prefs = Preferences.userRoot().node("com/bluemarsh/jswat/util");
            max = (Integer) rmiTimeoutSpinner.getValue();
            prefs.putInt("invocationTimeout", max.intValue());
        } // commitChanges

        /**
         * Creates the source mapper options panel.
         *
         * @return  newly created panel.
         */
        protected JPanel createFileMenuPanel() {
            GridBagLayout gbl = new GridBagLayout();
            JPanel panel = new JPanel(gbl);
            Border border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                Bundle.getString("Preferences.misc.fileMenu"));
            panel.setBorder(border);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);

            JLabel label = new JLabel(
                Bundle.getString("Preferences.misc.mroListSize"));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbl.setConstraints(label, gbc);
            panel.add(label);

            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/ui/mro");
            int size = prefs.getInt("maxListSize", Defaults.MRO_LIST_SIZE);
            SpinnerNumberModel smodel = new SpinnerNumberModel(size, 5, 25, 5);
            mroListSpinner = new JSpinner(smodel);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbl.setConstraints(mroListSpinner, gbc);
            panel.add(mroListSpinner);

            return panel;
        } // createFileMenuPanel

        /**
         * Creates the source mapper options panel.
         *
         * @return  newly created panel.
         */
        protected JPanel createMapperPanel() {
            GridBagLayout gbl = new GridBagLayout();
            JPanel panel = new JPanel(gbl);
            Border border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                Bundle.getString("Preferences.misc.mapper"));
            panel.setBorder(border);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);

            JLabel label = new JLabel(Bundle.getString(
                "Preferences.misc.fileExt"));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbl.setConstraints(label, gbc);
            panel.add(label);

            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat");
            String val = prefs.get("defaultFileExtension",
                                   Defaults.FILE_EXTENSION);
            fileExtField = new JTextField(val, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbl.setConstraints(fileExtField, gbc);
            panel.add(fileExtField);

            label = new JLabel(Bundle.getString(
                "Preferences.misc.stratum"));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbl.setConstraints(label, gbc);
            panel.add(label);

            prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/Session");
            val = prefs.get("defaultStratum", Defaults.STRATUM);
            stratumField = new JTextField(val, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbl.setConstraints(stratumField, gbc);
            panel.add(stratumField);

            return panel;
        } // createMapperPanel

        /**
         * Creates the source mapper options panel.
         *
         * @return  newly created panel.
         */
        protected JPanel createEvalPanel() {
            GridBagLayout gbl = new GridBagLayout();
            JPanel panel = new JPanel(gbl);
            Border border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                Bundle.getString("Preferences.misc.evaluator"));
            panel.setBorder(border);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);

            JLabel label = new JLabel(
                Bundle.getString("Preferences.misc.rmiTimeout"));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbl.setConstraints(label, gbc);
            panel.add(label);

            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/util");
            int timeout = prefs.getInt("invocationTimeout",
                                       Defaults.INVOCATION_TIMEOUT);
            SpinnerNumberModel smodel = new SpinnerNumberModel(
                timeout, 0, 60000, 1000);
            rmiTimeoutSpinner = new JSpinner(smodel);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbl.setConstraints(rmiTimeoutSpinner, gbc);
            panel.add(rmiTimeoutSpinner);

            return panel;
        } // createEvalPanel
    } // MiscPanel

    /**
     * Builds the keyboard shortcuts panel.
     *
     * @author  Nathan Fiedler
     */
    protected class ShortcutsPanel extends JPanel {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Table of text fields, keyed by action command names. */
        private Hashtable textFields;

        /**
         * Constructs a panel to represent keyboard shortcuts.
         */
        public ShortcutsPanel() {
            textFields = new Hashtable();

            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            setLayout(gbl);
            gbc.insets = new Insets(3, 3, 3, 3);

            JPanel panel = createShortcutsPanel();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbl.setConstraints(panel, gbc);
            add(panel);
        } // ShortcutsPanel

        /**
         * Build a single keyboard shortcut preference widget
         * using the name of the action command.
         *
         * @param  name   action command name.
         * @param  gbl    layout manager.
         * @param  gbc    layout constraints.
         * @param  panel  panel to add elements to.
         */
        protected void buildElement(String name, GridBagLayout gbl,
                                    GridBagConstraints gbc, JPanel panel) {
            JLabel label = new JLabel(Bundle.getString(
                "Preferences." + name));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbc.weightx = 0.0;
            gbl.setConstraints(label, gbc);
            panel.add(label);

            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/ui/graphical");
            String text = prefs.get(
                name, (String) Defaults.KEYBOARD_SHORTS.get(name));
            JTextField field = new JTextField(text, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbl.setConstraints(field, gbc);
            panel.add(field);

            textFields.put(name, field);
        } // buildElement

        /**
         * Commit the values in the interface widgets to the
         * preferences nodes.
         */
        public void commitChanges() {
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/ui/graphical");
            Enumeration names = textFields.keys();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                JTextField field = (JTextField) textFields.get(name);
                String text = field.getText();
                prefs.put(name, text);
            }
        } // commitChanges

        /**
         * Creates the shorcut mappings panel.
         *
         * @return  newly created panel.
         */
        protected JPanel createShortcutsPanel() {
            GridBagLayout gbl = new GridBagLayout();
            JPanel panel = new JPanel(gbl);
            Border border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                Bundle.getString("Preferences.keys.mappings"));
            panel.setBorder(border);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);

            buildElement("keys.step", gbl, gbc, panel);
            buildElement("keys.next", gbl, gbc, panel);
            buildElement("keys.finish", gbl, gbc, panel);
            buildElement("keys.vmStart", gbl, gbc, panel);
            buildElement("keys.vmSuspend", gbl, gbc, panel);
            buildElement("keys.vmResume", gbl, gbc, panel);
            buildElement("keys.setBreak", gbl, gbc, panel);
            //buildElement("keys.listBreak", gbl, gbc, panel);
            buildElement("keys.find", gbl, gbc, panel);
            buildElement("keys.findAgain", gbl, gbc, panel);
            buildElement("keys.gotoLine", gbl, gbc, panel);
            buildElement("keys.refresh", gbl, gbc, panel);
            buildElement("keys.helpIndex", gbl, gbc, panel);
            buildElement("keys.openFile", gbl, gbc, panel);
            buildElement("keys.evaluate", gbl, gbc, panel);
            buildElement("keys.vmAttach", gbl, gbc, panel);

            return panel;
        } // createShortcutsPanel
    } // ShortcutsPanel

    /**
     * Builds the source view colors panel.
     *
     * @author  Nathan Fiedler
     */
    protected class SourceColorsPanel extends JPanel {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Table of color changers, keyed by color names. */
        private Hashtable buttons;

        /**
         * Constructs a panel to represent source view colors.
         */
        public SourceColorsPanel() {
            buttons = new Hashtable();

            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            setLayout(gbl);
            gbc.insets = new Insets(3, 3, 3, 3);

            JPanel panel = createColorsPanel();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbl.setConstraints(panel, gbc);
            add(panel);
        } // SourceColorsPanel

        /**
         * Build a single keyboard shortcut preference widget
         * using the name of the action command.
         *
         * @param  name   action command name.
         * @param  panel  panel to add button to.
         */
        protected void buildElement(String name, JPanel panel) {
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/view");
            String color = prefs.get(
                name, (String) Defaults.VIEW_COLORS.get(name));
            String label = Bundle.getString("Preferences." + name);
            ColorButton button = new ColorButton(label, color);
            panel.add(button);
            buttons.put(name, button);
        } // buildElement

        /**
         * Commit the values in the interface widgets to the
         * preferences nodes.
         */
        public void commitChanges() {
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/view");
            Enumeration names = buttons.keys();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                ColorButton button = (ColorButton) buttons.get(name);
                prefs.put(name, button.getColor());
            }
        } // commitChanges

        /**
         * Creates the java syntax colors panel.
         *
         * @return  newly created panel.
         */
        protected JPanel createColorsPanel() {
            GridLayout gl = new GridLayout(3, 3, 5, 5);
            JPanel panel = new JPanel(gl);

            Border border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                Bundle.getString("Preferences.colors.java"));
            panel.setBorder(border);

            buildElement("colors.character", panel);
            buildElement("colors.comment", panel);
            buildElement("colors.highlight", panel);
            buildElement("colors.identifier", panel);
            buildElement("colors.keyword", panel);
            buildElement("colors.literal", panel);
            buildElement("colors.number", panel);
            buildElement("colors.primitive", panel);
            buildElement("colors.string", panel);
            return panel;
        } // createColorsPanel
    } // SourceColorsPanel

    /**
     * Class ColorButton is a button that has a color icon. When the button
     * is pressed, a color chooser panel is presented. After a new color
     * has been chosen, the color icon will change to reflect the selected
     * color.
     *
     * @author  Nathan Fiedler
     */
    public class ColorButton extends JButton implements ActionListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Array of zeros for padding numbers. */
        private String[] zeros = new String[] {
            "0", "00", "000", "0000", "00000"
        };
        /** Our color icon to show what the user has chosen. */
        private ColorIcon colorIcon;

        /**
         * Constructs a ColorButton with the given label.
         *
         * @param  label  label for the button.
         * @param  color  color value as RGB number.
         */
        public ColorButton(String label, String color) {
            super(label, new ColorIcon());
            colorIcon = (ColorIcon) getIcon();
            try {
                Color c = Color.decode(color);
                colorIcon.setColor(c);
            } catch (NumberFormatException nfe) {
                // Use the default color then.
            }
            addActionListener(this);
        } // ColorButton

        /**
         * Invoked when a button has been pressed.
         *
         * @param  e  action event.
         */
        public void actionPerformed(ActionEvent e) {
            Color c = JColorChooser.showDialog(
                PreferencesDialog.this, "Choose Color", colorIcon.getColor());
            if (c != null) {
                colorIcon.setColor(c);
                repaint();
            }
        } // actionPerformed

        /**
         * Returns the color this button represents, in the form of a
         * hexadecimal RGB value.
         *
         * @return  RGB value in hexadecimal.
         */
        public String getColor() {
            // Turn the color into an RGB value in hex.
            Color bg = colorIcon.getColor();
            // Lop off the alpha portion.
            int rgb = bg.getRGB() & 0x00FFFFFF;
            String hex = Integer.toHexString(rgb);
            // Pad with zeros.
            if (hex.length() < 6) {
                hex = zeros[5 - hex.length()].concat(hex);
            }
            return "0x" + hex;
        } // getColor
    } // ColorButton

    /**
     * A simple color icon whose background color shows what the user has
     * selected from the color chooser.
     *
     * @author  Nathan Fiedler
     */
    protected static class ColorIcon implements Icon {
        /** Color we are to paint with. */
        private Color ourColor;

        /**
         * Sets the default color of the icon.
         */
        public ColorIcon() {
            ourColor = Color.black;
        } // ColorIcon

        /**
         * Returns this icon's color.
         *
         * @return  icon's color.
         */
        public Color getColor() {
            return ourColor;
        } // getColor

        /**
         * Returns the icon's height.
         *
         * @return  an int specifying the fixed height of the icon.
         */
        public int getIconHeight() {
            return 20;
        } // getIconHeight

        /**
         * Returns the icon's width.
         *
         * @return  an int specifying the fixed width of the icon.
         */
        public int getIconWidth() {
            return 20;
        } // getIconWidth

        /**
         * Paint this component to the given graphics context.
         *
         * @param  c  component for getting properties.
         * @param  g  the <code>Graphics</code> context in which to paint.
         * @param  x  x offset.
         * @param  y  y offset.
         */
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(ourColor);
            g.fillRect(x, y, 20, 20);
        } // paintIcon

        /**
         * Changes this icon's color to the given color.
         *
         * @param  c  color to paint with.
         */
        public void setColor(Color c) {
            ourColor = c;
        } // setColor
    } // ColorIcon

    /**
     * Builds the source view options panel.
     *
     * @author  Nathan Fiedler
     */
    protected class ViewPanel extends JPanel
        implements ChangeListener, ListSelectionListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** List for selecting the font family. */
        private JList familyList;
        /** Spinner for entering the font size. */
        private JSpinner fontSizeSpinner;
        /** Spinner for entering the tab width. */
        private JSpinner tabWidthSpinner;
        /** Text area to sample the selected font. */
        private JTextArea fontSampleArea;
        /** Field for entering the external source editor. */
        private JTextField srcEditorField;
        /** Checkbox for specifying short method descriptors in the
         * scroll to method popup. */
        private JCheckBox shortMethodDescCheckBox;

        /**
         * Constructs a panel to represent source view options.
         */
        public ViewPanel() {
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            setLayout(gbl);
            gbc.insets = new Insets(3, 3, 3, 3);

            JPanel panel = createFontPanel();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbl.setConstraints(panel, gbc);
            add(panel);

            panel = createEditorPanel();
            gbl.setConstraints(panel, gbc);
            add(panel);

            panel = createPopupPanel();
            gbc.weighty = 1.0;
            gbl.setConstraints(panel, gbc);
            add(panel);
        } // ViewPanel

        /**
         * Commit the values in the interface widgets to the preferences
         * nodes.
         */
        public void commitChanges() {
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/view");
            Integer ival = (Integer) fontSizeSpinner.getValue();
            prefs.putInt("fontSize", ival.intValue());
            ival = (Integer) tabWidthSpinner.getValue();
            prefs.putInt("tabWidth", ival.intValue());
            String family = (String) familyList.getSelectedValue();
            if (family == null) {
                family = "Default";
            }
            prefs.put("fontFamily", family);
            prefs.put("extSourceEditor", srcEditorField.getText());
            prefs.putBoolean("shortMethodDescInPopup",
                             shortMethodDescCheckBox.isSelected());
        } // commitChanges

        /**
         * Creates the font options panel.
         *
         * @return  newly created panel.
         */
        protected JPanel createFontPanel() {
            GridBagLayout gbl = new GridBagLayout();
            JPanel panel = new JPanel(gbl);
            Border border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                Bundle.getString("Preferences.view.font"));
            panel.setBorder(border);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);

            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/view");

            // Font family list
            GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] familyNames = ge.getAvailableFontFamilyNames();
            familyList = new JList(familyNames);
            String family = prefs.get("fontFamily",
                                      Defaults.VIEW_FONT_FAMILY);
            familyList.setSelectedValue(family, true);
            familyList.addListSelectionListener(this);
            JScrollPane scroller = new JScrollPane(familyList);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridheight = 2;
            gbl.setConstraints(scroller, gbc);
            panel.add(scroller);

            // Point size selector
            JLabel label = new JLabel(Bundle.getString(
                "Preferences.view.fontSize"));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridheight = 1;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbl.setConstraints(label, gbc);
            panel.add(label);
            int fontSize = prefs.getInt("fontSize", Defaults.VIEW_FONT_SIZE);
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
                fontSize, 4, 192, 1);
            fontSizeSpinner = new JSpinner(spinnerModel);
            fontSizeSpinner.addChangeListener(this);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            // Dumb spinner is too small.
            gbc.ipadx = 10;
            gbl.setConstraints(fontSizeSpinner, gbc);
            panel.add(fontSizeSpinner);

            // Tab width selector
            label = new JLabel(Bundle.getString("Preferences.view.tabWidth"));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbc.ipadx = 0;
            gbl.setConstraints(label, gbc);
            panel.add(label);
            int tabWidth = prefs.getInt("tabWidth", Defaults.VIEW_TAB_WIDTH);
            spinnerModel = new SpinnerNumberModel(tabWidth, 1, 100, 1);
            tabWidthSpinner = new JSpinner(spinnerModel);
            tabWidthSpinner.addChangeListener(this);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            // Dumb spinner is too small.
            gbc.ipadx = 10;
            gbl.setConstraints(tabWidthSpinner, gbc);
            panel.add(tabWidthSpinner);

            // Font sample area
            fontSampleArea = new JTextArea(
                "for (int i = 0; i < arr.length; i++) {\n"
                + "\twriteString(i);\n"
                + "} // for");
            fontSampleArea.setBorder(BorderFactory.createEtchedBorder());
            fontSampleArea.setEditable(false);
            Font f = new Font(family, Font.PLAIN, fontSize);
            fontSampleArea.setFont(f);
            fontSampleArea.setPreferredSize(new Dimension(300, 150));
            fontSampleArea.setTabSize(tabWidth);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.ipadx = 0;
            gbc.weightx = 1.0;
            gbl.setConstraints(fontSampleArea, gbc);
            panel.add(fontSampleArea);

            return panel;
        } // createFontPanel

        /**
         * Creates the external editor options panel.
         *
         * @return  newly created panel.
         */
        protected JPanel createEditorPanel() {
            GridBagLayout gbl = new GridBagLayout();
            JPanel panel = new JPanel(gbl);
            Border border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                Bundle.getString("Preferences.view.editor"));
            panel.setBorder(border);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);

            // Source editor launching settings.
            JLabel label = new JLabel(Bundle.getString(
                "Preferences.misc.sourceEditor"));
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.insets = new Insets(3, 3, 0, 3);
            gbl.setConstraints(label, gbc);
            panel.add(label);

            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/view");
            String val = prefs.get("extSourceEditor", Defaults.SOURCE_EDITOR);
            srcEditorField = new JTextField(val, 30);
            gbc.insets = new Insets(0, 3, 3, 3);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbl.setConstraints(srcEditorField, gbc);
            panel.add(srcEditorField);

            return panel;
        } // createEditorPanel

        /**
         * Creates the popup menu options panel.
         *
         * @return  newly created panel.
         */
        protected JPanel createPopupPanel() {
            GridBagLayout gbl = new GridBagLayout();
            JPanel panel = new JPanel(gbl);
            Border border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                Bundle.getString("Preferences.view.popup"));
            panel.setBorder(border);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);

            // Short method descriptor checkbox.
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/view");
            boolean shortMethods = prefs.getBoolean(
                "shortMethodDescInPopup",
                Defaults.VIEW_POPUP_SHORT_METHOD_DESC);
            shortMethodDescCheckBox = new JCheckBox(
                Bundle.getString("Preferences.view.viewPopupShortMethodDesc"),
                shortMethods);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbl.setConstraints(shortMethodDescCheckBox, gbc);
            panel.add(shortMethodDescCheckBox);

            return panel;
        } // createPopupPanel

        /**
         * Invoked when the target of the listener has changed its state.
         *
         * @param  e  a ChangeEvent object.
         */
        public void stateChanged(ChangeEvent e) {
            // One of the spinners changed.
            updateSample();
        } // stateChanged

        /**
         * Update the sample text area to reflect the user's selections.
         */
        protected void updateSample() {
            Integer ival = (Integer) tabWidthSpinner.getValue();
            fontSampleArea.setTabSize(ival.intValue());
            ival = (Integer) fontSizeSpinner.getValue();
            String family = (String) familyList.getSelectedValue();
            if (family == null) {
                family = "Default";
            }
            Font f = new Font(family, Font.PLAIN, ival.intValue());
            fontSampleArea.setFont(f);
        } // updateSample

        /**
         * Called whenever the value of the selection changes.
         *
         * @param  e  the event that characterizes the change.
         */
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                // The list selection changed.
                updateSample();
            }
        } // valueChanged
    } // ViewPanel
} // PreferencesDialog
