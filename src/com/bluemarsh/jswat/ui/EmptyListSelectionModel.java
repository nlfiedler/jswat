/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: EmptyListSelectionModel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

/**
 * Class EmptyListSelectionModel implements a selection model in which
 * nothing can be selected.
 *
 * @author  Nathan Fiedler
 */
public class EmptyListSelectionModel extends DefaultListSelectionModel {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Our shared instance. */
    protected static final EmptyListSelectionModel SHARED_INSTANCE =
        new EmptyListSelectionModel();

    /**
     * None shall construct us.
     */
    private EmptyListSelectionModel() {
    } // EmptyListSelectionModel

    /**
     * Do absolutely nothing.
     *
     * @param  index0  one end of the interval.
     * @param  index1  other end of the interval
     */
    public void addSelectionInterval(int index0, int index1) { }

    /**
     * Do absolutely nothing.
     *
     * @param  index0  one end of the interval.
     * @param  index1  other end of the interval
     */
    public void removeSelectionInterval(int index0, int index1) { }

    /**
     * Do absolutely nothing.
     *
     * @param  index  selection row.
     */
    public void setAnchorSelectionIndex(int index) { }

    /**
     * Set the lead selection index.
     *
     * @param  index  selection row.
     */
    public void setLeadSelectionIndex(int index) { }

    /**
     * Do absolutely nothing.
     *
     * @param  index0  one end of the interval.
     * @param  index1  other end of the interval
     */
    public void setSelectionInterval(int index0, int index1) { }

    /**
     * Returns the shared instance.
     *
     * @return  shared instance.
     */
    public static ListSelectionModel sharedInstance() {
        return SHARED_INSTANCE;
    } // sharedInstance
} // EmptyListSelectionModel
