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

import com.google.common.collect.helpers.SerializationChecker;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Unit test for {@link StandardBiMap} when backed by two hash maps.
 *
 * @author kevinb
 */
public class StandardBiMapTest extends AbstractBiMapTest {
  protected BiMap<Integer, String> create() {
    return Maps.newHashBiMap();
  }

  public void testSerialization() throws Exception {
    BiMap<Integer,String> bimap = create();    
    bimap.put(1, "one");
    bimap.put(2, "two");
    bimap.put(3, "three");
    bimap.put(null, null);

    assertTrue(SerializationChecker.canSerialize(bimap));
  }
  
  private static final int N = 1000;

  public void testBashIt() throws Exception {
    BiMap<Integer,Integer> bimap = new HashBiMap<Integer,Integer>(N);
    BiMap<Integer,Integer> inverse = bimap.inverse();

    for (int i = 0; i < N; i++) {
      assertNull(bimap.put(2 * i, 2 * i + 1));
    }
    for (int i = 0; i < N; i++) {
      assertEquals(2 * i + 1, (int) bimap.get(2 * i));
    }
    for (int i = 0; i < N; i++) {
      assertEquals(2 * i, (int) inverse.get(2 * i + 1));
    }
    for (int i = 0; i < N; i++) {
      int oldValue = bimap.get(2 * i);
      assertEquals(2 * i + 1, (int) bimap.put(2 * i, oldValue - 2));
    }
    for (int i = 0; i < N; i++) {
      assertEquals(2 * i - 1, (int) bimap.get(2 * i));
    }
    for (int i = 0; i < N; i++) {
      assertEquals(2 * i, (int) inverse.get(2 * i - 1));
    }
    Set<Entry<Integer, Integer>> entries = bimap.entrySet();
    for (Entry<Integer, Integer> entry : entries) {
      entry.setValue(entry.getValue() + 2 * N);
    }
    for (int i = 0; i < N; i++) {
      assertEquals(2 * N + 2 * i - 1, (int) bimap.get(2 * i));
    }
  }
}
