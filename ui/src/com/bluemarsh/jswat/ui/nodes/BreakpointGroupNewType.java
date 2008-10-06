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
 * $Id: BreakpointGroupNewType.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 * Implements the action of creating a new breakpoint group.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointGroupNewType extends NewType {
    /** Group to which we add a group. */
    private BreakpointGroup group;

    /**
     * Creates a new instance of BreakpointGroupNewType.
     *
     * @param  group  the breakpoint group in which to create.
     */
    public BreakpointGroupNewType(BreakpointGroup group) {
        this.group = group;
    }

    public void create() {
        String title = NbBundle.getMessage(BreakpointGroupNewType.class,
                    "CTL_BreakpointGroupNewType_title");
        String label = NbBundle.getMessage(BreakpointGroupNewType.class,
                    "CTL_BreakpointGroupNewType_label");
        InputLine desc = new NotifyDescriptor.InputLine(label, title);
        Object ans = DialogDisplayer.getDefault().notify(desc);
        if (ans == NotifyDescriptor.OK_OPTION) {
            String name = desc.getInputText();
            BreakpointManager bm = BreakpointProvider.getBreakpointManager(group);
            BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
            BreakpointGroup ng = bf.createBreakpointGroup(name);
            bm.addBreakpointGroup(ng, group);
        }
    }

    public String getName() {
        return NbBundle.getMessage(BreakpointGroupNewType.class,
                    "LBL_BreakpointGroupNewType_name");
    }
}
