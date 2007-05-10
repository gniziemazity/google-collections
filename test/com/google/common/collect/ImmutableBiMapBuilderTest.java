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

import java.util.Map;
import junit.framework.TestCase;

/**
 * Test for the {@code ImmutableBiMapBuilder}.
 *
 * @author dovle@google.com (Alex Dovlecel)
 */
public class ImmutableBiMapBuilderTest extends TestCase {

  /** Instance to be tested. */
  private ImmutableBiMapBuilder<Integer, String> builder;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    builder = new ImmutableBiMapBuilder<Integer, String>();
  }

  /**
   * Tests the content of the bimap created with the {@code builder}.
   */
  public void testContent() throws Exception {
    assertSame(builder, builder.put(10, "ten"));
    builder.put(20, "twenty");

    BiMap<Integer, String> map = builder.getBiMap();

    assertNotNull(map);

    assertEquals("ten", map.get(10));
    assertEquals(10, (int) map.inverse().get("ten"));

    assertEquals("twenty", map.get(20));
    assertEquals(20, (int) map.inverse().get("twenty"));
  }

  /**
   * Tests that the bimap created with the {@code builder} is immutable.
   */
  public void testImmutability() throws Exception {
    builder.put(10, "ten");
    builder.put(20, "twenty");

    BiMap<Integer, String> map = builder.getBiMap();

    assertMapIsImmutable(map, 10, 11, "11");
    assertMapIsImmutable(map.inverse(), "twenty", "sixty", 60);
  }

  /**
   * Tests that the calls of the {@code put} and {@code getBiMap} methods after
   * the bimap was built will throw an {@code IllegalStateException}.
   */
  public void testIllegalStateCalls() throws Exception {
    builder.getBiMap();

    try {
      builder.put(10, "twenty");
      fail("After getBiMap(), the builder must be in an illegal state.");
    } catch (IllegalStateException e) { /* expected */ }

    try {
      builder.getBiMap();
      fail("After getBiMap(), the builder must be in an illegal state.");
    } catch (IllegalStateException e) { /* expected */ }
  }

  /**
   * Tests that putting the same value with different keys in the builder will
   * result in an {@code IllegalArgumentException} being thrown.
   */
  public void testDuplicateValues() {
    builder.put(100, "ten");
    try {
      builder.put(10, "ten");
      fail("Must not allow duplicate values.");
    } catch (IllegalArgumentException e) { /* expected */ }
  }

  /**
   * Asserts the {@code map} is immutable by trying to execute a {@code put} and
   * a {@code remove} and expecting an {@code Exception}.
   *
   * @param <K> the type of the key
   * @param <V> the type of the value
   * @param map the map to be checked
   * @param removeKey the key used for trying to execute {@code remove}
   * @param newKey the key used for trying to execute {@code put}
   * @param newValue the value used for trying to execute {@code put}
   */
  private <K,V> void assertMapIsImmutable(Map<K, V> map,
      K removeKey, K newKey, V newValue) {
    try {
      map.put(newKey, newValue);
      fail("put succeded");
    } catch (Exception e) { /* expected */ }

    try {
      map.remove(removeKey);
      fail("remove succeded");
    } catch (Exception e) { /* expected */ }

    for (Map.Entry<K,V> entry : map.entrySet()) {
      UnmodifiableCollectionTests.assertMapEntryIsUnmodifiable(entry);
    }
  }
}
