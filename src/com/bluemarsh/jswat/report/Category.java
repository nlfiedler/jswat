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
 * FILE:        Category.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/07/01        Initial version
 *
 * DESCRIPTION:
 *      This file defines the Category class.
 *
 * $Id: Category.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.report;

import com.bluemarsh.config.ConfigureListener;
import com.bluemarsh.config.JConfigure;
import com.bluemarsh.jswat.JSwat;
import java.util.Hashtable;

/**
 * Class Category is a singleton that provides access to a set of
 * reporting categories. Each reporting category has a set of reporters
 * that present logging events to the user in one way or another.
 *
 * @author  Nathan Fiedler
 * @version 1.0  4/7/01
 */
public class Category implements ConfigureListener {
    /** Table of instances of Category. */
    protected static Hashtable instances;
    /** Name of this category instance. */
    protected String categoryName;
    /** True if this category is enabled. */
    protected boolean isEnabled;
    /** Reporter group where logging events are sent. */
    protected ReporterGroup reporterGroup;

    static {
        instances = new Hashtable();
    }

    /**
     * Protected since this is a singleton.
     *
     * @param  name  category name.
     */
    protected Category(String name) {
        categoryName = name;
        // Get the reporter group instance so we can report to it.
        reporterGroup = new ReporterGroup(name);
        // Default to having the category enabled. That way, if there's
        // no configuration for it, it will still report messages.
        isEnabled = true;
        // Determine if we should be enabled or not.
        configurationChanged();
        // Listen for any future configuration changes.
        JConfigure config = JSwat.instanceOf().getJConfigure();
        config.addListener(this);
    } // Category

    /**
     * Invoked when the configuration has been accepted by the user.
     */
    public void configurationChanged() {
        JConfigure config = JSwat.instanceOf().getJConfigure();
        // See if this reporting category is enabled or not.
        boolean on = config.getBooleanProperty("report." + categoryName);
        // Set the category enabled or disabled appropriately.
        setEnabled(on);
        // Have the reporter group build out the reporters.
        reporterGroup.configureReporters();
    } // configurationChanged

    /**
     * Retrieves the default logging category. This is meant as a
     * fallback in those situations where the code generating the
     * event has no category assigned to it.
     *
     * @return  instance of default Category.
     */
    public static Category getDefaultInstance() {
        return instanceOf("default");
    } // getDefaultInstance

    /**
     * Returns the name of this category.
     *
     * @return  category name.
     */
    public String getName() {
        return categoryName;
    } // getName

    /**
     * Retrieves the instance of the named logging category.
     * If the named category does not exist, one will be created.
     *
     * @param  name  name of Category to get instance of.
     * @return  instance of named Category.
     */
    public static Category instanceOf(String name) {
        Category cat = (Category) instances.get(name);
        if (cat == null) {
            // It's not there, create a new one and put it there.
            cat = new Category(name);
            instances.put(name, cat);
        }
        return cat;
    } // instanceOf

    /**
     * Returns the enabled status of this category.
     *
     * @return  true if enabled, false if disabled.
     */
    public boolean isEnabled() {
        return isEnabled;
    } // isEnabled

    /**
     * Report a new debugging message to this category. A logging event
     * will be created and dispatched to any listening reporters.
     *
     * @param  msg  debugging message to report.
     */
    public void report(String msg) {
        if (isEnabled) {
            LoggingEvent event = new LoggingEvent(this, msg);
            // Send it off to the ReporterGroup instance.
            reporterGroup.report(event);
        }
    } // report

    /**
     * Set the enabled state of this category.
     *
     * @param  enable  true to enable this category, false to disable.
     */
    public void setEnabled(boolean enable) {
        isEnabled = enable;
    } // setEnabled
} // Category
