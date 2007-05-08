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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link BiMap} backed by an {@link EnumMap} instance for keys-to-values, and
 * a {@link HashMap} instance for values-to-keys.
 * 
 * @author mbostock@google.com (Mike Bostock)
 */
public final class EnumHashBiMap<K extends Enum<K>, V>
    extends StandardBiMap<K,V> implements Cloneable {
  final Class<K> keyType;

  /**
   * Constructs a new empty bimap using the specified key type, sized to contain
   * an entry for every possible key.
   * 
   * @param keyType the key type
   * @throws NullPointerException if any argument is null
   */
  public EnumHashBiMap(Class<K> keyType) {
    super(new EnumMap<K,V>(keyType),
        new HashMap<V,K>(keyType.getEnumConstants().length * 3 / 2));
    this.keyType = keyType;
  }

  /**
   * Constructs a new bimap with the same mappings as the specified map. If the
   * specified map is an {@code EnumHashBiMap} or an {@code EnumBiMap} instance,
   * this constructor behaves identically to {@link
   * #EnumHashBiMap(EnumHashBiMap)} or {@link #EnumHashBiMap(EnumBiMap)},
   * respectively. Otherwise, the specified map must contain at least one
   * mapping (in order to determine the new enum bimap's key type).
   * 
   * @param map the map whose mappings are to be placed in this map
   * @throws NullPointerException if the specified map is null
   * @throws IllegalArgumentException if map is not an {@code EnumBiMap} or an
   *         {@link EnumHashBiMap} instance and contains no mappings
   */
  public EnumHashBiMap(Map<K, ? extends V> map) {
    this(EnumBiMap.inferKeyType(map));
    putAll(map); // careful if we make this class non-final
  }

  /**
   * Constructs a new bimap with the same key type as the specified map,
   * initially containing the same mappings (if any).
   * 
   * @param map the map whose mappings are to be placed in this map
   * @throws NullPointerException if the specified map is null
   */
  public EnumHashBiMap(EnumHashBiMap<K, ? extends V> map) {
    this(map.keyType);
    putAll(map); // careful if we make this class non-final
  }

  /**
   * Constructs a new bimap with the same key type as the specified map,
   * initially containing the same mappings (if any).
   * <p>
   * Note: This constructor has been commented out to work around Eclipse bug
   * 179902. It should be restored when the bug has been fixed. In the mean 
   * time, the "(Map<K, ? extends V> map)" overload will suffice.
   * <p>
   * See: <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=179902">
   * 
   * @param map the map whose mappings are to be placed in this map
   * @throws NullPointerException if the specified map is null
   *
  @SuppressWarnings("unchecked") // argh, my generics-fu is weak!
  public EnumHashBiMap(EnumBiMap<K, ? extends V> map) { 
    this(map.keyType);
    putAll((Map) map); // careful if we make this class non-final
  }*/

  /** Returns the associated key type. */
  public Class<K> keyType() {
    return keyType;
  }

  @SuppressWarnings("unchecked")
  @Override public EnumHashBiMap<K,V> clone() {
    try {
      return (EnumHashBiMap<K,V>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }
}
