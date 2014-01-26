/*********************************************************************
 *
 *	Copyright (C) 2000 Nathan Fiedler
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
 * MODULE:	Lock
 * FILE:	SimpleLock.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *	Name	Date		Description
 *	----	----		-----------
 *	nf	07/03/00	Initial version
 *
 * DESCRIPTION:
 *      This file defines the SimpleLock class that defines a lock.
 *
 * $Id: SimpleLock.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.util;

/**
 * Class SimpleLock implements the Lock interface without any extras.
 *
 * @author  Nathan Fiedler
 * @version 1.0  7/3/00
 */
public class SimpleLock implements Lock {
    /** The key to this lock. */
    private Object mykey;

    /**
     * Tries to acquire a lock using the given key. The key can be
     * any object.
     *
     * @param  key  Any object that will act as the key to the lock.
     * @exception  LockException
     *             Thrown if failed to acquire lock.
     */
    public synchronized void acquire(Object key) throws LockException {
        // Block until the lock is released.
        while ((mykey != null) && (mykey != key)) {
            try {
                wait();
            } catch (InterruptedException ie) {
                throw new LockException("interrupted while waiting for lock");
            }
        }
        mykey = key;
    } // acquire

    /**
     * Tries to acquire a lock using the given key. The key can be
     * any object. If the key is not acquired within <code>timeout</code>
     * seconds, a LockException is thrown.
     *
     * @param  key      Any object that will act as the key to the lock.
     * @param  timeout  Number of milliseconds to wait before timing out.
     * @exception  LockException
     *             Thrown if failed to acquire lock.
     */
    public synchronized void acquire(Object key, long timeout)
        throws LockException {
        // Block until the lock is released or timeout occurs.
        if ((mykey != null) && (mykey != key)) {
            try {
                wait(timeout);
            } catch (InterruptedException ie) {
                throw new LockException("interrupted while waiting for lock");
            }
            if ((mykey != null) && (mykey != key)) {
                throw new LockException("timed out waiting for lock");
            }
        }
        mykey = key;
    } // acquire

    /**
     * Tests if the lock is owned by the given key.
     *
     * @param  key  Key to test on the lock.
     * @return  True if this key owns the lock.
     */
    public boolean hasKey(Object key) {
        return mykey == key;
    } // hasKey

    /**
     * Release the lock using the given key. Only works if the
     * given key matches the one already in this lock. Otherwise
     * exits silently.
     *
     * @param  key  Key to release the lock.
     */
    public synchronized void release(Object key) {
        if (mykey == key) {
            mykey = null;
            notifyAll();
        }
    } // release
} // Lock
