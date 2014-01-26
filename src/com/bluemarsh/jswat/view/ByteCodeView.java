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
 * FILE:        ByteCodeView.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/04/03        Initial version
 *
 * $Id: ByteCodeView.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.SourceSource;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

//
// How this would work:
//
// Given class name, find the .class file and create a ClassFile from
// jclasslib to get the complete class information; sort the methods by
// line number (if available); populate the text area with the method
// descriptors followed by their byte code as given by jclasslib; store
// the code offset information in a table that indicates the matching
// line offset within the text area; when showing the current location,
// use the code index of the method to determine which line of the
// source view to highlight.
//

/**
 * Class ByteCodeView displays bytecode from a class.
 *
 * @author  Nathan Fiedler
 */
class ByteCodeView implements View {
    /** Name of the class we are viewing. */
    private String className;
    /** Fully-qualified name of the class we are viewing. */
    private String fullClassName;
    /** Text area for displaying byte code. */
    private JTextComponent textArea;

    /**
     * Constructs a ByteCodeView to view the named class.
     *
     * @param  src  source object.
     */
    public ByteCodeView(SourceSource src) {
        className = src.getName();
        fullClassName = src.getLongName();
        textArea = new JTextArea(
            "The source for the class " + fullClassName
            + " could not be found.\nIn the future, this view will show"
            + " the class byte code.");
        textArea.setEditable(false);
    } // ByteCodeView

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
    public boolean findString(String query, boolean ignoreCase) {
        // Someday this will do something.
        return false;
    } // findString

    /**
     * Returns the long version of title of this view. This may be a
     * file name and path, a fully-qualified class name, or whatever is
     * appropriate for the type of view.
     *
     * @return  long view title.
     */
    public String getLongTitle() {
        return fullClassName;
    } // getLongTitle

    /**
     * Returns the title of this view. This may be a file name, a class
     * name, or whatever is appropriate for the type of view.
     *
     * @return  view title.
     */
    public String getTitle() {
        return className;
    } // getTitle

    /**
     * Returns a reference to the UI component which can be added to the
     * user interface component tree.
     *
     * @return  interface component.
     */
    public JComponent getUI() {
        return textArea;
    } // getUI

    /**
     * Read the source data and display the contents in the view, as
     * appropriate for the concrete view implementation.
     *
     * @param  src   source for the view to read from.
     * @param  line  line to make visible.
     * @throws  IOException
     *          if an I/O error occurs in reading the data.
     */
    public void refresh(SourceSource src, int line) throws IOException {
        // Someday this will do something.
    } // refresh

    /**
     * Scrolls the view to the given line, if possible. Any value less
     * than one is ignored.
     *
     * @param  line  line to scroll to (1-based).
     */
    public void scrollToLine(int line) {
        // Someday this will do something.
    } // scrollToLine
} // ByteCodeView
