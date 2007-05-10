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
import junit.framework.TestCase;

/**
 * Unit test for {@code com.google.common.collect.ImmutableMultimapBuilder}.
 *
 * @author lwerner, based on ImmutableMapBuilderTest by kevinb
 */
public class ImmutableMultimapBuilderTest extends TestCase {

  static private final Multimap<Integer,String> oldWay =
      ImmutableMultimapBuilderTest.createMultimap();

  static private Multimap<Integer,String> createMultimap() {
    Multimap<Integer,String> map = Multimaps.newArrayListMultimap();
    map.put(1, "foo");
    map.putAll(2, Arrays.asList("foo", "bar"));
    map.putAll(3, Arrays.asList("foo", "bar", "baz"));
    map.put(4, "four");
    map.put(5, "five");
    return Multimaps.unmodifiableMultimap(map);
  }

  static final Multimap<Integer,String> newWay
    = new ImmutableMultimapBuilder<Integer,String>()
      .put(1, "foo")
      .putAll(2, "foo", "bar")
      .putAll(3, Arrays.asList("foo", "bar", "baz"))
      .put(4, "four")
      .put(5, "five")
      .getMultimap();

  /**
   * Tests equality of a multimap built manually via the Multimap.putXXX
   * and using ImmutableMultimapBuilder.  Note that the builder used to
   * construct the {@code newWay} variable exercises all four ways of adding
   * items to the Multimap.
   */
  public void testEquality() throws Exception {
    assertEquals(oldWay, newWay);
  }

  public void testEmpty() throws Exception {
    Multimap<Integer,String> map =
        new ImmutableMultimapBuilder<Integer,String>().getMultimap();
    assertEquals(0, map.size());
  }

  public void testCantPutMore() throws Exception {
    Multimap<Integer,String> map
        = new ImmutableMultimapBuilder<Integer,String>().getMultimap();
    try {
      map.put(4, "four");
      fail("Shouldn't be able to modify an immutable Multimap");
    } catch (UnsupportedOperationException expected) {
    }
  }
}
