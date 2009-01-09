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
 * $Id$
 */

package com.bluemarsh.jswat.core.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Copies the selected text to the system clipboard.
 *
 * @author Nathan Fiedler
 */
public class CopyAction extends ContextAwareTextAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of CopyAction.
     */
    public CopyAction() {
        super(NbBundle.getMessage(CopyAction.class, "LBL_CopyAction_Name"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
    }

    public void actionPerformed(ActionEvent event) {
        JTextComponent target = getText(event);
        if (target != null) {
            target.copy();
        }
    }

    public Action createContextAwareInstance(Lookup lookup) {
        CopyAction action = new CopyAction();
        action.setLookup(lookup);
        return action;
    }
}
