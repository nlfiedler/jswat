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
 * MODULE:      Utilities
 * FILE:        JVMArguments.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/21/01        Initial version
 *      nf      12/15/01        Fixed bug 365
 *      nf      04/17/02        Using new preferences facility
 *      nf      05/03/02        No longer use custom tokenizer
 *
 * $Id: JVMArguments.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.bluemarsh.jswat.Defaults;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

/**
 * Class JVMArguments represents the arguments given by the user to
 * launch a debuggee VM. When given a string of arguments, it will
 * parse it into the component parts. These parts can be retrieved
 * using the provided methods. One of the methods will perform an
 * intelligent conversion on the options, appropriate for launching
 * a debuggee VM.
 *
 * <p>This class is used by the load command and the VMStart action.</p>
 *
 * @author  Nathan Fiedler
 */
public class JVMArguments {
    /** The original parsed options provided by the user. */
    private String parsedOptions;
    /** Everything that followed the original options, such as the
     * classname and class parameters. */
    private String stuffAfterOptions;
    /** True if the user-provided options specified which VM to use,
     * such as -hotspot, -classic, etc. */
    private boolean jvmSpecified;
    /** True if the user-provided options specified a classpath. */
    private boolean classpathSpecified;

    /**
     * Constructs a JVMArguments using the given arguments.
     *
     * @param  arguments  user-provided arguments.
     */
    public JVMArguments(String arguments) {
        parseOptions(new StringTokenizer(arguments));
    } // JVMArguments

    /**
     * Processes the parsed arguments and returns a String of arguments
     * that should be used to launch a debuggee VM. It will move any
     * HotSpot flags to the beginning of the string. It will insert the
     * -classic option if the "Use Classic VM" option is enabled, and no
     * HotSpot flag was given by the user. It will add the classpath
     * specification, if the user did not provide one.
     *
     * @param  cpath  classpath to be used for launching debuggee.
     * @return  arguments that should be used to launch debuggee VM.
     */
    public String normalizedOptions(String cpath) {
        StringBuffer realOptions = new StringBuffer(parsedOptions);

        if (!classpathSpecified) {
            // Get the classpath and add that to the options.
            if (cpath != null) {
                // Enclose the classpath in quotes in case it contains
                // any whitespace, which would confuse the debuggee VM.
                realOptions.append(" -cp \"");
                realOptions.append(cpath);
                realOptions.append("\" ");
            }
        }

        if (!jvmSpecified) {
            // Add the -classic switch if the option is enabled and the
            // VM has not already been explicitly specified in the arguments.
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat");
            boolean classic = prefs.getBoolean("useClassicVM",
                                               Defaults.USE_CLASSIC_VM);
            if (classic) {
                realOptions.insert(0, "-classic ");
            }
        }

        return realOptions.toString();
    } // normalizeOptions

    /**
     * Returns the options as entered by the user. This is probably
     * what you want to save from one session to the next.
     *
     * @return  the options after parsing.
     */
    public String parsedOptions() {
        return parsedOptions;
    } // parsedOptions

    /**
     * Parse the user-specified arguments and set the field variables
     * appropriately.
     *
     * @param  arguments  user-provided arguments.
     */
    protected void parseOptions(StringTokenizer arguments) {
        StringBuffer optionBuf = new StringBuffer();

        while (arguments.hasMoreTokens()) {
            String token = arguments.nextToken();
            if (token.charAt(0) != '-') {
                // Reached the end of options, exit the loop.
                if (arguments.hasMoreTokens()) {
                    stuffAfterOptions = token + arguments.nextToken("\n");
                } else {
                    stuffAfterOptions = token;
                }
                break;
            }

            if (token.equals("-classpath") || token.equals("-cp")) {
                // User has provided us with a classpath.
                // Add the option and value to our buffer.
                classpathSpecified = true;
                optionBuf.append(token);
                optionBuf.append(' ');
                try {
                    token = arguments.nextToken();
                    optionBuf.append(token);
                    if ((token.charAt(0) == '"')
                        && (token.charAt(token.length() - 1) != '"')
                        || token.length() == 1) {

                        // No point worrying about escaped quotes:
                        // the jvm doesn't handle them.

                        // Path starts with quote but doesn't end with one.
                        // Read the rest of the path up to the closing quote.
                        token = arguments.nextToken(token.substring(0, 1));
                        optionBuf.append(token);
                        // Grab the closing quote and append it.
                        // If it is missing, no such element exception occurs.
                        token = arguments.nextToken(" ");
                        optionBuf.append(token);
                    }
                    optionBuf.append(' ');
                } catch (NoSuchElementException nsee) {
                    optionBuf.append("[malformed classpath]");
                }

                // The following options must appear first.
            } else if (token.equals("-classic")) {
                optionBuf.insert(0, "-classic ");
                jvmSpecified = true;
            } else if (token.equals("-hotspot")) {
                optionBuf.insert(0, "-hotspot ");
                jvmSpecified = true;
            } else if (token.equals("-server")) {
                optionBuf.insert(0, "-server ");
                jvmSpecified = true;
            } else if (token.equals("-client")) {
                optionBuf.insert(0, "-client ");
                jvmSpecified = true;
            } else {
                optionBuf.append(token);
                optionBuf.append(' ');
            }
        }

        parsedOptions = optionBuf.toString();
    } // parseOptions

    /**
     * Return everything that followed the options.
     *
     * @return  stuff after options, or null if arguments only contained
     *          options.
     */
    public String stuffAfterOptions() {
        return stuffAfterOptions;
    } // stuffAfterOptions
} // JVMArguments
