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

import com.google.common.collect.helpers.SerializableTester;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Common tests for a {@link Multiset}.
 *
 * @author kevinb
 */
public abstract class AbstractMultisetTest extends AbstractCollectionTest {

  @Override protected abstract <E> Multiset<E> create();

  protected Multiset<String> ms;

  @Override protected void setUp() throws Exception {
    super.setUp();
    c = ms = create();
  }

  public void testCountZero() {
    assertEquals(0, ms.count("a"));
  }

  public void testCountOne() {
    ms.add("a");
    assertEquals(1, ms.count("a"));
  }

  public void testCountTwo() {
    ms.add("a");
    ms.add("a");
    assertEquals(2, ms.count("a"));
  }

  public void testCountAfterRemoval() {
    ms.add("a");
    ms.remove("a");
    assertEquals(0, ms.count("a"));
  }

  public void testAddNoneToNone() {
    assertFalse(ms.add("a", 0));
    assertContents();
  }

  public void testAddNoneToSome() {
    ms.add("a");
    assertFalse(ms.add("a", 0));
    assertContents("a");
  }

  public void testAddSeveralAtOnce() {
    assertTrue(ms.add("a", 3));
    assertContents("a", "a", "a");
  }

  @Override public void testAddSeveralTimes() {
    assertTrue(ms.add("a"));
    assertTrue(ms.add("b"));
    assertTrue(ms.add("a"));
    assertTrue(ms.add("b"));
    assertContents("a", "b", "a", "b");
  }

  public void testAddNegative() {
    try {
      ms.add("a", -1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Override public void testEqualsNo() {
    ms.add("a");
    ms.add("b");
    ms.add("b");

    Multiset<String> ms2 = create();
    ms2.add("a", 2);
    ms2.add("b");

    assertFalse(ms.equals(ms2));
  }

  public void testAddTooMany() {
    ms.add("a", Integer.MAX_VALUE); // so far so good
    ms.add("b", Integer.MAX_VALUE); // so far so good
    try {
      ms.add("a");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testAddAllEmptySet() throws Exception {
    c= ms = createSample();
    assertFalse(ms.addAll(Collections.<String>emptySet()));
    assertEquals(createSample(), ms);
  }

  public void testAddAllEmptyMultiset() throws Exception {
    c = ms = createSample();
    Multiset<String> empty = create();
    assertFalse(ms.addAll(empty));
    assertEquals(createSample(), ms);
  }

  public void testAddAllSet() throws Exception {
    c = ms = createSample();
    Set<String> more = Sets.immutableSet("c", "d", "e");
    assertTrue(ms.addAll(more));
    assertContents("a", "b", "b", "c", "c", "d", "d", "d", "d", "e");
  }

  public void testAddAllMultiset() throws Exception {
    c = ms = createSample();
    Multiset<String> more = Multisets.newHashMultiset("c", "c", "d", "d", "e");
    assertTrue(ms.addAll(more));
    assertContents("a", "b", "b", "c", "c", "c", "d", "d", "d", "d", "d", "e");
  }

  public void testRemoveAllOccurrencesNonexistent() {
    ms.add("a");
    assertEquals(0, ms.removeAllOccurrences("b"));
    assertContents("a");
  }

  public void testRemoveAllOccurrencesOne() {
    ms.add("a");
    ms.add("b");
    assertEquals(1, ms.removeAllOccurrences("a"));
    assertContents("b");
  }

  public void testRemoveAllOccurrencesSeveral() {
    ms.add("a", 3);
    ms.add("b");
    assertEquals(3, ms.removeAllOccurrences("a"));
    assertContents("b");
  }

  public void testRemoveNoneFromNone() {
    assertEquals(0, ms.remove("a", 0));
    assertContents();
  }

  public void testRemoveNoneFromSome() {
    ms.add("a");
    assertEquals(0, ms.remove("a", 0));
    assertContents("a");
  }

  public void testRemoveOneFromNone() {
    assertEquals(0, ms.remove("a", 1));
    assertContents();
  }

  public void testRemoveOneFromOne() {
    ms.add("a");
    assertEquals(1, ms.remove("a", 1));
    assertContents();
  }

  public void testRemoveSomeFromSome() {
    ms.add("a", 5);
    assertEquals(3, ms.remove("a", 3));
    assertContents("a", "a");
  }

  public void testRemoveTooMany() {
    ms.add("a", 3);
    assertEquals(3, ms.remove("a", 5));
    assertContents();
  }

  public void testRemoveNegative() {
    try {
      ms.remove("a", -1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testContainsSeveral() {
    ms.add("a", 3);
    assertTrue(ms.contains(new String("a")));
  }

  public void testContainsAllNo() {
    ms.add("a", 2);
    ms.add("b", 3);
    assertFalse(ms.containsAll(asList("a", "c")));
  }

  public void testContainsAllYes() {
    ms.add("a", 2);
    ms.add("b", 3);
    ms.add("c", 4);
    assertTrue(ms.containsAll(asList("a", "c")));
  }

  public void testRemoveAllOfOne() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.removeAll(asList("a", "c")));
    assertContents("b");
  }

  public void testRemoveAllOfDisjoint() {
    ms.add("a", 2);
    ms.add("b");
    assertFalse(ms.removeAll(asList("c", "d")));
    assertContents("a", "a", "b");
  }

  public void testRemoveAllOfEverything() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.removeAll(asList("a", "b")));
    assertContents();
  }

  public void testRetainAllOfOne() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.retainAll(asList("a", "c")));
    assertContents("a", "a");
  }

  public void testRetainAllOfDisjoint() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.retainAll(asList("c", "d")));
    assertContents();
  }

  public void testRetainAllOfEverything() {
    ms.add("a", 2);
    ms.add("b");
    assertFalse(ms.retainAll(asList("a", "b")));
    assertContents("a", "a", "b");
  }

  public void testContainsAllVacuousViaElementSet() {
    assertTrue(ms.elementSet().containsAll(Collections.emptySet()));
  }

  public void testContainsAllNoViaElementSet() {
    ms.add("a", 2);
    ms.add("b", 3);
    assertFalse(ms.elementSet().containsAll(asList("a", "c")));
  }

  public void testContainsAllYesViaElementSet() {
    ms.add("a", 2);
    ms.add("b", 3);
    ms.add("c", 4);
    assertTrue(ms.elementSet().containsAll(asList("a", "c")));
  }

  public void testRemoveAllVacuousViaElementSet() {
    assertFalse(ms.elementSet().removeAll(Collections.emptySet()));
  }

  public void testRemoveAllOfOneViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.elementSet().removeAll(asList("a", "c")));
    assertContents("b");
  }

  public void testRemoveAllOfDisjointViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertFalse(ms.elementSet().removeAll(asList("c", "d")));
    assertContents("a", "a", "b");
  }

  public void testRemoveAllOfEverythingViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.elementSet().removeAll(asList("a", "b")));
    assertContents();
  }

  public void testRetainAllVacuousViaElementSet() {
    assertFalse(ms.elementSet().retainAll(asList("a")));
    assertContents();
  }

  public void testRetainAllOfNothingViaElementSet() {
    ms.add("a");
    assertTrue(ms.elementSet().retainAll(Collections.emptySet()));
    assertContents();
  }

  public void testRetainAllOfOneViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.elementSet().retainAll(asList("a", "c")));
    assertContents("a", "a");
  }

  public void testRetainAllOfDisjointViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.elementSet().retainAll(asList("c", "d")));
    assertContents();
  }

  public void testRetainAllOfEverythingViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertFalse(ms.elementSet().retainAll(asList("a", "b")));
    assertContents("a", "a", "b");
  }

  public void testElementSetBasic() {
    ms.add("a", 3);
    ms.add("b", 2);
    ms.add("c", 1);
    HashSet<String> expected = Sets.newHashSet("a", "b", "c");
    Set<String> actual = ms.elementSet();
    assertEquals(expected, actual);
    assertEquals(actual, expected);
  }

  public void testElementSetIsNotACopy() {
    ms.add("a", 1);
    ms.add("b", 2);
    Set<String> elementSet = ms.elementSet();
    ms.add("c", 3);
    ms.removeAllOccurrences("b");
    assertEquals(Sets.newHashSet("a", "c"), elementSet);
  }

  public void testRemoveFromElementSetYes() {
    ms.add("a", 1);
    ms.add("b", 2);
    Set<String> elementSet = ms.elementSet();
    assertTrue(elementSet.remove("b"));
    assertContents("a");
  }

  public void testRemoveFromElementSetNo() {
    ms.add("a", 1);
    Set<String> elementSet = ms.elementSet();
    assertFalse(elementSet.remove("b"));
    assertContents("a");
  }

  public void testCantAddToElementSet() {
    try {
      ms.elementSet().add("a");
      fail();
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testClearViaElementSet() {
    ms = createSample();
    ms.elementSet().clear();
    assertContents();
  }

  public void testClearViaEntrySet() {
    ms = createSample();
    ms.entrySet().clear();
    assertContents();
  }

  public void testEntrySet() throws Exception {
    ms = createSample();
    for (Multiset.Entry<String> entry : ms.entrySet()) {
      String element = entry.getElement();
      if (element.equals("a")) {
        assertEquals(1, entry.getCount());
      } else if (element.equals("b")) {
        assertEquals(2, entry.getCount());
      } else if (element.equals("c")) {
        assertEquals(1, entry.getCount());
      } else if (element.equals("d")) {
        assertEquals(3, entry.getCount());
      }
    }
  }

  public void testEntrySetEmpty() throws Exception {
    assertEquals(Collections.emptySet(), ms.entrySet());
  }

  public void testReallyBig() {
    ms.add("a", Integer.MAX_VALUE - 1);
    assertEquals(Integer.MAX_VALUE - 1, ms.size());
    ms.add("b", 3);

    // See Collection.size() contract
    assertEquals(Integer.MAX_VALUE, ms.size());

    // Make sure we didn't forget our size
    ms.remove("a", 4);
    assertEquals(Integer.MAX_VALUE - 2, ms.size());
  }

  public void testToStringNull() throws Exception {
    ms.add("a", 3);
    ms.add("c", 1);
    ms.add("b", 2);
    ms.add(null, 4);

    // This test is brittle. The original test was meant to validate the
    // contents of the string itself, but key ordering tended to change
    // under unpredictable circumstances. Instead, we're just ensuring
    // that the string not return null, and implicitly, not throw an exception.
    assertNotNull(ms.toString());
  }

  public void testSerializable() {
    ms = createSample();
    assertEquals(ms, SerializableTester.reserialize(ms));
  }

  @Override protected Multiset<String> createSample() {
    @SuppressWarnings("hiding")
    Multiset<String> ms = create();
    ms.add("a", 1);
    ms.add("b", 2);
    ms.add("c", 1);
    ms.add("d", 3);
    return ms;
  }
}
