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

import static com.google.common.collect.ImmutableMapBuilder.fromMap;
import java.util.Collections;
import java.util.Map;
import junit.framework.TestCase;

/**
 * Tests for {@link ImmutableMapBuilder}.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public class ImmutableMapBuilderTest extends TestCase {
  private static final Map<Integer,String> oldWay = createMap();

  private static Map<Integer,String> createMap() {
    Map<Integer,String> map = Maps.newHashMap();
    map.put(1, "one");
    map.put(2, "two");
    map.put(3, "three");
    return Collections.unmodifiableMap(map);
  }

  private static final Map<Integer,String> newWay
    = new ImmutableMapBuilder<Integer,String>()
      .put(1, "one")
      .put(2, "two")
      .put(3, "three")
      .getMap();

  public void testSample() {
    assertEquals(oldWay, newWay);
  }

  public void testEmpty() {
    Map<Integer,String> map
        = new ImmutableMapBuilder<Integer,String>().getMap();
    assertEquals(Collections.emptyMap(), map);
  }

  public void testCantPutMore() {
    Map<Integer,String> map
        = new ImmutableMapBuilder<Integer,String>().getMap();
    try {
      map.put(4, "four");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testCantSetValue() {
    Map.Entry<Integer,String> entry = newWay.entrySet().iterator().next();
    try {
      entry.setValue("foo");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
  }

  @SuppressWarnings("unchecked")
  public void testCantModifyViaToArray() {
    Map.Entry<Integer,String> entry
        = (Map.Entry<Integer,String>) newWay.entrySet().toArray()[1];
    try {
      entry.setValue("foo");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testCantModifyViaEquals() {
    assertFalse(newWay.entrySet().contains(MapsTest.nefariousEntry(1, "pwnd")));
    assertFalse(newWay.values().contains("pwnd"));
  }

  public void testFromMap() {
    ImmutableMapBuilder<Integer, String> builder = fromMap(oldWay);
    Map<Integer, String> builtMap = builder.getMap();
    assertEquals(oldWay, builtMap);

    Map<Integer, String> emptyMap = Collections.emptyMap();
    builtMap = fromMap(emptyMap).getMap();
    assertEquals(emptyMap, builtMap);

    builder = fromMap(oldWay);
    builder.put(4, "four");
    builtMap = builder.getMap();
    assertEquals(4, builtMap.size());
    assertEquals("four", builtMap.get(4));

    try {
      fromMap(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {}
  }
}
