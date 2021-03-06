/*
 * Copyright 2015-2017 Alexandr Evstigneev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package editor.quoteLike;

import editor.PerlTypingTestCase;

public class PerlQuoteWithoutPrefixTest extends PerlTypingTestCase {
  public void testHeredocReplace() {doTest("<<caret>>", "<", "<<<caret>");}

  public void testHeredocNotReplace() {doTest("qq<<caret>>", "<", "qq<<<caret>>");}

  public void testHeredocReplaceWithOffset() {doTest("say <<caret>>", "<", "say <<<caret>");}

  public void testHeredocNotReplaceWithOffset() {doTest("say qq<<caret>>", "<", "say qq<<<caret>>");}

  public void testAfterRe() {doTest("$a =~ <caret>", "/", "$a =~ /<caret>/");}

  public void testAfterNotRe() {doTest("$a !~ <caret>", "/", "$a !~ /<caret>/");}

  public void testRegexInside() {doTest("$a =~ /<caret>/", "/", "$a =~ //<caret>");}

  public void testSequentialAdd() {doTest("say <caret>; say 'some';", "'", "say '<caret>'; say 'some';");}

  public void testSequentialRemove() {doTestBS("say '<caret>'; say 'some';", "say <caret>; say 'some';");}

  public void testSingleQuote() {
    doTest("say <caret>;", "'", "say '<caret>';");
  }

  public void testSingleQuoteEof() {
    doTest("say <caret>", "'", "say '<caret>'");
  }

  public void testSingleQuoteInside() {
    doTest("say '<caret>'", "'", "say ''<caret>");
  }

  public void testSingleQuoteAfter() {
    doTest("say ''<caret>", "'", "say '''<caret>'");
  }

  public void testSingleRemoveOpen() {
    doTestBS("say '<caret>'", "say <caret>");
  }

  public void testSingleRemoveClose() {
    doTestBS("say ''<caret>", "say '<caret>");
  }

  public void testDoubleQuote() {
    doTest("say <caret>;", "\"", "say \"<caret>\";");
  }

  public void testDoubleQuoteEof() {
    doTest("say <caret>", "\"", "say \"<caret>\"");
  }

  public void testDoubleQuoteInside() {
    doTest("say \"<caret>\"", "\"", "say \"\"<caret>");
  }

  public void testDoubleQuoteAfter() {
    doTest("say \"\"<caret>", "\"", "say \"\"\"<caret>\"");
  }

  public void testDoubleRemoveOpen() {
    doTestBS("say \"<caret>\"", "say <caret>");
  }

  public void testDoubleRemoveClose() {
    doTestBS("say \"\"<caret>", "say \"<caret>");
  }

  public void testTickQuote() {
    doTest("say <caret>;", "`", "say `<caret>`;");
  }

  public void testTickQuoteEof() {
    doTest("say <caret>", "`", "say `<caret>`");
  }

  public void testTickQuoteInside() {
    doTest("say `<caret>`", "`", "say ``<caret>");
  }

  public void testTickQuoteAfter() {
    doTest("say ``<caret>", "`", "say ```<caret>`");
  }

  public void testTickRemoveOpen() {
    doTestBS("say `<caret>`", "say <caret>");
  }

  public void testTickRemoveClose() {
    doTestBS("say ``<caret>", "say `<caret>");
  }

  public void testOpenBegin() {doTest("<caret>", "'", "'<caret>'");}

  public void testRemoveOpenBegin() {doTestBS("'<caret>'", "<caret>");}
}
