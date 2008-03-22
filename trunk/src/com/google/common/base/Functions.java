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
import java.io.Serializable;
import java.util.Map;

/**
 * Useful functions.
 *
 * @author Mike Bostock
 * @author Vlad Patryshev
 */
public final class Functions {

  private Functions() { }

  /**
   * See {@link #toStringFunction()}.
   *
   * TODO: Consider deprecating this in favor of an accessor method like
   * {@link #toStringFunction()}.
   */
  public static final Function<Object, String> TO_STRING =
      ToStringFunction.INSTANCE;

  /**
   * A function that returns {@link Object#toString} of its argument.
   * Note that this function is not {@literal @Nullable}: it will throw a
   * {@link NullPointerException} when applied to {@code null}.
   *
   * <p> Note also that this is assignable to variables of type
   * {@code Function<? super E, String>}, but not
   * {@code Function<E, String>}.
   */
  public static final Function<Object, String> toStringFunction() {
    return ToStringFunction.INSTANCE;
  }

  private static class ToStringFunction
      implements SerializableFunction<Object, String> {
    private static final long serialVersionUID = -2979910855853295325L;

    public String apply(Object o) {
      // avoiding String.valueOf(e) so we get an NPE instead of "null"
      return o.toString();
    }
    private Object readResolve() {
      return INSTANCE; /* Preserve singleton property. */
    }
    @Override public boolean equals(Object obj) {
      return obj == INSTANCE;
    }
    @Override public int hashCode() {
      return (int) serialVersionUID;
    }
    @Override public String toString() {
      return "toString";
    }
    private static final ToStringFunction INSTANCE = new ToStringFunction();
  }

  /**
   * @see #toHashCode()
   */
  private static class HashCodeFunction
      implements SerializableFunction<Object, Integer> {
    private static final long serialVersionUID = 749417670239189171L;

    public Integer apply(@Nullable Object o) {
      return (o == null) ? 0 : o.hashCode();
    }
    private Object readResolve() {
      return INSTANCE; /* Preserve singleton property. */
    }
    @Override public boolean equals(Object obj) {
      return obj == INSTANCE;
    }
    @Override public int hashCode() {
      return (int) serialVersionUID;
    }
    @Override public String toString() {
      return "hashCode";
    }
    static final HashCodeFunction INSTANCE = new HashCodeFunction();
  }

  /**
   * Returns a function that determines the {@link Object#hashCode()} of its
   * argument. For null arguments, this returns 0, as used in hash code
   * calculations by the Java Collections classes.
   */
  public static Function<Object, Integer> toHashCode() {
    return HashCodeFunction.INSTANCE;
  }

  /*
   * For constant Functions a single instance will suffice; we'll cast it to
   * the right parameterized type on demand.
   */

  /**
   * Returns the identity Function.
   */
  @SuppressWarnings("unchecked")
  public static <E> Function<E, E> identity() {
    return (Function<E, E>) IdentityFunction.INSTANCE;
  }

  /**
   * @see Functions#identity
   */
  private static class IdentityFunction
      implements SerializableFunction<Object, Object> {
    private static final long serialVersionUID = 3129841931134422007L;
    public Object apply(Object o) {
      return o;
    }
    private Object readResolve() {
      return INSTANCE;
    }
    @Override public boolean equals(Object obj) {
      return obj == INSTANCE;
    }
    @Override public int hashCode() {
      return (int) serialVersionUID;
    }
    @Override public String toString() {
      return "identity";
    }
    private static final IdentityFunction INSTANCE = new IdentityFunction();
  }

  /**
   * Returns a function which performs key-to-value lookup on {@code map}.
   *
   * <p>The difference between a map and a function is that a map is defined on
   * a set of keys, while a function is defined on a type.
   * The function built by this method returns {@code null}
   * for all arguments that do not belong to the map's keyset.
   *
   * @param map Map&lt;A,B> source map
   * @return Function&lt;A,B> function that returns map.get(a) for each A a
   */
  public static <A, B> Function<A, B> forMap(
      final Map<? super A, ? extends B> map) {
    return new FunctionForMapNoDefault<A, B>(map);
  }

  private static class FunctionForMapNoDefault<A, B>
      implements SerializableFunction<A, B> {
    private final Map<? super A, ? extends B> map;

    public FunctionForMapNoDefault(
        Map<? super A, ? extends B> map) {
      this.map = checkNotNull(map);
    }
    public B apply(A a) {
      return map.get(a);
    }
    @Override public boolean equals(Object o) {
      if (o instanceof FunctionForMapNoDefault) {
        FunctionForMapNoDefault<?, ?> that = (FunctionForMapNoDefault<?, ?>) o;
        return map.equals(that.map);
      }
      return false;
    }
    @Override public int hashCode() {
      return map.hashCode();
    }
    @Override public String toString() {
      return "forMap(" + map + ")";
    }
    private static final long serialVersionUID = 3270419028101751025L;
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
  public static <A, B> Function<A, B> forMap(
      final Map<? super A, ? extends B> map,
      @Nullable final B defaultValue) {
    if (defaultValue == null) {
      return forMap(map);
    }
    return new ForMapWithDefault<A, B>(map, defaultValue);
  }

  private static class ForMapWithDefault<A, B>
      implements SerializableFunction<A, B> {
    private static final long serialVersionUID = 1652422010500531299L;

    private final Map<? super A, ? extends B> map;
    private final B defaultValue;

    public ForMapWithDefault(Map<? super A, ? extends B> map, B defaultValue) {
      this.map = checkNotNull(map);
      this.defaultValue = checkNotNull(defaultValue);
    }
    public B apply(A a) {
      return map.containsKey(a) ? map.get(a) : defaultValue;
    }
    @Override public boolean equals(Object o) {
      if (o instanceof ForMapWithDefault) {
        ForMapWithDefault<?, ?> that = (ForMapWithDefault<?, ?>) o;
        return map.equals(that.map) && defaultValue.equals(that.defaultValue);
      }
      return false;
    }
    @Override public int hashCode() {
      return map.hashCode() + defaultValue.hashCode();
    }
    @Override public String toString() {
      return "forMap(" + map + ", defaultValue=" + defaultValue + ")";
    }

  }

  /**
   * Returns the composition of two functions. For {@code f: A->B} and
   * {@code g: B->C}, composition is defined as the function h such that
   * {@code h(x) == g(f(x))} for each {@code x}.
   *
   * @see <a href="//en.wikipedia.org/wiki/Function_composition">
   * function composition</a>
   *
   * @param g Function&lt;B,C>
   * @param f Function&lt;A,B>
   * @return Function&lt;A,C> composition of f and g
   */
  public static <A, B, C> Function<A, C> compose(
      final Function<? super B, ? extends C> g,
      final Function<? super A, ? extends B> f) {
    return new FunctionComposition<A, B, C>(g, f);
  }

  private static class FunctionComposition<A, B, C>
      implements SerializableFunction<A, C> {
    private final Function<? super B, ? extends C> g;
    private final Function<? super A, ? extends B> f;

    public FunctionComposition(Function<? super B, ? extends C> g,
        Function<? super A, ? extends B> f) {
      this.g = checkNotNull(g);
      this.f = checkNotNull(f);
    }
    public C apply(A a) {
      return g.apply(f.apply(a));
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof FunctionComposition) {
        FunctionComposition<?, ?, ?> that = (FunctionComposition<?, ?, ?>) obj;
        return f.equals(that.f) && g.equals(that.g);
      }
      return false;
    }

    @Override public int hashCode() {
      /*
       * TODO: To leave the door open for future enhancement, this
       * calculation should be coordinated with the hashCode() method of the
       * corresponding composition method in Predicates. To construct the
       * composition:
       *    predicate(function2(function1(x)))
       *
       * There are two different ways of composing it:
       *    compose(predicate, compose(function2, function1))
       *    compose(compose(predicate, function2), function1)
       *
       * It would be nice if these could be equal.
       */
      return f.hashCode() ^ g.hashCode();
    }
    @Override public String toString() {
      return g.toString() + "(" + f.toString() + ")";
    }
    private static final long serialVersionUID = 2530922454216511764L;
  }

  /**
   * Returns a boolean-valued function that evaluates to the same result as the
   * given predicate.
   */
  public static <T> Function<T, Boolean> forPredicate(
      Predicate<? super T> predicate) {
    checkNotNull(predicate);
    return new PredicateFunction<T>(predicate);
  }

  /** @see Functions#forPredicate */
  private static class PredicateFunction<T>
      implements SerializableFunction<T, Boolean> {
    private final Predicate<? super T> predicate;

    private PredicateFunction(Predicate<? super T> predicate) {
      this.predicate = checkNotNull(predicate);
    }

    public Boolean apply(T t) {
      return predicate.apply(t);
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof PredicateFunction) {
        PredicateFunction<?> that = (PredicateFunction<?>) obj;
        return predicate.equals(that.predicate);
      }
      return false;
    }
    @Override public int hashCode() {
      return predicate.hashCode();
    }
    @Override public String toString() {
      return "asFunction(" + predicate + ")";
    }
    private static final long serialVersionUID = 7159925838099303368L;
  }

  /**
   * Returns a {@link Function} that returns {@code value} for any input.
   *
   * @param value the constant value for the {@code Function} to return
   * @return a {@code Function} that always returns {@code value}.
   */
  public static <E> Function<Object, E> constant(@Nullable final E value) {
    return new ConstantFunction<E>(value);
  }

  private static class ConstantFunction<E>
      implements SerializableFunction<Object, E> {
    private static final long serialVersionUID = 2347134351918525179L;
    private final E value;

    public ConstantFunction(@Nullable E value) {
      this.value = value;
    }
    public E apply(Object from) {
      return value;
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof ConstantFunction) {
        ConstantFunction<?> that = (ConstantFunction<?>) obj;
        return Objects.equal(value, that.value);
      }
      return false;
    }
    @Override public int hashCode() {
      return (value == null) ? 0 : value.hashCode();
    }
    @Override public String toString() {
      return "constant(" + value + ")";
    }
  }

  private interface SerializableFunction<A, B>
      extends Function<A, B>, Serializable {}
}
