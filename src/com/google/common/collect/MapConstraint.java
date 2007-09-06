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

import com.google.common.base.Nullable;

/**
 * Interface for defining a constraint on the types of keys and values that are
 * allowed to be added to a {@code Map} or {@code Multimap}. For example, to
 * enforce that a map contains no null keys or values, you might say:
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
 * @author Mike Bostock
 * @see MapConstraints
 * @see Constraint
 */
public interface MapConstraint<K, V> {
  /**
   * Implement this method to throw a suitable {@code RuntimeException} if the
   * specified key or value is illegal. Typically this is either a {@link
   * NullPointerException}, an {@link IllegalArgumentException}, or a {@link
   * ClassCastException}, though a more application-specific exception class may
   * be used as appropriate.
   */
  void checkKeyValue(@Nullable K key, @Nullable V value);
}
