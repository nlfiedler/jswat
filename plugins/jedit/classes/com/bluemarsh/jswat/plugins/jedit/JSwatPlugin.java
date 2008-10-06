/*********************************************************************
 *
 *      Copyright (C) 2001-2004 David Taylor
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
 * $Id: JSwatPlugin.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.plugins.jedit;

import com.bluemarsh.jswat.Main;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.action.ActionTable;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.command.CommandManager;
import com.bluemarsh.jswat.lang.ClassDefinition;
import com.bluemarsh.jswat.lang.java.JavaParser;
import com.bluemarsh.jswat.parser.java.lexer.LexerException;
import com.bluemarsh.jswat.parser.java.parser.ParserException;
import com.bluemarsh.jswat.ui.UIAdapter;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditorExiting;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.msg.ViewUpdate;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.LogManager;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The primary interface with the jEdit application.
 *
 * @author  David Taylor
 * @author  Stefano Maestri
 * @author  Nathan Fiedler
 * @author  Dirk Moebius
 */
public class JSwatPlugin extends org.gjt.sp.jedit.EBPlugin {
    /** Internal name of our plugin. */
    public static final String NAME = "jswatplugin";
    /** Prefix for getting menu properties. */
    public static final String MENU = "jswatplugin.menu";
    /** Prefix for getting options. */
    public static final String OPTION_PREFIX = "options.jswatplugin.";
    /** Prefix for getting properties. */
    private static final String PROPERTY_PREFIX =
        "plugin.jswatplugin.JSwatPlugin.";
    /** The JSwatPlugin singleton instance */
    private static JSwatPlugin instance;
    /** The JSwat Session. */
    private Session theSession;
    /** The JEdit UIAdapter. */
    private JEditUIAdapter theUIAdapter;
    /** Maps text areas to class definition lists. */
    private Map textAreaClassesMap = new HashMap();
    /** True if the buffer changed and we need to reparse the buffer to
     * determine the class names of the buffer. */
    private boolean needReparse = false;
    /** Maps text areas to TextAreaDecorators instances */
    private Map textAreaDecoratorsMap = new HashMap();

    /**
     * Get the JSwatPlugin singleton instance.
     */
    public static JSwatPlugin getInstance()
    {
        if(instance == null)
            instance = (JSwatPlugin) jEdit.getPlugin(
                "com.bluemarsh.jswat.plugins.jedit.JSwatPlugin", true);
        return instance;
    }

    /**
     * Called by jEdit when the plugin is to be started.
     */
    public void start() {
        // If the tools.jar file contains the JPDA classes, this method
        // call will save the user from having to deal with it.
        MiscUtilities.isToolsJarAvailable();
        // Create our singleton session.
        createSession();
        // Attach our decorators to all open views.
        View[] views = jEdit.getViews();
        for (int i = 0; i < views.length; i++) {
            addTextAreaDecorators(views[i]);
        }
    } // start

    /**
     * Called by jEdit when it is about to terminate, or when the plugin is
     * unloaded.
     */
    public void stop() {
        if (theSession != null) {
            Main.endSession(theSession);
            theSession = null;
        }
        // Remove our decorators from all open views.
        View[] views = jEdit.getViews();
        for (int i = 0; i < views.length; i++) {
            removeTextAreaDecorators(views[i]);
        }
        // Forget the JSwatPlugin singleton instance
        instance = null;
    } // stop

    /**
     * Called by jEdit to get the dockable component.
     * This method returns a new panel of the JEditUIAdapter.
     *
     * @param  view  View instance.
     * @return  the dockable component.
     */
    public JComponent getDock(View view) {
        if (theUIAdapter.getView() == null) {
            theUIAdapter.setView(view);
            return theUIAdapter.getPanel();
        } else if (theUIAdapter.getView() == view) {
            return theUIAdapter.getPanel();
        } else {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel(jEdit.getProperty(OPTION_PREFIX
                + "error.onlyOneInstance.label")), BorderLayout.NORTH);
            return panel;
        }
    } // getDock

    /**
     * Determines the name of the class at the specified line number in
     * the specified jEdit text area. If the name is not yet known,
     * parse the buffer contents to make the determination. Returns a
     * fully qualified classname. Inner classes are returned as
     * "OuterClass$InnerClass".
     *
     * @param  textArea  the current editing area.
     * @param  lineNo    the line number, starting at 1.
     * @return  a fully qualified classname, or null if the buffer doesn't
     *          contain any class definition at the specified line.
     * @throws  IOException
     *          if error reading source file.
     * @throws  LexerException
     *          if file contains syntax error.
     * @throws  ParserException
     *          if file is not valid Java source.
     */
    String getClassnameAtLine(JEditTextArea textArea, int lineNo)
        throws IOException, LexerException, ParserException {
        List classLines = getClassDefinitions(textArea);
        if (classLines == null) {
            return null;
        } else {
            return ClassDefinition.findClassForLine(classLines, lineNo);
        }
    } // getClassnameAtLine

    /**
     * Determines the name of the class at the current cursor position in the
     * current text area of the specified view. If the name is not yet known,
     * parse the buffer contents to make the determination.
     * Returns a fully qualified classname. Inner classes are returned as
     * "OuterClass$InnerClass".
     *
     * @param  view  the jEdit view.
     * @return  a fully qualified classname, or null if the buffer doesn't
     *   contain any class definition at the specified line.
     */
    public String getClassnameAtCursor(View view)
        throws IOException, LexerException, ParserException
    {
        JEditTextArea textArea = view.getTextArea();
        int line = textArea.getCaretLine() + 1;
        String classname = getClassnameAtLine(textArea, line);
        return classname;
    } // getClassnameAtCursor

    /**
     * Determines all names of all class defined in the specified jEdit text
     * area. The text area is parsed only and the result is cached afterwards,
     * until the text area contents changes.
     * Returns a list of class definitions
     * (<code>com.bluemarsh.jswat.lang.ClassDefinition</code>).
     *
     * @param  textArea  the current editing area.
     * @return  a list of class definitions, or null, if there are no class
     *          definitions in the text area.
     * @throws  IOException
     *          if error reading source file.
     * @throws  LexerException
     *          if file contains syntax error.
     * @throws  ParserException
     *          if file is not valid Java source.
     */
    List getClassDefinitions(JEditTextArea textArea)
        throws IOException, LexerException, ParserException {
        List classLines = (List) textAreaClassesMap.get(textArea);
        if (classLines == null || needReparse) {
            // Parse buffer
            Buffer buffer = textArea.getBuffer();
            if (!buffer.isLoaded()) {
                return null;
            }
            String text = buffer.getText(0, buffer.getLength());
            StringReader reader = new StringReader(text);
            JavaParser parser = new JavaParser(reader);
            parser.parse();
            classLines = parser.getClassLines();
            textAreaClassesMap.put(textArea, classLines);
            needReparse = false;
        }
        return classLines;
    } // getClassDefinitions

    /**
     * Returns the Session instance.
     *
     * @return  the session.
     */
    public Session getSession() {
        return theSession;
    } // getSession

    /**
     * Creates the Session instance.
     */
    private void createSession() {
        // Call Main.init() because it might do something useful.
        Main.init();
        // Set the logging properties. By default we log to a file in the
        // user's home for performance reasons: if we'd log to stdout, it
        // would appear in jEdit's activity log, and this is slow.
        LogManager manager = LogManager.getLogManager();
        InputStream is = JSwatPlugin.class.getResourceAsStream(
            "logging.properties");
        try {
            manager.readConfiguration(is);
        } catch (IOException e) {
            Log.log(Log.ERROR, this,
                "Error reading JSwat logging properties: " + e);
        }
        // Set UI adapter class.
        Main.setUIAdapter(JEditUIAdapter.class);
        // Create new adapter.
        theUIAdapter = (JEditUIAdapter) Main.newUIAdapter();
        // This assumes only one JSwat session...
        theSession = Main.newSession(theUIAdapter);
    } // createSession

    /**
     * Handle a message from the jEdit core.
     *
     * @param  message  the message.
     */
    public void handleMessage(EBMessage message) {
        if (message instanceof EditPaneUpdate) {
            EditPaneUpdate epu = (EditPaneUpdate) message;
            EditPane editPane = epu.getEditPane();
            JEditTextArea textArea = editPane.getTextArea();

            if (epu.getWhat() == EditPaneUpdate.CREATED) {
                addTextAreaDecorators(textArea);
                needReparse = true;
            } else if (epu.getWhat() == EditPaneUpdate.BUFFER_CHANGED) {
                needReparse = true;
            } else if (epu.getWhat() == EditPaneUpdate.DESTROYED) {
                removeTextAreaDecorators(textArea);
            }

        } else if (message instanceof BufferUpdate) {
            BufferUpdate bu = (BufferUpdate) message;
            if (bu.getWhat() == BufferUpdate.LOADED
                || bu.getWhat() == BufferUpdate.DIRTY_CHANGED) {
                needReparse = true;
            }

        } else if (message instanceof ViewUpdate) {
            if (theUIAdapter != null) {
                ViewUpdate vu = (ViewUpdate) message;
                if (vu.getWhat() == ViewUpdate.CREATED) {
                    if (theUIAdapter.getView() == null) {
                        theUIAdapter.setView(vu.getView());
                    }
                    // Attach our decorators to all available text areas.
                    addTextAreaDecorators(vu.getView());
                } else if (vu.getWhat() == ViewUpdate.CLOSED) {
                    View closedView = vu.getView();
                    // Remove our decorators from all available text areas.
                    removeTextAreaDecorators(closedView);
                    // If the user closed the view that holds the UIAdapter,
                    // associate the UIAdapter with another arbitrarily chosen
                    // view (in case there is one).
                    View[] views = jEdit.getViews();
                    if (theUIAdapter.getView() == closedView) {
                        for (int i = 0; i < views.length; ++i) {
                            if (views[i] != closedView) {
                                theUIAdapter.setView(views[i]);
                                break;
                            }
                        }
                    }
                }
            }

        } else if (message instanceof PropertiesChanged) {
            if (theUIAdapter != null) {
                theUIAdapter.propertiesChanged();
            }
        }
    } // handleMessage

    /**
     * Display the JSwat help index page.
     *
     * @param  view  view on which to display help.
     */
    public void helpIndex(View view) {
        JEditUIAdapter adapter = (JEditUIAdapter) getSession().getUIAdapter();
        adapter.showHelp(com.bluemarsh.jswat.Bundle.getResource("helpIndex"));
    } // helpIndex

    /**
     * Toggles a breakpoint at the current line of the current file between
     * the three states "on", "off" and "disabled".
     *
     * @param  view  associated jEdit view.
     */
    public void toggleBreakpoint(View view) {
        try {
            JEditTextArea textArea = view.getTextArea();
            int line = textArea.getCaretLine() + 1;
            String classname = getClassnameAtLine(textArea, line);

            if (classname == null) {
                GUIUtilities.error(view, OPTION_PREFIX
                                   + "error.noCodeAtLine", null);
                return;
            }

            BreakpointManager breakMgr = (BreakpointManager)
                getSession().getManager(BreakpointManager.class);

            Breakpoint bp = breakMgr.getBreakpoint(classname, line);
            if (bp == null) {
                bp = new LineBreakpoint(classname, line);
                breakMgr.addNewBreakpoint(bp);
            } else if (bp.isEnabled()) {
                breakMgr.disableBreakpoint(bp);
            } else {
                breakMgr.removeBreakpoint(bp);
            }
        } catch (Exception e) {
            Log.log(Log.ERROR, this,
                "An error occured during execution of a JSwat action:");
            Log.log(Log.ERROR, this, e);
            GUIUtilities.error(view, OPTION_PREFIX + "error.invokeAction",
                new Object[] { e.toString() });
        }
    } // toggleBreakpoint

    /**
     * Runs to the current line of the current file.
     *
     * @param  view  associated jEdit view.
     */
    public void runUntil(View view) {
        try {
            if (!getSession().isActive()) {
                return;
            }

            JEditTextArea textArea = view.getTextArea();
            int line = textArea.getCaretLine() + 1;
            String classname = getClassnameAtLine(textArea, line);

            if (classname == null) {
                GUIUtilities.error(view, OPTION_PREFIX
                                   + "error.noCodeAtLine", null);
                return;
            }

            Breakpoint bp = new LineBreakpoint(classname, line);
            bp.setExpireCount(1);
            bp.deleteOnExpire();

            BreakpointManager breakMgr = (BreakpointManager)
                getSession().getManager(BreakpointManager.class);
            breakMgr.addNewBreakpoint(bp);

            vmResume(view);
        } catch (Exception e) {
            Log.log(Log.ERROR, this,
                "An error occured during execution of a JSwat action:");
            Log.log(Log.ERROR, this, e);
            GUIUtilities.error(view, OPTION_PREFIX + "error.invokeAction",
                new Object[] { e.toString() });
        }
    } // runUntil

    /**
     * Show the plugin's options with respect to the given view.
     *
     * @param  view  current view.
     * @return  true to continue, false if user cancelled.
     */
    public boolean showOptions(View view) {
        JSwatOptionPane options = new JSwatOptionPane();
        options._init();
        Icon icon = new ImageIcon(com.bluemarsh.jswat.ui.Bundle
            .getResource("houseflyImage"));
        int result = JOptionPane.showOptionDialog(
            view, options, "JSwat Options", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE, icon, null, null);
        if (result == JOptionPane.OK_OPTION) {
            options._save();
            return true;
        } else {
            return false;
        }
    } // showOptions

    /**
     * Perform a single-step operation.
     *
     * @param  view  current view.
     */
    public void stepInto(View view) {
        invokeJSwatAction(view, "step");
    } // stepInto

    /**
     * Perform a step out operation.
     *
     * @param  view  current view.
     */
    public void stepOut(View view) {
        invokeJSwatAction(view, "finish");
    } // stepOut

    /**
     * Perform a step over operation.
     *
     * @param  view  current view.
     */
    public void stepOver(View view) {
        invokeJSwatAction(view, "next");
    } // stepOver

    /**
     * Start debugging the debuggee. If the session is already active,
     * deactivate it before proceeding.
     *
     * @param  view  the current view.
     */
    public void vmStart(View view) {
        invokeJSwatAction(view, "vmStart");
    } // vmStart

    /**
     * Stop the debuggee and close it.
     *
     * @param  view  current view.
     */
    public void vmStop(View view) {
        invokeJSwatAction(view, "vmStop");
    } // vmStop

    /**
     * Resume the debuggee.
     *
     * @param  view  current view.
     */
    public void vmResume(View view) {
        invokeJSwatAction(view, "vmResume");
    } // vmResume

    /**
     * Suspend the debuggee.
     *
     * @param  view  current view.
     */
    public void vmSuspend(View view) {
        invokeJSwatAction(view, "vmSuspend");
    } // vmSuspend

    /**
     * Attach to a remote debuggee.
     *
     * @param  view  current view.
     */
    public void vmAttach(View view) {
        invokeJSwatAction(view, "vmAttach");
    } // vmSuspend

    /**
     * Detach from a remote debuggee.
     *
     * @param  view  current view.
     */
    public void vmDetach(View view) {
        invokeJSwatAction(view, "vmClose");
    } // vmSuspend

    /**
     * Toggles a breakpoint at the current line of the current file between
     * the three states "on", "off" and "disabled".
     *
     * @param  view  associated jEdit view.
     */
    public void hotswapClass(View view) {
        try {
            String classname = getClassnameAtCursor(view);

            if (classname == null) {
                GUIUtilities.error(view, OPTION_PREFIX + "error.noCodeAtLine",
                                   null);
                return;
            }

            invokeJSwatCommand(view, "hotswap " + classname);
        } catch (Exception e) {
            Log.log(Log.ERROR, this,
                "An error occured during execution of a JSwat action:");
            Log.log(Log.ERROR, this, e);
            GUIUtilities.error(view, OPTION_PREFIX + "error.invokeAction",
                new Object[] { e.toString() });
        }
    } // toggleBreakpoint

    /**
     * Invoke a JSwat action.
     * Note that only JSwat's Swing actions can be invoked with this method,
     * but not JSwat commands of the command shell.
     *
     * @param  view  associated jEdit view.
     * @param  actionName  the internal name of the JSwat action.
     */
    private void invokeJSwatAction(View view, String actionName) {
        try {
            // Check whether the frame of the panel has been registered.
            // Most JSwat actions require a frame associated with the current
            // session.
            if (!theUIAdapter.isFrameRegistered()) {
                // Show the JSwat panel. Create it if necessary.
                view.getDockableWindowManager()
                    .showDockableWindow("jswatplugin");
            }
            // Check whether this is the right view.
            if (theUIAdapter.getView() != view) {
                return;
            }
            // Invoke action.
            Action a = ActionTable.getAction(actionName);
            a.actionPerformed(new ActionEvent(view,
                ActionEvent.ACTION_PERFORMED, actionName));
        } catch (Exception e) {
            Log.log(Log.ERROR, this,
                "An error occured during execution of JSwat action '"
                + actionName + "': ");
            Log.log(Log.ERROR, this, e);
            GUIUtilities.error(view, OPTION_PREFIX + "error.invokeAction",
                new Object[] { e.toString() });
        }
    } // invokeJSwatAction

    /**
     * Invoke a JSwat command.
     * This method may be used by BeanShell scripts to invoke arbitrary JSwat
     * commands.
     *
     * @param  view  associated jEdit view.
     * @param  command  the command, optionally with arguments, separated
     *         by spaces.
     */
    public void invokeJSwatCommand(View view, String command) {
        try {
            // Show the JSwat panel.
            view.getDockableWindowManager().showDockableWindow("jswatplugin");
            // Check whether this is the right view.
            if (theUIAdapter.getView() != view) {
                return;
            }
            // Show messages panel.
            JEditUIAdapter adapter = (JEditUIAdapter)
                getSession().getUIAdapter();
            adapter.showMessagesPane();
            // Write command to messages log.
            getSession().getStatusLog().writeln("> " + command);
            // Get command manager.
            CommandManager commandMgr =  (CommandManager)
                getSession().getManager(CommandManager.class);
            // Invoke command.
            commandMgr.handleInput(command);
        } catch (Exception e) {
            Log.log(Log.ERROR, this,
                "An error occured during execution of JSwat command '"
                + command + "':");
            Log.log(Log.ERROR, this, e);
            GUIUtilities.error(view, OPTION_PREFIX + "error.invokeCommand",
                new Object[] { command, e.toString() });
        }
    } // invokeJSwatCommand

    /**
     * Helper class remembering the decorators added to jEdit text areas.
     */
    private class TextAreaDecorators {
        BreakpointHighlight bph;
        VariableValueTooltip vvt;

        public TextAreaDecorators(
            BreakpointHighlight bph,
            VariableValueTooltip vvt)
        {
            this.bph = bph;
            this.vvt = vvt;
        }
    } // class TextAreaDecorators

    /**
     * Attach our breakpoint highlighter and variable value tooltip renderer
     * to all text areas of the given view.
     *
     * @param  view  view to attach to.
     */
    private void addTextAreaDecorators(View view) {
        Log.log(Log.DEBUG, this, "adding text area decorators for view...");
        // Attach to all available text areas.
        EditPane[] panes = view.getEditPanes();
        for (int i = 0; i < panes.length; i++) {
            EditPane pane = panes[i];
            JEditTextArea textArea = pane.getTextArea();
            addTextAreaDecorators(textArea);
        }
    } // addTextAreaDecorators

    /**
     * Attach our breakpoint highlighter and variable value tooltip renderer
     * to the given text area.
     *
     * @param  textArea  text area to attach to.
     */
    private void addTextAreaDecorators(JEditTextArea textArea) {
        removeTextAreaDecorators(textArea);
        Log.log(Log.DEBUG, this, "adding text area decorators for text area...");
        BreakpointHighlight bph = new BreakpointHighlight(textArea);
        textArea.getGutter().addExtension(textArea.getGutter().HIGHEST_LAYER, bph);
        VariableValueTooltip vvt = new VariableValueTooltip(textArea);
        textArea.getPainter().addExtension(vvt);
        textAreaDecoratorsMap.put(textArea, new TextAreaDecorators(bph, vvt));
    } // addTextAreaDecorators

    /**
     * Remove our breakpoint highlighter and variable value tooltip renderer
     * from all text areas of the given view.
     *
     * @param  view  view to remove from.
     */
    private void removeTextAreaDecorators(View view) {
        Log.log(Log.DEBUG, this, "removing text area decorators from view...");
        // Remove from all available text areas.
        EditPane[] panes = view.getEditPanes();
        for (int i = 0; i < panes.length; i++) {
            EditPane pane = panes[i];
            JEditTextArea textArea = pane.getTextArea();
            removeTextAreaDecorators(textArea);
        }
    } // removeTextAreaDecorators

    /**
     * Remove our breakpoint highlighter and variable value tooltip renderer
     * from the given text area (in case they are there).
     *
     * @param  textArea  text area to remove from.
     */
    private void removeTextAreaDecorators(JEditTextArea textArea) {
        if (textArea != null) {
            TextAreaDecorators decorators = (TextAreaDecorators)
                textAreaDecoratorsMap.remove(textArea);
            if (decorators != null) {
                Log.log(Log.DEBUG, this,
                    "removing text area decorators from text area...");
                textArea.getGutter().removeExtension(decorators.bph);
                textArea.getPainter().removeExtension(decorators.vvt);
            }
        }
    } // removeTextAreaDecorators

} // JSwatPlugin
