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
import com.google.common.collect.helpers.NullPointerTester;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import junit.framework.TestCase;

/**
 * Unit test for {@link Sets}.
 *
 * @author kevinb
 */
public class SetsTest extends TestCase {

  private static final Collection<Integer> EMPTY_COLLECTION =
      Arrays.asList();

  private static final Collection<Integer> SOME_COLLECTION =
      Arrays.asList(0, 1, 1);

  private static final Iterable<Integer> SOME_ITERABLE =
      new Iterable<Integer>() {
        public Iterator<Integer> iterator() {
          return SOME_COLLECTION.iterator();
        }
      };

  private static final List<Integer> LONGER_LIST =
      Arrays.asList(8, 6, 7, 5, 3, 0, 9);

  private static final Comparator<Integer> SOME_COMPARATOR =
      Collections.reverseOrder();

  public void testImmutableSetEmpty() throws Exception {
    Set<Integer> set = Sets.immutableSet();
    verifySetContents(set, EMPTY_COLLECTION);
    try {
      set.add(1);
      fail();
    } catch (UnsupportedOperationException expected) {}
  }

  public void testImmutableSetOne() throws Exception {
    Set<Integer> set = Sets.immutableSet(1);
    verifySetContents(set, Collections.singleton(1));
    try {
      set.add(2);
      fail();
    } catch (UnsupportedOperationException expected) {}
    try {
      set.remove(1);
      fail();
    } catch (UnsupportedOperationException expected) {}
  }

  public void testImmutableSet() throws Exception {
    Set<Integer> set = Sets.immutableSet(1, 2, 3);
    verifySetContents(set, Arrays.asList(1, 2, 3));
  }

  public void testImmutableSetConstructorFromMutableSet() throws Exception {
    Set<Integer> mutable = Sets.newHashSet(1, 2, 3);
    Set<Integer> immutable = Sets.immutableSet(mutable);
    MoreAsserts.checkEqualsAndHashCodeMethods(mutable, immutable, true);

    // Check that immutable is a copy of mutable, not an alias.
    mutable.remove(2);
    MoreAsserts.checkEqualsAndHashCodeMethods(mutable, immutable, false);

    mutable.add(2);
    MoreAsserts.checkEqualsAndHashCodeMethods(mutable, immutable, true);
  }

  public void testCanConstructImmutableSetFromCollection() throws Exception {
    Collection<Integer> source = Lists.immutableList(1, 2, 3, 1);
    Set<Integer> set = Sets.immutableSet(source);
    verifySetContents(set, source);
  }

  public void testImmutableSortedSetEmpty() throws Exception {
    SortedSet<Integer> set = Sets.immutableSortedSet();
    verifySetContents(set, EMPTY_COLLECTION);
    try {
      set.add(1);
      fail();
    } catch (UnsupportedOperationException expected) {}
  }

  public void testImmutableSortedSetEmptyWithComparator() throws Exception {
    SortedSet<Integer> set = Sets.immutableSortedSet(SOME_COMPARATOR);
    verifySortedSetContents(set, EMPTY_COLLECTION, SOME_COMPARATOR);

    try {
      set.add(1);
      fail("Should not be able to add to an empty immutableSortedSet.");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testImmutableSortedSetOne() throws Exception {
    SortedSet<Integer> set = Sets.immutableSortedSet(1);

    verifySetContents(set, Arrays.asList(1));

    try {
      set.add(2);
      fail("Should not be able to add to an immutableSortedSet.");
    } catch (UnsupportedOperationException expected) {}
    try {
      set.remove(1);
      fail("Should not be able to remove from an immutableSortedSet.");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testImmutableSortedSetOneWithComparator() throws Exception {
    SortedSet<Integer> set = Sets.immutableSortedSet(SOME_COMPARATOR, 1);

    verifySortedSetContents(set, Arrays.asList(1), SOME_COMPARATOR);

    try {
      set.add(2);
      fail("Should not be able to add to an immutableSortedSet.");
    } catch (UnsupportedOperationException expected) {}
    try {
      set.remove(1);
      fail("Should not be able to remove from an immutableSortedSet.");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testImmutableSortedSet() throws Exception {
    SortedSet<Integer> set = Sets.immutableSortedSet(3, 1, 2);

    verifySetContents(set, Arrays.asList(1, 2, 3));
  }

  public void testImmutableSortedSetWithComparator()
      throws Exception {
    SortedSet<Integer> set = Sets.immutableSortedSet(SOME_COMPARATOR, 3, 1, 2);

    verifySortedSetContents(set, Arrays.asList(1, 2, 3), SOME_COMPARATOR);
  }

  private enum SomeEnum { A, B, C, D }

  public void testImmutableEnumSet() throws Exception {
    Set<SomeEnum> units = Sets.immutableEnumSet(SomeEnum.B, SomeEnum.D);

    verifySetContents(units, Arrays.asList(SomeEnum.B, SomeEnum.D));

    try {
      units.remove(SomeEnum.B);
      fail("ImmutableEnumSet should throw an exception on remove()");
    } catch (UnsupportedOperationException expected) {}
    try {
      units.add(SomeEnum.C);
      fail("ImmutableEnumSet should throw an exception on add()");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testNewHashSetEmpty() {
    HashSet<Integer> set = Sets.newHashSet();
    verifySetContents(set, EMPTY_COLLECTION);
  }

  public void testNewHashSetVarArgs() {
    HashSet<Integer> set = Sets.newHashSet(0, 1, 1);
    verifySetContents(set, Arrays.asList(0, 1));
  }

  public void testNewHashSetFromCollection() {
    HashSet<Integer> set = Sets.newHashSet(SOME_COLLECTION);
    verifySetContents(set, SOME_COLLECTION);
  }

  public void testNewHashSetFromIterable() {
    HashSet<Integer> set = Sets.newHashSet(SOME_ITERABLE);
    verifySetContents(set, SOME_ITERABLE);
  }

  public void testNewHashSetFromIterator() {
    HashSet<Integer> set = Sets.newHashSet(SOME_COLLECTION.iterator());
    verifySetContents(set, SOME_COLLECTION);
  }

  public void testNewConcurrentHashSetEmpty() {
    Set<Integer> set = Sets.newConcurrentHashSet();
    verifySetContents(set, EMPTY_COLLECTION);
  }

  public void testNewConcurrentHashSetVarArgs() {
    Set<Integer> set = Sets.newConcurrentHashSet(0, 1, 1);
    verifySetContents(set, Arrays.asList(0, 1));
  }

  public void testNewConcurrentHashSetFromCollection() {
    Set<Integer> set = Sets.newConcurrentHashSet(SOME_COLLECTION);
    verifySetContents(set, SOME_COLLECTION);
  }

  public void testNewConcurrentHashSetFromIterable() {
    Set<Integer> set = Sets.newHashSet(SOME_ITERABLE);
    verifySetContents(set, SOME_ITERABLE);
  }

  public void testNewConcurrentHashSetFromIterator() {
    Set<Integer> set = Sets.newHashSet(SOME_COLLECTION.iterator());
    verifySetContents(set, SOME_COLLECTION);
  }

  public void testNewLinkedHashSetEmpty() {
    LinkedHashSet<Integer> set = Sets.newLinkedHashSet();
    verifyLinkedHashSetContents(set, EMPTY_COLLECTION);
  }

  public void testNewLinkedHashSetVarArgs() {
    LinkedHashSet<Integer> set = Sets.newLinkedHashSet(8, 6, 7, 5, 3, 0, 9);
    verifyLinkedHashSetContents(set, Arrays.asList(8, 6, 7, 5, 3, 0, 9));
  }

  public void testNewLinkedHashSetFromCollection() {
    LinkedHashSet<Integer> set = Sets.newLinkedHashSet(LONGER_LIST);
    verifyLinkedHashSetContents(set, LONGER_LIST);
  }

  public void testNewLinkedHashSetFromIterable() {
    LinkedHashSet<Integer> set = Sets.newLinkedHashSet(new Iterable<Integer>()
    {
      public Iterator<Integer> iterator() {
        return LONGER_LIST.iterator();
      }
    });
    verifyLinkedHashSetContents(set, LONGER_LIST);
  }

  public void testNewLinkedHashSetFromIterator() {
    LinkedHashSet<Integer> set = Sets.newLinkedHashSet(LONGER_LIST.iterator());
    verifyLinkedHashSetContents(set, LONGER_LIST);
  }

  public void testNewTreeSetEmpty() {
    TreeSet<Integer> set = Sets.newTreeSet();
    verifySortedSetContents(set, EMPTY_COLLECTION, null);
  }

  public void testNewTreeSetVarArgs() {
    TreeSet<Integer> set = Sets.newTreeSet(0, 1, 1);
    verifySortedSetContents(set, Arrays.asList(0, 1), null);
  }

  public void testNewTreeSetFromCollection() {
    TreeSet<Integer> set = Sets.newTreeSet(SOME_COLLECTION);
    verifySortedSetContents(set, SOME_COLLECTION, null);
  }

  public void testNewTreeSetFromIterable() {
    TreeSet<Integer> set = Sets.newTreeSet(SOME_ITERABLE);
    verifySortedSetContents(set, SOME_ITERABLE, null);
  }

  public void testNewTreeSetFromIterator() {
    TreeSet<Integer> set = Sets.newTreeSet(SOME_COLLECTION.iterator());
    verifySortedSetContents(set, SOME_COLLECTION, null);
  }

  public void testNewTreeSetEmptyWithComparator() {
    TreeSet<Integer> set = Sets.newTreeSet(SOME_COMPARATOR);
    verifySortedSetContents(set, EMPTY_COLLECTION, SOME_COMPARATOR);
  }

  public void testNewTreeSetVarArgsWithComparator() {
    TreeSet<Integer> set = Sets.newTreeSet(SOME_COMPARATOR, 0, 1, 1);
    verifySortedSetContents(set, Arrays.asList(0, 1), SOME_COMPARATOR);
  }

  public void testNewTreeSetFromCollectionWithComparator() {
    TreeSet<Integer> set = Sets.newTreeSet(SOME_COMPARATOR, SOME_COLLECTION);
    verifySortedSetContents(set, SOME_COLLECTION, SOME_COMPARATOR);
  }

  public void testNewTreeSetFromIterableWithComparator() {
    TreeSet<Integer> set = Sets.newTreeSet(SOME_COMPARATOR, SOME_ITERABLE);
    verifySortedSetContents(set, SOME_ITERABLE, SOME_COMPARATOR);
  }

  public void testNewTreeSetFromIteratorWithComparator() {
    TreeSet<Integer> set = Sets.newTreeSet(SOME_COMPARATOR,
        SOME_COLLECTION.iterator());
    verifySortedSetContents(set, SOME_COLLECTION, SOME_COMPARATOR);
  }

  public void testSizeOfIntersection() {
    Set<String> s0 = Sets.newHashSet(); // empty set
    Set<String> s1 = Sets.newHashSet("a", "b", "c");
    Set<String> s2 = Sets.newHashSet("c", "b", "a");
    Set<String> s3 = Sets.newHashSet("b", "a");
    Set<String> s4 = Sets.newHashSet("f", "b", "c", "d", "e", "a");

    // numbers from 1 to 10
    Set<String> s5 = new HashSet<String>();
    for (int i = 0; i < 10; i++)
      s5.add("" + i);

    // even numbers from 1 to 100
    Set<String> s6 = new HashSet<String>();
    for (int i = 0; i < 100; i += 2)
      s6.add("" + i);

    // every 3rd number from 1 to 1000
    Set<String> s7 = new HashSet<String>();
    for (int i = 0; i < 1000; i += 3)
      s7.add("" + i);

    Set<Integer> s8 = Sets.newHashSet(5, 10, 15, 20);

    Set<Object> s9 = new HashSet<Object>();
    s9.add("a");
    s9.add(20);

    Set<Base> s10 = Sets.newHashSet(new Base("a"), new Base("b"));
    Set<Derived> s11 = Sets.newHashSet(new Derived("a"), new Derived("b"));
    Set<Base> s12 = Sets.newHashSet(new Derived("a"), new Base("b"));

    // sanity check:  verify that these classes work the way we think they do
    Base b = new Base("x");
    Base b2 = new Base("x");
    Derived d = new Derived("x");
    assert(b.equals(d));
    assertEquals(b, b2);
    assertEquals(b.hashCode(), d.hashCode());
    assertEquals(b.hashCode(), b2.hashCode());

    // all sets, for exhaustive tests
    final List<Set<?>> all = Lists.immutableList(
        s0, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12);

    // manual npe tests
    try {
      Sets.sizeOfIntersection(s1, s3, (Set<?>) null);
      fail();
    } catch (NullPointerException expected) {}

    try {
      Sets.sizeOfIntersection(s1, s3, (Set<?>[]) null);
      fail();
    } catch (NullPointerException expected) {}

    try {
      Sets.sizeOfIntersection(s1, null, s4);
      fail();
    } catch (NullPointerException expected) {}

    try {
      Sets.sizeOfIntersection((Set<?>) null, s1, (Set<?>) null);
      fail();
    } catch (NullPointerException expected) {}

    assertEquals(0, Sets.sizeOfIntersection(s0, s1));
    assertEquals(3, Sets.sizeOfIntersection(s1, s1));
    assertEquals(3, Sets.sizeOfIntersection(s1, s2));
    assertEquals(3, Sets.sizeOfIntersection(s2, s1));
    assertEquals(2, Sets.sizeOfIntersection(s1, s3));
    assertEquals(2, Sets.sizeOfIntersection(s2, s3));
    assertEquals(3, Sets.sizeOfIntersection(s1, s4));
    assertEquals(2, Sets.sizeOfIntersection(s3, s4));
    assertEquals(2, Sets.sizeOfIntersection(s1, s2, s3, s4));
    assertEquals(0, Sets.sizeOfIntersection(s1, s2, s3, s8, s4));
    assertEquals(2, Sets.sizeOfIntersection(s1, s2, s3));
    assertEquals(5, Sets.sizeOfIntersection(s5, s6));
    assertEquals(4, Sets.sizeOfIntersection(s5, s7));
    assertEquals(0, Sets.sizeOfIntersection(s5, s8));
    assertEquals(1, Sets.sizeOfIntersection(s9, s8));
    assertEquals(1, Sets.sizeOfIntersection(s9, s1));
    assertEquals(2, Sets.sizeOfIntersection(s10, s11));
    assertEquals(2, Sets.sizeOfIntersection(s10, s12));
    assertEquals(2, Sets.sizeOfIntersection(s11, s12));

    // also make sure the smallest set is correctly calculated
    assertEquals(s1, Sets.smallest(Collections.singleton(s1)));
    assertEquals(s1, Sets.smallest(s1, s1));
    assertEquals(s1, Sets.smallest(s1, s2));
    assertEquals(s2, Sets.smallest(s1, s2));
    assertEquals(s3, Sets.smallest(s1, s3));
    assertEquals(s1, Sets.smallest(s1, s4));
    assertEquals(s1, Sets.smallest(s1, s5));
    assertEquals(s5, Sets.smallest(s6, s5));
    assertEquals(s5, Sets.smallest(s7, s5));
    assertEquals(s3, Sets.smallest(s1, s2, s3, s4, s5, s6, s7));

    // Exhaustive test of every 1-ple, 2-ple and 3-ple of sets and compare
    // the fast impl with the "slow" impl below.
    final int len = all.size();
    for (int i = 0; i < len; i++) {
      Set<?> ss1 = all.get(i);

      for (int j = 0; j < len; j++) {
        Set<?> ss2 = all.get(j);
        // try different ordering
        assertEquals(Sets.sizeOfIntersection(ss1, ss2),
                     slowSizeOfIntersection(ss1, ss2));
        assertEquals(Sets.sizeOfIntersection(ss2, ss1),
                     slowSizeOfIntersection(ss2, ss1));
        assertEquals(Sets.sizeOfIntersection(ss1, ss2),
                     slowSizeOfIntersection(ss2, ss1));

        for (int k = 0; k < len; k++) {
          Set<?> ss3 = all.get(k);
          assertEquals(Sets.sizeOfIntersection(ss1, ss2, ss3),
                       slowSizeOfIntersection(ss1, ss2, ss3));
          assertEquals(Sets.sizeOfIntersection(ss1, ss2, ss3),
                       slowSizeOfIntersection(ss3, ss2, ss1));
        }
      }
    }
  }

  // slow impl (presumably) - creates an intermediate set
  private static int slowSizeOfIntersection(
      Set<?> firstSet, Set<?> ... otherSets) {
    Set<Object> intersection = new HashSet<Object>(firstSet);
    for (Set<?> set : otherSets) {
      intersection.retainAll(set);
    }
    return intersection.size();
  }

  public void testComplementOfEnumSet() throws Exception {
    Set<SomeEnum> units = EnumSet.of(SomeEnum.B, SomeEnum.D);
    Set<SomeEnum> otherUnits = Sets.complementOf(units);
    verifySetContents(otherUnits, EnumSet.of(SomeEnum.A, SomeEnum.C));
  }

  public void testComplementOfRegularSet() throws Exception {
    Set<SomeEnum> units = Sets.newHashSet(SomeEnum.B, SomeEnum.D);
    Set<SomeEnum> otherUnits = Sets.complementOf(units);
    verifySetContents(otherUnits, EnumSet.of(SomeEnum.A, SomeEnum.C));
  }

  public void testComplementOfRegularSetWithType() throws Exception {
    Set<SomeEnum> units = Sets.newHashSet(SomeEnum.B, SomeEnum.D);
    Set<SomeEnum> otherUnits = Sets.complementOf(units, SomeEnum.class);
    verifySetContents(otherUnits, EnumSet.of(SomeEnum.A, SomeEnum.C));
  }

  public void testComplementOfEmptySet() throws Exception {
    Set<SomeEnum> noUnits = Collections.emptySet();
    Set<SomeEnum> allUnits = Sets.complementOf(noUnits, SomeEnum.class);
    verifySetContents(EnumSet.allOf(SomeEnum.class), allUnits);
  }

  public void testComplementOfFullSet() throws Exception {
    Set<SomeEnum> allUnits = Sets.newHashSet(SomeEnum.values());
    Set<SomeEnum> noUnits = Sets.complementOf(allUnits, SomeEnum.class);
    verifySetContents(noUnits, EnumSet.noneOf(SomeEnum.class));
  }

  public void testComplementOfEmptyEnumSetWithoutType() throws Exception {
    Set<SomeEnum> noUnits = EnumSet.noneOf(SomeEnum.class);
    Set<SomeEnum> allUnits = Sets.complementOf(noUnits);
    verifySetContents(allUnits, EnumSet.allOf(SomeEnum.class));
  }

  public void testComplementOfEmptySetWithoutTypeDoesntWork() throws Exception {
    Set<SomeEnum> set = Collections.emptySet();
    try {
      Sets.complementOf(set);
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  public void testNullPointerExceptions() throws Exception {
    NullPointerTester tester = new NullPointerTester();
    tester.setDefault(Enum.class, SomeEnum.A);

    // TODO(kevinb): make NPT create empty arrays for defaults automatically
    tester.setDefault(Collection[].class, new Collection[0]);
    tester.setDefault(Enum[].class, new Enum[0]);
    tester.setDefault(Set[].class, new Set[0]);
    tester.testAllPublicStaticMethods(Sets.class);
  }

  public void testNewSetFromMap() throws Exception {
    Set<Integer> set = Sets.newSetFromMap(new HashMap<Integer, Boolean>());
    set.addAll(SOME_COLLECTION);
    verifySetContents(set, SOME_COLLECTION);
  }

  public void testEmptySortedSet() {
    SortedSet<Integer> empty = Sets.emptySortedSet();
    SortedSet<Integer> empty2 = Sets.newTreeSet();
    assertTrue(empty.isEmpty());
    assertEquals(0, empty.size());
    assertNull(empty.comparator());
    assertEquals("[]", empty.toString());
    MoreAsserts.checkEqualsAndHashCodeMethods(empty, empty2, true);
    assertFalse(empty.equals(null));
    assertFalse(empty.equals(new Object()));
    try {
      empty.add(4);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      empty.first();
      fail("NoSuchElementException expected");
    } catch (NoSuchElementException expected) {}
    try {
      empty.last();
      fail("NoSuchElementException expected");
    } catch (NoSuchElementException expected) {}
    try {
      empty.subSet(null, null);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {}
    try {
      empty.headSet(null);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {}
    try {
      empty.tailSet(null);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {}
  }

  /**
   * Utility method to verify that the given LinkedHashSet is equal to and
   * hashes identically to a set constructed with the elements in the given
   * collection.  Also verifies that the ordering in the set is the same
   * as the ordering of the given contents.
   */
  private static <E> void verifyLinkedHashSetContents(
      LinkedHashSet<E> set, Collection<E> contents) {
    assertEquals("LinkedHashSet should have preserved order for iteration",
        new ArrayList<E>(set), new ArrayList<E>(contents));
    verifySetContents(set, contents);
  }

  /**
   * Utility method to verify that the given SortedSet is equal to and
   * hashes identically to a set constructed with the elements in the
   * given iterable.  Also verifies that the comparator is the same as the
   * given comparator.
   */
  private static <E> void verifySortedSetContents(
      SortedSet<E> set, Iterable<E> iterable, Comparator<E> comparator) {
    assertSame(comparator, set.comparator());
    verifySetContents(set, iterable);
  }

  /**
   * Utility method that verifies that the given set is equal to and hashes
   * identically to a set constructed with the elements in the given iterable.
   */
  private static <E> void verifySetContents(Set<E> set, Iterable<E> contents) {
    Set<E> expected = null;
    if (contents instanceof Set) {
      expected = (Set<E>) contents;
    } else {
      expected = new HashSet<E>();
      for (E element : contents) {
        expected.add(element);
      }
    }
    MoreAsserts.checkEqualsAndHashCodeMethods(expected, set, true);
  }

  /**
   * Simple base class for use in {@link SetsTest#testSizeOfIntersection}
   * to verify that we handle generics correctly.
   */
  private static class Base {
    private Base(String s) {
      this.s = s;
    }

    @Override public int hashCode() { // delgate to 's'
      return s.hashCode();
    }

    @Override public boolean equals(Object other) {
      if (other == null) {
        return false;
      } else if (other instanceof Base) {
        return s.equals(((Base) other).s);
      } else {
        return false;
      }
    }

    private String s;
  }

  /**
   * Simple derived class for use in {@link SetsTest#testSizeOfIntersection}
   * to verify that we handle generics correctly.
   */
  private static class Derived
      extends Base {
    private Derived(String s) {
      super(s);
    }
  }
}
