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
 * $Id: BeanPropertySupport.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

/**
 * Like PropertySupport.Reflection, but allow beans that do not have
 * setter methods for their properties.
 *
 * @author  Nathan Fiedler
 */
public class BeanPropertySupport extends Node.Property {
    /** Instance of a bean. */
    protected Object instance;
    /** The setter method. */
    private Method setter;
    /** The getter method. */
    private Method getter;
    /** The class of the property editor. */
    private Class propertyEditorClass;

    /**
     * Create a support with method objects specified.
     * The methods must be public.
     *
     * @param  instance   (Bean) object to work on.
     * @param  valueType  type of the property.
     * @param  getter     getter method, can be <code>null</code>.
     * @param  setter     setter method, can be <code>null</code>.
     * @throws  IllegalArgumentException
     *          if the methods are not public.
     */
    public BeanPropertySupport(Object instance, Class valueType,
            Method getter, Method setter) {
        super(valueType);
        if (getter != null && !Modifier.isPublic(getter.getModifiers())) {
            throw new IllegalArgumentException(
                    "Cannot use a non-public getter " + getter);
        }
        if (setter != null && !Modifier.isPublic(setter.getModifiers())) {
            throw new IllegalArgumentException(
                    "Cannot use a non-public setter " + setter);
        }
        this.instance = instance;
        this.setter = setter;
        this.getter = getter;
    }

    /**
     * Create a support based on the property name. The getter and setter
     * methods are constructed by capitalizing the first letter in the
     * name of propety and prefixing it with <code>get</code> and
     * <code>set</code>, respectively. If <code>get</code> is missing and
     * the property type is boolean, then <code>is</code> is used.
     *
     * @param  instance   object to work on.
     * @param  valueType  type of the property.
     * @param  property   name of property.
     * @exception  NoSuchMethodException
     *             if the getter or setter methods cannot be found.
     */
    public BeanPropertySupport(Object instance, Class valueType,
            String property) throws NoSuchMethodException {
        this(instance, valueType,
                findGetter(instance, valueType, property),
                findSetter(instance, valueType, property));
    }

    public boolean canRead() {
        return getter != null;
    }

    public boolean canWrite() {
        return setter != null;
    }

    /**
     * Finds the proper getter, with either a 'get' or 'is' prefix.
     *
     * @param  instance   bean instance.
     * @param  valueType  type of the property.
     * @param  property   name of the property.
     * @return  the getter method.
     */
    private static Method findGetter(Object instance, Class valueType,
            String property) throws NoSuchMethodException {

        try {
            BeanInfo bi = Introspector.getBeanInfo(instance.getClass());
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for(PropertyDescriptor prop : props) {
                String name = prop.getName();
                if (name.equals(property)) {
                    return prop.getReadMethod();
                }
            }
        }  catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
        }
        throw new NoSuchMethodException("getter for " + property);
    }

    /**
     * Finds the setter method, if any is available.
     *
     * @param  instance   bean instance.
     * @param  valueType  type of the property.
     * @param  property   name of the property.
     * @return  the setter method, or null if none.
     */
    private static Method findSetter(Object instance, Class valueType,
            String property) throws NoSuchMethodException {

        try {
            BeanInfo bi = Introspector.getBeanInfo(instance.getClass());
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for(PropertyDescriptor prop : props) {
                String name = prop.getName();
                if (name.equals(property)) {
                    return prop.getWriteMethod();
                }
            }
        }  catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
        }
        throw new NoSuchMethodException("setter for " + property);
    }

    /**
     * Returns property editor for this property.
     *
     * @return  the property editor or null if there should be no editor.
     */
    public PropertyEditor getPropertyEditor() {
        if (propertyEditorClass != null) {
            try {
                return (PropertyEditor) propertyEditorClass.newInstance();
            }  catch (InstantiationException ie) {
                ErrorManager.getDefault().notify(ie);
            }  catch (IllegalAccessException iae) {
                ErrorManager.getDefault().notify(iae);
            }
        }
        return super.getPropertyEditor();
    }

    public Object getValue() throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        if (!canRead()) {
            throw new IllegalAccessException();
        }
        Object valideInstance = Beans.getInstanceOf(
                instance, getter.getDeclaringClass());
        try {
            try {
                return getter.invoke(valideInstance, new Object[0]);
            } catch (IllegalAccessException iae) {
                try {
                    getter.setAccessible(true);
                    return getter.invoke(valideInstance, new Object[0]);
                } finally {
                    getter.setAccessible(false);
                }
            }
        }  catch (IllegalArgumentException iae) {
            StringBuilder sb = new StringBuilder("Attempted to invoke method ");
            sb.append(getter.getName());
            sb.append(" from class ");
            sb.append(getter.getDeclaringClass().getName());
            sb.append(" on an instance of ");
            sb.append(valideInstance.getClass().getName());
            sb.append(" Problem:");
            sb.append(iae.getMessage());
            IllegalArgumentException nue = new IllegalArgumentException(
                    sb.toString());
            ErrorManager.getDefault().annotate(nue, iae);
            throw nue;
        }
    }

    /**
     * Set the property editor to the given class.
     *
     * @param  clazz  class type of the property editor.
     */
    public void setPropertyEditorClass(Class clazz) {
        propertyEditorClass = clazz;
    }

    public void setValue(Object val) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        if (!canWrite()) {
            throw new IllegalAccessException();
        }
        Object valideInstance = Beans.getInstanceOf(
                instance, setter.getDeclaringClass());
        try {
            setter.invoke(valideInstance, new Object[]{val});
        }  catch (IllegalAccessException ex) {
            try {
                setter.setAccessible(true);
                setter.invoke(valideInstance, new Object[]{val});
            }  finally {
                setter.setAccessible(false);
            }
        }
    }
}
