/*********************************************************************
 *
 *	Copyright (C) 2000-2005 Nathan Fiedler
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
 * $Id: FileWriter.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * Class FileWriter takes a tree of Element objects and writes them
 * out to a file using the JConfigure preferences file format.
 *
 * @author  Nathan Fiedler
 */
public class FileWriter {

    /**
     * Writes the element tree to the output stream in the
     * JConfigure preferences file format.
     *
     * @param  os    Output stream to write to.
     * @param  root  Root of element tree.
     * @return  True if successful, false if error.
     */
    public boolean write(OutputStream os, RootElement root) {
        // Create a print writer to make things easy.
        PrintWriter pw = new PrintWriter(os);

        // Write the 'root' keyword and opening brace.
        pw.println("root [");

        // Write out root element attributes.
        pw.print("  version ");
        pw.println(root.getVersion());
        writeNameAndComment(pw, root);
        writeLabel(pw, root);
        writeTypedefs(pw, root);

        // Write out root's children.
        writeChildren(pw, root);

        // Write the closing brace for root and close the file.
        pw.println("]");
        pw.close();
        return true;
    } // write

    /**
     * Writes the given buttongroup to the print writer.
     *
     * @param  pw    Print writer to write to.
     * @param  elem  ButtonGroupElement to write.
     * @return  True if successful, false if error.
     */
    protected boolean writeButtonGroup(PrintWriter pw,
                                       ButtonGroupElement elem) {
        pw.println("buttongroup [");

        writeNameAndComment(pw, elem);

        // Write out the element children.
        writeChildren(pw, elem);

        // Write the closing brace and return okay.
        pw.println("]");
        return true;
    } // writeButtonGroup

    /**
     * Writes the given element's children to the print writer.
     *
     * @param  pw    Print writer to write to.
     * @param  elem  Parent element.
     * @return  True if successful, false if error.
     */
    protected boolean writeChildren(PrintWriter pw, Element elem) {

        Element child = elem.getChild();
        while (child != null) {
            if (child instanceof OptionElement) {
                writeOption(pw, (OptionElement) child);
            } else if (child instanceof GroupElement) {
                writeGroup(pw, (GroupElement) child);
            } else if (child instanceof LabeledElement) {
                writeLabel(pw, (LabeledElement) child);
            } else if (child instanceof ButtonGroupElement) {
                writeButtonGroup(pw, (ButtonGroupElement) child);
            }

            // Move to the next sibling.
            child = child.getSibling();
        }

        return true;
    } // writeChildren

    /**
     * Writes the given group to the print writer.
     *
     * @param  pw    Print writer to write to.
     * @param  elem  GroupElement to write.
     * @return  True if successful, false if error.
     */
    protected boolean writeGroup(PrintWriter pw, GroupElement elem) {
        pw.println("group [");

        writeNameAndComment(pw, elem);
        writeLabel(pw, elem);

        // For groups, write out the description.
        String desc = elem.getDescription();
        if (desc != null) {
            pw.print("  description \"");
            pw.print(desc);
            pw.println("\"");
        }

        // Write out the element children.
        writeChildren(pw, elem);

        // Write the closing brace and return okay.
        pw.println("]");
        return true;
    } // writeGroup

    /**
     * Writes the given element's label to the print writer.
     *
     * @param  pw    Print writer to write to.
     * @param  elem  LabeledElement to write.
     * @return  True if successful, false if error.
     */
    protected boolean writeLabel(PrintWriter pw, LabeledElement elem) {
        pw.print("  label \"");
        pw.print(elem.getLabel());
        pw.println("\"");
        return true;
    } // writeLabel

    /**
     * Writes the give element's name and comment to the print writer.
     *
     * @param  pw    Print writer to write to.
     * @param  elem  Element to write.
     * @return  True if successful, false if error.
     */
    protected boolean writeNameAndComment(PrintWriter pw, Element elem) {
        String name = elem.getName();
        if (name != null) {
            pw.print("  name \"");
            pw.print(name);
            pw.println("\"");
        }
        String comment = elem.getComment();
        if ((comment != null) && (comment.length() > 0)) {
            pw.print("  comment \"");
            pw.print(comment);
            pw.println("\"");
        }
        return true;
    } // writeNameAndComment

    /**
     * Writes the given option to the print writer.
     *
     * @param  pw    Print writer to write to.
     * @param  elem  OptionElement to write.
     * @return  True if successful, false if error.
     */
    protected boolean writeOption(PrintWriter pw, OptionElement elem) {
        pw.print("option ");
        pw.print(elem.getTypeName());
        pw.println(" [");

        writeNameAndComment(pw, elem);
        writeLabel(pw, elem);

        pw.print("  value \"");
        pw.print(elem.getValue());
        pw.println("\"");
        String width = elem.getWidth();
        if (width != null) {
            pw.print("  width \"");
            pw.print(width);
            pw.println("\"");
        }

        // Write out the element children.
        writeChildren(pw, elem);

        // Write the closing brace and return okay.
        pw.println("]");
        return true;
    } // writeOption

    /**
     * Writes the type definitions contained in the root element.
     *
     * @param  pw    Print writer to write to.
     * @param  root  RootElement.
     * @return  True if successful, false if error.
     */
    protected boolean writeTypedefs(PrintWriter pw, RootElement root) {
        Enumeration enmr = root.typedefs();
        while (enmr.hasMoreElements()) {
            Typedef type = (Typedef) enmr.nextElement();
            pw.println("  type [");
            pw.print("    typename \"");
            pw.print(type.getTypeName());
            pw.println("\"");
            pw.print("    typeclass \"");
            pw.print(type.getTypeClass().getName());
            pw.println("\"");
            pw.println("  ]");
        }
        return true;
    } // writeTypedefs
} // FileWriter
