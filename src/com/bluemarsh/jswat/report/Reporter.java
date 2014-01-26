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
 * FILE:        Reporter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/07/01        Initial version
 *
 * DESCRIPTION:
 *      This file defines the Reporter interface.
 *
 * $Id: Reporter.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.report;

/**
 * A Reporter listens for logging events and displays them to the
 * user in some fashion or another. An implementation of Reporter
 * may print to the console, or it may print to a text pane, or
 * it could write to a log file.
 *
 * @author  Nathan Fiedler
 * @version 1.0  4/7/01
 */
public interface Reporter {

    /**
     * Report the given logging event.
     *
     * @param  event  logging event.
     */
    public void report(LoggingEvent event);
} // Reporter
