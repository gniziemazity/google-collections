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

import java.util.Collections;
import java.util.Map;
import junit.framework.TestCase;

/**
 * Tests for {@code EnumHashBiMap}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class EnumHashBiMapTest extends TestCase {
  private enum Currency { DOLLAR, PESO, FRANC };
  private enum Country { CANADA, CHILE, SWITZERLAND };

  public void testClassConstructor() {
    EnumHashBiMap<Currency, String> bimap =
        new EnumHashBiMap<Currency, String>(Currency.class);
    assertTrue(bimap.isEmpty());
    assertEquals("{}", bimap.toString());
    assertEquals(Maps.newHashBiMap(), bimap);
    bimap.put(Currency.DOLLAR, "dollar");
    assertEquals("dollar", bimap.get(Currency.DOLLAR));
    assertEquals(Currency.DOLLAR, bimap.inverse().get("dollar"));
  }

  public void testMapConstructor() {
    /* Test with non-empty Map. */
    Map<Currency, String> map = Maps.immutableMap(
        Currency.DOLLAR, "dollar",
        Currency.PESO, "peso",
        Currency.FRANC, "franc");
    EnumHashBiMap<Currency, String> bimap
        = new EnumHashBiMap<Currency, String>(map);
    assertEquals("dollar", bimap.get(Currency.DOLLAR));
    assertEquals(Currency.DOLLAR, bimap.inverse().get("dollar"));

    /* Map must have at least one entry if not an EnumHashBiMap. */
    try {
      new EnumHashBiMap<Currency, String>(
          Collections.<Currency, String>emptyMap());
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {}

    /* Map can be empty if it's an EnumHashBiMap. */
    Map<Currency, String> emptyBimap =
        new EnumHashBiMap<Currency, String>(Currency.class);
    bimap = new EnumHashBiMap<Currency, String>(emptyBimap);
    assertTrue(bimap.isEmpty());

    /* Map can be empty if it's an EnumBiMap. */
    Map<Currency, Country> emptyBimap2 =
        new EnumBiMap<Currency, Country>(Currency.class, Country.class);
    EnumHashBiMap<Currency, Country> bimap2
        = new EnumHashBiMap<Currency, Country>(emptyBimap2);
    assertTrue(bimap2.isEmpty());
  }

  public void testEnumHashBiMapConstructor() {
    /* Test that it copies existing entries. */
    EnumHashBiMap<Currency, String> bimap1 =
        new EnumHashBiMap<Currency, String>(Currency.class);
    bimap1.put(Currency.DOLLAR, "dollar");
    EnumHashBiMap<Currency, String> bimap2 =
        new EnumHashBiMap<Currency, String>(bimap1);
    assertEquals("dollar", bimap2.get(Currency.DOLLAR));
    assertEquals(bimap1, bimap2);
    bimap2.inverse().put("franc", Currency.FRANC);
    assertEquals("franc", bimap2.get(Currency.FRANC));
    assertNull(bimap1.get(Currency.FRANC));
    assertFalse(bimap2.equals(bimap1));

    /* Test that it can be empty. */
    EnumHashBiMap<Currency, String> emptyBimap =
        new EnumHashBiMap<Currency, String>(Currency.class);
    EnumHashBiMap<Currency, String> bimap3 =
        new EnumHashBiMap<Currency, String>(emptyBimap);
    assertEquals(bimap3, emptyBimap);
  }

  public void testEnumBiMapConstructor() {
    /* Test that it copies existing entries. */
    EnumBiMap<Currency, Country> bimap1 =
        new EnumBiMap<Currency, Country>(Currency.class, Country.class);
    bimap1.put(Currency.DOLLAR, Country.SWITZERLAND);
    EnumHashBiMap<Currency, Object> bimap2 = // use supertype
        new EnumHashBiMap<Currency, Object>(bimap1);
    assertEquals(Country.SWITZERLAND, bimap2.get(Currency.DOLLAR));
    assertEquals(bimap1, bimap2);
    bimap2.inverse().put("franc", Currency.FRANC);
    assertEquals("franc", bimap2.get(Currency.FRANC));
    assertNull(bimap1.get(Currency.FRANC));
    assertFalse(bimap2.equals(bimap1));

    /* Test that it can be empty. */
    EnumBiMap<Currency, Country> emptyBimap =
        new EnumBiMap<Currency, Country>(Currency.class, Country.class);
    EnumHashBiMap<Currency, Country> bimap3 = // use exact type
        new EnumHashBiMap<Currency, Country>(emptyBimap);
    assertEquals(bimap3, emptyBimap);
  }

  public void testKeyType() {
    EnumHashBiMap<Currency, String> bimap =
        new EnumHashBiMap<Currency, String>(Currency.class);
    assertEquals(Currency.class, bimap.keyType());
  }

  public void testClone() {
    EnumHashBiMap<Currency, String> bimap =
        new EnumHashBiMap<Currency, String>(Currency.class);
    bimap.inverse().put("dollar", Currency.DOLLAR);
    EnumHashBiMap<Currency, String> clone = bimap.clone();
    assertEquals(bimap, clone);
    assertEquals(bimap.inverse(), clone.inverse());
    bimap.put(Currency.FRANC, "franc");
    assertFalse(bimap.equals(clone));
    assertFalse(bimap.inverse().equals(clone.inverse()));
  }

  /* Remaining behavior tested by StandardBiMapTest. */
}
