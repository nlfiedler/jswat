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
 *      nf      11/17/03        Moved to new package
 *
 * $Id: JavaScanner.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang.java;

import com.bluemarsh.jswat.parser.java.analysis.AnalysisAdapter;
import com.bluemarsh.jswat.parser.java.lexer.Lexer;
import com.bluemarsh.jswat.parser.java.lexer.LexerException;
import com.bluemarsh.jswat.parser.java.node.*;
import com.bluemarsh.jswat.view.SourceContent;
import com.bluemarsh.jswat.util.SkipList;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import javax.swing.text.BadLocationException;

/**
 * Class JavaScanner is responsible for lexically scanning a Java source
 * file and generating a list of JavaTokenInfo objects.
 *
 * @author  Nathan Fiedler
 */
public class JavaScanner extends AnalysisAdapter {
    /** Source file reader. */
    private Reader reader;
    /** Source content. */
    private SourceContent content;
    /** List of JavaTokenInfo objects. */
    private SkipList tokenList;

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
     * @throws  IOException
     *          if an I/O error occurred.
     * @throws  LexerException
     *          if a lexer error occurred.
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

    /**
     *
     * @param  node  decimal literal.
     */
    public void caseTDecimalIntegerLiteral(TDecimalIntegerLiteral node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_NUMBER));
    }

    /**
     *
     * @param  node  hexadecimal literal.
     */
    public void caseTHexIntegerLiteral(THexIntegerLiteral node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_NUMBER));
    }

    /**
     *
     * @param  node  octal literal.
     */
    public void caseTOctalIntegerLiteral(TOctalIntegerLiteral node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_NUMBER));
    }

    /**
     *
     * @param  node  floating literal.
     */
    public void caseTFloatingPointLiteral(TFloatingPointLiteral node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_NUMBER));
    }

    /**
     *
     * @param  node  character literal.
     */
    public void caseTCharacterLiteral(TCharacterLiteral node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_CHARACTER));
    }

    /**
     *
     * @param  node  string literal.
     */
    public void caseTStringLiteral(TStringLiteral node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_STRING));
    }

    /**
     *
     * @param  node  comment.
     */
    public void caseTTraditionalComment(TTraditionalComment node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_COMMENT));
    }

    /**
     *
     * @param  node  comment.
     */
    public void caseTDocumentationComment(TDocumentationComment node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_COMMENT));
    }

    /**
     *
     * @param  node  comment.
     */
    public void caseTEndOfLineComment(TEndOfLineComment node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_COMMENT));
    }

    /**
     *
     * @param  node  identifier.
     */
    public void caseTIdentifier(TIdentifier node) {
        int offset = getOffset(node);
        tokenList.insert(offset, new JavaTokenInfo(
            offset, node.getText().length(),
            JavaTokenInfo.TOKEN_IDENTIFIER));
    }

    /**
     *
     * @param  node  assert keyword.
     */
    public void caseTAssert(TAssert node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  abstract keyword.
     */
    public void caseTAbstract(TAbstract node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  boolean keyword.
     */
    public void caseTBoolean(TBoolean node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  break keyword.
     */
    public void caseTBreak(TBreak node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  byte keyword.
     */
    public void caseTByte(TByte node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  case keyword.
     */
    public void caseTCase(TCase node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  catch keyword.
     */
    public void caseTCatch(TCatch node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  char keyword.
     */
    public void caseTChar(TChar node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  class keyword.
     */
    public void caseTClass(TClass node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  const keyword.
     */
    public void caseTConst(TConst node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  continue keyword.
     */
    public void caseTContinue(TContinue node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  default keyword.
     */
    public void caseTDefault(TDefault node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  do keyword.
     */
    public void caseTDo(TDo node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  double keyword.
     */
    public void caseTDouble(TDouble node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  else keyword.
     */
    public void caseTElse(TElse node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  extends keyword.
     */
    public void caseTExtends(TExtends node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  final keyword.
     */
    public void caseTFinal(TFinal node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  finally keyword.
     */
    public void caseTFinally(TFinally node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  float keyword.
     */
    public void caseTFloat(TFloat node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  for keyword.
     */
    public void caseTFor(TFor node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  goto keyword.
     */
    public void caseTGoto(TGoto node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  if keyword.
     */
    public void caseTIf(TIf node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  implements keyword.
     */
    public void caseTImplements(TImplements node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  import keyword.
     */
    public void caseTImport(TImport node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  instanceof keyword.
     */
    public void caseTInstanceof(TInstanceof node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  int keyword.
     */
    public void caseTInt(TInt node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  interface keyword.
     */
    public void caseTInterface(TInterface node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  long keyword.
     */
    public void caseTLong(TLong node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  native keyword.
     */
    public void caseTNative(TNative node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  new keyword.
     */
    public void caseTNew(TNew node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  package keyword.
     */
    public void caseTPackage(TPackage node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  private keyword.
     */
    public void caseTPrivate(TPrivate node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  protected keyword.
     */
    public void caseTProtected(TProtected node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  public keyword.
     */
    public void caseTPublic(TPublic node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  return keyword.
     */
    public void caseTReturn(TReturn node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  short keyword.
     */
    public void caseTShort(TShort node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  static keyword.
     */
    public void caseTStatic(TStatic node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  strictfp keyword.
     */
    public void caseTStrictfp(TStrictfp node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  super keyword.
     */
    public void caseTSuper(TSuper node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  switch keyword.
     */
    public void caseTSwitch(TSwitch node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  synchronized keyword.
     */
    public void caseTSynchronized(TSynchronized node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  this keyword.
     */
    public void caseTThis(TThis node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  throw keyword.
     */
    public void caseTThrow(TThrow node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  throws keyword.
     */
    public void caseTThrows(TThrows node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  transient keyword.
     */
    public void caseTTransient(TTransient node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  try keyword.
     */
    public void caseTTry(TTry node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  void keyword.
     */
    public void caseTVoid(TVoid node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  volatile keyword.
     */
    public void caseTVolatile(TVolatile node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  while keyword.
     */
    public void caseTWhile(TWhile node) {
        handleKeyword(node);
    }

    /**
     *
     * @param  node  true literal.
     */
    public void caseTTrue(TTrue node) {
        handleLiteral(node);
    }

    /**
     *
     * @param  node  false literal.
     */
    public void caseTFalse(TFalse node) {
        handleLiteral(node);
    }

    /**
     *
     * @param  node  null literal.
     */
    public void caseTNull(TNull node) {
        handleLiteral(node);
    }
} // JavaScanner
