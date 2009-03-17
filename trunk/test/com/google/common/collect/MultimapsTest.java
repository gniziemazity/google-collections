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

import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import static com.google.common.collect.Sets.newHashSet;
import com.google.common.collect.SetsTest.Derived;
import static com.google.common.collect.testing.Helpers.assertContentsAnyOrder;
import static com.google.common.collect.testing.IteratorFeature.MODIFIABLE;
import com.google.common.collect.testing.IteratorTester;
import com.google.common.testing.junit3.JUnitAsserts;
import com.google.common.testutils.SerializableTester;
import java.io.Serializable;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import javax.annotation.Nullable;

/**
 * Unit test for {@code Multimaps}.
 *
 * @author Jared Levy
 */
public class MultimapsTest extends AbstractMultimapTest {
  private static final Comparator<Integer> INT_COMPARATOR =
      Ordering.<Integer>natural().reverse().nullsFirst();

  @Override protected Multimap<String, Integer> create() {
    return Multimaps.synchronizedSetMultimap(
        HashMultimap.<String, Integer>create());
  }

  public void testUnmodifiableArrayListMultimap() {
    checkUnmodifiableMultimap(
        ArrayListMultimap.<String, Integer>create(), true);
  }

  public void testUnmodifiableHashMultimap() {
    checkUnmodifiableMultimap(HashMultimap.<String, Integer>create(), false);
  }

  public void testUnmodifiableTreeMultimap() {
    checkUnmodifiableMultimap(
        new TreeMultimap<String, Integer>(), false, "null", 42);
  }

  public void testUnmodifiableSynchronizedArrayListMultimap() {
    checkUnmodifiableMultimap(Multimaps.synchronizedListMultimap(
        ArrayListMultimap.<String, Integer>create()), true);
  }

  public void testUnmodifiableSynchronizedHashMultimap() {
    checkUnmodifiableMultimap(Multimaps.synchronizedSetMultimap(
        HashMultimap.<String, Integer>create()), false);
  }

  public void testUnmodifiableSynchronizedTreeMultimap() {
    SortedSetMultimap<String, Integer> multimap =
        Multimaps.synchronizedSortedSetMultimap(
            new TreeMultimap<String, Integer>(null, INT_COMPARATOR));
    checkUnmodifiableMultimap(multimap, false, "null", 42);
    assertSame(INT_COMPARATOR, multimap.valueComparator());
  }

  public void testUnmodifiableMultimapIsView() {
    Multimap<String, Integer> mod = HashMultimap.create();
    Multimap<String, Integer> unmod = Multimaps.unmodifiableMultimap(mod);
    assertEquals(mod, unmod);
    mod.put("foo", 1);
    assertTrue(unmod.containsEntry("foo", 1));
    assertEquals(mod, unmod);
  }

  @SuppressWarnings("unchecked")
  public void testUnmodifiableMultimapEntries() {
    Multimap<String, Integer> mod = HashMultimap.create();
    Multimap<String, Integer> unmod = Multimaps.unmodifiableMultimap(mod);
    mod.put("foo", 1);
    Entry<String, Integer> entry = unmod.entries().iterator().next();
    try {
      entry.setValue(2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    entry = (Entry<String, Integer>) unmod.entries().toArray()[0];
    try {
      entry.setValue(2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    Entry<String, Integer>[] array
        = (Entry<String, Integer>[]) new Entry<?, ?>[2];
    assertSame(array, unmod.entries().toArray(array));
    try {
      array[0].setValue(2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    assertFalse(unmod.entries().contains(MapsTest.nefariousEntry("pwnd", 2)));
    assertFalse(unmod.keys().contains("pwnd"));
  }

  /**
   * The supplied multimap will be mutated and an unmodifiable instance used
   * in its stead. The multimap must support null keys and values.
   */
  private static void checkUnmodifiableMultimap(
      Multimap<String, Integer> multimap, boolean permitsDuplicates) {
    checkUnmodifiableMultimap(multimap, permitsDuplicates, null, null);
  }

  /**
   * The supplied multimap will be mutated and an unmodifiable instance used
   * in its stead. If the multimap does not support null keys or values,
   * alternatives may be specified for tests involving nulls.
   */
  private static void checkUnmodifiableMultimap(
      Multimap<String, Integer> multimap, boolean permitsDuplicates,
      @Nullable String nullKey, @Nullable Integer nullValue) {
    multimap.clear();
    multimap.put("foo", 1);
    multimap.put("foo", 2);
    multimap.put("foo", 3);
    multimap.put("bar", 5);
    multimap.put("bar", -1);
    multimap.put(nullKey, nullValue);
    multimap.put("foo", nullValue);
    multimap.put(nullKey, 5);
    multimap.put("foo", 2);

    if (permitsDuplicates) {
      assertEquals(9, multimap.size());
    } else {
      assertEquals(8, multimap.size());
    }

    Multimap<String, Integer> unmodifiable;
    if (multimap instanceof SortedSetMultimap) {
      unmodifiable = Multimaps.unmodifiableSortedSetMultimap(
          (SortedSetMultimap<String, Integer>) multimap);
    } else if (multimap instanceof SetMultimap) {
      unmodifiable = Multimaps.unmodifiableSetMultimap(
          (SetMultimap<String, Integer>) multimap);
    } else if (multimap instanceof ListMultimap) {
      unmodifiable = Multimaps.unmodifiableListMultimap(
          (ListMultimap<String, Integer>) multimap);
    } else {
      unmodifiable = Multimaps.unmodifiableMultimap(multimap);
    }

    UnmodifiableCollectionTests.assertMultimapIsUnmodifiable(
        unmodifiable, "test", 123);

    assertUnmodifiableIterableInTandem(
        unmodifiable.keys(), multimap.keys());

    assertUnmodifiableIterableInTandem(
        unmodifiable.keySet(), multimap.keySet());

    assertUnmodifiableIterableInTandem(
        unmodifiable.entries(), multimap.entries());

    assertUnmodifiableIterableInTandem(
        unmodifiable.asMap().entrySet(), multimap.asMap().entrySet());

    assertEquals(multimap.toString(), unmodifiable.toString());
    assertEquals(multimap.hashCode(), unmodifiable.hashCode());
    assertEquals(multimap, unmodifiable);

    assertContentsAnyOrder(unmodifiable.asMap().get("bar"), 5, -1);
    assertNull(unmodifiable.asMap().get("missing"));

    assertFalse(unmodifiable.entries() instanceof Serializable);
    assertFalse(unmodifiable.asMap().values() instanceof Serializable);

    SerializableTester.reserializeAndAssert(unmodifiable);
  }

  private static <T> void assertUnmodifiableIterableInTandem(
      Iterable<T> unmodifiable, Iterable<T> modifiable) {
    UnmodifiableCollectionTests.assertIteratorIsUnmodifiable(
        unmodifiable.iterator());
    UnmodifiableCollectionTests.assertIteratorsInOrder(
        unmodifiable.iterator(), modifiable.iterator());
  }

  public void testInvertFrom() {
    ImmutableMultimap<Integer, String> empty = ImmutableMultimap.empty();

    // typical usage example - sad that ArrayListMultimap.create() won't work
    Multimap<String, Integer> multimap = Multimaps.invertFrom(empty,
        ArrayListMultimap.<String, Integer>create());
    assertTrue(multimap.isEmpty());

    ImmutableMultimap<Integer, String> single
        = new ImmutableMultimap.Builder<Integer, String>()
            .put(1, "one")
            .put(2, "two")
            .build();

    // copy into existing multimap
    assertSame(multimap, Multimaps.invertFrom(single, multimap));

    ImmutableMultimap<String, Integer> expected
        = new ImmutableMultimap.Builder<String, Integer>()
        .put("one", 1)
        .put("two", 2)
        .build();

    assertEquals(expected, multimap);
  }

  public void testForMap() {
    Map<String, Integer> map = Maps.newHashMap();
    map.put("foo", 1);
    map.put("bar", 2);
    Multimap<String, Integer> multimap = HashMultimap.create();
    multimap.put("foo", 1);
    multimap.put("bar", 2);
    Multimap<String, Integer> multimapView = Multimaps.forMap(map);
    assertTrue(multimap.equals(multimapView));
    assertTrue(multimapView.equals(multimap));
    assertTrue(multimapView.equals(multimapView));
    assertFalse(multimapView.equals(map));
    Multimap<String, Integer> multimap2 = HashMultimap.create();
    multimap2.put("foo", 1);
    assertFalse(multimapView.equals(multimap2));
    multimap2.put("bar", 1);
    assertFalse(multimapView.equals(multimap2));
    ListMultimap<String, Integer> listMultimap
        = new ImmutableMultimap.Builder<String, Integer>()
            .put("foo", 1).put("bar", 2).build();
    assertFalse("SetMultimap equals ListMultimap",
        multimapView.equals(listMultimap));
    assertEquals(multimap.toString(), multimapView.toString());
    assertEquals(multimap.hashCode(), multimapView.hashCode());
    assertEquals(multimap.size(), multimapView.size());
    assertTrue(multimapView.containsKey("foo"));
    assertTrue(multimapView.containsValue(1));
    assertTrue(multimapView.containsEntry("bar", 2));
    assertEquals(Collections.singleton(1), multimapView.get("foo"));
    assertEquals(Collections.singleton(2), multimapView.get("bar"));
    SerializableTester.reserializeAndAssert(multimapView);
    try {
      multimapView.put("baz", 3);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      multimapView.putAll("baz", Collections.singleton(3));
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      multimapView.putAll(multimap);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      multimapView.replaceValues("foo", Collections.<Integer>emptySet());
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    multimapView.remove("bar", 2);
    assertFalse(multimapView.containsKey("bar"));
    assertFalse(map.containsKey("bar"));
    assertEquals(map.keySet(), multimapView.keySet());
    assertEquals(map.keySet(), multimapView.keys().elementSet());
    assertContentsAnyOrder(multimapView.keys(), "foo");
    assertContentsAnyOrder(multimapView.values(), 1);
    assertContentsAnyOrder(multimapView.entries(),
        Maps.immutableEntry("foo", 1));
    assertContentsAnyOrder(multimapView.asMap().entrySet(),
        Maps.immutableEntry("foo", Collections.singleton(1)));
    multimapView.clear();
    assertFalse(multimapView.containsKey("foo"));
    assertFalse(map.containsKey("foo"));
    assertTrue(map.isEmpty());
    assertTrue(multimapView.isEmpty());
    multimap.clear();
    assertEquals(multimap.toString(), multimapView.toString());
    assertEquals(multimap.hashCode(), multimapView.hashCode());
    assertEquals(multimap.size(), multimapView.size());
    assertEquals(multimapView, ArrayListMultimap.create());
  }

  public void testForMapRemoveAll() {
    Map<String, Integer> map = Maps.newHashMap();
    map.put("foo", 1);
    map.put("bar", 2);
    map.put("cow", 3);
    Multimap<String, Integer> multimap = Multimaps.forMap(map);
    assertEquals(3, multimap.size());
    assertEquals(Collections.emptySet(), multimap.removeAll("dog"));
    assertEquals(3, multimap.size());
    assertTrue(multimap.containsKey("bar"));
    assertEquals(Collections.singleton(2), multimap.removeAll("bar"));
    assertEquals(2, multimap.size());
    assertFalse(multimap.containsKey("bar"));
  }

  public void testForMapAsMap() {
    Map<String, Integer> map = Maps.newHashMap();
    map.put("foo", 1);
    map.put("bar", 2);
    Map<String, Collection<Integer>> asMap = Multimaps.forMap(map).asMap();
    assertEquals(Collections.singleton(1), asMap.get("foo"));
    assertNull(asMap.get("cow"));
    assertTrue(asMap.containsKey("foo"));
    assertFalse(asMap.containsKey("cow"));

    Set<Entry<String, Collection<Integer>>> entries = asMap.entrySet();
    assertFalse(entries.contains(4.5));
    assertFalse(entries.remove(4.5));
    assertFalse(entries.contains(Maps.immutableEntry("foo",
        Collections.singletonList(1))));
    assertFalse(entries.remove(Maps.immutableEntry("foo",
        Collections.singletonList(1))));
    assertFalse(entries.contains(Maps.immutableEntry("foo",
        Sets.newLinkedHashSet(asList(1, 2)))));
    assertFalse(entries.remove(Maps.immutableEntry("foo",
        Sets.newLinkedHashSet(asList(1, 2)))));
    assertFalse(entries.contains(Maps.immutableEntry("foo",
        Collections.singleton(2))));
    assertFalse(entries.remove(Maps.immutableEntry("foo",
        Collections.singleton(2))));
    assertTrue(map.containsKey("foo"));
    assertTrue(entries.contains(Maps.immutableEntry("foo",
        Collections.singleton(1))));
    assertTrue(entries.remove(Maps.immutableEntry("foo",
        Collections.singleton(1))));
    assertFalse(map.containsKey("foo"));
  }

  public void testForMapGetIteration() throws Exception {
    IteratorTester<Integer> tester =
        new IteratorTester<Integer>(4, MODIFIABLE, newHashSet(1),
            IteratorTester.KnownOrder.KNOWN_ORDER) {
          private Multimap<String, Integer> multimap;

          @Override protected Iterator<Integer> newTargetIterator() {
            Map<String, Integer> map = Maps.newHashMap();
            map.put("foo", 1);
            map.put("bar", 2);
            multimap = Multimaps.forMap(map);
            return multimap.get("foo").iterator();
          }

          @Override protected void verify(List<Integer> elements) {
            assertEquals(newHashSet(elements), multimap.get("foo"));
          }
        };

    tester.ignoreSunJavaBug6529795();
    tester.test();
  }

  public void testTreeMultimapDerived() {
    TreeMultimap<Derived, Derived> multimap = Multimaps.newTreeMultimap();
    assertEquals(ImmutableMultimap.<Derived, Derived>empty(), multimap);
    multimap.put(new Derived("foo"), new Derived("f"));
    multimap.put(new Derived("foo"), new Derived("o"));
    multimap.put(new Derived("foo"), new Derived("o"));
    multimap.put(new Derived("bar"), new Derived("b"));
    multimap.put(new Derived("bar"), new Derived("a"));
    multimap.put(new Derived("bar"), new Derived("r"));
    JUnitAsserts.assertContentsInOrder(multimap.keySet(),
        new Derived("bar"), new Derived("foo"));
    JUnitAsserts.assertContentsInOrder(multimap.values(),
        new Derived("a"), new Derived("b"), new Derived("r"),
        new Derived("f"), new Derived("o"));
    assertNull(multimap.keyComparator());
    assertNull(multimap.valueComparator());
    SerializableTester.reserializeAndAssert(multimap);
  }

  public void testTreeMultimapNonGeneric() {
    TreeMultimap<LegacyComparable, LegacyComparable> multimap
        = Multimaps.newTreeMultimap();
    assertEquals(ImmutableMultimap.<LegacyComparable, LegacyComparable>empty(), multimap);
    multimap.put(new LegacyComparable("foo"), new LegacyComparable("f"));
    multimap.put(new LegacyComparable("foo"), new LegacyComparable("o"));
    multimap.put(new LegacyComparable("foo"), new LegacyComparable("o"));
    multimap.put(new LegacyComparable("bar"), new LegacyComparable("b"));
    multimap.put(new LegacyComparable("bar"), new LegacyComparable("a"));
    multimap.put(new LegacyComparable("bar"), new LegacyComparable("r"));
    JUnitAsserts.assertContentsInOrder(multimap.keySet(),
        new LegacyComparable("bar"), new LegacyComparable("foo"));
    JUnitAsserts.assertContentsInOrder(multimap.values(),
        new LegacyComparable("a"),
        new LegacyComparable("b"),
        new LegacyComparable("r"),
        new LegacyComparable("f"),
        new LegacyComparable("o"));
    assertNull(multimap.keyComparator());
    assertNull(multimap.valueComparator());
    SerializableTester.reserializeAndAssert(multimap);
  }

  private enum Color {BLUE, RED, YELLOW, GREEN}

  private static abstract class CountingSupplier<E>
      implements Supplier<E>, Serializable {
    int count;

    abstract E getImpl();

    public E get() {
      count++;
      return getImpl();
    }
  }

  private static class QueueSupplier extends CountingSupplier<Queue<Integer>> {
    @Override public Queue<Integer> getImpl() {
      return new ArrayBlockingQueue<Integer>(10);
    }
    private static final long serialVersionUID = 0;
  }

  public void testNewMultimap() {
    // The ubiquitous EnumArrayBlockingQueueMultimap
    CountingSupplier<Queue<Integer>> factory = new QueueSupplier();

    Map<Color, Collection<Integer>> map = Maps.newEnumMap(Color.class);
    Multimap<Color, Integer> multimap = Multimaps.newMultimap(map, factory);
    assertEquals(0, factory.count);
    multimap.putAll(Color.BLUE, asList(3, 1, 4));
    assertEquals(1, factory.count);
    multimap.putAll(Color.RED, asList(2, 7, 1, 8));
    assertEquals(2, factory.count);
    assertEquals("[3, 1, 4]", multimap.get(Color.BLUE).toString());
    /*
     * reserializeAndAssert fails since ArrayBlockingQueue doesn't override
     * equals()
     */
    SerializableTester.reserialize(multimap);

    Multimap<Color, Integer> ummodifiable =
        Multimaps.unmodifiableMultimap(multimap);
    assertEquals("[3, 1, 4]", ummodifiable.get(Color.BLUE).toString());

    Collection<Integer> collection = multimap.get(Color.BLUE);
    assertEquals(collection, collection);
  }

  private static class ListSupplier extends
      CountingSupplier<LinkedList<Integer>> {
    @Override public LinkedList<Integer> getImpl() {
      return new LinkedList<Integer>();
    }
    private static final long serialVersionUID = 0;
  }

  public void testNewListMultimap() {
    CountingSupplier<LinkedList<Integer>> factory = new ListSupplier();
    Map<Color, Collection<Integer>> map = Maps.newLinkedHashMap();
    ListMultimap<Color, Integer> multimap =
        Multimaps.newListMultimap(map, factory);
    assertEquals(0, factory.count);
    multimap.putAll(Color.BLUE, asList(3, 1, 4, 1));
    assertEquals(1, factory.count);
    multimap.putAll(Color.RED, asList(2, 7, 1, 8));
    assertEquals(2, factory.count);
    assertEquals("{BLUE=[3, 1, 4, 1], RED=[2, 7, 1, 8]}", multimap.toString());
    SerializableTester.reserializeAndAssert(multimap);
  }

  private static class SetSupplier extends CountingSupplier<HashSet<Integer>> {
    @Override public HashSet<Integer> getImpl() {
      return new HashSet<Integer>(4);
    }
    private static final long serialVersionUID = 0;
  }

  public void testNewSetMultimap() {
    CountingSupplier<HashSet<Integer>> factory = new SetSupplier();
    Map<Color, Collection<Integer>> map = Maps.newHashMap();
    SetMultimap<Color, Integer> multimap =
        Multimaps.newSetMultimap(map, factory);
    assertEquals(0, factory.count);
    multimap.putAll(Color.BLUE, asList(3, 1, 4));
    assertEquals(1, factory.count);
    multimap.putAll(Color.RED, asList(2, 7, 1, 8));
    assertEquals(2, factory.count);
    assertEquals(Sets.newHashSet(4, 3, 1), multimap.get(Color.BLUE));
    SerializableTester.reserializeAndAssert(multimap);
  }

  private static class SortedSetSupplier extends
      CountingSupplier<TreeSet<Integer>> {
    @Override public TreeSet<Integer> getImpl() {
      return Sets.newTreeSet(INT_COMPARATOR);
    }
    private static final long serialVersionUID = 0;
  }

  public void testNewSortedSetMultimap() {
    CountingSupplier<TreeSet<Integer>> factory = new SortedSetSupplier();
    Map<Color, Collection<Integer>> map = Maps.newEnumMap(Color.class);
    SortedSetMultimap<Color, Integer> multimap =
        Multimaps.newSortedSetMultimap(map, factory);
    // newSortedSetMultimap calls the factory once to determine the comparator.
    assertEquals(1, factory.count);
    multimap.putAll(Color.BLUE, asList(3, 1, 4));
    assertEquals(2, factory.count);
    multimap.putAll(Color.RED, asList(2, 7, 1, 8));
    assertEquals(3, factory.count);
    assertEquals("[4, 3, 1]", multimap.get(Color.BLUE).toString());
    SerializableTester.reserializeAndAssert(multimap);
    assertEquals(INT_COMPARATOR, multimap.valueComparator());
  }

  public void testIndex() {
    final Multimap<String, Object> stringToObject =
        new ImmutableMultimap.Builder<String, Object>()
            .put("1", 1)
            .put("1", 1L)
            .put("1", "1")
            .put("2", 2)
            .put("2", 2L)
            .build();

    ListMultimap<String, Object> outputMap =
        Multimaps.index(stringToObject.values(),
            Functions.toStringFunction());
    assertEquals(stringToObject, outputMap);

    ListMultimap<String, Object> outputMap2 = LinkedListMultimap.create();
    Multimaps.index(
        stringToObject.values(), Functions.toStringFunction(), outputMap2);
    assertEquals(stringToObject, outputMap2);
  }
}
