/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * MODULE:      View
 * FILE:        JavaScanner.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/19/02        Initial version
 *
 * DESCRIPTION:
 *      This file contains the JavaScanner class definition.
 *
 * $Id: JavaScanner.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.adt.SkipList;
import com.bluemarsh.jswat.parser.java.analysis.AnalysisAdapter;
import com.bluemarsh.jswat.parser.java.lexer.*;
import com.bluemarsh.jswat.parser.java.node.*;
import java.io.*;
import javax.swing.text.BadLocationException;

/**
 * Class JavaScanner is responsible for lexically scanning a Java
 * source file and generating a list of JavaTokenInfo objects.
 *
 * @author  Nathan Fiedler
 */
public class JavaScanner extends AnalysisAdapter {
    /** Source file reader. */
    protected Reader reader;
    /** Source content. */
    protected SourceContent content;
    /** List of JavaTokenInfo objects. */
    protected SkipList tokenList;

    /**
     * Constructs a JavaScanner to read from the given Reader.
     *
     * @param  r    input reader.
     * @param  doc  source content.
     */
    public JavaScanner(Reader r, SourceContent doc) {
        reader = r;
        content = doc;
        tokenList = new SkipList();
    } // JavaScanner

    /**
     * Scan the input source file and return a SkipList of JavaTokenInfo
     * objects.
     *
     * @return  list of tokens, or null if error.
     * @exception  IOException
     *             thrown if an I/O error occurred.
     * @exception  LexerException
     *             thrown if a lexer error occurred.
     */
    public SkipList scan() throws IOException, LexerException {
        PushbackReader pbr = new PushbackReader(reader);
        Lexer lexer = new Lexer(pbr);
        Token token = lexer.next();
        while (!(token instanceof EOF)) {
            token.apply(this);
            token = lexer.next();
        }
        pbr.close();
        return tokenList;
    } // scan

    /**
     * Determines the offset from the start of the source file of the
     * given token.
     *
     * @param  t  token whose position is in question.
     * @return  zero-based offset of token.
     */
    protected int getOffset(Token t) {
        try {
            int line = t.getLine() - 1;
            int offset = t.getPos() - 1;
            offset += content.getLineStartOffset(line);
            return offset;
        } catch (BadLocationException ble) {
            return -1;
        }
    } // getOffset

    /**
     * Handle a keyword token.
     *
     * @param  node  token representing a keyword.
     */
    protected void handleKeyword(Token node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_KEYWORD));
    } // handleKeyword

    /**
     * Handle a literal token ('false', 'true', 'null').
     *
     * @param  node  token representing a literal.
     */
    protected void handleLiteral(Token node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_LITERAL));
    } // handleLiteral

    //
    // These are the AnalysisAdapter methods that we override.
    //

    public void caseTDecimalIntegerLiteral(TDecimalIntegerLiteral node)
    {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_NUMBER));
    }

    public void caseTHexIntegerLiteral(THexIntegerLiteral node)
    {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_NUMBER));
    }

    public void caseTOctalIntegerLiteral(TOctalIntegerLiteral node)
    {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_NUMBER));
    }

    public void caseTFloatingPointLiteral(TFloatingPointLiteral node)
    {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_NUMBER));
    }

    public void caseTCharacterLiteral(TCharacterLiteral node)
    {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_CHARACTER));
    }

    public void caseTStringLiteral(TStringLiteral node)
    {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_STRING));
    }

    public void caseTTraditionalComment(TTraditionalComment node)
    {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_COMMENT));
    }

    public void caseTDocumentationComment(TDocumentationComment node)
    {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_COMMENT));
    }

    public void caseTEndOfLineComment(TEndOfLineComment node)
    {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_COMMENT));
    }

    public void caseTIdentifier(TIdentifier node)
    {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_IDENTIFIER));
    }

    public void caseTAbstract(TAbstract node)
    {
        handleKeyword(node);
    }

    public void caseTBoolean(TBoolean node)
    {
        handleKeyword(node);
    }

    public void caseTBreak(TBreak node)
    {
        handleKeyword(node);
    }

    public void caseTByte(TByte node)
    {
        handleKeyword(node);
    }

    public void caseTCase(TCase node)
    {
        handleKeyword(node);
    }

    public void caseTCatch(TCatch node)
    {
        handleKeyword(node);
    }

    public void caseTChar(TChar node)
    {
        handleKeyword(node);
    }

    public void caseTClass(TClass node)
    {
        handleKeyword(node);
    }

    public void caseTConst(TConst node)
    {
        handleKeyword(node);
    }

    public void caseTContinue(TContinue node)
    {
        handleKeyword(node);
    }

    public void caseTDefault(TDefault node)
    {
        handleKeyword(node);
    }

    public void caseTDo(TDo node)
    {
        handleKeyword(node);
    }

    public void caseTDouble(TDouble node)
    {
        handleKeyword(node);
    }

    public void caseTElse(TElse node)
    {
        handleKeyword(node);
    }

    public void caseTExtends(TExtends node)
    {
        handleKeyword(node);
    }

    public void caseTFinal(TFinal node)
    {
        handleKeyword(node);
    }

    public void caseTFinally(TFinally node)
    {
        handleKeyword(node);
    }

    public void caseTFloat(TFloat node)
    {
        handleKeyword(node);
    }

    public void caseTFor(TFor node)
    {
        handleKeyword(node);
    }

    public void caseTGoto(TGoto node)
    {
        handleKeyword(node);
    }

    public void caseTIf(TIf node)
    {
        handleKeyword(node);
    }

    public void caseTImplements(TImplements node)
    {
        handleKeyword(node);
    }

    public void caseTImport(TImport node)
    {
        handleKeyword(node);
    }

    public void caseTInstanceof(TInstanceof node)
    {
        handleKeyword(node);
    }

    public void caseTInt(TInt node)
    {
        handleKeyword(node);
    }

    public void caseTInterface(TInterface node)
    {
        handleKeyword(node);
    }

    public void caseTLong(TLong node)
    {
        handleKeyword(node);
    }

    public void caseTNative(TNative node)
    {
        handleKeyword(node);
    }

    public void caseTNew(TNew node)
    {
        handleKeyword(node);
    }

    public void caseTPackage(TPackage node)
    {
        handleKeyword(node);
    }

    public void caseTPrivate(TPrivate node)
    {
        handleKeyword(node);
    }

    public void caseTProtected(TProtected node)
    {
        handleKeyword(node);
    }

    public void caseTPublic(TPublic node)
    {
        handleKeyword(node);
    }

    public void caseTReturn(TReturn node)
    {
        handleKeyword(node);
    }

    public void caseTShort(TShort node)
    {
        handleKeyword(node);
    }

    public void caseTStatic(TStatic node)
    {
        handleKeyword(node);
    }

    public void caseTStrictfp(TStrictfp node)
    {
        handleKeyword(node);
    }

    public void caseTSuper(TSuper node)
    {
        handleKeyword(node);
    }

    public void caseTSwitch(TSwitch node)
    {
        handleKeyword(node);
    }

    public void caseTSynchronized(TSynchronized node)
    {
        handleKeyword(node);
    }

    public void caseTThis(TThis node)
    {
        handleKeyword(node);
    }

    public void caseTThrow(TThrow node)
    {
        handleKeyword(node);
    }

    public void caseTThrows(TThrows node)
    {
        handleKeyword(node);
    }

    public void caseTTransient(TTransient node)
    {
        handleKeyword(node);
    }

    public void caseTTry(TTry node)
    {
        handleKeyword(node);
    }

    public void caseTVoid(TVoid node)
    {
        handleKeyword(node);
    }

    public void caseTVolatile(TVolatile node)
    {
        handleKeyword(node);
    }

    public void caseTWhile(TWhile node)
    {
        handleKeyword(node);
    }

    public void caseTTrue(TTrue node)
    {
        handleLiteral(node);
    }

    public void caseTFalse(TFalse node)
    {
        handleLiteral(node);
    }

    public void caseTNull(TNull node)
    {
        handleLiteral(node);
    }
} // JavaScanner
