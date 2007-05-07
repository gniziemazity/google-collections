// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

/**
 * Interface for defining a constraint on the types of keys and values that are
 * allowed to be added to a {@link Map} or {@link Multimap}. For example, to
 * enforce that a map contains no {@code null} keys or values, you might say:
 *
 * <pre>  public void checkKeyValue(Object key, Object value) {
 *    if (key == null) {
 *      throw new NullPointerException();
 *    }
 *    if (value == null) {
 *      throw new NullPointerException();
 *    }
 *  }</pre>
 *
 * Then use {@link MapConstraints#constrainedMap} to enforce the constraint.
 * This example is contrived; to check for {@code null} use {@link
 * MapConstraints#NOT_NULL}.
 *
 * <p>See {@link Constraint} for an important comment regarding determinism,
 * thread-safety and mutability when implementing constraints.
 *
 * @see MapConstraints
 * @see Constraint
 * @author mbostock@google.com (Mike Bostock)
 */
public interface MapConstraint<K, V> {

  /**
   * Implement this method to throw a suitable {@link RuntimeException} if the
   * specified key or value is illegal. Typically this is either a {@link
   * NullPointerException}, an {@link IllegalArgumentException}, or a {@link
   * ClassCastException}, though a more application-specific exception class may
   * be used as appropriate.
   */
  void checkKeyValue(K key, V value);
}
