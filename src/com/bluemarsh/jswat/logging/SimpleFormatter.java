/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * $Id: SimpleFormatter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.logging;

import com.bluemarsh.jswat.util.Strings;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A simple logging formatter that shows just the logger name, the log
 * level, and the message.
 *
 * @author  Nathan Fiedler
 */
public class SimpleFormatter extends Formatter {
    /** Line separator string. */
    private static String lineSeparator =
        System.getProperty("line.separator");

    /**
     * Returns just the name of the class, without the package name.
     *
     * @param  cname  Name of class, possibly fully-qualified.
     * @return  Just the class name.
     */
    protected static String justTheName(String cname) {
        int i = cname.lastIndexOf('.');
        if (i > 0) {
            return cname.substring(i + 1);
        }
        return cname;
    } // justTheName

    /**
     * Format the given LogRecord.
     *
     * @param  record  the log record to be formatted.
     * @return  a formatted log record.
     */
    public synchronized String format(LogRecord record) {
        // It is important that this code does not depend on the
        // java.util.prefs.Preferences class. That class reports
        // warnings through this reporter, so this code must not try to
        // access Preferences.

        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(justTheName(record.getLoggerName()));
        sb.append("-");
        sb.append(record.getLevel().getLocalizedName());
        sb.append("]: ");
        sb.append(formatMessage(record));
        sb.append(lineSeparator);

        // Check for an exception.
        if (record.getThrown() != null) {
            try {
                sb.append(Strings.exceptionToString(record.getThrown()));
            } catch (Exception e) {
                // Ignore it.
            }
        }

        return sb.toString();
    } // format
} // SimpleFormater
