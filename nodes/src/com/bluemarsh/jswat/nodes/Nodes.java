/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Nodes.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes;

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
