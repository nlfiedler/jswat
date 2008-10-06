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
 * MODULE:      Panel
 * FILE:        PanelFactory.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/06/03        Initial version
 *
 * $Id: PanelFactory.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.SessionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Class PanelFactory is a singleton that creates panels based on
 * keywords. Each type of panel has its own keyword, such as
 * "breakpoints" and "threads".
 *
 * @author  Nathan Fiedler
 */
public class PanelFactory {
    /** Name of panel that displays defined breakpoints. */
    public static final String PANEL_BREAKPOINTS = "breakpoints";
    /** Name of panel that displays loaded classes. */
    public static final String PANEL_CLASSES = "classes";
    /** Name of panel that displays local variables. */
    public static final String PANEL_LOCALS = "locals";
    /** Name of panel that displays methods at current location. */
    public static final String PANEL_METHODS = "methods";
    /** Name of panel that displays stack frame. */
    public static final String PANEL_STACK = "stack";
    /** Name of panel that displays threads. */
    public static final String PANEL_THREADS = "threads";
    /** Name of panel that displays variable watches. */
    public static final String PANEL_WATCHES = "watches";
    /** The one instance of this class. */
    private static PanelFactory theInstance;
    /** Map of panels that have been created. Keys are their names,
     * while the values are the Panel instances. */
    private Map loadedPanels;

    static {
        theInstance = new PanelFactory();
    }

    /**
     * This class cannot be instantiated.
     */
    private PanelFactory() {
        loadedPanels = new HashMap();
    } // PanelFactory

    /**
     * Retrieve the instance of the Panel for the given name. This method
     * will create the panel if necessary.
     *
     * @param  name  name of panel to get.
     * @return  a new panel instance.
     * @throws  ClassNotFoundException
     *          if the panel class could not be found.
     * @throws  IllegalAccessException
     *          if constructor has protected access.
     * @throws  InstantiationException
     *          if an panel instance could not be instantiated.
     */
    public Panel get(String name)
        throws ClassNotFoundException,
               IllegalAccessException,
               InstantiationException {
        return get(name, null);
    } // get

    /**
     * Retrieve the instance of the Panel for the given name. This
     * method will create the panel if necessary. If the panel
     * implements the SessionListener interface, add it as a listener to
     * the given session.
     *
     * @param  name     name of panel to get.
     * @param  session  session to add panel to as a listener.
     * @return  a new panel instance.
     * @throws  ClassNotFoundException
     *          if the panel class could not be found.
     * @throws  IllegalAccessException
     *          if constructor has protected access.
     * @throws  InstantiationException
     *          if an panel instance could not be instantiated.
     */
    public synchronized Panel get(String name, Session session)
        throws ClassNotFoundException,
               IllegalAccessException,
               InstantiationException {
        Panel panel = (Panel) loadedPanels.get(name);
        if (panel == null) {
            // All the panel classes are specified in the Bundle.
            String panelClass = Bundle.getString(name + "Class");
            if (panelClass != null) {
                Class clazz = Class.forName(panelClass);
                panel = (Panel) clazz.newInstance();
            }
            loadedPanels.put(name, panel);
            if (session != null && panel instanceof SessionListener) {
                session.addListener((SessionListener) panel);
            }
        }
        return panel;
    } // get

    /**
     * Returns the instance of this class.
     *
     * @return  a PanelFactory instance.
     */
    public static PanelFactory getInstance() {
        return theInstance;
    } // getInstance
} // PanelFactory
