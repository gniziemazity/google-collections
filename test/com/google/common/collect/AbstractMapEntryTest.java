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

import java.util.Map.Entry;
import junit.framework.TestCase;

/**
 * Tests for {@code AbstractMapEntry}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class AbstractMapEntryTest extends TestCase {
  private static final String NK = null;
  private static final Integer NV = null;

  private static <K,V> Entry<K,V> entry(final K key, final V value) {
    return new AbstractMapEntry<K,V>() {
        public K getKey() {
          return key;
        }
        public V getValue() {
          return value;
        }
      };
  }

  private static <K,V> Entry<K,V> control(K key, V value) {
    return Maps.immutableMap(key, value).entrySet().iterator().next();
  }

  public void testToString() {
    assertEquals("foo=1", entry("foo", 1).toString());
  }

  public void testToStringNull() {
    assertEquals("null=1", entry(NK, 1).toString());
    assertEquals("foo=null", entry("foo", NV).toString());
    assertEquals("null=null", entry(NK, NV).toString());
  }

  public void testEquals() {
    assertEquals(control("foo", 1), entry("foo", 1));
    assertEquals(control("bar", 2), entry("bar", 2));
    assertFalse(control("foo", 1).equals(entry("foo", 2)));
    assertFalse(entry("foo", 1).equals(control("bar", 1)));
    assertFalse(entry("foo", 1).equals(new Object()));
    assertFalse(entry("foo", 1).equals(null));
  }

  public void testEqualsNull() {
    assertEquals(control(NK, 1), entry(NK, 1));
    assertEquals(control("bar", NV), entry("bar", NV));
    assertFalse(control(NK, 1).equals(entry(NK, 2)));
    assertFalse(entry(NK, 1).equals(control("bar", 1)));
    assertFalse(entry(NK, 1).equals(new Object()));
    assertFalse(entry(NK, 1).equals(null));
  }

  public void testHashCode() {
    assertEquals(control("foo", 1).hashCode(), entry("foo", 1).hashCode());
    assertEquals(control("bar", 2).hashCode(), entry("bar", 2).hashCode());
  }

  public void testHashCodeNull() {
    assertEquals(control(NK, 1).hashCode(), entry(NK, 1).hashCode());
    assertEquals(control("bar", NV).hashCode(), entry("bar", NV).hashCode());
    assertEquals(control(NK, NV).hashCode(), entry(NK, NV).hashCode());
  }
}
