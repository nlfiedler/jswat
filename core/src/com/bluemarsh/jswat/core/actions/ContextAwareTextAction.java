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
 * $Id: ContextAwareTextAction.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author Nathan Fiedler
 */
public abstract class ContextAwareTextAction extends TextAction
        implements ContextAwareAction {
    /** Our action context. */
    private Lookup lookup;

    /**
     * Creates a new instance of ContextAwareTextAction.
     *
     * @param  name  display name of the action.
     */
    public ContextAwareTextAction(String name) {
        super(name);
    }

    /**
     * Retrieve the text component using the action context, but if that
     * is unsuccessful, return null.
     *
     * @return  a text component, or null.
     */
    protected JTextComponent getText() {
        if (lookup != null) {
            JTextComponent text = (JTextComponent) lookup.lookup(
                    JTextComponent.class);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    /**
     * Retrieve the text component using the action context, and if that
     * is unsuccessful, then use the default TextAction method.
     *
     * @param  event  possible source for the text component.
     * @return  a text component.
     */
    protected JTextComponent getText(ActionEvent event) {
        JTextComponent text = getText();
        if (text == null) {
            text = getTextComponent(event);
        }
        return text;
    }

    /**
     * Sets the action context for this action.
     *
     * @param  lookup  the action context.
     */
    protected void setLookup(Lookup lookup) {
        this.lookup = lookup;
    }
}
