/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: MessageTooltipProducer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang.java;

import com.bluemarsh.jswat.view.SourceViewTextArea;
import com.bluemarsh.jswat.view.TooltipProducer;
import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 * Produces tooltips that display an arbitrary message for a particular
 * line in the source view.
 *
 * @author  Nathan Fiedler
 */
public class MessageTooltipProducer implements TooltipProducer {
    /** Our tooltip producer priority. */
    private static final int PRIORITY = 256;
    /** The message to be displayed. */
    private String ourMessage;
    /** The line for which the messae is displayed. */
    private int ourLine;

    /**
     * Constructs a message tooltip producer.
     */
    public MessageTooltipProducer() {
    } // MessageTooltipProducer

    /**
     * Gets the priority level of this particular tooltip producer.
     * Typically each type of producer has its own priority. Lower
     * values are higher priority.
     *
     * @return  priority level.
     */
    public int getPriority() {
        return PRIORITY;
    } // getPriority

    /**
     * Generate the appropriate tooltip for the given mouse event.
     *
     * @param  event  the event that caused the tooltip to appear.
     * @param  area   source view text area making the request.
     * @return  the tooltip, or null if none.
     */
    public String produceTooltip(MouseEvent event, SourceViewTextArea area) {
        if (ourLine == area.viewToLine(event.getPoint())) {
            return ourMessage;
        } else {
            return null;
        }
    } // produceTooltip

    /**
     * Sets the message and the line associated with that message.
     *
     * @param  message  message to display in tooltip.
     * @param  line     line for which to display message.
     */
    public void setMessage(String message, int line) {
        ourMessage = message;
        ourLine = line;
    } // setMessage
} // MessageTooltipProducer
