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
 * MODULE:      JSwat
 * FILE:        AbstractSource.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      11/17/03        Initial version
 *
 * $Id: AbstractSource.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

/**
 * Class AbstractSource provides the implementation for some of the
 * methods defined in the SourceSource interface. Concrete source
 * implementations should extend this class.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractSource implements SourceSource {
    /** Name of the package containing the class this object represents,
     * if known. */
    private String packageName;

    /**
     * Returns the name of the package for the class that this source
     * object represents, if available.
     *
     * @return  package name, or null if not applicable.
     */
    public String getPackage() {
        return packageName;
    } // getPackage

    /**
     * Sets the name of the package for the class that this source
     * object represents. The name cannot be changed once it is set.
     *
     * @param  pkg  package name; must not be null.
     */
    public void setPackage(String pkg) {
        if (pkg == null) {
            throw new IllegalArgumentException("pkg cannot be null");
        }
        if (packageName != null) {
            throw new IllegalStateException("package name already set");
        }
        packageName = pkg;
    } // setPackage
} // AbstractSource
