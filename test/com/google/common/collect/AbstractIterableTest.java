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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests for {@code AbstractIterable}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class AbstractIterableTest extends TestCase {

  public void testToString() {
    assertEquals("[]", create().toString());
    assertEquals("[a]", create("a").toString());
    assertEquals("[a, b, c]", create("a", "b", "c").toString());
    assertEquals("[c, a, a]", create("c", "a", "a").toString());
  }

  public void testToStringNull() {
    assertEquals("[null]", create((String) null).toString());
    assertEquals("[null, null]", create(null, null).toString());
    assertEquals("[, null, a]", create("", null, "a").toString());
  }

  public void testToStringSelfReferential() {
    Iterable<Object> i = new AbstractIterable<Object>() {
        List<Object> list = Arrays.asList("a", "b", this, null, "c");
        public Iterator<Object> iterator() {
          return list.iterator();
        }
      };
    assertEquals("[a, b, (this Iterable), null, c]", i.toString());
  }

  /** Returns a new AbstractIterable over the specified strings. */
  private static Iterable<String> create(String... strings) {
    final List<String> list = Arrays.asList(strings);
    return new AbstractIterable<String>() {
        public Iterator<String> iterator() {
          return list.iterator();
        }
      };
  }
}
