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

import static com.google.common.collect.helpers.MoreAsserts.assertContentsAnyOrder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

/**
 * Tests for {@link Multisets}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class MultisetsTest extends TestCase {

  public void testImmutableEntry() {
    /* See AbstractMultisetEntryTest. */
  }

  public void testForSet() {
    Set<String> set = new HashSet<String>();
    set.add("foo");
    set.add("bar");
    Multiset<String> multiset = new HashMultiset<String>();
    multiset.addAll(set);
    Multiset<String> multisetView = Multisets.forSet(set);
    assertTrue(multiset.equals(multisetView));
    assertTrue(multisetView.equals(multiset));
    assertEquals(multiset.toString(), multisetView.toString());
    assertEquals(multiset.hashCode(), multisetView.hashCode());
    assertEquals(multiset.size(), multisetView.size());
    assertTrue(multisetView.contains("foo"));
    assertEquals(set, multisetView.elementSet());
    assertEquals(multisetView.elementSet(), set);
    assertEquals(multiset.elementSet(), multisetView.elementSet());
    assertEquals(multisetView.elementSet(), multiset.elementSet());
    try {
      multisetView.add("baz");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      multisetView.addAll(Collections.singleton("baz"));
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      multisetView.elementSet().add("baz");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      multisetView.elementSet().addAll(Collections.singleton("baz"));
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    multisetView.remove("bar");
    assertFalse(multisetView.contains("bar"));
    assertFalse(set.contains("bar"));
    assertEquals(set, multisetView.elementSet());
    assertContentsAnyOrder(multisetView.elementSet(), "foo");
    assertContentsAnyOrder(multisetView.entrySet(),
        Multisets.immutableEntry("foo", 1));
    multisetView.clear();
    assertFalse(multisetView.contains("foo"));
    assertFalse(set.contains("foo"));
    assertTrue(set.isEmpty());
    assertTrue(multisetView.isEmpty());
    multiset.clear();
    assertEquals(multiset.toString(), multisetView.toString());
    assertEquals(multiset.hashCode(), multisetView.hashCode());
    assertEquals(multiset.size(), multisetView.size());
  }

  public void testSingletonMultiset() {
    Multiset<String> set = Multisets.singletonMultiset("foo");
    Multiset<String> control = new HashMultiset<String>();
    control.add("foo");
    assertEquals(control, set);
    assertEquals(control.toString(), set.toString());
    assertEquals(control.hashCode(), set.hashCode());
    assertEquals(1, set.size());
    assertFalse(set.isEmpty());
    assertEquals(Collections.singleton("foo"), set.elementSet());
    assertTrue(set.contains("foo"));
    assertFalse(set.contains("bar"));
    try {
      set.add("bar");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      set.addAll(set);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      set.remove("foo");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      set.clear();
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
  }

}
