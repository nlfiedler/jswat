/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: AboutAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.ui.EditPopup;
import com.bluemarsh.jswat.util.AppVersion;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * Implements the about file action used to show the credits for JSwat.
 *
 * @author  Nathan Fiedler
 */
public class AboutAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new AboutAction object with the default action command
     * string of "about".
     */
    public AboutAction() {
        super("about");
    } // AboutAction

    /**
     * Performs the about action. This simply displays a dialog showing the
     * credits for the program.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Frame topFrame = getFrame(event);
        JDialog dialog = new AboutDialog(topFrame);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(topFrame);
        dialog.setVisible(true);
    } // actionPerformed
} // AboutAction

/**
 * Defines the about dialog.
 *
 * @author  Nathan Fiedler
 */
class AboutDialog extends JDialog {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an AboutDialog.
     *
     * @param  topFrame  owner of this dialog.
     */
    public AboutDialog(Frame topFrame) {
        super(topFrame, Bundle.getString("About.title"));

        Container pane = getContentPane();
        GridBagLayout gbl = new GridBagLayout();
        pane.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);

        ImageIcon ii = new ImageIcon(
            com.bluemarsh.jswat.ui.Bundle.getResource("houseflyImage"));
        JLabel label = new JLabel(ii);
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        pane.add(label);

        JTabbedPane tabbedPane = new JTabbedPane();
        gbc.gridheight = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(tabbedPane, gbc);
        pane.add(tabbedPane);

        JPanel panel = buildPropertiesPanel();
        tabbedPane.add(Bundle.getString("About.tabProperties"), panel);

        panel = buildAuthorsPanel();
        tabbedPane.add(Bundle.getString("About.tabAuthors"), panel);

        JButton button = new JButton(Bundle.getString("closeButton"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbl.setConstraints(button, gbc);
        pane.add(button);
    } // AboutDialog

    /**
     * Builds the authors dialog.
     *
     * @return  panel component.
     */
    protected JPanel buildAuthorsPanel() {
        JPanel pane = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        pane.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);

        JLabel label = new JLabel(Bundle.getString("About.maintainer"));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(label, gbc);
        pane.add(label);

        label = new JLabel(Bundle.getString("About.contributorsLabel"));
        gbl.setConstraints(label, gbc);
        pane.add(label);

        JTextArea textArea = new JTextArea(
            Bundle.getString("About.contributors"));
        textArea.setEditable(false);
        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setPreferredSize(new Dimension(400, 300));
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridheight = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(scroller, gbc);
        pane.add(scroller);

        return pane;
    } // buildAuthorsPanel

    /**
     * Builds the copyright message and system properties dialog.
     *
     * @return  panel component.
     */
    protected JPanel buildPropertiesPanel() {
        JPanel pane = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        pane.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);

        String version = AppVersion.getVersion();
        JLabel label = new JLabel("< JSwat " + version + " >");
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(label, gbc);
        pane.add(label);

        label = new JLabel(com.bluemarsh.jswat.Bundle.getString("about1"));
        gbl.setConstraints(label, gbc);
        pane.add(label);

        label = new JLabel(com.bluemarsh.jswat.Bundle.getString("about2"));
        gbl.setConstraints(label, gbc);
        pane.add(label);

        label = new JLabel(com.bluemarsh.jswat.Bundle.getString("about3"));
        gbl.setConstraints(label, gbc);
        pane.add(label);

        // Get the system properties and dump them to a text area. Note
        // that Properties.list() truncates values with '...' which is
        // completely useless.
        Properties props = System.getProperties();
        StringBuffer buf = new StringBuffer(1024);
        Enumeration names = props.propertyNames();
        List sortedNames = Collections.list(names);
        Collections.sort(sortedNames);
        Iterator iter = sortedNames.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            buf.append(key);
            buf.append(" = ");
            buf.append(props.getProperty(key));
            buf.append('\n');
        }

        JTextArea textArea = new JTextArea(buf.toString());
        textArea.setEditable(false);
        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setPreferredSize(new Dimension(400, 300));
        gbc.gridheight = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(scroller, gbc);
        pane.add(scroller);

        // Set up the edit popup menu for the text area.
        EditPopup popup = new EditPopup(textArea, false, false);
        textArea.addMouseListener(popup);

        return pane;
    } // buildPropertiesPanel
} // AboutDialog
