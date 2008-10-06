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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BaseNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.bluemarsh.jswat.core.util.Arrays;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * An AbstractNode subclass that offers features needed in many cases.
 *
 * @author  Nathan Fiedler
 */
public class BaseNode extends AbstractNode {
    /** Node actions for this node, or null if none. */
    private Action[] nodeActions;
    /** The preferred action for this node, or null if none. */
    private Action preferredAction;

    /**
     * Constructs a simple RootNode.
     */
    public BaseNode() {
        super(Children.LEAF);
    }

    /**
     * Constructs a simple RootNode.
     *
     * @param  kids  children heirarchy.
     */
    public BaseNode(Children kids) {
        super(kids);
    }

    /**
     * The display name needs to change, fire the name change event.
     */
    public void displayNameChanged() {
        // Note that trying to fire a PROP_DISPLAY_NAME property
        // change results in an exception as that is illegal.
        fireDisplayNameChange(null, null);
    }

    public Action[] getActions(boolean context) {
        Action[] retValue = super.getActions(context);
        if (nodeActions != null) {
            retValue = (Action[]) Arrays.join(retValue, nodeActions);
        }
        return retValue;
    }

    public Action getPreferredAction() {
        return preferredAction;
    }

    /**
     * The icon needs to change, fire the icon change event.
     */
    public void iconChanged() {
        fireIconChange();
    }

    /**
     * A property has changed, fire a property change event.
     *
     * @param  name  name of changed property.
     * @param  o     old property value.
     * @param  n     new property value.
     */
    public void propertyChanged(String name, Object o, Object n) {
        firePropertyChange(name, o, n);
    }

    /**
     * Sets the actions for this node.
     *
     * @param  actions  array of actions, or null if none.
     */
    public void setActions(Action[] actions) {
        nodeActions = actions;
    }

    /**
     * Sets the preferred action for this node.
     *
     * @param  action  the preferred action, or null to reset.
     */
    public void setPreferredAction(Action action) {
        preferredAction = action;
    }
}
