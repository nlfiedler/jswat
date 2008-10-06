/*********************************************************************
 *
 *      Copyright (C) 2001-2004 Nathan Fiedler
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
 * $Id: ToolbarCreator.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.plugins.jedit;

import com.bluemarsh.jswat.action.ActionTable;
import com.bluemarsh.jswat.action.SessionAction;
import com.bluemarsh.jswat.ui.SessionActionAdapter;
import com.bluemarsh.jswat.ui.Bundle;
import com.bluemarsh.jswat.util.Strings;
import org.gjt.sp.jedit.jEdit;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;

/**
 * Helper class to create the toolbar. Almost all of this code is copied
 * from <code>com.bluemarsh.jswat.ui.graphical.MainWindow</code>.
 *
 * @author  Nathan Fiedler
 * @author  Dirk Moebius
 */
public class ToolbarCreator {

    /**
     * Constructs a ToolbarCreator.
     */
    public ToolbarCreator() {
    } // ToolbarCreator

    /**
     * Create the toolbar. By default this reads the resource file
     * for the definition of the toolbar.
     *
     * @param  saa  the SessionActionAdapter instance.
     * @return  new toolbar built out
     */
    public JToolBar createToolbar(SessionActionAdapter saa) {
        JToolBar toolbar = new JToolBar();
        String[] toolKeys = Strings.tokenize(jEdit.getProperty(
            "options.jswatplugin.toolbar"));
        for (int i = 0; i < toolKeys.length; i++) {
            if (toolKeys[i].equals("-")) {
                toolbar.addSeparator();
            } else {
                toolbar.add(createToolbarButton(toolKeys[i], saa));
            }
        }
        toolbar.setRollover(true);
        return toolbar;
    } // createToolbar

    /**
     * Create a button to go inside of the toolbar. By default this
     * will load an image resource. The image filename is relative to
     * the classpath (including the '.' directory if its a part of the
     * classpath), and may either be in a JAR file or a separate file.
     *
     * @param  key  key in resource bundle for tool
     * @param  saa  the SessionActionAdapter instance.
     * @return  new toolbar button
     */
    protected JButton createToolbarButton(String key,
                                          SessionActionAdapter saa) {
        // create the button
        Icon icon = getToolbarIcon(key);
        JButton b = icon == null ? new JButton(key) : new JButton(icon);
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));

        // get action and attach it to button as a listener
        b.setActionCommand(key);
        Action a = null;
        try {
            a = ActionTable.getAction(key);
        } catch (Exception e) {
            // ignored
        }
        if (a != null) {
            b.addActionListener(a);
            a.addPropertyChangeListener(new ActionChangedListener(b));
            b.setEnabled(a.isEnabled());
            if (a instanceof SessionAction) {
                saa.addComponent((SessionAction) a, b);
            }
        } else {
            b.setEnabled(false);
        }

        // Attach the tooltip to button.
        assignTooltip(key, b);
        return b;
    } // createToolbarButton

    /**
     * Create the icon for the toolbar button with the given action
     * command.
     *
     * @param  cmd  action command string.
     * @return  image icon for button.
     */
    protected ImageIcon getToolbarIcon(String cmd) {
        URL url = Bundle.getResource(cmd + "MenuImage");
        return url == null ? null : new ImageIcon(url);
    } // getToolbarIcon

    /**
     * Look up the tooltip for the named action and assign it as the tooltip
     * of the component.
     *
     * @param  key   the action command for the button.
     * @param  comp  component to which to assign tooltip.
     */
    protected void assignTooltip(String key, JComponent comp) {
        String tip = Bundle.getString(key + "Tooltip");
        if (tip == null || tip.length() == 0) {
            // Do nothing in this case, as we want to either leave the
            // custom tooltips in place, and would prefer not to display
            // anything when there are no tooltips.
            return;
        }
        if (tip != null) {
            // Use HTML for multi-line tooltips.
            // The font seems awfully big, so let's shrink it.
            tip = "<html><small>" + tip + "</small></html>";
            comp.setToolTipText(tip);
        }
    } // assignTooltip

    /**
     * Watches for changes in the actions and deals with them by
     * changing the corresponding menu items or toolbar buttons.
     */
    protected class ActionChangedListener implements PropertyChangeListener {
        /** Component we are associated with. */
        private JComponent component;

        /**
         * Constructor for our action change listener.
         *
         * @param  c  component we are to associate with.
         */
        public ActionChangedListener(JComponent c) {
            super();
            component = c;
        } // ActionChangedListener

        /**
         * Handles changes in the action. If the action name
         * changed we change our menu name. If the action changed
         * it's enabled state, we change our component's state.
         *
         * @param  e  property change event
         */
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (propertyName.equals("enabled")) {
                Boolean enabledState = (Boolean) e.getNewValue();
                component.setEnabled(enabledState.booleanValue());
            }
        } // propertyChange
    } // ActionChangedListener
} // ToolbarCreator

