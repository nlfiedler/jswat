/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * FILE:        ClassDefinition.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/19/02        Initial version
 *
 * DESCRIPTION:
 *      This file contains the ClassDefinition class definition.
 *
 * $Id: ClassDefinition.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

/**
 * ClassDefinition gives the classname and the start and end line
 * numbers of a class definition.
 *
 * @author  Nathan Fiedler
 */
public class ClassDefinition {
    /** Line number at which class definition begins. */
    private int beginLine;
    /** Line number at which class definition ends. */
    private int endLine;
    /** Name of class. */
    private String className;

    /**
     * Constructs a ClassDefinition object with the given name.
     *
     * @param  name   name of the class.
     * @param  begin  first line of the class.
     * @param  end    last line of the class.
     */
    public ClassDefinition(String name, int begin, int end) {
        className = name;
        beginLine = begin;
        endLine = end;
    } // ClassDefinition

    /**
     * Get the line where the class definition begins.
     *
     * @return  beginning line.
     */
    public int getBeginLine() {
        return beginLine;
    } // getBeginLine

    /**
     * Get the name of the class definition.
     *
     * @return  class name.
     */
    public String getClassName() {
        return className;
    } // getClassName

    /**
     * Get the line where the class definition ends.
     *
     * @return  ending line.
     */
    public int getEndLine() {
        return endLine;
    } // getEndLine

    /**
     * Set the name of the class definition.
     *
     * @param  cname  name of the class.
     */
    public void setClassName(String cname) {
        className = cname;
    } // setClassName

    /**
     * Return a String representation of this.
     */
    public String toString() {
        StringBuffer b = new StringBuffer("ClassDefinition=[");
        b.append(className);
        b.append(", ");
        b.append(beginLine);
        b.append(", ");
        b.append(endLine);
        b.append(']');
        return b.toString();
    } // toString
} // ClassDefinition
