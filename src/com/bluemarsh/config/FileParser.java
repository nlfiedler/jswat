/*********************************************************************
 *
 *	Copyright (C) 1999-2002 Nathan Fiedler
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
 * FILE:        FileParser.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/23/99        Initial version
 *      nf      09/02/01        Fixed reading width attribute
 *      nf      11/10/01        Fixing bug 292
 *      nf      04/07/02        Added color option support
 *
 * DESCRIPTION:
 *      This file defines the class that handles reading the JConfigure
 *      preferences file. See the class description below for more
 *      information.
 *
 * $Id: FileParser.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.io.*;
import java.util.Hashtable;

/**
 * This class can parse streams written in the JConfigure preferences
 * file format and return a tree of Element objects.
 *
 * @author  Nathan Fiedler
 */
public class FileParser {
    /** Stream tokenizer used in parsing the file. */
    protected StreamTokenizer st;
    /** Map of option type names to option element classes. */
    protected Hashtable optionClasses;

    /**
     * Constructs a new FileParser object.
     */
    public FileParser() {
        optionClasses = new Hashtable();
        // Default option type to class mappings.
        optionClasses.put("boolean", BooleanOptionElement.class);
        optionClasses.put("number", NumberOptionElement.class);
        optionClasses.put("text", TextOptionElement.class);
        optionClasses.put("color", ColorOptionElement.class);
        optionClasses.put("label", LabelOptionElement.class);
    } // FileParser

    /**
     * Gets the next token from the stream.
     *
     * @return  token from stream
     */
    protected int nextToken() {
        // Try to read next token from stream tokenizer.
        try {
            return st.nextToken();
        } catch (IOException e) {
            // If something bad happened then return end-of-file.
            return StreamTokenizer.TT_EOF;
        }
    } // nextToken

    /**
     * Parse the given input stream and builds out the properties
     * tree. The given root element (which begins in its default
     * state) will be filled out and elements will be added to it
     * as children after parsing is complete. If the parse failed
     * at any point then false is returned.
     *
     * @param  is    Input stream to read from.
     * @param  root  Root element of the properties tree. This is
     *               empty and will be filled by this method.
     * @return  True if successful, false otherwise.
     */
    public boolean read(InputStream is, RootElement root) {
        // Initiate the stream tokenizer for parsing.
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        st = new StreamTokenizer(br);
        // Have to make numbers ordinary before they can be
        // truly word characters. Also do underscores.
        st.ordinaryChars('0', '9');
        st.ordinaryChar('_');
        st.wordChars('0', '9');
        st.wordChars('_', '_');
        // current state of parser
        int state = 0;
        // indicates whether to loop or not
        boolean keepGoing = true;
        // indicates if parse was successful (assume failure)
        boolean success = false;

        // Beginning of the finite state machine. This loops until keepGoing
        // equals false, which only occurs when there is a parse error,
        // we have reached the end of the file, or when we are finished
        // parsing the root element (which may be before the EOF).
        while (keepGoing) {
            // Read in the next token and switch based on current state.
            int token = nextToken();
            // If we reached end of file, break out of while loop.
            if (token == StreamTokenizer.TT_EOF) {
                break;
            }
            switch (state) {

            case 0 :
                // Look for the opening 'root' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    if (st.sval.equals("root")) {
                        state = 1;
                    } else {
                        System.err.println("Error: Line " + st.lineno() +
                                           ": missing 'root' keyword.");
                        keepGoing = false;
                    }
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": No valid word token found.");
                    keepGoing = false;
                }
                break;

            case 1 :
                // A [ must follow the 'root' keyword.
                if (token == '[') {
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing '[' after 'root'.");
                    keepGoing = false;
                }
                break;

            case 2 :
                // Look for 'comment', 'label', 'type', 'version',
                // 'group', or ']'.
                if (token == StreamTokenizer.TT_WORD) {
                    if (st.sval.equals("comment")) {
                        state = 3;
                    } else if (st.sval.equals("label")) {
                        state = 4;
                    } else if (st.sval.equals("name")) {
                        state = 5;
                    } else if (st.sval.equals("version")) {
                        state = 6;
                    } else if (st.sval.equals("type")) {
                        Typedef type = new Typedef();
                        keepGoing = readTypedef(type);
                        if (keepGoing) {
                            root.addType(type);
                        }

                    } else if (st.sval.equals("group")) {
                        GroupElement grp = new GroupElement();
                        keepGoing = readGroup(grp);
                        if (keepGoing) {
                            root.addChild(grp);
                        }
                    }
                    // else we ignore the token
                } else if (token == ']') {
                    // Stop parsing file and return "success".
                    success = true;
                    keepGoing = false;
                }
                // else we ignore the token
                break;

            case 3 :
                // A string must follow the 'comment' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set root comment.
                    root.appendComment(st.sval);
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing root comment value.");
                    keepGoing = false;
                }
                break;

            case 4 :
                // A string must follow the 'label' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set root label.
                    root.setLabel(st.sval);
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing root label value.");
                    keepGoing = false;
                }
                break;

            case 5 :
                // A string must follow the 'name' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set root name.
                    root.setName(st.sval);
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing root name value.");
                    keepGoing = false;
                }
                break;

            case 6 :
                // A number must follow the 'version' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set root version number.
                    try {
                        root.setVersion(Integer.parseInt(st.sval));
                        state = 2;
                    } catch (NumberFormatException nfe) {
                        System.err.println("Error: Line " + st.lineno() +
                                           ": root version must be a number.");
                        keepGoing = false;
                    }
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing root version value.");
                    keepGoing = false;
                }
                break;

            default :
                // Something strange happened.
                System.err.println("Error: Line " + st.lineno() +
                                   ": Invalid state value.");
                keepGoing = false;
                break;
            } // switch
        } // while

        try {
            br.close();
            isr.close();
        } catch (IOException ioe) { }
        return success;
    } // read

    /**
     * Parse the stream expecting to find a group element definition.
     *
     * @param  group  Group element to fill in.
     * @return  True if okay, false if error.
     */
    protected boolean readGroup(GroupElement group) {
        // current state of our state machine
        int state = 0;
        // success indicates a good parse
        boolean success = false;
        // keeping parsing while this is true
        boolean keepGoing = true;

        // the main parse loop
        while (keepGoing) {
            // read a token and check for EOF
            int token = nextToken();
            if (token == StreamTokenizer.TT_EOF) {
                break;
            }

            switch (state) {
            case 0 :
                // A '[' must follow the 'group' keyword.
                if (token == '[') {
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing '[' after 'group'.");
                    keepGoing = false;
                }
                break;

            case 1 :
                // Look for keywords appropriate for a group.
                if (token == StreamTokenizer.TT_WORD) {
                    if (st.sval.equals("comment")) {
                        state = 2;
                    } else if (st.sval.equals("description")) {
                        state = 3;
                    } else if (st.sval.equals("group")) {
                        GroupElement grp = new GroupElement();
                        keepGoing = readGroup(grp);
                        group.addChild(grp);
                    } else if (st.sval.equals("label")) {
                        state = 4;
                    } else if (st.sval.equals("name")) {
                        state = 5;
                    } else if (st.sval.equals("option")) {
                        OptionElement option = readOption();
                        if (option != null) {
                            group.addChild(option);
                        } else {
                            keepGoing = false;
                        }
                    } else if (st.sval.equals("subgroup")) {
                        SubGroupElement sub = new SubGroupElement();
                        keepGoing = readSubGroup(sub);
                        group.addChild(sub);
                    }
                } else if (token == ']') {
                    // 'group' definition complete
                    success = true;
                    keepGoing = false;
                }
                // else we ignore the token
                break;

            case 2 :
                // A string must follow the 'comment' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set group comment.
                    group.appendComment(st.sval);
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing group comment value.");
                    keepGoing = false;
                }
                break;

            case 3 :
                // A string must follow the 'description' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set group description.
                    group.setDescription(st.sval);
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing group description value.");
                    keepGoing = false;
                }
                break;

            case 4 :
                // A string must follow the 'label' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set group label.
                    group.setLabel(st.sval);
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing group label value.");
                    keepGoing = false;
                }
                break;

            case 5 :
                // A string must follow the 'name' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set group name.
                    group.setName(st.sval);
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing group name value.");
                    keepGoing = false;
                }
                break;

            default :
                // Something strange happened.
                System.err.println("Error: Line " + st.lineno() +
                                   ": Invalid state value.");
                keepGoing = false;
                break;
            } // switch
        } // while
        return success;
    } // readGroup

    /**
     * Parse the stream expecting to find an option element definition.
     *
     * @return  The new option element, or null if error.
     */
    protected OptionElement readOption() {
        // current state of our state machine
        int state = 0;
        // keeping parsing while this is true
        boolean keepGoing = true;
        OptionElement option = null;

        // the main parse loop
        while (keepGoing) {
            // read a token and check for EOF
            int token = nextToken();
            if (token == StreamTokenizer.TT_EOF) {
                break;
            }

            switch (state) {
            case 0:
                // A type string must follow the 'option' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    Class type = (Class) optionClasses.get(st.sval);
                    if (type != null) {
                        try {
                            option = (OptionElement) type.newInstance();
                        } catch (Exception e) {
                            System.err.println("Error: Line " + st.lineno() +
                                               ": error creating option: " +
                                               e);
                        }
                    } else {
                        System.err.println("Error: Line " + st.lineno() +
                                           ": invalid option type: " +
                                           st.sval);
                        keepGoing = false;
                    }
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing option type.");
                    keepGoing = false;
                }
                break;

            case 1 :
                // A '[' must follow the 'option' type.
                if (token == '[') {
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing '[' after 'option'.");
                    keepGoing = false;
                }
                break;

            case 2 :
                // Look for keywords appropriate for an option.
                if (token == StreamTokenizer.TT_WORD) {
                    if (st.sval.equals("comment")) {
                        state = 3;
                    } else if (st.sval.equals("label")) {
                        state = 4;
                    } else if (st.sval.equals("name")) {
                        state = 5;
                    } else if (st.sval.equals("value")) {
                        state = 6;
                    } else if (st.sval.equals("width")) {
                        state = 7;
                    } else if (st.sval.equals("movedFrom")) {
                        state = 8;
                    //} else if (st.sval.equals("values")) {
                        // xxx -  see the file spec
                        // keepGoing = readValues();
                    }
                } else if (token == ']') {
                    // 'option' definition complete
                    keepGoing = false;
                }
                // else we ignore the token
                break;

            case 3 :
                // A string must follow the 'comment' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set option comment.
                    option.appendComment(st.sval);
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing option comment value.");
                    keepGoing = false;
                }
                break;

            case 4 :
                // A string must follow the 'label' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set option label.
                    option.setLabel(st.sval);
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing option label value.");
                    keepGoing = false;
                }
                break;

            case 5:
                // A string must follow the 'name' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set option name.
                    option.setName(st.sval);
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing option name value.");
                    keepGoing = false;
                }
                break;

            case 6:
                // A string must follow the 'value' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set option value.
                    option.setValue(st.sval);
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing option 'value' value.");
                    System.err.println(st);
                    keepGoing = false;
                }
                break;

            case 7:
                // A number must follow the 'width' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set option width.
                    option.setWidth(st.sval);
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing option 'width' value.");
                    System.err.println(st);
                    keepGoing = false;
                }
                break;

            case 8:
                // A string must follow the 'movedFrom' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set option "movedFrom" name.
                    option.setMovedFrom(st.sval);
                    state = 2;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing option 'movedFrom' value.");
                    System.err.println(st);
                    keepGoing = false;
                }
                break;

            default :
                // Something strange happened.
                System.err.println("Error: Line " + st.lineno() +
                                   ": Invalid state value.");
                keepGoing = false;
                break;
            } // switch
        } // while
        return option;
    } // readOption

    /**
     * Parse the stream expecting to find a subgroup element definition.
     *
     * @param  subgroup  Subgroup element to fill in.
     * @return  True if okay, false if error.
     */
    protected boolean readSubGroup(SubGroupElement subgroup) {
        // current state of our state machine
        int state = 0;
        // success indicates a good parse
        boolean success = false;
        // keeping parsing while this is true
        boolean keepGoing = true;

        // the main parse loop
        while (keepGoing) {
            // read a token and check for EOF
            int token = nextToken();
            if (token == StreamTokenizer.TT_EOF) {
                break;
            }

            switch (state) {
            case 0 :
                // A '[' must follow the 'subgroup' keyword.
                if (token == '[') {
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing '[' after 'subgroup'.");
                    keepGoing = false;
                }
                break;

            case 1 :
                // Look for keywords appropriate for an subgroup.
                if (token == StreamTokenizer.TT_WORD) {
                    if (st.sval.equals("comment")) {
                        state = 2;
                    } else if (st.sval.equals("label")) {
                        state = 3;
                    } else if (st.sval.equals("buttongroup")) {
                        // xxx - read the button group
                        // decide if we should keep going
                    } else if (st.sval.equals("option")) {
                        OptionElement option = readOption();
                        if (option != null) {
                            subgroup.addChild(option);
                        } else {
                            keepGoing = false;
                        }
                    }
                } else if (token == ']') {
                    // 'subgroup' definition complete
                    success = true;
                    keepGoing = false;
                }
                // else we ignore the token
                break;

            case 2 :
                // A string must follow the 'comment' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set subgroup comment.
                    subgroup.appendComment(st.sval);
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing subgroup comment value.");
                    keepGoing = false;
                }
                break;

            case 3 :
                // A string must follow the 'label' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set subgroup label.
                    subgroup.setLabel(st.sval);
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing subgroup label value.");
                    keepGoing = false;
                }
                break;

            default :
                // Something strange happened.
                System.err.println("Error: Line " + st.lineno() +
                                   ": Invalid state value.");
                keepGoing = false;
                break;
            } // switch
        } // while
        return success;
    } // readSubGroup

    /**
     * Parse the stream expecting to find a type definition.
     *
     * @param  typedef  Type definition object.
     * @return  True if successful, false otherwise.
     */
    protected boolean readTypedef(Typedef typedef) {
        // current state of our state machine
        int state = 0;
        // success indicates a good parse
        boolean success = false;
        // keeping parsing while this is true
        boolean keepGoing = true;
        // type name and class
        String typename = null;
        Class typeclass = null;

        // the main parse loop
        while (keepGoing) {
            // read a token and check for EOF
            int token = nextToken();
            if (token == StreamTokenizer.TT_EOF) {
                break;
            }

            switch (state) {
            case 0 :
                // A '[' must follow the 'type' keyword.
                if (token == '[') {
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing '[' after 'type'.");
                    keepGoing = false;
                }
                break;

            case 1 :
                // Look for keywords appropriate for a type.
                if (token == StreamTokenizer.TT_WORD) {
                    if (st.sval.equals("typeclass")) {
                        state = 2;
                    } else if (st.sval.equals("typename")) {
                        state = 3;
                    }
                } else if (token == ']') {
                    // 'type' definition complete
                    optionClasses.put(typename, typeclass);
                    success = true;
                    keepGoing = false;
                }
                // else we ignore the token
                break;

            case 2 :
                // A string must follow the 'typeclass' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // look for the class given its name
                    try {
                        typeclass = Class.forName(st.sval);
                        typedef.setTypeClass(typeclass);
                    } catch (ClassNotFoundException cnfe) {
                        System.err.println("Error: Line " + st.lineno() +
                                           ": class not found: " + st.sval);
                    }
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing type class value.");
                    keepGoing = false;
                }
                break;

            case 3 :
                // A string must follow the 'typename' keyword.
                if ((token == StreamTokenizer.TT_WORD) ||
                    (token == '\'' || token == '\"')) {
                    // Set type name.
                    typename = st.sval;
                    typedef.setTypeName(typename);
                    state = 1;
                } else {
                    System.err.println("Error: Line " + st.lineno() +
                                       ": missing type name value.");
                    keepGoing = false;
                }
                break;

            default :
                // Something strange happened.
                System.err.println("Error: Line " + st.lineno() +
                                   ": Invalid state value.");
                keepGoing = false;
                break;
            } // switch
        } // while
        return success;
    } // readTypedef
} // FileParser
