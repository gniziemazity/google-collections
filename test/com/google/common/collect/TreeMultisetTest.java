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

import static com.google.common.collect.helpers.MoreAsserts.assertContentsInOrder;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Unit test for {@code com.google.common.collect.TreeMultiset}.
 *
 * @author nkanodia (Neal Kanodia)
 */
public class TreeMultisetTest extends AbstractMultisetTest {

  @Override protected <E> Multiset<E> create() {
    return new TreeMultiset<E>();
  }

  public void testToString() throws Exception {
    ms.add("a", 3);
    ms.add("c", 1);
    ms.add("b", 2);

    assertEquals("[a x 3, b x 2, c]", ms.toString());
  }

  public void testIteratorBashing() throws Exception {
    ms = createSample();
    IteratorTester tester = new IteratorTester(9) {
      @Override protected Iterator<?> newReferenceIterator() {
        return Lists.newArrayList(ms).iterator();
      }
      @Override protected Iterator<?> newTargetIterator() {
        return createSample().iterator();
      }
    };
    tester.test();
  }

  public void testElementSetIteratorBashing() throws Exception {
    IteratorTester tester = new IteratorTester(7) {
      @Override protected Iterator<?> newReferenceIterator() {
        return Lists.newArrayList("a", "b", "c").iterator();
      }
      @Override protected Iterator<?> newTargetIterator() {
        Multiset<String> multiset = create();
        multiset.add("a", 3);
        multiset.add("c", 1);
        multiset.add("b", 2);
        return multiset.elementSet().iterator();
      }
    };
    tester.test();
  }

  public void testCustomComparator() throws Exception {
    Multiset<String> ms = new TreeMultiset<String>(new Comparator<String>() {
      public int compare(String o1, String o2) {
        return o2.compareTo(o1);
      }
    });

    ms.add("b");
    ms.add("c");
    ms.add("a");
    ms.add("b");
    ms.add("d");

    List<String> expected = Lists.newArrayList("d", "c", "b", "b", "a");
    Iterator<String> iterator = expected.iterator();
    for (String s : ms) {
      assertEquals(iterator.next(), s);
    }
  }

  @Override public void testToStringNull() throws Exception {
    try {
      super.testToStringNull();
      fail("exception expected");
    } catch(NullPointerException expected) {
    }
  }

  @SuppressWarnings("unchecked")
  public void testClone() {
    ms.add("a");
    ms.add("b", 2);
    ms.add("c");
    Multiset<String> clone = ((TreeMultiset<String>) ms).clone();
    assertContentsInOrder(ms, "a", "b", "b", "c");
    assertContentsInOrder(clone, "a", "b", "b", "c");
    assertTrue(ms.equals(clone));
    assertTrue(clone.equals(ms));
    ms.add("foo");
    assertContentsInOrder(ms, "a", "b", "b", "c", "foo");
    assertContentsInOrder(clone, "a", "b", "b", "c");
    assertFalse(ms.equals(clone));
    assertFalse(clone.equals(ms));
  }
}
