/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Marko van Dooren
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
 * MODULE:      JSwat Actions
 * FILE:        JSwatFileFilter.java
 *
 * AUTHOR:      Marko van Dooren
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      MvD     03/10/02        Initial version
 *
 * $Id: JSwatFileFilter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import java.io.File;
import java.util.List;
import javax.swing.filechooser.FileFilter;

/**
 * Class to implement a FileFilter to allow only supported
 * source files in the JFileChooser. Note that this FileFilter
 * is the com.sun.java.swing.filechooser.FileFilter and not
 * the java.io.FileFilter.
 *
 * @author  Marko van Dooren
 */
public class JSwatFileFilter extends FileFilter {
    /** The extensions of this JSwatFileFilter. */
    private List extensions;
    /** The description of this JSwatFileFilter. */
    private String description;

    /**
     * Initialize a new JSwatFileFilter with the given extension
     * and description.
     *
     * @param extensions   the extensions of the new JSwatFileFilter.
     * @param description  description for the file type of the new
     *                     JSwatFileFilter.
     */
    public JSwatFileFilter(List extensions, String description) {
        this.extensions = extensions;
        this.description = description;
    } // JSwatFileFilter

    /**
     * Tests whether or not the specified abstract pathname should
     * be included in a pathname list.
     *
     * @param  pathname  The pathname to be tested.
     * @return  true if the pathname should be included.
     */
    public boolean accept(File pathname) {
        if (pathname == null)
            return false;

        if (pathname.isDirectory())
            return true;

        String name = pathname.getName().toLowerCase();

        for (java.util.Iterator i = extensions.iterator(); i.hasNext();)
            if (name.endsWith((String) i.next()))
                return true;

        return false;
    } // accept

    /**
     * Returns the description of this JSwatFileFilter.
     *
     * @return  string representation of this JSwatFileFilter.
     */
    public String getDescription() {
        return description;
    } // getDescription

    /**
     * Return the extensions of this JSwatFileFilter.
     *
     * @return  file name extension.
     */
    public List getExtensions() {
        return extensions;
    } // getExtension
} // JSwatFileFilter
