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

import com.google.common.collect.helpers.MoreAsserts;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.RandomAccess;

/**
 * Unit tests for {@link ArrayListMultimap}.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public class ArrayListMultimapTest extends AbstractListMultimapTest {

  @Override protected ListMultimap<String, Integer> create() {
    return Multimaps.newArrayListMultimap();
  }

  @SuppressWarnings("unchecked")
  @Override protected Multimap<String, Integer>
      makeClone(Multimap<String, Integer> multimap) {
    return ((ArrayListMultimap<String, Integer>) multimap).clone();
  }

  /**
   * Confirm that get() returns a List implementing RandomAccess.
   */
  public void testGetRandomAccess() {
    Multimap<String, Integer> multimap = create();
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    assertTrue(multimap.get("foo") instanceof RandomAccess);
    assertTrue(multimap.get("bar") instanceof RandomAccess);
  }

  /**
   * Test throwing ConcurrentModificationException when a sublist's ancestor's
   * delegate changes.
   */
  public void testSublistConcurrentModificationException() {
    ListMultimap<String, Integer> multimap = create();
    multimap.putAll("foo", Arrays.asList(1, 2, 3, 4, 5));
    List<Integer> list = multimap.get("foo");
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 4, 5);
    List<Integer> sublist = list.subList(0, 5);
    MoreAsserts.assertContentsInOrder(sublist, 1, 2, 3, 4, 5);

    sublist.retainAll(Collections.EMPTY_LIST);
    assertTrue(sublist.isEmpty());
    multimap.put("foo", 6);

    try {
      sublist.isEmpty();
      fail("Expected ConcurrentModificationException");
    } catch (ConcurrentModificationException expected) {}
  }

  public void testMultimapConstructor() {
    Multimap<String, Integer> multimap = createSample();
    ArrayListMultimap<String, Integer> copy =
        Multimaps.newArrayListMultimap(multimap);
    assertEquals(multimap, copy);
  }
}
