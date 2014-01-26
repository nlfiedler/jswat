/*********************************************************************
 *
 *      Copyright (C) 2000 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * PROJECT:     JConfigure
 * FILE:        Typedef.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      10/01/00        Initial version
 *
 * DESCRIPTION:
 *      Defines the type definition class.
 *
 * $Id: Typedef.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

/**
 * Class Typedef defines an option element type.
 *
 * @author  Nathan Fiedler
 * @version 1.0  10/1/00
 */
public class Typedef {
    /** Name of the type (e.g. "text"). */
    protected String name;
    /** Class of the type
     * (e.g. "com.bluemarsh.config.TextOptionElement"). */
    protected Class clazz;

    /**
     * Returns the class of this type.
     */
    public Class getTypeClass() {
        return clazz;
    } // getTypeClass

    /**
     * Returns the name of this type.
     */
    public String getTypeName() {
        return name;
    } // getTypeName

    /**
     * Set the class of this type definition.
     */
    public void setTypeClass(Class clazz) {
        if (this.clazz == null) {
            this.clazz = clazz;
        }
    } // setTypeClass

    /**
     * Set the name of this type definition.
     */
    public void setTypeName(String name) {
        if (this.name == null) {
            this.name = name;
        }
    } // setTypeName
} // Typedef
