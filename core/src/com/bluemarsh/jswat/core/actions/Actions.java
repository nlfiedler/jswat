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
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Actions.java 6 2007-05-16 07:14:24Z nfiedler $
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
