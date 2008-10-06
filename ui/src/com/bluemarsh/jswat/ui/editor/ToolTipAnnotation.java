/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ToolTipAnnotation.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.editor;

import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.expr.EvaluationException;
import com.bluemarsh.jswat.core.expr.Evaluator;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.ThreadReference;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;

/**
 * Provides the tooltips in the Java editor.
 *
 * @author Nathan Fiedler
 */
public class ToolTipAnnotation extends Annotation implements Runnable {
    /** The expression that we are to evaluate. */
    private String expression;

    /**
     * Returns name of the file which describes the annotation type.
     *
     * @return  name of the anotation type.
     */
    public String getAnnotationType() {
        return null;
    }

    /**
     * Retrieve the Java-like expression at the given offset.
     *
     * @param  doc     document in which to find expression.
     * @param  offset  the position at which to look for the expression.
     * @return  the expression, or null if error.
     */
    private static String getExpression(StyledDocument doc, int offset) {
        // Get the line offsets and the line length.
        int line = NbDocument.findLineNumber(doc, offset);
        int column = NbDocument.findLineColumn(doc, offset);
        Element lineElem = NbDocument.findLineRootElement(doc).getElement(line);
        if (lineElem == null) {
            return null;
        }
        int lineStart = lineElem.getStartOffset();
        int lineLength = lineElem.getEndOffset() - lineStart;

        // Acquire the text of this line.
        String text = null;
        try {
            text = doc.getText(lineStart, lineLength);
        } catch (BadLocationException ble) {
            return null;
        }

        // Find the start of the Java expression.
        int exprBegin = column;
        while (exprBegin > 0) {
            char ch = text.charAt(exprBegin);
            if (!Character.isJavaIdentifierPart(ch)
                && ch != '.' && ch != '[' && ch != ']') {
                // We have actually decremented one too many, but we will
                // compensate for that later.
                break;
            }
            exprBegin--;
        }

        // Find the end of the Java expression. Note that we will not
        // walk backward into an array reference.
        int exprEnd = column;
        while (exprEnd < lineLength) {
            char ch = text.charAt(exprEnd);
            if (!Character.isJavaIdentifierPart(ch) && ch != ']') {
                break;
            }
            exprEnd++;
        }

        // No progress was made whatsoever, not a valid expression.
        if (exprBegin == exprEnd) {
            return null;
        }

        // Correct the begin value that was over-decremented.
        exprBegin++;
        return text.substring(exprBegin, exprEnd);
    }

    /**
     * Returns the tooltip text for this annotation.
     *
     * @return  tooltip for this annotation.
     */
    public String getShortDescription() {
        // Get the document for which we are annotating.
        Line.Part lp = (Line.Part) getAttachedAnnotatable();
        if (lp == null) {
            return null;
        }
        Line line = lp.getLine();
        DataObject dobj = DataEditorSupport.findDataObject(line);
        if (dobj == null) {
            return null;
        }
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        if (ec == null) {
            return null;
        }
        StyledDocument doc = null;
        try {
            doc = ec.openDocument();
        } catch (IOException ioe) {
            return null;
        }

        // Find the expression at the annotable element.
        int offset = NbDocument.findLineOffset(doc, line.getLineNumber());
        offset += lp.getColumn();
        expression = getExpression(doc, offset);
        if (expression == null) {
            return null;
        }

        // Perform the evaluation on another thread so we don't block
        // the user interface thread.
        RequestProcessor.getDefault().post(this);                    
        return null;
    }

    /**
     * Performs the expression evaluation.
     */
    public void run() {
        // Need the current session to do anything.
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();

        // Get the current thread and stack frame for evaluation.
        DebuggingContext dc = ContextProvider.getContext(session);
        ThreadReference thread = dc.getThread();
        int frame = dc.getFrame();

        // Evaluate the expression.
        String result = null;
        Evaluator eval = new Evaluator(expression);
        try {
            Object o = eval.evaluate(thread, frame);
            if (o != null) {
                result = expression + " = " + o.toString();
            } else {
                result = expression + " = null";
            }
        } catch (EvaluationException ee) {
            // Ignore all evaluation errors.
            return;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }

        // Let the editor know that our tooltip is available.
        firePropertyChange(PROP_SHORT_DESCRIPTION, null, result);
    }
}
