/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * MODULE:      JSwat
 * FILE:        SourceSource.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/20/02        Initial version
 *
 * $Id: SourceSource.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import java.io.InputStream;

/**
 * SourceSource provides a means of reading program code through an
 * input stream. Related information is provided through accessor
 * methods to facilitate displaying the program code.
 *
 * @author  Nathan Fiedler
 */
public interface SourceSource {

    /**
     * Attempts to ensure that the referenced source actually exists.
     *
     * @return  true if source exists, false if not found.
     */
    boolean exists();

    /**
     * Get the input stream for reading the source object.
     *
     * @return  input stream to the source object.
     */
    InputStream getInputStream();

    /**
     * Get the full name of the source object. This may be the path and
     * file name of a file (may or may not be a canonical path), the
     * fully-qualified name of a class, or a zip entry and file name.
     * This may be the same as the short name.
     *
     * @return  long name of source object.
     */
    String getLongName();

    /**
     * Returns just the name of the source object, not including any
     * prefix such as a path or package name.
     *
     * @return  name of source object.
     */
    String getName();

    /**
     * Returns the name of the package for the class that this source
     * object represents, if available.
     *
     * @return  package name, or null if not applicable.
     */
    String getPackage();

    /**
     * Returns the complete path to the source file, if the source
     * object is stored in a file that is not an archive.
     *
     * @return  file path, or null if not applicable.
     */
    String getPath();

    /**
     * Indicates if this source object represents byte code, as
     * opposed to source code of a high-level language.
     *
     * @return  true if byte code, false otherwise.
     */
    boolean isByteCode();

    /**
     * Sets the name of the package for the class that this source
     * object represents.
     *
     * @param  pkg  package name.
     */
    void setPackage(String pkg);
} // SourceSource
