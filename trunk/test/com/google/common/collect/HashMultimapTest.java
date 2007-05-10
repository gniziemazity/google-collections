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

/**
 * Unit tests for {@code HashMultimap}.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public class HashMultimapTest extends AbstractSetMultimapTest {
  @Override protected Multimap<String, Integer> create() {
    return Multimaps.newHashMultimap();
  }

  @SuppressWarnings("unchecked")
  @Override protected Multimap<String, Integer>
      makeClone(Multimap<String, Integer> multimap) {
    return ((HashMultimap<String,Integer>) multimap).clone();
  }

  /*
   * The behavior of toString() is tested by ArrayListMultimap, which shares a
   * lot of code with HashMultimap and has deterministic iteration order for
   * values.
   */

  public void testMultimapConstructor() {
    Multimap<String, Integer> multimap = createSample();
    HashMultimap<String, Integer> copy = Multimaps.newHashMultimap(multimap);
    assertEquals(multimap, copy);
  }
}
