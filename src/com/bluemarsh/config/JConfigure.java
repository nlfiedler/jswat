/*********************************************************************
 *
 *	Copyright (C) 1999-2001 Nathan Fiedler
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
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * PROJECT:     JConfigure
 * FILE:        JConfigure.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/03/00        Initial version
 *      nf      04/07/01        Added support for multiple listeners.
 *      nf      09/08/01        Fire config change on setProperty()
 *      nf      11/10/01        Fixed bug 292
 *      nf      11/17/01        Fixed bug 312
 *
 * DESCRIPTION:
 *      This file defines the main class of JConfigure.
 *
 * $Id: JConfigure.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.awt.Frame;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.JDialog;
import javax.swing.event.EventListenerList;

/**
 * Class JConfigure is the main class of this library. It provides
 * the primary interface for creating and maintaining the preferences
 * dialog, loading and saving the preferences, and generally tying
 * the other classes together.
 *
 * @author  Nathan Fiedler
 */
public class JConfigure {
    /** File parser that reads the properties file and builds out
     * the element tree. */
    protected FileParser fileParser;
    /** File writer to write out preferences to a file. */
    protected FileWriter fileWriter;
    /** Root element of the properties tree. */
    protected RootElement rootNode;
    /** Holds the name/value pairs for all of the user preferences. */
    protected Properties propertiesObj;
    /** Name of file to which we save the preferences. */
    protected String preferencesFilename;
    /** Dialog presenter used to display dialog. */
    protected DialogPresenter dialogPresenter;
    /** List of ConfigureListeners that are notified whenever the
     * configuration changes. */
    protected EventListenerList listenerList;

    /**
     * Constructs a new JConfigure object.
     */
    public JConfigure() {
        propertiesObj = new Properties();
        fileParser = new FileParser();
        fileWriter = new FileWriter();
        listenerList = new EventListenerList();
    } // JConfigure

    /**
     * Adds the configuration change listener so it will be notified
     * whenever the configuration changes.
     *
     * @param  l  configuration change listener.
     */
    public void addListener(ConfigureListener l) {
        listenerList.add(ConfigureListener.class, l);
    } // addListener

    /**
     * Processes the element tree to find any elements that have
     * moved from one group to another. If any are found, their
     * original location is searched for a property. If that
     * property value exists, it is copied to the new location.
     *
     * @param  userprops   Properties object from which to get values.
     * @param  prefix      Name prefix (may be null).
     * @param  elem        Element to start with.
     * @param  appprops    Properties object in which to set values.
     */
    protected void copyMovedElements(Properties userprops, String prefix,
                                     Element elem, Properties appprops) {
        // Process children elements first (for no particular reason).
        if (elem.hasChild()) {
            if ((prefix == null) || (prefix.length() == 0)) {
                copyMovedElements(userprops, elem.getName(), elem.getChild(),
                                  appprops);
            } else {
                copyMovedElements(userprops, prefix + "." + elem.getName(),
                                  elem.getChild(), appprops);
            }
        }

        // Base case: option elements get new values.
        if (elem instanceof OptionElement) {
            OptionElement oe = (OptionElement) elem;
            String movedFrom = oe.getMovedFrom();
            if (movedFrom != null) {
                String old = userprops.getProperty(movedFrom);
                if (old != null) {
                    // Get the value in the properties from the old name.
                    appprops.setProperty(prefix + "." + elem.getName(), old);
                }
            }
            // xxx - need to handle multi-value options
        }

        // Process the sibling element. Note we do not have to use
        // a loop here, it will be taken care of automatically.
        if (elem.hasSibling()) {
            copyMovedElements(userprops, prefix, elem.getSibling(), appprops);
        }
    } // copyMovedElements

    /**
     * Notify the registered configuration change listeners that the
     * configuration has just been changed.
     */
    protected void fireConfigChanged() {
        // Get the listener list as class/instance pairs.
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first.
        // List is in pairs: class, instance
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ConfigureListener.class) {
                ConfigureListener l = (ConfigureListener) listeners[i + 1];
                l.configurationChanged();
            }
        }
    } // fireConfigChanged

    /**
     * Searches for the boolean property with the specified key in the
     * property list. The method returns false if the property is not
     * found.
     *
     * @param  key  the boolean property key.
     * @return  the value in this property list with the specified
     *          key value.
     */
    public boolean getBooleanProperty(String key) {
        String bstr = propertiesObj.getProperty(key);
        if (bstr != null) {
            return Boolean.valueOf(bstr).booleanValue();
        } else {
            return false;
        }
    } // getBooleanProperty

    /**
     * Searches for the boolean property with the specified key in the
     * property list. The method returns the default value argument
     * if the property is not found.
     *
     * @param  key           the boolean property key.
     * @param  defaultValue  a default value.
     * @return  the value in this property list with the specified
     *          key value.
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String bstr = propertiesObj.getProperty(key);
        if (bstr != null) {
            return Boolean.valueOf(bstr).booleanValue();
        } else {
            return defaultValue;
        }
    } // getBooleanProperty

    /**
     * Gets the filename used when saving the preferences to disk.
     *
     * @return  name of preferences file, or null if not set.
     */
    public String getFilename() {
        return preferencesFilename;
    } // getFilename

    /**
     * Searches for the property with the specified key in the
     * property list. The method returns null if the property is
     * not found.
     *
     * @param  key  the property key.
     * @return  the value in this property list with the specified
     *          key value.
     */
    public String getProperty(String key) {
        return propertiesObj.getProperty(key);
    } // getProperty

    /**
     * Searches for the property with the specified key in the
     * property list. The method returns the default value argument
     * if the property is not found.
     *
     * @param  key           the property key.
     * @param  defaultValue  a default value.
     * @return  the value in this property list with the specified
     *          key value.
     */
    public String getProperty(String key, String defaultValue) {
        return propertiesObj.getProperty(key, defaultValue);
    } // getProperty
    
    /**
     * Returns a string representation of the current properties.
     *
     * @param  valueSeparator   separates option name from value.
     * @param  labelSeparator   separates option value from label.
     * @param  optionSeparator  separates one option from another.
     */
    public String listProperties(String valueSeparator,
                                 String labelSeparator,
                                 String optionSeparator) {
        StringBuffer results = new StringBuffer(256);
        listProperties2(results, null, rootNode,
                        valueSeparator, labelSeparator, optionSeparator);
        return results.toString();
    } // listProperties

    /**
     * Subroutine of <code>listProperties</code> which does the work.
     *
     * @param  buf     buffer to append to.
     * @param  prefix  name prefix (may be null).
     * @param  elem    Element to start with.
     * @param  vs      separates option name from value.
     * @param  ls      separates option value from label.
     * @param  os      separates one option from another.
     */
    protected void listProperties2(StringBuffer buf, String prefix,
                                   Element elem,
                                   String vs, String ls, String os) {
        // Process children elements first (for no particular reason).
        if (elem.hasChild()) {
            if ((prefix == null) || (prefix.length() == 0)) {
                listProperties2(buf, elem.getName(),
                                elem.getChild(), vs, ls, os);
            } else {
                listProperties2(buf, prefix + "." + elem.getName(),
                                elem.getChild(), vs, ls, os);
            }
        }

        // Base case: option elements are printed to the buffer.
        if (elem instanceof OptionElement) {
            OptionElement oe = (OptionElement) elem;
            buf.append(prefix);
            buf.append(".");
            buf.append(oe.getName());
            buf.append(vs);
            buf.append(oe.getValue());
            buf.append(ls);
            buf.append(oe.getLabel());
            buf.append(os);
            // xxx - need to handle multi-value options
        }

        // Process the sibling element. Note we do not have to use
        // a loop here, it will be taken care of automatically.
        if (elem.hasSibling()) {
            listProperties2(buf, prefix, elem.getSibling(), vs, ls, os);
        }
    } // listProperties2

    /**
     * Reads and parses the named file as if it were a properties
     * definition. This method expects to find element definitions
     * as defined in the JConfigure properties file specification.
     * After successful completion, the Properties will have all
     * the name/value pairs set.
     *<p>
     * This method implicitly calls the <code>setFilename()</code>
     * method to save the given filename.
     *
     * @param  filename  Name of preferences file to load.
     * @return  True if successful, false if error.
     * @exception  IOException
     *             Thrown if there's an exception reading the file.
     */
    public boolean loadSettings(String filename) throws IOException {
        // Read the settings file.
        FileInputStream fis = new FileInputStream(filename);
        boolean ok = loadSettings(fis);
        if (ok) {
            // Set the filename.
            setFilename(filename);
        }
        fis.close();
        return ok;
    } // loadSettings

    /**
     * Reads and parses the input stream as if it were a properties
     * definition. This method expects to find element definitions
     * as defined in the JConfigure properties file specification.
     * After successful completion, the Properties will have all
     * the name/value pairs set.
     *
     * <p>The passed input stream is left open, so the caller should
     * close it.
     *
     * @param  is  InputStream to read settings from (left open).
     * @return  True if successful, false if error.
     * @exception  IOException
     *             Thrown if there's an exception reading the file.
     */
    public boolean loadSettings(InputStream is) throws IOException {
        // Clear existing properties and element tree.
        propertiesObj.clear();
        rootNode = new RootElement();
        boolean ok = fileParser.read(is, rootNode);
        if (ok) {
            setProperties(propertiesObj, null, rootNode);
        }
        return ok;
    } // loadSettings

    /**
     * Prints out the entire element tree. Useful for debugging.
     *
     * @param  root  Root element.
     */
    public void printTree() {
        LinkedList q = new LinkedList();
        q.addLast(rootNode);
        while (q.size() > 0) {
            Element e = (Element) q.removeFirst();
            System.out.println(e);
            System.out.println("------------------------------------------------------------------------------");
            if (e.getSibling() != null) {
                q.addLast(e.getSibling());
            }
            if (e.getChild() != null) {
                q.addLast(e.getChild());
            }
        }
    } // printTree

    /**
     * Removes the given configuration change listener from the
     * list of registered listeners. It will no longer receive
     * change notifications from this object.
     *
     * @param  l  configuration change listener to remove.
     */
    public void removeListener(ConfigureListener l) {
        listenerList.remove(ConfigureListener.class, l);
    } // removeListener

    /**
     * Saves the user input to the Properties object and writes it
     * to disk.
     */
    protected void saveUserInput() {
        // Save all the settings to the Properties object.
        setProperties(propertiesObj, null, rootNode);
        if ((preferencesFilename != null) &&
            (preferencesFilename.length() > 0)) {
            try {
                storeSettings(preferencesFilename);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    } // saveUserInput

    /**
     * Processes the element tree to save all of the Property values
     * to the Element objects.
     *
     * @param  props   Properties object from which to get values.
     * @param  prefix  Name prefix (may be null).
     * @param  elem    Element to start with.
     */
    protected void setElementValues(Properties props,
                                    String prefix, Element elem) {
        // Process children elements first (for no particular reason).
        if (elem.hasChild()) {
            if ((prefix == null) || (prefix.length() == 0)) {
                setElementValues(props, elem.getName(), elem.getChild());
            } else {
                setElementValues(props, prefix + "." + elem.getName(),
                                 elem.getChild());
            }
        }

        // Base case: option elements get new values.
        if (elem instanceof OptionElement) {
            OptionElement oe = (OptionElement) elem;
            oe.setValue(props.getProperty(prefix + "." + elem.getName()));
            // xxx - need to handle multi-value options
        }

        // Process the sibling element. Note we do not have to use
        // a loop here, it will be taken care of automatically.
        if (elem.hasSibling()) {
            setElementValues(props, prefix, elem.getSibling());
        }
    } // setElementValues

    /**
     * Sets the filename to be used when saving the preferences to
     * disk. If the <code>filename</code> argument is non-null then
     * the preferences will be saved to this file automatically.
     *
     * @param  filename  a filename.
     */
    public void setFilename(String filename) {
        preferencesFilename = filename;
    } // setFilename

    /**
     * Processes the element tree to save all of the element values
     * to the Properties object. After successful completion, the
     * Properties object will have all the name/value pairs set.
     *
     * @param  props   Properties object in which to set values.
     * @param  prefix  Name prefix (may be null).
     * @param  elem    Element to start with.
     */
    protected void setProperties(Properties props,
                                 String prefix, Element elem) {
        // Process children elements first (for no particular reason).
        if (elem.hasChild()) {
            if ((prefix == null) || (prefix.length() == 0)) {
                setProperties(props, elem.getName(), elem.getChild());
            } else {
                setProperties(props, prefix + "." + elem.getName(),
                              elem.getChild());
            }
        }

        // Base case: option elements have their settings saved.
        if (elem instanceof OptionElement) {
            OptionElement oe = (OptionElement) elem;
            props.setProperty(prefix + "." + elem.getName(), oe.getValue());
            // xxx - need to handle multi-value options
        }

        // Process the sibling element. Note we do not have to use
        // a loop here, it will be taken care of automatically.
        if (elem.hasSibling()) {
            setProperties(props, prefix, elem.getSibling());
        }
    } // setProperties

    /**
     * Sets a new value for the given property.
     *
     * @param  key    the property key.
     * @param  value  new property value.
     */
    public void setProperty(String key, String value) {
        // Set the property's value.
        propertiesObj.setProperty(key, value);

        // Tokenize the key into the component names.
        StringTokenizer tokenizer = new StringTokenizer(key, ".");

        // Retrieve all the tokens into an array.
        String[] names = new String[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            names[index] = tokenizer.nextToken();
            index++;
        }

        index = 0;
        if (names[0].equals(rootNode.getName())) {
            // First name matches root's name, so skip it.
            index = 1;
        }

        // Walk down through the node tree to find the named element.
        Element elem = rootNode;
        while ((index < names.length) && (elem != null)) {
            if (elem.hasChild()) {
                elem = elem.getChild();
                do {
                    if (names[index].equals(elem.getName())) {
                        // Found the element with that name.
                        break;
                    }
                    elem = elem.getSibling();
                } while (elem != null);
            }
            index++;
        }

        // If found, set the option's value.
        if ((elem != null) && (elem instanceof OptionElement)) {
            OptionElement oe = (OptionElement) elem;
            oe.setValue(value);
        }

        fireConfigChanged();
    } // setProperty

    /**
     * Build the preferences dialog and present it to the user.
     * When the dialog is closed, the user's input will either
     * be accepted or rejected based on how the user closed the
     * preferences dialog. If the user presses the Ok button,
     * the preferences will be commited to disk. If the user
     * presses Cancel, no changes will be made to the preferences.
     *<p>
     * If the dialog is not modal, the caller may want register one
     * or more ConfigureListeners to be notified of changes to the
     * configuration.
     *
     * @param  owner  owning frame for the new dialog.
     * @param  modal  true if dialog is to be modal.
     * @exception  IllegalStateException
     *             Thrown if preferences have not been loaded.
     */
    public void showPreferences(Frame owner, boolean modal) {
        if (rootNode == null) {
            throw new IllegalStateException("preferences must be loaded");
        }
        if ((dialogPresenter != null) && (dialogPresenter.isShowing())) {
            // Already showing a preferences dialog, do nothing.
            return;
        }
        DialogInfo dinfo = UIBuilder.buildUI(rootNode, owner, modal);
        dialogPresenter = new DialogPresenter(dinfo);
        if (!modal) {

            // Set up a configure listener so we can ensure the
            // preferences are properly saved.
            dialogPresenter.present(new ConfigureListener() {
                    public void configurationChanged() {
                        // Save the user input to the preferences file.
                        saveUserInput();
                        fireConfigChanged();
                    }
                });
        } else {

            // Call will not return until dialog is closed.
            if (dialogPresenter.present()) {
                // Save the user input to the preferences file.
                saveUserInput();
                fireConfigChanged();
            }
        }
    } // showPreferences

    /**
     * <p>Writes the preferences to the given file.</p>
     *
     * <p>This method calls the <code>setFilename()</code> method to
     * save the given filename.</p>
     *
     * @param  filename  File to write settings to.
     * @return  True if successful, false if error.
     * @exception  IOException
     *             Thrown if there's an exception writing to the file.
     */
    public boolean storeSettings(String filename) throws IOException {
        setFilename(filename);
        FileOutputStream fos = new FileOutputStream(filename);
        boolean ok = storeSettings(fos);
        fos.close();
        return ok;
    } // storeSettings

    /**
     * Writes the preferences to the given output stream.
     *
     * @param  os  OutputStream to write settings to.
     * @return  True if successful, false if error.
     * @exception  IOException
     *             Thrown if there's an exception writing to the file.
     */
    public boolean storeSettings(OutputStream os) throws IOException {
        return fileWriter.write(os, rootNode);
    } // storeSettings

    /**
     * This method takes the user preferences file and compares its
     * version to the application preferences file. If the user file
     * is out of date, the settings are copied to the new application
     * file and those new preferences become the current values for
     * this instance of JConfigure.
     *
     * <p>If the user preferences are up to date, they are used as
     * the new settings, overriding any values previously stored in
     * this JConfigure instance.
     *
     * <p>The passed input streams are left open, so the caller should
     * close them.
     *
     * @param  useris  Input stream for user preferences.
     * @param  appis   Input stream for application preferences.
     * @return  True if successful, false if error.
     * @exception  IOException
     *             Thrown if there's an exception reading the file.
     */
    public boolean upgrade(InputStream useris, InputStream appis)
        throws IOException {

        // Read the user preferences into an element tree.
        RootElement userroot = new RootElement();
        boolean ok = fileParser.read(useris, userroot);
        if (!ok) {
            return false;
        }

        // Read the app preferences into an element tree.
        RootElement approot = new RootElement();
        ok = fileParser.read(appis, approot);
        if (!ok) {
            return false;
        }

        // Read the element values into a Properties object.
        Properties userprops = new Properties();
        setProperties(userprops, null, userroot);

        // Compare the version numbers of the root elements to
        // see if we really need to merge or not.
        if (userroot.getVersion() == approot.getVersion()) {
            // They are the same, use the user prefs.
            propertiesObj = userprops;
            rootNode = userroot;
            return true;
        }
        // Otherwise, we'll copy the user's settings over to the
        // application copy and use that as our new preferences.

        // Read the element values into a Properties object.
        Properties appprops = new Properties();
        setProperties(appprops, null, approot);

        // Copy like-named properties from user to app.
        Enumeration appnames = appprops.propertyNames();
        while (appnames.hasMoreElements()) {
            // Use the app names to get the user values to reset the
            // app values.
            String name = (String) appnames.nextElement();
            String value = userprops.getProperty(name);
            if (value != null) {
                appprops.setProperty(name, value);
            }
        }

        // Look for any options that moved and see if the user
        // has values at the original locations.
        copyMovedElements(userprops, null, approot, appprops);

        // Set app tree element values from the app properties.
        setElementValues(appprops, null, approot);

        // Overwrite the existing settings with these new settings.
        propertiesObj = appprops;
        rootNode = approot;
        return true;
    } // upgrade

    /**
     * This method takes the user preferences file and compares its
     * version to the application preferences file. If the user file
     * is out of date, the settings are copied to the new application
     * file and those new preferences become the current values for
     * this instance of JConfigure.
     *
     * <p>If the user preferences are up to date, they are used as
     * the new settings, overriding any values previously stored in
     * this JConfigure instance.
     *
     * @param  userPrefs  Name of user preferences file.
     * @param  appPrefs   Name of application preferences file.
     * @return  True if successful, false if error.
     * @exception  IOException
     *             Thrown if there's an exception reading the file.
     */
    public boolean upgrade(String userPrefs, String appPrefs)
        throws IOException {
        FileInputStream useris = new FileInputStream(userPrefs);
        FileInputStream appis = new FileInputStream(appPrefs);
        boolean ok = upgrade(useris, appis);
        if (ok) {
            setFilename(userPrefs);
        }
        useris.close();
        appis.close();
        return ok;
    } // upgrade
} // JConfigure
