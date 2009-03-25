/*
 * Copyright (C) 2008 Google Inc.
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

import com.google.common.testutils.EqualsTester;
import com.google.common.testutils.SerializableTester;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Tests for {@link ImmutableMultimap}.
 *
 * @author Jared Levy
 */
public class ImmutableMultimapTest extends TestCase {

  public void testBuilderPutAllIterable() {
    ImmutableMultimap.Builder<String, Integer> builder
        = ImmutableMultimap.builder();
    builder.putAll("foo", Arrays.asList(1, 2, 3));
    builder.putAll("bar", Arrays.asList(4, 5));
    builder.putAll("foo", Arrays.asList(6, 7));
    Multimap<String, Integer> multimap = builder.build();
    assertEquals(Arrays.asList(1, 2, 3, 6, 7), multimap.get("foo"));
    assertEquals(Arrays.asList(4, 5), multimap.get("bar"));
    assertEquals(7, multimap.size());
  }

  public void testBuilderPutAllVarargs() {
    ImmutableMultimap.Builder<String, Integer> builder
        = ImmutableMultimap.builder();
    builder.putAll("foo", 1, 2, 3);
    builder.putAll("bar", 4, 5);
    builder.putAll("foo", 6, 7);
    Multimap<String, Integer> multimap = builder.build();
    assertEquals(Arrays.asList(1, 2, 3, 6, 7), multimap.get("foo"));
    assertEquals(Arrays.asList(4, 5), multimap.get("bar"));
    assertEquals(7, multimap.size());
  }

  public void testBuilderPutAllWithDuplicates() {
    ImmutableMultimap.Builder<String, Integer> builder
        = ImmutableMultimap.builder();
    builder.putAll("foo", 1, 2, 3);
    builder.putAll("bar", 4, 5);
    builder.putAll("foo", 1, 6, 7);
    ImmutableMultimap<String, Integer> multimap = builder.build();
    assertEquals(Arrays.asList(1, 2, 3, 1, 6, 7), multimap.get("foo"));
    assertEquals(Arrays.asList(4, 5), multimap.get("bar"));
    assertEquals(8, multimap.size());
  }

  public void testBuilderPutWithDuplicates() {
    ImmutableMultimap.Builder<String, Integer> builder
        = ImmutableMultimap.builder();
    builder.putAll("foo", 1, 2, 3);
    builder.putAll("bar", 4, 5);
    builder.put("foo", 1);
    ImmutableMultimap<String, Integer> multimap = builder.build();
    assertEquals(Arrays.asList(1, 2, 3, 1), multimap.get("foo"));
    assertEquals(Arrays.asList(4, 5), multimap.get("bar"));
    assertEquals(6, multimap.size());
  }

  public void testBuilderPutNullKey() {
    ImmutableMultimap.Builder<String, Integer> builder
        = ImmutableMultimap.builder();
    try {
      builder.put(null, 1);
      fail();
    } catch (NullPointerException expected) {}
    try {
      builder.putAll(null, Arrays.asList(1, 2, 3));
      fail();
    } catch (NullPointerException expected) {}
    try {
      builder.putAll(null, 1, 2, 3);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testBuilderPutNullValue() {
    ImmutableMultimap.Builder<String, Integer> builder
        = ImmutableMultimap.builder();
    try {
      builder.put("foo", null);
      fail();
    } catch (NullPointerException expected) {}
    try {
      builder.putAll("foo", Arrays.asList(1, null, 3));
      fail();
    } catch (NullPointerException expected) {}
    try {
      builder.putAll("foo", 1, null, 3);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testCopyOf() {
    ArrayListMultimap<String, Integer> input = ArrayListMultimap.create();
    input.put("foo", 1);
    input.put("bar", 2);
    input.put("foo", 3);
    Multimap<String, Integer> multimap = ImmutableMultimap.copyOf(input);
    assertEquals(multimap, input);
    assertEquals(input, multimap);
  }

  public void testCopyOfWithDuplicates() {
    ArrayListMultimap<String, Integer> input = ArrayListMultimap.create();
    input.put("foo", 1);
    input.put("bar", 2);
    input.put("foo", 3);
    input.put("foo", 1);
    Multimap<String, Integer> multimap = ImmutableMultimap.copyOf(input);
    assertEquals(multimap, input);
    assertEquals(input, multimap);
  }

  public void testCopyOfEmpty() {
    ArrayListMultimap<String, Integer> input = ArrayListMultimap.create();
    Multimap<String, Integer> multimap = ImmutableMultimap.copyOf(input);
    assertEquals(multimap, input);
    assertEquals(input, multimap);
  }

  public void testCopyOfImmutableMultimap() {
    Multimap<String, Integer> multimap = createMultimap();
    assertSame(multimap, ImmutableMultimap.copyOf(multimap));
  }

  public void testCopyOfNullKey() {
    ArrayListMultimap<String, Integer> input = ArrayListMultimap.create();
    input.put(null, 1);
    try {
      ImmutableMultimap.copyOf(input);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testCopyOfNullValue() {
    ArrayListMultimap<String, Integer> input = ArrayListMultimap.create();
    input.putAll("foo", Arrays.asList(1, null, 3));
    try {
      ImmutableMultimap.copyOf(input);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testEmptyMultimapReads() {
    Multimap<String, Integer> multimap = ImmutableMultimap.empty();
    assertFalse(multimap.containsKey("foo"));
    assertFalse(multimap.containsValue(1));
    assertFalse(multimap.containsEntry("foo", 1));
    assertTrue(multimap.entries().isEmpty());
    assertTrue(multimap.equals(ArrayListMultimap.create()));
    assertEquals(Collections.emptyList(), multimap.get("foo"));
    assertEquals(0, multimap.hashCode());
    assertTrue(multimap.isEmpty());
    assertEquals(HashMultiset.create(), multimap.keys());
    assertEquals(Collections.emptySet(), multimap.keySet());
    assertEquals(0, multimap.size());
    assertTrue(multimap.values().isEmpty());
    assertEquals("{}", multimap.toString());
  }

  public void testEmptyMultimapWrites() {
    Multimap<String, Integer> multimap = ImmutableMultimap.empty();
    UnmodifiableCollectionTests.assertMultimapIsUnmodifiable(
        multimap, "foo", 1);
  }

  private Multimap<String, Integer> createMultimap() {
    return ImmutableMultimap.<String, Integer>builder()
        .put("foo", 1).put("bar", 2).put("foo", 3).build();
  }

  public void testMultimapReads() {
    Multimap<String, Integer> multimap = createMultimap();
    assertTrue(multimap.containsKey("foo"));
    assertFalse(multimap.containsKey("cat"));
    assertTrue(multimap.containsValue(1));
    assertFalse(multimap.containsValue(5));
    assertTrue(multimap.containsEntry("foo", 1));
    assertFalse(multimap.containsEntry("cat", 1));
    assertFalse(multimap.containsEntry("foo", 5));
    assertFalse(multimap.entries().isEmpty());
    assertEquals(3, multimap.size());
    assertFalse(multimap.isEmpty());
    assertEquals("{foo=[1, 3], bar=[2]}", multimap.toString());
  }

  public void testMultimapWrites() {
    Multimap<String, Integer> multimap = createMultimap();
    UnmodifiableCollectionTests.assertMultimapIsUnmodifiable(
        multimap, "bar", 2);
  }

  public void testMultimapEquals() {
    Multimap<String, Integer> multimap = createMultimap();
    Multimap<String, Integer> arrayListMultimap
        = ArrayListMultimap.create();
    arrayListMultimap.putAll("foo", Arrays.asList(1, 3));
    arrayListMultimap.put("bar", 2);

    new EqualsTester(multimap)
        .addEqualObject(createMultimap())
        .addEqualObject(arrayListMultimap)
        .addEqualObject(ImmutableMultimap.<String, Integer>builder()
            .put("bar", 2).put("foo", 1).put("foo", 3).build())
        .addNotEqualObject(ImmutableMultimap.<String, Integer>builder()
            .put("bar", 2).put("foo", 3).put("foo", 1).build())
        .addNotEqualObject(ImmutableMultimap.<String, Integer>builder()
            .put("foo", 2).put("foo", 3).put("foo", 1).build())
        .addNotEqualObject(ImmutableMultimap.<String, Integer>builder()
            .put("bar", 2).put("foo", 3).build())
        .testEquals();
  }

  public void testSerialization() {
    Multimap<String, Integer> multimap = createMultimap();
    SerializableTester.reserializeAndAssert(multimap);
    assertEquals(multimap.size(),
        SerializableTester.reserialize(multimap).size());
    SerializableTester.reserializeAndAssert(multimap.get("foo"));
    SerializableTester.reserializeAndAssert(multimap.keySet());
    SerializableTester.reserializeAndAssert(multimap.keys());
    SerializableTester.reserializeAndAssert(multimap.asMap());
    Collection<Integer> valuesCopy
        = SerializableTester.reserialize(multimap.values());
    assertEquals(HashMultiset.create(multimap.values()),
        HashMultiset.create(valuesCopy));
  }

  public void testEmptySerialization() {
    Multimap<String, Integer> multimap = ImmutableMultimap.empty();
    assertSame(multimap, SerializableTester.reserialize(multimap));
  }
}
