// Copyright 2006 Google Inc. All Rights Reserved.

package com.google.common.collect;

import com.google.common.base.Nullable;
import com.google.common.base.Preconditions;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A convenient way to populate immutable Map instances, especially static-final
 * "constant Maps".  Code such as
 *
 * <pre>
 *   static final Map&lt;String,Integer> ENGLISH_TO_INTEGER_MAP
 *       = createNumbersMap();
 *
 *   static Map&lt;String,Integer> createNumbersMap() {
 *     Map&lt;String,Integer> map = Maps.newHashMap();
 *     map.put("one", 1);
 *     map.put("two", 2);
 *     map.put("three", 3);
 *     return Collections.unmodifiableMap(map);
 *   }
 * </pre>
 * ... can be rewritten far more simply as ...
 * <pre>
 *   static final Map&lt;String,Integer> ENGLISH_TO_INTEGER_MAP
 *     = new ImmutableMapBuilder&lt;String,Integer>()
 *       .put("one", 1)
 *       .put("two", 2)
 *       .put("three", 3)
 *       .getMap();
 * </pre>
 * (Actually, for <i>small</i> immutable Maps, you can use the
 * even-more-convenient {@link Maps#immutableMap} methods.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public class ImmutableMapBuilder<K,V> {

  private ImmutableHashMap<K,V> map;

  /**
   * @param map map to populate new builder with, cannot be null
   * @return new ImmutableMapBuilder populated with mappings from specified map
   */
  public static <K, V> ImmutableMapBuilder<K,V> fromMap(Map<K,V> map) {
    Preconditions.checkNotNull(map);
    final ImmutableMapBuilder<K, V> builder
        = new ImmutableMapBuilder<K, V>(map.size() * 3 / 2);

    for (final Map.Entry<K,V> entry : map.entrySet()) {
      builder.put(entry.getKey(), entry.getValue());
    }

    return builder;
  }

  /**
   * Creates a new ImmutableMapBuilder with an unspecified expected size.
   */
  public ImmutableMapBuilder() {
    this(8);
  }

  /**
   * Creates a new ImmutableMapBuilder with the given expected size.
   *
   * @param expectedSize the approximate number of key-value pairs you
   *     expect this map to contain
   */
  public ImmutableMapBuilder(int expectedSize) {
    map = new ImmutableHashMap<K,V>(expectedSize);
  }

  /**
   * Adds a key-value mapping to the map that will be returned by
   * {@link #getMap}.
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @throws IllegalStateException if {@link #getMap} has already been called
   * @return this map builder (to enable call chaining)
   */
  public ImmutableMapBuilder<K,V> put(@Nullable K key, @Nullable V value) {
    checkState(map != null, "map has already been created");
    map.secretPut(key, value);
    return this;
  }

  /**
   * Returns a newly-created, immutable HashMap instance containing the keys and
   * values that were specified using {@link #put}.
   *
   * @return a new, immutable HashMap instance
   * @throws IllegalStateException if {@link #getMap} has already been called
   */
  public Map<K,V> getMap() {
    checkState(map != null, "map has already been created");
    try {
      return map;
    } finally {
      map = null;
    }
  }

  private static class ImmutableHashMap<K,V> extends HashMap<K,V> {

    private static final long serialVersionUID = -5187626034923451074L;

    ImmutableHashMap(int expectedSize) {
      // avoid collisions by using 2-4x as many buckets as expected entries
      super(expectedSize * 2);
    }

    transient volatile Set<K> keySet;

    @Override public Set<K> keySet() {
      if (keySet == null) {
        keySet = Collections.unmodifiableSet(super.keySet());
      }
      return keySet;
    }

    transient volatile Collection<V> values;

    @Override public Collection<V> values() {
      if (values == null) {
        values = Collections.unmodifiableCollection(super.values());
      }
      return values;
    }

    transient volatile Set<Map.Entry<K,V>> entrySet;

    @Override public Set<Map.Entry<K, V>> entrySet() {
      if (entrySet == null) {
        entrySet = Maps.unmodifiableEntrySet(super.entrySet());
      }
      return entrySet;
    }

    transient Integer cachedHashCode;

    /*
     * It is very important that no one call hashCode() until after all the
     * calls to secretPut() are finished... luckily this is impossible, but be
     * very careful if you are changing this code.
     */

    @Override public int hashCode() {
      if (cachedHashCode == null) {
        cachedHashCode = super.hashCode();
      }
      return cachedHashCode;
    }

    private void secretPut(K key, V value) {
      super.put(key, value);
    }

    @Override public V put(K key, V value) {
      throw up();
    }
    @Override public void putAll(Map<? extends K, ? extends V> m) {
      throw up();
    }
    @Override public V remove(Object key) {
      throw up();
    }
    @Override public void clear() {
      throw up();
    }

    private static UnsupportedOperationException up() {
      return new UnsupportedOperationException();
    }
  }
}
