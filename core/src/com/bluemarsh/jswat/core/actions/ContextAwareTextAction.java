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
 * $Id: ContextAwareTextAction.java 29 2008-06-30 00:41:09Z nfiedler $
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
            JTextComponent text = lookup.lookup(JTextComponent.class);
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
