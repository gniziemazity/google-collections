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

import static com.google.common.base.Preconditions.checkArgument;
import java.util.EnumMap;
import java.util.Map;

/**
 * A {@code BiMap} backed by two {@code EnumMap} instances. Null keys and values
 * are not permitted.
 *
 * @author Mike Bostock
 */
public final class EnumBiMap<K extends Enum<K>, V extends Enum<V>>
    extends StandardBiMap<K, V> {
  private final Class<K> keyType;
  private final Class<V> valueType;

  /**
   * Constructs a new empty bimap using the specified key type and value type.
   *
   * @param keyType the key type
   * @param valueType the value type
   */
  public EnumBiMap(Class<K> keyType, Class<V> valueType) {
    super(new EnumMap<K, V>(keyType), new EnumMap<V, K>(valueType));
    this.keyType = keyType;
    this.valueType = valueType;
  }

  /**
   * Constructs a new bimap with the same mappings as the specified map.
   *
   * @param map the map whose mappings are to be placed in this map
   */
  public EnumBiMap(EnumBiMap<K, V> map) {
    this(map.keyType, map.valueType);
    putAll(map); // careful if we make this class non-final
  }

  /**
   * Constructs a new bimap with the same mappings as the specified map. If the
   * specified map is an {@code EnumBiMap} instance, this constructor behaves
   * identically to {@link #EnumBiMap(EnumBiMap)}. Otherwise, the specified map
   * must contain at least one mapping (in order to determine the new enum
   * bimap's key and value types).
   *
   * @param map the map whose mappings are to be placed in this map
   * @throws IllegalArgumentException if map is not an {@code EnumBiMap}
   *     instance and contains no mappings
   */
  public EnumBiMap(Map<K, V> map) {
    this(inferKeyType(map), inferValueType(map));
    putAll(map); // careful if we make this class non-final
  }

  static <K extends Enum<K>> Class<K> inferKeyType(Map<K, ?> map) {
    if (map instanceof EnumBiMap<?, ?>) {
      return ((EnumBiMap<K, ?>) map).keyType;
    }
    if (map instanceof EnumHashBiMap<?, ?>) {
      return ((EnumHashBiMap<K, ?>) map).keyType;
    }
    checkArgument(!map.isEmpty());
    return map.keySet().iterator().next().getDeclaringClass();
  }

  private static <V extends Enum<V>> Class<V> inferValueType(Map<?, V> map) {
    if (map instanceof EnumBiMap<?, ?>) {
      return ((EnumBiMap<?, V>) map).valueType;
    }
    checkArgument(!map.isEmpty());
    return map.values().iterator().next().getDeclaringClass();
  }

  /** Returns the associated key type. */
  public Class<K> keyType() {
    return keyType;
  }

  /** Returns the associated value type. */
  public Class<V> valueType() {
    return valueType;
  }
}
