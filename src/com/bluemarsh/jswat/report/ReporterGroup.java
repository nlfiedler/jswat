/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * MODULE:      Report
 * FILE:        ReporterGroup.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/08/01        Initial version
 *
 * DESCRIPTION:
 *      This file defines the ReporterGroup class.
 *
 * $Id: ReporterGroup.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.report;

import java.util.ArrayList;
import java.util.List;

/**
 * Class ReporterGroup manages a list of reporters for a particular
 * reporting category.
 *
 * @author  Nathan Fiedler
 * @version 1.0  4/8/01
 */
public class ReporterGroup {
    /** List of registered reporters. */
    protected List reporterList;
    /** Name of our reporting category. */
    protected String categoryName;

    /**
     * Construct a ReporterGroup for the named category.
     *
     * @param  name  reporting category name.
     */
    public ReporterGroup(String name) {
        reporterList = new ArrayList();
        categoryName = name;
    } // ReporterGroup

    /**
     * Uses the JConfigure settings to create the appropriate reporters
     * for this reporting category.
     */
    public void configureReporters() {
        try {
            synchronized (reporterList) {
                reporterList.clear();
            }
        } catch (UnsupportedOperationException uoe) {
            reporterList = new ArrayList();
        }
        // For now, just create a reporter that sends events to
        // the standard console.
        synchronized (reporterList) {
            reporterList.add(new ConsoleReporter());
        }
    } // configureReporters

    /**
     * Report a logging event to all of the registered reporters.
     *
     * @param  event  logging event to report.
     */
    public void report(LoggingEvent event) {
        synchronized (reporterList) {
            for (int i = 0; i < reporterList.size(); i++) {
                Reporter reporter = (Reporter) reporterList.get(i);
                reporter.report(event);
            }
        }
    } // report
} // ReporterGroup
