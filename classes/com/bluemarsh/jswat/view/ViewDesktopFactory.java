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
 * FILE:        ViewDesktopFactory.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/12/03        Initial version
 *
 * $Id: ViewDesktopFactory.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

/**
 * Class ViewDesktopFactory creates ViewDesktop instances.
 *
 * @author  Nathan Fiedler
 */
public class ViewDesktopFactory {
    /** Display the views using a tabbed pane. */
    public static final int MODE_TABBED = 1;
    /** Display the views using a set of internal frames. */
    public static final int MODE_IFRAMES = 2;

    /**
     * Constructs a ViewDesktop of the requested variety.
     *
     * @param  mode  one of the mode constants.
     * @param  vm    view manager, passed to the new desktop.
     * @return  newly created view desktop.
     */
    public static ViewDesktop create(int mode, ViewManager vm) {
        ViewDesktop desktop = null;
        switch (mode) {
        case MODE_TABBED:
            desktop = new TabbedViewDesktop(vm);
            break;
        case MODE_IFRAMES:
            desktop = new WindowedViewDesktop(vm);
            break;
        default:
            throw new IllegalArgumentException("invalid mode value");
        }
        return desktop;
    } // create
} // ViewDesktopFactory
