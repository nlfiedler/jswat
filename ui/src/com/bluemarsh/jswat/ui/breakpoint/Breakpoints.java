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
 * are Copyright (C) 2006-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.util.NameValuePair;
import java.util.Iterator;
import java.util.Stack;
import javax.swing.JComboBox;

/**
 * A utility class containing methods used by one or more of the classes
 * contained in this package.
 *
 * @author Nathan Fiedler
 */
public class Breakpoints {

    /**
     * Creates a new instance of Breakpoints.
     */
    private Breakpoints() {
        // None shall instantiate us.
    }

    /**
     * Builds a combo box to display all the available breakpoint groups.
     * Each group is represented by its name, prefixed with the names of
     * all of its parents, separated by periods.
     *
     * @param  group  the breakpoint group acting as root of all groups.
     * @param  cbox   combo box to be populated with items.
     */
    public static void buildGroupList(BreakpointGroup group, JComboBox cbox) {
        buildGroupList(group, cbox, null);
    }

    /**
     * Builds a combo box to display all the available breakpoint groups.
     * Each group is represented by its name, prefixed with the names of
     * all of its parents, separated by periods.
     *
     * @param  group    the breakpoint group acting as root of all groups.
     * @param  cbox     combo box to be populated with items.
     * @param  exclude  a breakpoint group to be excluded; this will exclude
     *                  the group and all of its child groups.
     */
    public static void buildGroupList(BreakpointGroup group, JComboBox cbox,
            BreakpointGroup exclude) {
        // Stack holds breakpoint group/composite name pairs.
        Stack<NameValuePair<BreakpointGroup>> stack =
                new Stack<NameValuePair<BreakpointGroup>>();
        stack.push(new NameValuePair<BreakpointGroup>(null, group));
        while (!stack.empty()) {
            // Get next composite name and breakpoint group.
            NameValuePair<BreakpointGroup> nvp = stack.pop();
            String name = nvp.getName();
            BreakpointGroup grp = nvp.getValue();
            if (name != null) {
                name = name + "." + grp.getName();
            } else {
                name = grp.getName();
            }
            // Add this group's name to the list.
            cbox.addItem(new NameValuePair<BreakpointGroup>(name, grp));

            // Visit this group's subgroups.
            Iterator<BreakpointGroup> iter = grp.groups(false);
            while (iter.hasNext()) {
                BreakpointGroup subgroup = iter.next();
                // Ignore the excluded group, and all of its children.
                if (exclude == null || !subgroup.equals(exclude)) {
                    stack.push(new NameValuePair<BreakpointGroup>(name, subgroup));
                }
            }
        }
    }

    /**
     * Using the given combo box, previously built using
     * <code>buildGroupList()</code>, set one of the breakpoint groups
     * in the combo box as selected according to the parent group of
     * the given breakpoint.
     *
     * @param  box  combo box in which to select a group.
     * @param  bp   breakpoint whose parent group should be selected.
     */
    public static void findAndSelectGroup(JComboBox box, Breakpoint bp) {
        findAndSelectGroup(box, bp.getBreakpointGroup());
    }

    /**
     * Using the given combo box, previously built using
     * <code>buildGroupList()</code>, set one of the breakpoint groups
     * in the combo box as selected according to the given group.
     *
     * @param  box    combo box in which to select a group.
     * @param  group  group to be selected.
     */
    public static void findAndSelectGroup(JComboBox box, BreakpointGroup group) {
        int count = box.getItemCount();
        for (int ii = 0; ii < count; ii++) {
            NameValuePair<?> pair = (NameValuePair<?>) box.getItemAt(ii);
            if (pair.getValue() == group) {
                box.setSelectedIndex(ii);
                break;
            }
        }
    }

    /**
     * Retrieves the selected BreakpointGroup from the given combo box.
     * The combo box must have been built using the
     * <code>buildGroupList()</code> method.
     *
     * @param  box  combo box that lists breakpoint groups.
     * @return  selected breakpoint group, or null if none.
     */
    public static BreakpointGroup getSelectedGroup(JComboBox box) {
        try {
            NameValuePair<?> grpname = (NameValuePair<?>) box.getSelectedItem();
            if (grpname != null) {
                return (BreakpointGroup) grpname.getValue();
            } else {
                return null;
            }
        } catch (ClassCastException cce) {
            return null;
        }
    }
}
