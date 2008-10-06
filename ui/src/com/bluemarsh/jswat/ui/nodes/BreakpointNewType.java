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
 * $Id: BreakpointNewType.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.bluemarsh.jswat.ui.actions.NewBreakpointAction;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/**
 * Implements the action of creating a new breakpoint.
 *
 * @author  Nathan Fiedler
 */
class BreakpointNewType extends NewType {

    public void create() {
        CallableSystemAction action = (CallableSystemAction)
                    SystemAction.get(NewBreakpointAction.class);
        action.performAction();
    }

    public String getName() {
        return NbBundle.getMessage(BreakpointNewType.class,
                    "LBL_BreakpointNewType_name");
    }
}
