/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.common.collect.testing;

import java.util.Collection;
import java.util.List;

/**
 * Reserializes the sets created by another test set generator.
 *
 * TODO: make CollectionTestSuiteBuilder test reserialized collections
 *
 * @author Jesse Wilson
 */
public class ReserializingTestCollectionGenerator<E>
    implements TestCollectionGenerator<E> {
  private final TestCollectionGenerator<E> delegate;

  ReserializingTestCollectionGenerator(TestCollectionGenerator<E> delegate) {
    this.delegate = delegate;
  }

  public static <E> ReserializingTestCollectionGenerator<E> newInstance(
      TestCollectionGenerator<E> delegate) {
    return new ReserializingTestCollectionGenerator<E>(delegate);
  }

  public Collection<E> create(Object... elements) {
    return Helpers.reserialize(delegate.create(elements));
  }

  public SampleElements<E> samples() {
    return delegate.samples();
  }

  public E[] createArray(int length) {
    return delegate.createArray(length);
  }

  public Iterable<E> order(List<E> insertionOrder) {
    return delegate.order(insertionOrder);
  }
}