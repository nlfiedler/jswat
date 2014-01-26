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
 * $Id: MethodBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.MalformedMemberNameException;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.MethodBreakpointUI;
import com.bluemarsh.jswat.util.Classes;
import com.bluemarsh.jswat.util.Names;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.util.Types;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.request.EventRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Class MethodBreakpoint extends the LocationBreakpoint class. Its
 * properties include a class and a method name, and an argument list
 * specifying where the breakpoint should stop.
 *
 * @author Nathan Fiedler
 */
public class MethodBreakpoint extends LocationBreakpoint {
    /** Name of the method this breakpoint is set at. */
    private String methodId;
    /** List of method arguments (if any are given), where each element is
     * a String object reprepsenting the argument type. */
    private List methodArgs;

    /**
     * Default constructor for deserialization.
     */
    MethodBreakpoint() {
    } // MethodBreakpoint

    /**
     * Constructs a MethodBreakpoint for the given class at the specified
     * line within that class.
     *
     * @param  classPattern  name of class in which to set breakpoint,
     *                       possibly using wildcards.
     * @param  methodId      name of method to stop at.
     * @param  methodArgs    list of method argument types as Strings.
     * @throws  ClassNotFoundException
     *          if classPattern is not a valid identifier.
     * @throws  MalformedMemberNameException
     *          if the method name is invalid.
     */
    public MethodBreakpoint(String classPattern, String methodId, List methodArgs)
        throws ClassNotFoundException,
               MalformedMemberNameException {

        super(classPattern);
        this.methodId = methodId;
        if (methodArgs != null) {
            this.methodArgs = methodArgs;
        }
        if (!Names.isMethodIdentifier(methodId)) {
            throw new MalformedMemberNameException(methodId);
        }
        // By default we do not have a line number.
        lineNumber = -1;
    } // MethodBreakpoint

    /**
     * Retrieve the arguments to the method at which this breakpoint
     * is set. The returned list is unmodifiable.
     *
     * @return  list of method arguments.
     */
    public List getMethodArgs() {
        return methodArgs;
    } // getMethodArgs

    /**
     * Retrieve the method name associated with this breakpoint.
     *
     * @return  name of method this breakpoint is set to.
     */
    public String getMethodName() {
        return methodId;
    } // getMethodName

    /**
     * Returns the user interface widget for customizing this breakpoint.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return new MethodBreakpointUI(this);
    } // getUIAdapter

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        if (!super.readObject(prefs)) {
            return false;
        }
        methodId = prefs.get("methodId", null);
        if (methodId == null) {
            return false;
        }
        String args = prefs.get("methodArgs", null);
        if (args != null && args.length() > 0) {
            methodArgs = Strings.stringToList(args);
        } else {
            methodArgs = null;
        }
        return true;
    } // readObject

    /**
     * Determine the location at which to set the breakpoint using
     * the given class type.
     *
     * @param  clazz  ClassType against which to resolve.
     * @return  Location at which to create breakpoint.
     * @throws  ResolveException
     *          if location fails to resolve.
     */
    protected Location resolveLocation(ClassType clazz)
        throws ResolveException {

        try {
            List argumentTypes;
            if (methodArgs != null) {
                argumentTypes = Types.typeNamesToJNI(methodArgs);
            } else {
                argumentTypes = new ArrayList();
            }
            Method method = Classes.findMethod(
                clazz, methodId, argumentTypes, false);
            Location loc = method.location();
            // Now is our chance to get the line number.
            lineNumber = loc.lineNumber();
            return loc;
        } catch (AmbiguousMethodException ame) {
            throw new ResolveException(ame);
        } catch (ClassNotLoadedException cnle) {
            throw new ResolveException(cnle);
        } catch (InvalidTypeException ite) {
            throw new ResolveException(
                new InvalidArgumentTypeException(ite.getMessage()));
        } catch (NoSuchMethodException nsme) {
            throw new ResolveException(nsme);
        }
    } // resolveLocation

    /**
     * Set the list of arguments to the method at which this breakpoint
     * is set.
     *
     * @param  args  method argument list.
     */
    public void setMethodArgs(List args) {
        if (args != null) {
            methodArgs = args;
        } else {
            methodArgs = null;
        }
        // Reset ourselves so we get resolved all over again.
        deleteEventRequest();
        fireChange();
    } // setMethodArgs

    /**
     * Set the method name associated with this breakpoint.
     *
     * @param  name  name of method this breakpoint is set to.
     */
    public void setMethodName(String name) {
        methodId = name;
        // Reset ourselves so we get resolved all over again.
        deleteEventRequest();
        fireChange();
    } // setMethodName

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     * @return  string of this.
     */
    public String toString(boolean terse) {
        StringBuffer buf = new StringBuffer(80);
        if (referenceSpec == null) {
            return "<not initialized>";
        }
        String cname = referenceSpec.toString();
        if (terse) {
            cname = Names.justTheName(cname);
        }
        buf.append(cname);

        // Append the method name, plus any method args.
        buf.append('.');
        buf.append(methodId);
        if (methodArgs != null) {
            buf.append('(');
            buf.append(Strings.listToString(methodArgs, ","));
            buf.append(')');
        }

        if (!terse) {
            buf.append(' ');
            if (suspendPolicy == EventRequest.SUSPEND_ALL) {
                buf.append(Bundle.getString("suspendAll"));
            } else if (suspendPolicy == EventRequest.SUSPEND_EVENT_THREAD) {
                buf.append(Bundle.getString("suspendThread"));
            } else if (suspendPolicy == EventRequest.SUSPEND_NONE) {
                buf.append(Bundle.getString("suspendNone"));
            }
        }
        return buf.toString();
    } // toString

    /**
     * Writes the breakpoint properties to the given preferences node.
     * It is assumed that the preferences node is completely empty.
     *
     * @param  prefs  Preferences node to which to serialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean writeObject(Preferences prefs) {
        if (!super.writeObject(prefs)) {
            return false;
        }
        prefs.put("methodId", methodId);
        if (methodArgs != null) {
            prefs.put("methodArgs", Strings.listToString(methodArgs));
        } else if (prefs.get("methodArgs", null) != null) {
            prefs.remove("methodArgs");
        }
        return true;
    } // writeObject
} // MethodBreakpoint
