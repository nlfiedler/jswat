/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes;

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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
    public BeanPropertySupport(Object instance, Class valueType,
            String property) throws NoSuchMethodException {
        this(instance, valueType,
                findGetter(instance, property),
                findSetter(instance, property));
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
     * @param  instance  bean instance.
     * @param  property  name of the property.
     * @return  the getter method.
     */
    private static Method findGetter(Object instance, String property)
            throws NoSuchMethodException {

        try {
            BeanInfo bi = Introspector.getBeanInfo(instance.getClass());
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
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
     * @param  instance  bean instance.
     * @param  property  name of the property.
     * @return  the setter method, or null if none.
     */
    private static Method findSetter(Object instance, String property)
            throws NoSuchMethodException {

        try {
            BeanInfo bi = Introspector.getBeanInfo(instance.getClass());
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
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

    @Override
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
