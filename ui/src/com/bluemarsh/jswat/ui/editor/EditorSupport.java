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
 * are Copyright (C) 2005-2008. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: EditorSupport.java 29 2008-06-30 00:41:09Z nfiedler $
 */

package com.bluemarsh.jswat.ui.editor;

import com.bluemarsh.jswat.core.breakpoint.LineBreakpoint;
import java.awt.EventQueue;
//import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
//import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
//import org.openide.src.ClassElement;
//import org.openide.src.ConstructorElement;
//import org.openide.src.Element;
//import org.openide.src.FieldElement;
//import org.openide.src.Identifier;
//import org.openide.src.InitializerElement;
//import org.openide.src.SourceElement;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.windows.TopComponent;

/**
 * Class EditorSupport provides methods for accessing the current state
 * of the NetBeans editor.
 *
 * @author Nathan Fiedler
 */
public class EditorSupport {
    /** The singleton instance of this class. */
    private static EditorSupport theInstance;
    /** Document listener for keeping breakpoints updated. */
    private DocumentWatcher docWatcher;
    /** Mapping of Documents and their Annotations. */
    private Map<Document, Set<Annotation>> documentAnnotations;

    /**
     * Creates a new instance of EditorSupport.
     */
    private EditorSupport() {
        docWatcher = new DocumentWatcher();
        documentAnnotations = new HashMap<Document, Set<Annotation>>();
    }

    /**
     * Adds an annotation to the given line of a specific file.
     *
     * @param  url   source annotation should be set in.
     * @param  line  line number for annotation.
     * @param  type  type of annotation to be set.
     * @return  the newly created annotation, or null if error.
     */
    public DebugAnnotation annotate(String url, int line, String type) {
        return annotate(url, line, type, null);
    }

    /**
     * Adds an annotation to the given line of a specific file.
     *
     * @param  url   source annotation should be set in.
     * @param  line  line number for annotation.
     * @param  type  type of annotation to be set.
     * @param  obj   an object to associate with the annotation.
     * @return  the newly created annotation, or null if error.
     */
    public DebugAnnotation annotate(String url, int line, String type, Object obj) {
        Line l = getLine(url, line);
        if (l == null) {
            return null;
        }
        DebugAnnotation annotation;
        if (obj == null) {
            annotation = new DebugAnnotation(type, l);
        } else {
            annotation = new DebugAnnotation(type, l, obj);
        }
        DataObject dobj = getDataObject(url);
        Node dnode = dobj.getNodeDelegate();
        EditorCookie ec = dnode.getCookie(EditorCookie.class);
        if (ec != null) {
            // Need to track which annotations belong to which documents
            // so we can keep them updated as the documents change.
            Document doc = ec.getDocument();
            if (doc != null) {
                Set<Annotation> anns = documentAnnotations.get(doc);
                if (anns == null) {
                    anns = new HashSet<Annotation>();
                    documentAnnotations.put(doc, anns);
                    // First time document, add our listener.
                    doc.addDocumentListener(docWatcher);
                }
                anns.add(annotation);
            } else {
                // This may happen if the NB scanner encounters an error.
                ErrorManager.getDefault().log(
                        ErrorManager.WARNING, "Missing Document for " + url);
            }
        } else {
            ErrorManager.getDefault().log(
                    ErrorManager.WARNING, "Missing EditorCookie for " + url);
        }
        annotation.attach(l);
        return annotation;
    }

    /**
     * Returns class name for given url and line number.
     *
     * @param  url   the URL for the source file.
     * @param  line  line number somewhere in class definition.
     * @return  class name for given url and line number, or null if not found.
     */
    public String getClassName(String url, int line) {
        // XXX: org.openide.src is gone
//        Element element = getElement(url, line);
//        if (element == null) {
//            return "";
//        }
//        if (element instanceof ClassElement) {
//            return getClassName((ClassElement) element);
//        }
//        if (element instanceof ConstructorElement) {
//            return getClassName(((ConstructorElement) element).getDeclaringClass());
//        }
//        if (element instanceof FieldElement) {
//            return getClassName(((FieldElement) element).getDeclaringClass());
//        }
//        if (element instanceof InitializerElement) {
//            return getClassName(((InitializerElement) element).getDeclaringClass());
//        }
        return "";
    }

//    /**
//     * Converts the given ClassElement into a class name String.
//     *
//     * @param  e  class element from which to get name.
//     * @return  the name of the class.
//     */
//    private static String getClassName(ClassElement e) {
//        String f = e.getName().getFullName();
//        if (!e.isInner()) {
//            return f;
//        }
//        SourceElement sourceEl = e.getSource();
//        if (sourceEl == null) {
//            return f;
//        }
//        Identifier ident = sourceEl.getPackage();
//        String c;
//        if (ident == null) {
//            c = "";
//        } else {
//            c = ident.getFullName();
//        }
//        if (c.length() > 0) {
//            return c + '.' + f.substring(c.length() + 1).replace('.', '$');
//        }
//        return f.replace('.', '$');
//    }

    /**
     * Retrieves the currently selected editor pane, if any.
     *
     * @return  current editor pane, or null if not available.
     */
    private static JEditorPane getCurrentEditor() {
        EditorCookie ec = getCurrentEditorCookie();
        if (ec == null) {
            return null;
        }
        JEditorPane[] ep = ec.getOpenedPanes();
        if (ep == null || ep.length < 1) {
            return null;
        }
        return ep[0];
    }

    /**
     * Retrieves the currently selected editor cookie, if any.
     *
     * @return  current editor cookie, or null if not available.
     */
    private static EditorCookie getCurrentEditorCookie() {
        Node[] nodes = TopComponent.getRegistry().getCurrentNodes();
        // There must be exactly one selected editor node.
        if (nodes == null || nodes.length != 1) {
            return null;
        }
        return nodes[0].getCookie(EditorCookie.class);
    }

    /**
     * Returns the line number of caret in current editor.
     *
     * @return  one-based line number, or -1 if not available.
     */
    public int getCurrentLineNumber() {
        EditorCookie ec = getCurrentEditorCookie();
        if (ec == null) {
            return -1;
        }
        JEditorPane ep = getCurrentEditor();
        if (ep == null) {
            return -1;
        }
        StyledDocument d = ec.getDocument();
        if (d == null) {
            return -1;
        }
        Caret caret = ep.getCaret();
        if (caret == null) {
            return -1;
        }
        // Convert zero-based value to one-based, since breakpoints
        // in JDI are all one-based lines.
        return NbDocument.findLineNumber(d, caret.getDot()) + 1;
    }

    /**
     * Returns the unique file URL of the currently selected source.
     *
     * @return  URL of file, or null if not available.
     */
    public String getCurrentURL() {
        Node[] nodes = TopComponent.getRegistry().getCurrentNodes();
        // There must be exactly one selected editor node.
        if (nodes == null || nodes.length != 1) {
            return null;
        }
        Node n = nodes[0];
        DataObject dO = null;
        if (n instanceof DataNode) {
            dO = ((DataNode) n).getDataObject();
        }
        if (dO == null) {
            dO = n.getCookie(DataObject.class);
        }
        if (dO == null) {
            return null;
        }
        if (dO instanceof DataShadow) {
            dO = ((DataShadow) dO).getOriginal();
        }
        try {
            return dO.getPrimaryFile().getURL().toString();
        } catch (FileStateInvalidException fsie) {
            return null;
        }
    }

    /**
     * Retrieves the data object representing the file identified by the url.
     *
     * @param  url  unique identifier of which to data object.
     * @return  data object, or null if not available.
     */
    private DataObject getDataObject(String url) {
        try {
            FileObject fo = URLMapper.findFileObject(new URL(url));
            if (fo != null) {
                return DataObject.find(fo);
            }
        } catch (DataObjectNotFoundException donfe) {
        } catch (MalformedURLException mue) {
        }
        return null;
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return  instance of this class.
     */
    public static synchronized EditorSupport getDefault() {
        if (theInstance == null) {
            theInstance = new EditorSupport();
        }
        return theInstance;
    }

    /**
     * Returns the source element for given url and line number, if any.
     *
     * @param  url   the URL for the source file.
     * @param  line  line number somewhere in class definition.
     * @return  source element, or null if none.
     */
//    public Element getElement(String url, int line) { XXX org.openide.src is gone
//        DataObject dobj = getDataObject(url);
//        if (dobj != null) {
//            SourceCookie.Editor sc = (SourceCookie.Editor) dobj.
//                    getCookie(SourceCookie.Editor.class);
//            if (sc != null) {
//                try {
//                    StyledDocument sd = sc.openDocument();
//                    if (sd != null) {
//                        int offset = NbDocument.findLineOffset(sd, line - 1);
//                        return sc.findElement(offset);
//                    }
//                } catch (IOException ioe) {
//                    // Fall through to return null.
//                } catch (IndexOutOfBoundsException ioobe) {
//                    // Fall through to return null.
//                }
//            }
//        }
//        return null;
//    }

    /**
     * Get the Line instance from the given file at the given line.
     *
     * @param  url   unique identifier of which to find line.
     * @param  line  desired line number.
     * @return  line from the line set, or null if not available.
     */
    private Line getLine(String url, int line) {
        Line.Set ls = getLineSet(url);
        if (ls != null) {
            try {
                return ls.getCurrent(line - 1);
            } catch (IndexOutOfBoundsException ioobe) {
            } catch (IllegalArgumentException iae) {
            }
        }
        return null;
    }

    /**
     * Retrieve the current line set of the given file.
     *
     * @param  url  unique identifier of which to find line set.
     * @return  line set as given by line cookie, or null if not available.
     */
    private Line.Set getLineSet(String url) {
        DataObject dataObject = getDataObject(url);
        if (dataObject != null) {
            LineCookie lineCookie = dataObject.getCookie(LineCookie.class);
            if (lineCookie != null) {
                return lineCookie.getLineSet();
            }
        }
        return null;
    }

    /**
     * Removes the given annotation from whatever line it is attached to.
     */
    public void removeAnnotation(Annotation ann) {
        Annotatable antbl = ann.getAttachedAnnotatable();
        if (antbl instanceof Line) {
            // Remove the annotation from our Document map.
            Line line = (Line) antbl;
            DataObject dobj = line.getLookup().lookup(DataObject.class);
            Node dnode = dobj.getNodeDelegate();
            EditorCookie ec = dnode.getCookie(EditorCookie.class);
            Document doc = ec.getDocument();
            Set<Annotation> anns = documentAnnotations.get(doc);
            if (anns != null) {
                anns.remove(ann);
                if (anns.isEmpty()) {
                    documentAnnotations.remove(doc);
                    // Don't need to listen to this document anymore.
                    doc.removeDocumentListener(docWatcher);
                }
            }
        }
        ann.detach();
    }

    /**
     * Shows source file as given by the url, at the given line number.
     *
     * @param  url   url of source file to be shown.
     * @param  line  line number to be made visible.
     * @return  true if successful, false otherwise.
     */
    public boolean showSource(String url, int line) {
        final Line l = getLine(url, line);
        if (l == null) {
            return false;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
            }
        });
        return true;
    }

    /**
     * Watches documents for changes and takes appropriate steps.
     */
    private class DocumentWatcher implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent event) {
        }

        @Override
        public void insertUpdate(DocumentEvent event) {
            updateAnnotations(event);
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            updateAnnotations(event);
        }

        /**
         * Update the annotations associated with the document.
         *
         * @param  event  document event.
         */
        private void updateAnnotations(DocumentEvent event) {
            Document doc = event.getDocument();
            // Get annotations associated with this document.
            Set<Annotation> anns = documentAnnotations.get(doc);
            if (anns != null) {
                for (Annotation ann : anns) {
                    // Get line associated with this annotation.
                    Annotatable antbl = ann.getAttachedAnnotatable();
                    if (antbl instanceof Line) {
                        Line line = (Line) antbl;
                        // Get current line number, assuming it changed.
                        int linenum = line.getLineNumber() + 1;
                        // Get associated breakpoint.
                        DebugAnnotation dann = (DebugAnnotation) ann;
                        Object obj = dann.getUserObject();
                        if (obj instanceof LineBreakpoint) {
                            LineBreakpoint lb = (LineBreakpoint) obj;
                            // Update breakpoint line number.
                            if (lb.getLineNumber() != linenum) {
                                lb.setLineNumber(linenum);
                            }
                        }
                    }
                }
            }
        }
    }
}
