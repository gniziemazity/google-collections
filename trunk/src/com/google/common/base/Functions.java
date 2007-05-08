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

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Map;

/**
 * Useful {@link Function}s.
 *
 * @author mbostock
 * @author vpatryshev
 */
public final class Functions {

  private Functions() { }

  /**
   * A function that returns {@link Object#toString} of its argument.
   * Note that this function is not {@literal @Nullable}: it will throw a
   * {@link NullPointerException} when applied to {@code null}.
   *
   * <p> Note also that this is assignable to variables of type
   * {@code Function<? super E, String>}, but not
   * {@code Function<E, String>}.
   */
  public static final Function<Object, String> TO_STRING =
      new Function<Object, String>() {
        public String apply(Object o) {

          // avoiding String.valueOf(e) so we get an NPE instead of "null"
          return o.toString();
        }
      };

  /*
   * For constant Functions a single instance will suffice; we'll cast it to
   * the right parameterized type on demand.
   */

  private static final Function<?,?> IDENTITY = new IdentityFunction();

  /**
   * Returns the identity Function.
   */
  @SuppressWarnings("unchecked")
  public static <E> Function<E,E> identity() {
    return (Function<E,E>) IDENTITY;
  }

  /**
   * @see Functions#identity
   */
  private static class IdentityFunction<E> implements Function<E,E> {
    /* TODO(mbostock): serialVersionUID? */
    public E apply(E e) {
      return e;
    }
  }

  /**
   * Returns a function which performs key-to-value lookup on {@code map}.
   *
   * The difference between a map and a function is that a map is defined on
   * a set of keys, while a function is defined on a type.
   * The function built by this method returns {@code null}
   * for all its arguments that do not belong to the map's keyset.
   *
   * @param map Map&lt;A,B> source map
   * @return Function&lt;A,B> function that returns map.get(a) for each A a
   */
  public static <A,B> Function<A,B> forMap(final Map<A,B> map) {
    checkNotNull(map);
    return new Function<A,B>() {
      public B apply(A a) {
        return map.get(a);
      }
    };
  }

  /**
   * Returns a function which performs key-to-value lookup on {@code map}.
   * The function built by this method returns {@code defaultValue}
   * for all its arguments that do not belong to the map's keyset.
   *
   * @param map Map&lt;A,B>
   * @param defaultValue B
   * @return Function {@code f} such that {@code f(a)=map.get(a)}
   * if {@code map.containsKey(x)}, and {@code defaultValue} otherwise.
   *
   * @see #forMap(Map)
   */
  public static <A,B> Function<A,B>
      forMap(final Map<A,? extends B> map, @Nullable final B defaultValue) {
    checkNotNull(map);
    return new Function<A,B>() {
      public B apply(A a) {
        return map.containsKey(a) ? map.get(a) : defaultValue;
      }
    };
  }

  /**
   * Returns a compostion {@code g<sup>0</sup>f : A->C} of two functions,
   * {@code f: A->B} and {@code g: B->C}.
   * Compostion is defined as a function h such that
   * h(x) = g(f(x)) for each x.
   *
   * @see <a href="//en.wikipedia.org/wiki/Function_composition">
   * function composition</a>
   *
   * @param g Function&lt;B,C>
   * @param f Function&lt;A,B>
   * @return Function&lt;A,C> composition of f and g
   */
  public static <A,B,C> Function<A,C> compose(final Function<B,C> g,
                                              final Function<A,? extends B> f) {
    checkNotNull(g);
    checkNotNull(f);
    return new Function<A,C> () {
      public C apply(A a) {
        return g.apply(f.apply(a));
      }
    };
  }

}
