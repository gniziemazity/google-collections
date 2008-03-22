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

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Static utility methods pertaining to object arrays.
 *
 * @author Kevin Bourrillion
 */
public final class ObjectArrays {
  private ObjectArrays() {}

  /** An empty object array. */
  public static final Object[] EMPTY_ARRAY = new Object[0];

  /**
   * Returns a new array with the specified component type and length.
   *
   * @param type the component type
   * @param length the length of the new array
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] newArray(Class<T> type, int length) {
    return (T[]) Array.newInstance(type, length);
  }

  /**
   * Returns a new array with the same type as a reference array, and a given
   * length.
   *
   * @param reference any array of the desired type
   * @param length the length of the new array
   */
  public static <T> T[] newArray(T[] reference, int length) {
    Class<?> type = reference.getClass().getComponentType();

    // the cast is safe because result.getClass() == reference.getClass()
    @SuppressWarnings("unchecked")
    T[] result = (T[]) Array.newInstance(type, length);
    return result;
  }

  /**
   * Returns an empty (immutable) array with the same component type as the
   * specified array.
   *
   * @param array the array from which to infer the component type
   */
  public static <T> T[] emptyArray(T[] array) {
    return (array.length == 0) ? array : newArray(array, 0);
  }

  /**
   * Returns a new array of a specified type, containing the concatenated
   * contents of the two given arrays.
   *
   * @param first the first array of elements to concatenate
   * @param second the second array of elements to concatenate
   * @param type the component type of the returned concatenated array
   */
  public static <T> T[] concat(T[] first, T[] second, Class<T> type) {
    T[] result = newArray(type, first.length + second.length);
    System.arraycopy(first, 0, result, 0, first.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  /**
   * This method implements {@code Collection.toArray(Object[])} for Collection
   * implementations to share. Other code should never need to use it, as it can
   * call {@link Collection#toArray(Object[])}.
   *
   * @param self the collection for which to return an array of elements
   * @param array the array in which to place the collection elements
   */
  static <T> T[] toArrayImpl(Collection<?> self, T[] array) {
    int size = self.size();
    if (array.length < size) {
      @SuppressWarnings("unchecked")
      Class<? extends T> type
          = (Class<? extends T>) array.getClass().getComponentType();
      array = newArray(type, size);
    }
    fillArray(self, array);
    if (array.length > size) {
      array[size] = null;
    }
    return array;
  }

  /**
   * This method implements {@code Collection.toArray()} for Collection
   * implementations to share. Other code should never need to use it, as it can
   * call {@link Collection#toArray()}.
   *
   * @param self the collection for which to return an array of elements
   */
  static Object[] toArrayImpl(Collection<?> self) {
    return fillArray(self, new Object[self.size()]);
  }

  private static Object[] fillArray(Iterable<?> elements, Object[] array) {
    int i = 0;
    for (Object element : elements) {
      array[i++] = element;
    }
    return array;
  }
}
