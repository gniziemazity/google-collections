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

package com.google.common.base;

import java.io.Serializable;

/**
 * Useful {@code Supplier}s
 *
 * @author Laurence Gonsalves
 * @author Harry Heymann
 */
public final class Suppliers {
  private Suppliers() {}

  /**
   * Returns a new {@code Supplier} which is the composition of the {@code
   * Function function} and the {@code Supplier first}. In other words, this
   * new {@code Supplier}'s value will be computed by retrieving the value from
   * {@code first}, and then applying {@code function} to that value. Note that
   * the resulting {@code Supplier} will not force {@code first} (or
   * invoke {@code function}) until it is, itself, forced.
   */
  public static <K, V> Supplier<V> compose(final Function<K, V> function,
                                           final Supplier<? extends K> first) {
    return new SupplierComposition<K, V>(function, first);
  }
  
  private static class SupplierComposition<K, V>
      implements SerializableSupplier<V> {
    private final Function<K, V> function;
    private final Supplier<? extends K> first;
    
    public SupplierComposition(Function<K, V> function,
        Supplier<? extends K> first) {
      this.function = function;
      this.first = first;
    }
    public V get() {
      return function.apply(first.get());
    }
    private static final long serialVersionUID = 239402700273621706L;
  }

  /**
   * Returns a {@code Supplier} which delegates to the given {@code Supplier}
   * on the first call to get(), records the value returned, and returns this
   * recorded value on all subsequent calls to get(). See:
   * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
   *
   * The returned {@code Supplier} will throw {@code CyclicDependencyException}
   * if the call to get() tries to get its own value.
   *
   * The returned supplier is <em>not</em> MT-safe.
   */
  public static <T> Supplier<T> memoize(final Supplier<T> delegate) {
    return new MemoizingSupplier<T>(delegate);
  }

  private static class MemoizingSupplier<T>
      implements SerializableSupplier<T> {
    private final Supplier<T> delegate;
    private MemoizationState state = MemoizationState.NOT_YET;
    private T value;

    public MemoizingSupplier(Supplier<T> delegate) {
      this.delegate = delegate;
    }
    public T get() {
      switch (state) {
        case NOT_YET:
          state = MemoizationState.COMPUTING;
          try {
            value = delegate.get();
          } finally {
            state = MemoizationState.NOT_YET;
          }
          state = MemoizationState.DONE;
          break;
        case COMPUTING:
          throw new CyclicDependencyException();
      }
      return value;
    }
    private static final long serialVersionUID = 1138306392412025275L;
  }
  
  private enum MemoizationState { NOT_YET, COMPUTING, DONE }

  /**
   * Exception thrown when a memoizing {@code Supplier} tries to get its
   * own value.
   */
  public static class CyclicDependencyException extends RuntimeException {
    private static final long serialVersionUID = -1;

    public CyclicDependencyException() {
      super("Cycle detected when forcing a memoizing supplier.");
    }
  }

  /**
   * Returns a {@code Supplier} that always supplies {@code instance}.
   */
  public static <T> Supplier<T> ofInstance(@Nullable final T instance) {
    return new SupplierOfInstance<T>(instance);
  }
  
  private static class SupplierOfInstance<T>
      implements SerializableSupplier<T> {
    private final T instance;
    
    public SupplierOfInstance(T instance) {
      this.instance = instance;
    }
    public T get() {
      return instance;
    }
    private static final long serialVersionUID = 1052627637788228454L;
  }
  
  private interface SerializableSupplier<T>
      extends Supplier<T>, Serializable {}
}
