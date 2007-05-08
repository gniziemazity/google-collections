/*
 * Copyright (C) 2007 Google Inc.
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

package com.google.common.collect;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 * @author crazybob@google.com (Bob Lee)
 */
public class PrefixTrieTest extends TestCase {
  public void testPutGet() {
    PrefixTrie<Object> trie = new PrefixTrie<Object>();
    Object foo = new Object();
    trie.put("foo:", foo);
    Object a = new Object();
    trie.put("a:", a);
    assertSame(foo, trie.get("foo:bar"));
    assertSame(a, trie.get("a:bar"));
    assertNull(trie.get("tee:bar"));
    assertNull(trie.get("foobar"));
  }

  public void testClosePrefixes() {
    PrefixTrie<Object> trie = new PrefixTrie<Object>();
    trie.put("fooa", new Object());
    trie.put("foob", new Object());
  }

  public void testLongestPrefixWins() {
    PrefixTrie<String> trie = new PrefixTrie<String>();
    trie.put("foobar", "1");
    trie.put("foo", "2");
    assertEquals("2", trie.get("foob"));

    PrefixTrie<String> trie2 = new PrefixTrie<String>();
    trie2.put("foo", "1");
    trie2.put("foobar", "2");
    assertEquals("1", trie2.get("foob"));
  }

  public void testEmptyPrefix() {
    PrefixTrie<String> trie = new PrefixTrie<String>();
    trie.put("", "empty");
    trie.put("abc", "nonempty");
    assertEquals("empty", trie.get("cd"));
  }

  public void testAsMap() {
    PrefixTrie<String> trie = new PrefixTrie<String>('0', '9');
    Map<String, String> golden = new HashMap<String, String>();

    assertEquals(golden, trie.toMap());

    trie.put("1", "one");
    trie.put("12", "one-two");
    trie.put("14", "one-four");
    golden.put("1", "one");
    golden.put("12", "one-two");
    golden.put("14", "one-four");
    assertEquals(golden, trie.toMap());
  }
}
