/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Nodes.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import java.awt.Color;

/**
 * Utility class for the nodes package.
 *
 * @author Nathan Fiedler
 */
public class Nodes {

    /**
     * Creates a new instance of Nodes.
     */
    private Nodes() {
    }

    /**
     * Converts the given label into HTML, adding the appropriate tags to
     * make the label bold, italic, or have a particular color.
     *
     * @param  text     plain text label.
     * @param  bold     true to make label bold.
     * @param  italics  true to make label italic.
     * @param  color    color to apply to label, or null for none.
     * @return  label with HTML markup.
     */
    public static String toHTML(String text, boolean bold, boolean italics,
            Color color) {
        // Code borrowed from NetBeans debugger.
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        if (bold) sb.append("<b>");
        if (italics) sb.append("<i>");
        if (color != null) {
            sb.append("<font color=");
            sb.append(Integer.toHexString(color.getRGB() & 0xffffff));
            sb.append(">");
        }
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll(">", "&gt;");
        sb.append(text);
        if (color != null) sb.append("</font>");
        if (italics) sb.append("</i>");
        if (bold) sb.append("</b>");
        sb.append("</html>");
        return sb.toString();
    }
}
