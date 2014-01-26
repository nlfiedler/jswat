/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * MODULE:      Report
 * FILE:        LoggingEvent.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/07/01        Initial version
 *
 * DESCRIPTION:
 *      This file defines the LoggingEvent class.
 *
 * $Id: LoggingEvent.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.report;

/**
 * Class LoggingEvent represents a debug log event. It encapsulates
 * the message, originating category, and possibly other information
 * about the event.
 *
 * @author  Nathan Fiedler
 * @version 1.0  4/7/01
 */
public class LoggingEvent {
    /** Category that originated the event. */
    protected Category eventCategory;
    /** Log message. */
    protected String eventMessage;
    /** Name of the thread that generated the event. */
    protected String threadName;

    /**
     * Constructs a logging event using the given source.
     *
     * @param  cat  category from which the event was reported.
     * @param  msg  log message.
     */
    public LoggingEvent(Category cat, String msg) {
        eventCategory = cat;
        eventMessage = msg;
        threadName = Thread.currentThread().getName();
    } // LoggingEvent

    /**
     * Returns the originating category for this event.
     *
     * @return  Category that created this event.
     */
    public Category getCategory() {
        return eventCategory;
    } // getCategory

    /**
     * Returns the name of the category for this event.
     *
     * @return  name of event category.
     */
    public String getCategoryName() {
        return eventCategory.getName();
    } // getCategoryName

    /**
     * Returns the message for this event.
     *
     * @return  log message.
     */
    public String getMessage() {
        return eventMessage;
    } // getMessage

    /**
     * Returns the name of the thread that created this event.
     *
     * @return  name of thread.
     */
    public String getThreadName() {
        return threadName;
    } // getThreadName
} // LoggingEvent
