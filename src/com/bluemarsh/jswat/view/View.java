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
 * FILE:        View.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/06/03        Initial version
 *
 * $Id: View.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import java.io.IOException;
import javax.swing.JComponent;

/**
 * View defines the API expected of source and bytecode viewers in JSwat.
 *
 * @author  Nathan Fiedler
 */
public interface View {

    /**
     * Look for the given string in this view. Uses the view's current
     * text selection as the starting point. Will wrap around if the
     * string was not found after the current selection.
     *
     * @param  query       string to look for.
     * @param  ignoreCase  true to ignore case.
     * @return  true if string was found somewhere, false if string
     *          does not exist in this view.
     */
    boolean findString(String query, boolean ignoreCase);

    /**
     * Returns the long version of title of this view. This may be a
     * file name and path, a fully-qualified class name, or whatever is
     * appropriate for the type of view.
     *
     * @return  long view title.
     */
    String getLongTitle();

    /**
     * Returns the title of this view. This may be a file name, a class
     * name, or whatever is appropriate for the type of view.
     *
     * @return  view title.
     */
    String getTitle();

    /**
     * Returns a reference to the UI component which can be added to the
     * user interface component tree.
     *
     * @return  interface component.
     */
    JComponent getUI();

    /**
     * Read the source data and display the contents in the view, as
     * appropriate for the concrete view implementation.
     *
     * @param  src   source for the view to read from.
     * @param  line  line to make visible.
     * @throws  IOException
     *          if an I/O error occurs in reading the data.
     */
    void refresh(SourceSource src, int line) throws IOException;

    /**
     * Scrolls the view to the given line, if possible. Any value less
     * than one is ignored.
     *
     * @param  line  line to scroll to (1-based).
     */
    void scrollToLine(int line);
} // View
