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
 * $Id: CutAction.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Copies the selected text to the system clipboard and removes it
 * from the text component.
 *
 * @author Nathan Fiedler
 */
public class CutAction extends ContextAwareTextAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of CutAction.
     */
    public CutAction() {
        super(NbBundle.getMessage(CutAction.class, "LBL_CutAction_Name"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
    }

    public void actionPerformed(ActionEvent event) {
        JTextComponent target = getText(event);
        if (target != null) {
            target.cut();
        }
    }

    public Action createContextAwareInstance(Lookup lookup) {
        CutAction action = new CutAction();
        action.setLookup(lookup);
        return action;
    }
}
