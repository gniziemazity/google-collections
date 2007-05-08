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


/**
 * Tests for {@link HashBiMap}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class HashBiMapTest extends AbstractBiMapTest {

  protected BiMap<Integer, String> create() {
    return new HashBiMap<Integer, String>();
  }

  public void testMapConstructor() {
    /* Test with non-empty Map. */
    Map<String, String> map = Maps.immutableMap(
        "canada", "dollar",
        "chile", "peso",
        "switzerland", "franc");
    HashBiMap<String, String> bimap
        = new HashBiMap<String, String>(map);
    assertEquals("dollar", bimap.get("canada"));
    assertEquals("canada", bimap.inverse().get("dollar"));
  }

  public void testClone() {
    HashBiMap<String, String> bimap =
        new HashBiMap<String, String>();
    bimap.inverse().put("dollar", "canada");
    HashBiMap<String, String> clone = bimap.clone();
    assertEquals(bimap, clone);
    assertEquals(bimap.inverse(), clone.inverse());
    bimap.put("switzerland", "franc");
    assertFalse(bimap.equals(clone));
    assertFalse(bimap.inverse().equals(clone.inverse()));
  }

  /* Remaining behavior tested by StandardBiMapTest. */
}
