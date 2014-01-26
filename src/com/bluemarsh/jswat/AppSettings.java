/*********************************************************************
 *
 *	Copyright (C) 2000-2001 Nathan Fiedler
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
 * MODULE:      JSwat
 * FILE:        AppSettings.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      10/14/97        Initial version from GraphMaker
 *      nf      03/01/98        Made into a singleton
 *      nf      07/05/00        Copied over to JSwat
 *      nf      08/14/01        Removed support for listeners so
 *                              we wouldn't need JFC classes.
 *
 * DESCRIPTION:
 *      This file defines the AppSettings class which holds a set of
 *      properties. Each property is stored with a key value that
 *      is used to access the property value. This class is also
 *      capable of saving the settings to a file and restoring from
 *      that file later.
 *
 *      We use the java.util.Properties class for our storage since
 *      it offers the load() and commit() methods for easily storing
 *      the properties to a file.
 *
 * $Id: AppSettings.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Implements a properties storage class. This is used to store a variety
 * of properties, each associated with a key string. Each key string must
 * be unique or otherwise you will overwrite existing properties in the
 * table. The properties can be saved to a file and restored at a later
 * time, allowing persistent data storage.
 *
 * <p>
 * This class implements the Singleton design pattern to ensure that only
 * one instance of this class exists in the system. To get the single
 * instance you can call the instanceOf() method.
 * </p>
 *
 * @author  Nathan Fiedler
 * @see java.util.Properties
 */
public class AppSettings {
    /** Table that stores our properties. */
    protected Properties table;
    /** Reference to the singular instance of this class. */
    protected static AppSettings instance;
    /** Ini file passed to load(). Saved for the commit() call. */
    protected File iniFile;

    /**
     * No-arg constructor; sets up the hash table used to store
     * the properites. This is protected because this class is a
     * Singleton. You gain access to the instance of this class
     * via the instanceOf() method.
     */
    protected AppSettings() {
	table = new Properties();
    } // AppSettings

    /**
     * This saves the current settings to the file given in load().
     * After making any changes to the settings you should call this
     * method. Ideally you should make all the necessary changes and
     * then call this method to save the changes.
     *
     * @exception  IllegalStateException
     *             Thrown if <code>load()</code> method has not already
     *             been called to set the INI file.
     */
    public void commit() {
	if (iniFile == null) {
            throw new IllegalStateException("ini file not set");
	}
	try {
	    FileOutputStream fos = new FileOutputStream(iniFile);
	    table.store(fos, "JSwat settings");
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    } // commit

    /**
     * Tests if the specified key maps to an entry in the table.
     *
     * @param  key  key to look up in the table
     * @return true if the key exists in the table; false otherwise
     */
    public boolean contains(String key) {
	return table.containsKey(key);
    } // contains

    /**
     * Get the value of the given boolean. If the key does not exist
     * this method will return false.
     *
     * @param  key  name of the boolean property to retrieve
     * @return the value of the boolean
     */
    public boolean getBoolean(String key) {
	String sVal = table.getProperty(key);
	if (sVal == null) {
	    return false;
	}
	Boolean bVal = Boolean.valueOf(sVal);
	return bVal.booleanValue();
    } // getBoolean

    /**
     * Get the value of the given double. If the key does not exist
     * this method will return zero.
     *
     * @param  key  name of the double property to retrieve
     * @return the value of the double
     */
    public double getDouble(String key) {
	String sVal = table.getProperty(key);
	if (sVal == null) {
	    return 0;
	}
	Double dVal = Double.valueOf(sVal);
	return dVal.doubleValue();
    } // getDouble

    /**
     * Get the value of the given integer. If the key does not exist
     * this method will return the given default value.
     *
     * @param  key  name of the integer property to retrieve
     * @param  def  default value if property not set.
     * @return the value of the integer
     */
    public int getInteger(String key, int def) {
	String sVal = table.getProperty(key);
	if (sVal == null) {
	    return def;
	}
	Integer iVal = Integer.valueOf(sVal);
	return iVal.intValue();
    } // getInteger

    /**
     * Get the value of the given string. If the key does not exist
     * this method will return an empty string.
     *
     * @param  key  name of the string property to retrieve
     * @return the value of the string
     */
    public String getString(String key) {
        String sVal = table.getProperty(key);
        if (sVal == null) {
            return "";
        }
	return sVal;
    } // getString

    /**
     * Returns a reference to the single instance of this class. If
     * one does not exist it will be created.
     *
     * @return instance of class AppSettings
     */
    public static AppSettings instanceOf() {
        // Use a double-checked lock to avoid creating more than
        // one instance.
        if (instance == null) {
            synchronized (AppSettings.class) {
                if (instance == null) {
                    instance = new AppSettings();
                }
            }
        }
        return instance;
    } // instanceOf

    /**
     * Loads a new set of properties from the given file. The current
     * set will be overwritten with the contents of the file.
     *
     * @param  file  file containing properties
     * @return false if error
     */
    public boolean load(File file) {
        // Save this file despite any problems.
        iniFile = file;
	try {
	    FileInputStream fis = new FileInputStream(file);
	    table.load(fis);
	} catch (FileNotFoundException e) {
	    return false;
	} catch (IOException e) {
	    return false;
        }
	return true;
    } // load

    /**
     * Removes the key and value from the properties table. After
     * calling this method you should make sure the change is
     * committed by calling commit().
     *
     * @param  key  key to remove from table
     */
    public void remove(String key) {
	table.remove(key);
    } // remove

    /**
     * Set the value of a boolean, given the name of the property
     * and the new boolean value. Be sure to call commit() to save
     * the change to file.
     *
     * @param  key    name of the boolean property to set
     * @param  value  new value for the boolean property
     */
    public void setBoolean(String key, boolean value) {
	Boolean bVal = value ? Boolean.TRUE : Boolean.FALSE;
	String sVal = bVal.toString();
	table.put(key, sVal);
    } // setBoolean

    /**
     * 
     * Set the value of a double, given the name of the property
     * and the new double value. Be sure to call commit() to save
     * the change to file.
     *
     * @param  key    name of the boolean property to set
     * @param  value  new value for the double property
     */
    public void setDouble(String key, double value) {
	Double dVal = new Double(value);
	String sVal = dVal.toString();
	table.put(key, sVal);
    } // setDouble

    /**
     * 
     * Set the value of a integer, given the name of the property
     * and the new integer value. Be sure to call commit() to save
     * the change to file.
     *
     * @param  key    name of the boolean property to set
     * @param  value  new value for the integer property
     */
    public void setInteger(String key, int value) {
	Integer iVal = new Integer(value);
	String sVal = iVal.toString();
	table.put(key, sVal);
    } // setInteger

    /**
     * 
     * Set the value of a string, given the name of the property
     * and the new string value. Be sure to call commit() to save
     * the change to file.
     *
     * @param  key    name of the boolean property to set
     * @param  value  new value for the string property
     */
    public void setString(String key, String value) {
	table.put(key, value);
    } // setString
} // AppSettings
