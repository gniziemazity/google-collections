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

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ListIterator;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Helper class for testing {@code ListIterator} implementations.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class ListIteratorTester<E> {
  protected final ListIterator<E> iterator;

  protected ListIteratorTester(ListIterator<E> iterator) {
    checkNotNull(iterator);
    this.iterator = iterator;
  }

  public static <E> ListIteratorTester<E> of(ListIterator<E> iterator) {
    return new ListIteratorTester<E>(iterator);
  }

  public void checkNext(E... elements) {
    int nextIndex = iterator.nextIndex();
    for (E element : elements) {
      assertTrue(iterator.hasNext());
      assertEquals(element, iterator.next());
      assertEquals(nextIndex + 1, iterator.nextIndex());
      assertEquals(nextIndex, iterator.previousIndex());
      assertTrue(iterator.hasPrevious());
      nextIndex++;
    }
  }

  public void checkPrevious(E... elements) {
    int previousIndex = iterator.previousIndex();
    for (E element : elements) {
      assertTrue(iterator.hasPrevious());
      assertEquals(element, iterator.previous());
      assertEquals(previousIndex, iterator.nextIndex());
      assertEquals(previousIndex - 1, iterator.previousIndex());
      assertTrue(iterator.hasNext());
      previousIndex--;
    }
  }

  public void checkIndex(int nextIndex, int size) {
    assertEquals(nextIndex, iterator.nextIndex());
    assertEquals(nextIndex - 1, iterator.previousIndex());
    assertEquals(nextIndex < size, iterator.hasNext());
    assertEquals(nextIndex > 0, iterator.hasPrevious());
  }
}
