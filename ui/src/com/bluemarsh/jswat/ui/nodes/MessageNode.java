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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: MessageNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.bluemarsh.jswat.ui.views.*;
import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

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

    public String getDisplayName() {
        return message;
    }

    public Image getIcon(int type) {
        if (nodeIcon == null) {
            String url = NbBundle.getMessage(MessageNode.class,
                        "IMG_MessageNode");
            nodeIcon = Utilities.loadImage(url);
        }
        return nodeIcon;
    }

    public String getShortDescription() {
        if (tooltip != null) {
            return tooltip;
        }
        return super.getShortDescription();
    }
}
