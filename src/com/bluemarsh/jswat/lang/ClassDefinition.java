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
 * $Id: ClassDefinition.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang;

import java.util.Iterator;
import java.util.List;

/**
 * ClassDefinition gives the classname and the start and end line
 * numbers of a class definition.
 *
 * @author  Nathan Fiedler
 */
public class ClassDefinition {
    /** Line number at which class definition begins. */
    private int beginLine;
    /** Line number at which the opening brace exists. */
    private int openBraceLine;
    /** Line number at which class definition ends. */
    private int endLine;
    /** Name of class. */
    private String className;

    /**
     * Constructs a ClassDefinition object with the given name.
     *
     * @param  name   name of the class.
     * @param  brace  line of opening brace for class.
     * @param  begin  first line of the class.
     * @param  end    last line of the class.
     */
    public ClassDefinition(String name, int brace, int begin, int end) {
        className = name;
        openBraceLine = brace;
        beginLine = begin;
        endLine = end;
    } // ClassDefinition

    /**
     * Finds the class defined at this line number.
     *
     * @param  classDefs  list of ClassDefinition objects.
     * @param  line       line number.
     * @return  class name, or null if line not in a class.
     */
    public static String findClassForLine(List classDefs, int line) {
        if (classDefs == null) {
            // Happens if parsing failed due to syntax error.
            return null;
        }
        int begin = Integer.MIN_VALUE;
        int end = Integer.MAX_VALUE;
        String cname = null;
        Iterator iter = classDefs.iterator();
        // Go through the entire list to find the closest match.
        while (iter.hasNext()) {
            ClassDefinition cd = (ClassDefinition) iter.next();
            if ((cd.getBeginLine() <= line) && (cd.getEndLine() >= line)
                && (cd.getBeginLine() > begin) && (cd.getEndLine() < end)) {
                cname = cd.getClassName();
                begin = cd.getBeginLine();
                end = cd.getEndLine();
            }
        }
        return cname;
    } // findClassForLine

    /**
     * Get the line where the class definition begins.
     *
     * @return  beginning line.
     */
    public int getBeginLine() {
        return beginLine;
    } // getBeginLine

    /**
     * Get the line where the opening brace for the class exists.
     *
     * @return  open brace line.
     */
    public int getBraceLine() {
        return openBraceLine;
    } // getBraceLine

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
     *
     * @return  string of this.
     */
    public String toString() {
        StringBuffer b = new StringBuffer("ClassDefinition=[");
        b.append(className);
        b.append(", ");
        b.append(openBraceLine);
        b.append(", ");
        b.append(beginLine);
        b.append(", ");
        b.append(endLine);
        b.append(']');
        return b.toString();
    } // toString
} // ClassDefinition
