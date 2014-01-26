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
 * FILE:        ConsoleReporter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/13/01        Initial version
 *
 * DESCRIPTION:
 *      This file defines the ConsoleReporter class.
 *
 * $Id: ConsoleReporter.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.report;

/**
 * Class ConsoleReporter writes logging events to the standard output
 * output stream. This class is a convenience class that simply
 * extends FileReporter and uses the <code>System.out</code> output
 * stream by default.
 *
 * @author  Nathan Fiedler
 * @version 1.0  4/13/01
 */
public class ConsoleReporter extends FileReporter {

    /**
     * Constructs a ConsoleReporter that uses the standard output
     * stream as the default output.
     */
    public ConsoleReporter() {
        super(System.out);
    } // ConsoleReporter
} // ConsoleReporter
