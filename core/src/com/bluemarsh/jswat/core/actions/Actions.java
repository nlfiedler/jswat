/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Actions.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.actions;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Utility class for the actions package.
 *
 * @author Nathan Fiedler
 */
public class Actions {

    /**
     * Creates a new instance of Actions.
     */
    private Actions() {
    }

    /**
     * Associate the set of actions with the given text components.
     * A popup menu will be created and the actions will be associated
     * with each text component (assuming they are context aware).
     *
     * @param  actions  set of actions to attach.
     * @param  comps    text components to receive actions.
     */
    public static void attachActions(Action[] actions, JTextComponent... comps) {
        for (JTextComponent comp : comps) {
            Lookup lookup = Lookups.singleton(comp);
            JPopupMenu menu = Utilities.actionsToPopup(actions, lookup);
            comp.addMouseListener(new PopupAdapter(menu));
        }
    }

    /**
     * Associate the actions with the given component via the ActionMap
     * and InputMap of that component. The actions must have names, and
     * they should have keyboard accelerators in order for this method
     * to have any effect.
     *
     * @param  actions  set of actions to attach.
     * @param  comp     component to connect with actions.
     */
    public static void attachShortcuts(Action[] actions, JComponent comp) {
        ActionMap am = comp.getActionMap();
        InputMap im = comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        for (Action action : actions) {
            Object key = action.getValue(Action.NAME);
            if (key != null) {
                am.put(key, action);
                KeyStroke ks = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
                if (ks != null) {
                    im.put(ks, key);
                }
            }
        }
    }
}
