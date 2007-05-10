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
 * Tests for {@code EnumBiMap}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class EnumBiMapTest extends TestCase {
  private enum Currency { DOLLAR, PESO, FRANC };
  private enum Country { CANADA, CHILE, SWITZERLAND };

  public void testClassClassConstructor() {
    EnumBiMap<Currency, Country> bimap =
        new EnumBiMap<Currency, Country>(Currency.class, Country.class);
    assertTrue(bimap.isEmpty());
    assertEquals("{}", bimap.toString());
    assertEquals(Maps.newHashBiMap(), bimap);
    bimap.put(Currency.DOLLAR, Country.CANADA);
    assertEquals(Country.CANADA, bimap.get(Currency.DOLLAR));
    assertEquals(Currency.DOLLAR, bimap.inverse().get(Country.CANADA));
  }

  public void testMapConstructor() {
    /* Test with non-empty Map. */
    Map<Currency, Country> map = Maps.immutableMap(
        Currency.DOLLAR, Country.CANADA,
        Currency.PESO, Country.CHILE,
        Currency.FRANC, Country.SWITZERLAND);
    EnumBiMap<Currency, Country> bimap = new EnumBiMap<Currency, Country>(map);
    assertEquals(Country.CANADA, bimap.get(Currency.DOLLAR));
    assertEquals(Currency.DOLLAR, bimap.inverse().get(Country.CANADA));

    /* Map must have at least one entry if not an EnumBiMap. */
    try {
      new EnumBiMap<Currency, Country>(
          Collections.<Currency, Country>emptyMap());
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {}

    /* Map can be empty if it's an EnumBiMap. */
    Map<Currency, Country> emptyBimap =
        new EnumBiMap<Currency, Country>(Currency.class, Country.class);
    bimap = new EnumBiMap<Currency, Country>(emptyBimap);
    assertTrue(bimap.isEmpty());
  }

  public void testEnumBiMapConstructor() {
    /* Test that it copies existing entries. */
    EnumBiMap<Currency, Country> bimap1 =
        new EnumBiMap<Currency, Country>(Currency.class, Country.class);
    bimap1.put(Currency.DOLLAR, Country.CANADA);
    EnumBiMap<Currency, Country> bimap2 =
        new EnumBiMap<Currency, Country>(bimap1);
    assertEquals(Country.CANADA, bimap2.get(Currency.DOLLAR));
    assertEquals(bimap1, bimap2);
    bimap2.inverse().put(Country.SWITZERLAND, Currency.FRANC);
    assertEquals(Country.SWITZERLAND, bimap2.get(Currency.FRANC));
    assertNull(bimap1.get(Currency.FRANC));
    assertFalse(bimap2.equals(bimap1));

    /* Test that it can be empty. */
    EnumBiMap<Currency, Country> emptyBimap =
        new EnumBiMap<Currency, Country>(Currency.class, Country.class);
    EnumBiMap<Currency, Country> bimap3 =
        new EnumBiMap<Currency, Country>(emptyBimap);
    assertEquals(bimap3, emptyBimap);
  }

  public void testKeyType() {
    EnumBiMap<Currency, Country> bimap =
        new EnumBiMap<Currency, Country>(Currency.class, Country.class);
    assertEquals(Currency.class, bimap.keyType());
  }

  public void testValueType() {
    EnumBiMap<Currency, Country> bimap =
        new EnumBiMap<Currency, Country>(Currency.class, Country.class);
    assertEquals(Country.class, bimap.valueType());
  }

  public void testClone() {
    EnumBiMap<Currency, Country> bimap =
        new EnumBiMap<Currency, Country>(Currency.class, Country.class);
    bimap.put(Currency.DOLLAR, Country.CANADA);
    EnumBiMap<Currency, Country> clone = bimap.clone();
    assertEquals(bimap, clone);
    assertEquals(bimap.inverse(), clone.inverse());
    bimap.put(Currency.FRANC, Country.SWITZERLAND);
    assertFalse(bimap.equals(clone));
    assertFalse(bimap.inverse().equals(clone.inverse()));
  }

  /* Remaining behavior tested by StandardBiMapTest. */
}
