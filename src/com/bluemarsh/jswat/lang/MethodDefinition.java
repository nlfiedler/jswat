/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
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
 * FILE:        MethodDefinition.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/22/02        Initial version
 *      nf      04/28/03        Added class name attribute
 *      nf      11/12/03        Fixed bug 806
 *      nf      11/17/03        Moved to new package
 *      nf      01/14/04        Fixed bug 819
 *
 * $Id: MethodDefinition.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang;

/**
 * MethodDefinition gives the method descriptor and the line number of a
 * method definition.
 *
 * @author  Nathan Fiedler
 */
public class MethodDefinition {
    /** Starting line number of the method definition. */
    private int line;
    /** Name of the class containing this method. */
    private String className;
    /** Full descriptor of method. */
    private String methodDesc;
    /** Short descriptor of method. */
    private String methodDescShort;

    /**
     * Constructs a MethodDefinition object with the given data.
     *
     * @param  desc  full method descriptor.
     * @param  sign  short method descriptor.
     * @param  line  line number of start of method.
     */
    public MethodDefinition(String desc, String sign, int line) {
        methodDesc = desc;
        methodDescShort = sign;
        this.line = line;
    } // MethodDefinition

    /**
     * Get the name of the class that defines the method.
     *
     * @return  defining class.
     */
    public String getClassName() {
        return className;
    } // getClassName

    /**
     * Get the line where the method definition begins.
     *
     * @return  beginning line.
     */
    public int getLine() {
        return line;
    } // getLine

    /**
     * Return the method descriptor.
     *
     * @return  method descriptor.
     */
    public String getMethodDesc() {
        return methodDesc;
    } // getMethodDesc

    /**
     * Return the short method descriptor.
     *
     * @return  short method descriptor.
     */
    public String getMethodDescShort() {
        return methodDescShort;
    } // getMethodDescShort

    /**
     * Sets the name of the class that defines this method.
     *
     * @param  cname  name of the defining class.
     */
    public void setClassName(String cname) {
        // Have to allow for the name change to occur.
        className = cname;
        methodDesc = cname + "." + methodDesc;
        int i = cname.lastIndexOf('.');
        if (i > 0) {
            cname = cname.substring(i + 1);
        }
        methodDescShort = cname + "." + methodDescShort;
    } // setClassName

    /**
     * Return a String representation of this.
     *
     * @return  string of this.
     */
    public String toString() {
        StringBuffer b = new StringBuffer("MethodDefinition=[");
        b.append(methodDesc);
        b.append(" @ ");
        b.append(line);
        b.append(']');
        return b.toString();
    } // toString
} // MethodDefinition
