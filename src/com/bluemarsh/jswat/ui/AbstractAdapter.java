/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 * MODULE:      JSwat UI
 * FILE:        AbstractAdapter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/11/01        Initial version
 *      nf      12/27/02        Added property change support
 *      nf      11/22/03        Renamed
 *
 * $Id: AbstractAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.view.View;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.Hashtable;

/**
 * Class AbstractAdapter provides a limited implementation of the
 * UIAdapter interface. Concrete adapter implementations should extend
 * this class.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractAdapter implements UIAdapter {
    /** Table of properties. */
    private Hashtable propertyTable;
    /** Used for signaling changes in bound properties. */
    private PropertyChangeSupport changeSupport;

    /**
     * Constructor for AbstractAdapter class.
     */
    public AbstractAdapter() {
        propertyTable = new Hashtable();
        changeSupport = new PropertyChangeSupport(this);
    } // AbstractAdapter

    /**
     * Add a PropertyChangeListener to the listener list. The listener is
     * registered for all properties.
     *
     * @param  listener  the PropertyChangeListener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    } // addPropertyChangeListener

    /**
     * Add a PropertyChangeListener for a specific property. The listener
     * will be invoked only when a call on firePropertyChange names that
     * specific property.
     *
     * @param  propertyName  the name of the property to listen on.
     * @param  listener      the PropertyChangeListener to be added.
     */

    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    } // addPropertyChangeListener

    /**
     * Returns the PropertyChangeSupport instance for this adapter.
     *
     * @return  property change support.
     */
    public PropertyChangeSupport getChangeSupport() {
        return changeSupport;
    } // getChangeSupport

    /**
     * Searches for the property with the specified key in the property
     * list. The method returns null if the property is not found.
     *
     * @param  key  the property key.
     * @return  the value in the property list with the specified key value.
     */
    public Object getProperty(String key) {
        return propertyTable.get(key);
    } // getProperty

    /**
     * Remove a PropertyChangeListener from the listener list. This removes
     * a PropertyChangeListener that was registered for all properties.
     *
     * @param  listener  the PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    } // removePropertyChangeListener

    /**
     * Remove a PropertyChangeListener for a specific property.
     *
     * @param  propertyName  the name of the property that was listened on.
     * @param  listener      the PropertyChangeListener to be removed.
     */

    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    } // removePropertyChangeListener

    /**
     * Stores the given value in the properties list with the given key
     * as a reference. If the value is null, then the key and value will
     * be removed from the properties.
     *
     * @param  key    the key to be placed into this property list.
     * @param  value  the value corresponding to key, or null to remove
     *                the key and value from the properties.
     * @return  previous value stored using this key.
     */
    public Object setProperty(String key, Object value) {
        if (value == null) {
            return propertyTable.remove(key);
        } else {
            return propertyTable.put(key, value);
        }
    } // setProperty
} // AbstractAdapter
