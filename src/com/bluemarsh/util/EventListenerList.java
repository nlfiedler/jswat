/*********************************************************************
 *
 *	Copyright (C) 2001-2002 Nathan Fiedler
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
 * License along with this library; if not, write to the Free
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * PROJECT:	Utilities
 * MODULE:	EventListenerList
 * FILE:	EventListenerList.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *	Name	Date		Description
 *	----	----		-----------
 *	nf	08/14/01	Initial version
 *      nf      01/12/02        Added toString() method
 *
 * DESCRIPTION:
 *      Defines the event listener list data type.
 *
 * $Id: EventListenerList.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.util;

import java.lang.reflect.Array; 
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

/**
 * Class EventListenerList is much like the same-named class in the
 * Sun JFC package. It is a trivial implementation backed by an
 * instance of <code>java.util.ArrayList</code>.
 *
 * <p>Many of these methods are not synchronized and so care should
 * be exercised.</p>
 *
 * @author  Nathan Fiedler
 */
public class EventListenerList {
    /** Event listener list. The odd-numbered entries are the
     * listener types and the even-numbered entries are the
     * listeners instances. */
    protected List listenerList;

    /**
     * Constructs an empty EventListenerList.
     */
    public EventListenerList() {
        listenerList = new ArrayList();
    } // EventListenerList

    /**
     * Adds the listener as a listener of the specified type.
     *
     * @param  t  the type of the listener to be added.
     * @param  l  the listener to be added.
     */
    public synchronized void add(Class t, EventListener l) {
	if (l == null) {
	    return;
	}
	if (!t.isInstance(l)) {
	    throw new IllegalArgumentException("Listener " + l +
                                               " is not of type " + t);
	}

        // Add the type first, then the listener.
        listenerList.add(t);
        listenerList.add(l);
    } // add

    /**
     * Returns the total number of listeners for this listener list.
     *
     * @return  number of listeners in list.
     */
    public int getListenerCount() {
	return listenerList.size() / 2;
    } // getListenerCount

    /**
     * Returns the total number of listeners of the supplied type 
     * for this listener list.
     *
     * @return  number of listeners of given type.
     */
    public int getListenerCount(Class t) {
	int count = 0;
        Iterator iter = listenerList.iterator();
        while (iter.hasNext()) {
            Class type = (Class) iter.next();
            if (t == type) {
                count++;
            }
            // If we made a mistake this may throw NoSuchElementException.
            iter.next();
        }
	return count;
    } // getListenerCount

    /**
     * Passes back the event listener list as an array of
     * ListenerType-listener pairs.
     * This method is guaranteed to pass back a non-null array,
     * so that no null-checking is required in fire methods.
     * A zero-length array of Object should be returned if there
     * are currently no listeners.
     */
    public Object[] getListenerList() {
        return listenerList.toArray();
    } // getListenerList

    /**
     * Return an array of all the listeners of the given type.
     * 
     * @return  all of the listeners of the specified type. 
     */
    public EventListener[] getListeners(Class t) {
        // Create an array of the appropriate size.
	int n = getListenerCount(t); 
        EventListener[] result = (EventListener[]) Array.newInstance(t, n); 

        // Copy the listeners of the given type to the array.
        Iterator iter = listenerList.iterator();
        int i = 0;
        while (iter.hasNext()) {
            Class type = (Class) iter.next();
            // If we made a mistake this may throw NoSuchElementException.
            Object l = iter.next();
            if (t == type) {
                result[i] = (EventListener) l;
                i++;
            }
        }
	return result;   
    } // getListeners

    /**
     * Removes the listener as a listener of the specified type.
     *
     * @param  t  the type of the listener to be removed.
     * @param  l  the listener to be removed.
     */
    public synchronized void remove(Class t, EventListener l) {
	if (l == null) {
	    return;
	}
	if (!t.isInstance(l)) {
	    throw new IllegalArgumentException("Listener " + l +
                                               " is not of type " + t);
	}

	// Find the listener and remove it.
	int index = listenerList.indexOf(l);
	if (index != -1) {
            // Remove the listener instance and the type.
            listenerList.remove(index);
            // If we made a mistake, this may throw
            // ArrayIndexOutOfBoundsException.
            listenerList.remove(index - 1);
        }
    } // remove

    /**
     * Returns a string representation of this.
     *
     * @return  String of this.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("EventListenerList=[");
        buf.append(listenerList.toString());
        buf.append("]");
        return buf.toString();
    } // toString
} // EventListenerList
