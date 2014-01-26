/*********************************************************************
 *
 *      Copyright (C) 2003 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      View
 * FILE:        BreakpointTooltipProducer.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      11/05/03        Initial version
 *
 * $Id: BreakpointTooltipProducer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.Condition;
import com.bluemarsh.jswat.breakpoint.Monitor;
import com.bluemarsh.jswat.lang.ClassDefinition;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

/**
 * Produces tooltips that display detailed information for the
 * breakpoint under the mouse pointer, if the mouse is over the view
 * gutter and a breakpoint exists at that line.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointTooltipProducer implements TooltipProducer {
    /** Our tooltip producer priority. */
    private static final int PRIORITY = 256;
    /** List of ClassDefinition objects for the source view we are
     * attached to. */
    private List classDefinitions;

    /**
     * Gets the priority level of this particular tooltip producer.
     * Typically each type of producer has its own priority. Lower
     * values are higher priority.
     *
     * @return  priority level.
     */
    public int getPriority() {
        return PRIORITY;
    } // getPriority

    /**
     * Produce the detailed information for the given breakpoint.
     *
     * @param  brk  breakpoint to detail.
     * @return  detailed, multi-line, breakpoint information.
     */
    private String produceDetail(Breakpoint brk) {
        StringBuffer buf = new StringBuffer();

        // Breakpoint and its group
        buf.append(brk);
        buf.append("<br>");
        BreakpointGroup brkgrp = brk.getBreakpointGroup();
        buf.append(Bundle.getString("brkinfo.brkgrp"));
        buf.append(' ');
        buf.append(brkgrp.getName());

        // Breakpoint expiration, skip count, etc.
        int expires = brk.getExpireCount();
        if (expires > 0) {
            buf.append("<br>");
            buf.append(Bundle.getString("brkinfo.expires"));
            buf.append(' ');
            buf.append(Integer.toString(expires));
        }
        int skips = brk.getSkipCount();
        if (skips > 0) {
            buf.append("<br>");
            buf.append(Bundle.getString("brkinfo.skips"));
            buf.append(' ');
            buf.append(Integer.toString(skips));
        }

        // Breakpoint state
        buf.append("<br>");
        if (brk.isEnabled()) {
            buf.append(Bundle.getString("brkinfo.enabled"));
        } else {
            buf.append(Bundle.getString("brkinfo.disabled"));
        }
        buf.append("<br>");
        if (brk.isResolved()) {
            buf.append(Bundle.getString("brkinfo.resolved"));
        } else {
            buf.append(Bundle.getString("brkinfo.unresolved"));
        }
        if (brk.hasExpired()) {
            buf.append("<br>");
            buf.append(Bundle.getString("brkinfo.expired"));
        }
        if (brk.isSkipping()) {
            buf.append("<br>");
            buf.append(Bundle.getString("brkinfo.skipping"));
        }

        // Breakpoint filters
        String filters = brk.getClassFilters();
        if (filters != null && filters.length() > 0) {
            buf.append("<br>");
            buf.append(Bundle.getString("brkinfo.classFilters"));
            buf.append(' ');
            buf.append(filters);
        }
        filters = brk.getThreadFilters();
        if (filters != null && filters.length() > 0) {
            buf.append("<br>");
            buf.append(Bundle.getString("brkinfo.threadFilters"));
            buf.append(' ');
            buf.append(filters);
        }

        // Breakpoint conditions
        Iterator conditer = brk.conditions();
        if (conditer.hasNext()) {
            buf.append("<br>");
            buf.append(Bundle.getString("brkinfo.conditions"));
            while (conditer.hasNext()) {
                buf.append("<br>");
                Condition cond = (Condition) conditer.next();
                buf.append(cond.toString());
            }
        }

        // Breakpoint monitors
        Iterator moniter = brk.monitors();
        if (moniter.hasNext()) {
            buf.append("<br>");
            buf.append(Bundle.getString("brkinfo.monitors"));
            while (moniter.hasNext()) {
                buf.append("<br>");
                Monitor mon = (Monitor) moniter.next();
                buf.append(mon.toString());
            }
        }

        return buf.toString();
    } // produceDetail

    /**
     * Generate the appropriate tooltip for the given mouse event.
     * Checks if the mouse is over a breakpoint indicator, and if so
     * returns the detailed breakpoint information.
     *
     * @param  event  the event that caused the tooltip to appear.
     * @param  area   source view text area making the request.
     * @return  the tooltip, or null if none.
     */
    public String produceTooltip(MouseEvent event, SourceViewTextArea area) {
        Session session = SessionFrameMapper.getSessionForEvent(event);

        // First see if the mouse is in the gutter or not.
        Point pt = event.getPoint();
        if (area.isWithinGutter((int) pt.getX())) {
            int line = area.viewToLine(pt);

            String cname = ClassDefinition.findClassForLine(
                classDefinitions, line);
            if (cname != null) {
                BreakpointManager bpman = (BreakpointManager)
                    session.getManager(BreakpointManager.class);
                Breakpoint bp = bpman.getBreakpoint(cname, line);
                if (bp != null) {
                    String detail = produceDetail(bp);
                    return "<html><small>" + detail + "</small></html>";
                }
            }
        }

        // We have nothing to say.
        return null;
    } // produceTooltip

    /**
     * Sets the class definitions list for this producer.
     *
     * @param  classDefs  list of ClassDefinition objects.
     */
    public void setClassDefinitions(List classDefs) {
        classDefinitions = classDefs;
    } // setClassDefinitions
} // BreakpointTooltipProducer
