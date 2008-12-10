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
 * are Copyright (C) 2004-2008. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: MessageNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes;

import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Node for displaying a message and nothing more.
 *
 * @author  Nathan Fiedler
 */
public class MessageNode extends AbstractNode {
    /** The icon for nodes of this type. */
    private static Image nodeIcon;
    /** The message. */
    private String message;
    /** The tooltip for this node, if provided. */
    private String tooltip;

    /**
     * Constructs a new instance of MessageNode.
     *
     * @param  msg  message to be displayed.
     */
    public MessageNode(String msg) {
        super(Children.LEAF);
        message = msg;
    }

    /**
     * Constructs a new instance of MessageNode.
     *
     * @param  msg  message to be displayed.
     * @param  tip  tooltip value.
     */
    public MessageNode(String msg, String tip) {
        this(msg);
        tooltip = tip;
    }

    @Override
    public String getDisplayName() {
        return message;
    }

    @Override
    public Image getIcon(int type) {
        if (nodeIcon == null) {
            String url = NbBundle.getMessage(MessageNode.class,
                        "IMG_MessageNode");
            nodeIcon = ImageUtilities.loadImage(url);
        }
        return nodeIcon;
    }

    @Override
    public String getShortDescription() {
        if (tooltip != null) {
            return tooltip;
        }
        return super.getShortDescription();
    }
}
