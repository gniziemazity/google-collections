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
import java.util.Arrays;

/**
 * Predicates contains static methods for creating the standard set of
 * {@code Predicate} objects.
 *
 * <p>"Lispy, but good."
 *
 * <p>TODO: considering having these implement a {@code VisitablePredicate}
 * interface which specifies an {@code accept(PredicateVisitor)} method.
 *
 * @author Kevin Bourrillion
 */
public final class Predicates {
  private Predicates() {}

  /*
   * For constant Predicates a single instance will suffice; we'll cast it to
   * the right parameterized type on demand.
   */

  private static final Predicate<Object> ALWAYS_TRUE =
      new AlwaysTruePredicate();
  private static final Predicate<Object> ALWAYS_FALSE =
      new AlwaysFalsePredicate();
  private static final Predicate<Object> IS_NULL =
      new IsNullPredicate();

  /**
   * Returns a Predicate that always evaluates to true.
   */
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> alwaysTrue() {
    return (Predicate<T>) ALWAYS_TRUE;
  }

  /**
   * Returns a Predicate that always evaluates to false.
   */
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> alwaysFalse() {
    return (Predicate<T>) ALWAYS_FALSE;
  }

  /**
   * Returns a Predicate that evaluates to true if the object reference being
   * tested is null.
   */
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> isNull() {
    return (Predicate<T>) IS_NULL;
  }

  /**
   * Returns a Predicate that evaluates to true iff the given Predicate
   * evaluates to false.
   */
  public static <T> Predicate<T> not(Predicate<? super T> predicate) {
    checkNotNull(predicate);
    return new NotPredicate<T>(predicate);
  }

  /**
   * Returns a Predicate that evaluates to true iff each of its components
   * evaluates to true.  The components are evaluated in order, and evaluation
   * will be "short-circuited" as soon as the answer is determined.  Does not
   * defensively copy the iterable passed in, so future changes to it will alter
   * the behavior of this Predicate. If components is empty, the returned
   * Predicate will always evaluate to true.
   */
  public static <T> Predicate<T> and(
      Iterable<? extends Predicate<? super T>> components) {

    checkNotNull(components);
    return new AndPredicate<T>(components);
  }

  /**
   * Returns a Predicate that evaluates to true iff each of its components
   * evaluates to true.  The components are evaluated in order, and evaluation
   * will be "short-circuited" as soon as the answer is determined.  Does not
   * defensively copy the array passed in, so future changes to it will alter
   * the behavior of this Predicate. If components is empty, the returned
   * Predicate will always evaluate to true.
   */
  public static <T> Predicate<T> and(Predicate<? super T>... components) {
    checkNotNull(components);
    return and(Arrays.asList(components));
  }

  /**
   * Returns a Predicate that evaluates to true iff any one of its components
   * evaluates to true.  The components are evaluated in order, and evaluation
   * will be "short-circuited" as soon as the answer is determined.  Does not
   * defensively copy the iterable passed in, so future changes to it will alter
   * the behavior of this Predicate. If components is empty, the returned
   * Predicate will always evaluate to false.
   */
  public static <T> Predicate<T> or(
      Iterable<? extends Predicate<? super T>> components) {
    
    checkNotNull(components);
    return new OrPredicate<T>(components);
  }

  /**
   * Returns a Predicate that evaluates to true iff any one of its components
   * evaluates to true.  The components are evaluated in order, and evaluation
   * will be "short-circuited" as soon as the answer is determined.  Does not
   * defensively copy the array passed in, so future changes to it will alter
   * the behavior of this Predicate. If components is empty, the returned
   * Predicate will always evaluate to false.
   */
  public static <T> Predicate<T> or(Predicate<? super T>... components) {
    checkNotNull(components);
    return or(Arrays.asList(components));
  }

  /**
   * Returns a Predicate that evaluates to true iff the object being tested
   * equals() the given target or if both are null.
   */
  public static <T> Predicate<T> isEqualTo(@Nullable T target) {
    return (target == null)
        ? Predicates.<T>isNull()
        : new IsEqualToPredicate<T>(target);
  }

  /** @see Predicates#alwaysTrue */
  private static class AlwaysTruePredicate implements Predicate<Object>,
      Serializable {
    private static final long serialVersionUID = 8759914710239461322L;
    public boolean apply(Object o) {
      return true;
    }
  }

  /** @see Predicates#alwaysFalse */
  private static class AlwaysFalsePredicate implements Predicate<Object>,
      Serializable {
    private static final long serialVersionUID = -565481022115659695L;
    public boolean apply(Object o) {
      return false;
    }
  }

  /** @see Predicates#not */
  private static class NotPredicate<T> implements Predicate<T>, Serializable {
    private static final long serialVersionUID = -5113445916422049953L;
    private final Predicate<? super T> predicate;

    private NotPredicate(Predicate<? super T> predicate) {
      this.predicate = predicate;
    }
    public boolean apply(T t) {
      return !predicate.apply(t);
    }
  }

  /** @see Predicates#and(Iterable) */
  private static class AndPredicate<T> implements Predicate<T>, Serializable {
    private static final long serialVersionUID = 1022358602593297546L;
    private final Iterable<? extends Predicate<? super T>> components;

    private AndPredicate(Iterable<? extends Predicate<? super T>> components) {
      this.components = components;
    }
    public boolean apply(T t) {
      for (Predicate<? super T> predicate : components) {
        if (!predicate.apply(t)) {
          return false;
        }
      }
      return true;
    }
  }

  /** @see Predicates#or(Iterable) */
  private static class OrPredicate<T> implements Predicate<T>, Serializable {
    private static final long serialVersionUID = -7942366790698074803L;
    private final Iterable<? extends Predicate<? super T>> components;

    private OrPredicate(Iterable<? extends Predicate<? super T>> components) {
      this.components = components;
    }
    public boolean apply(T t) {
      for (Predicate<? super T> predicate : components) {
        if (predicate.apply(t)) {
          return true;
        }
      }
      return false;
    }
  }

  /** @see Predicates#isEqualTo */
  private static class IsEqualToPredicate<T> implements Predicate<T>,
      Serializable {
    private static final long serialVersionUID = 6457380537065200145L;
    private final T target;

    private IsEqualToPredicate(T target) {
      checkNotNull(target);
      this.target = target;
    }
    public boolean apply(T t) {
      return target.equals(t);
    }
  }

  /**
   * @see Predicates#isNull
   * @see Predicates#isEqualTo
   */
  private static class IsNullPredicate implements Predicate<Object>,
      Serializable {
    private static final long serialVersionUID = -2507344851931204908L;
    public boolean apply(Object o) {
      return o == null;
    }
  }
}
