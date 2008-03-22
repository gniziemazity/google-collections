/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.base;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkContentsNotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Contains static methods for creating the standard set of {@code Predicate}
 * objects.
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

  /**
   * Returns a predicate that always evaluates to {@code true}.
   */
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> alwaysTrue() {
    return (Predicate<T>) AlwaysTruePredicate.INSTANCE;
  }

  /**
   * Returns a predicate that always evaluates to {@code false}.
   */
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> alwaysFalse() {
    return (Predicate<T>) AlwaysFalsePredicate.INSTANCE;
  }

  /**
   * Returns a predicate that evaluates to true if the object reference being
   * tested is null.
   */
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> isNull() {
    return (Predicate<T>) IsNullPredicate.INSTANCE;
  }

  /**
   * Returns a predicate that evaluates to true if the object reference being
   * tested is not null.
   */
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> notNull() {
    return (Predicate<T>) NotNullPredicate.INSTANCE;
  }

  /**
   * Returns a predicate that evaluates to true iff the given predicate
   * evaluates to {@code false}.
   */
  public static <T> Predicate<T> not(Predicate<? super T> predicate) {
    return new NotPredicate<T>(predicate);
  }

  /**
   * Returns a predicate that evaluates to {@code true} iff each of its
   * components evaluates to {@code true}. The components are evaluated in
   * order, and evaluation will be "short-circuited" as soon as the answer is
   * determined. Does not defensively copy the iterable passed in, so future
   * changes to it will alter the behavior of this predicate. If
   * {@code components} is empty, the returned predicate will always evaluate to
   * {@code true}.
   */
  public static <T> Predicate<T> and(
      Iterable<? extends Predicate<? super T>> components) {
    return new AndPredicate<T>(components);
  }

  /**
   * Returns a predicate that evaluates to {@code true} iff each of its
   * components evaluates to {@code true}. The components are evaluated in
   * order, and evaluation will be "short-circuited" as soon as the answer is
   * determined. Does not defensively copy the array passed in, so future
   * changes to it will alter the behavior of this predicate. If
   * {@code components} is empty, the returned predicate will always evaluate to
   * {@code true}.
   */
  public static <T> Predicate<T> and(Predicate<? super T>... components) {
    return and(Arrays.asList(components));
  }

  /**
   * Returns a predicate that evaluates to {@code true} iff both of its
   * components evaluate to {@code true}. The components are evaluated in
   * order, and evaluation will be "short-circuited" as soon as the answer is
   * determined.
   */
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> and(Predicate<? super T> first,
      Predicate<? super T> second) {
    return and(Arrays.<Predicate<? super T>> asList(first, second));
  }

  /**
   * Returns a predicate that evaluates to {@code true} iff any one of its
   * components evaluates to {@code true}. The components are evaluated in
   * order, and evaluation will be "short-circuited" as soon as the answer is
   * determined. Does not defensively copy the iterable passed in, so future
   * changes to it will alter the behavior of this predicate. If
   * {@code components} is empty, the returned predicate will always evaluate to
   * {@code false}.
   */
  public static <T> Predicate<T> or(
      Iterable<? extends Predicate<? super T>> components) {
    return new OrPredicate<T>(components);
  }

  /**
   * Returns a predicate that evaluates to true iff any one of its components
   * evaluates to true. The components are evaluated in order, and evaluation
   * will be "short-circuited" as soon as the answer is determined. Does not
   * defensively copy the array passed in, so future changes to it will alter
   * the behavior of this predicate. If components is empty, the returned
   * predicate will always evaluate to false.
   */
  public static <T> Predicate<T> or(Predicate<? super T>... components) {
    return or(Arrays.asList(components));
  }

  /**
   * Returns a predicate that evaluates to {@code true} iff either of its
   * components evaluates to {@code true}. The components are evaluated in
   * order, and evaluation will be "short-circuited" as soon as the answer is
   * determined.
   */
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> or(Predicate<? super T> first,
      Predicate<? super T> second) {
    return or(Arrays.<Predicate<? super T>> asList(first, second));
  }

  /**
   * Returns a predicate that evaluates to {@code true} iff the object being
   * tested {@code equals()} the given target or if both are null.
   *
   * TODO: Change signature to return Predicate&lt;Object&gt;
   */
  public static <T> Predicate<T> isEqualTo(@Nullable T target) {
    return (target == null)
        ? Predicates.<T> isNull()
        : new IsEqualToPredicate<T>(target);
  }

  /**
   * Returns a predicate that evaluates to {@code true} iff the object being
   * tested refers to the same object as the given target.
   */
  public static Predicate<Object> isSameAs(@Nullable Object target) {
    return (target == null)
        ? Predicates.isNull()
        : new IsSameAsPredicate(target);
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the object reference
   * being tested is a member of the given collection. Does not defensively copy
   * the collection passed in, so future changes to it will alter the behavior
   * of the predicate.
   *
   * @param target the collection to test against
   *
   * <p>TODO: Consider changing signature to {@code in(Collection<?> target)}.
   */
  public static <T> Predicate<T> in(Collection<T> target) {
    return new InPredicate<T>(target);
  }

  /**
   * Returns the composition of a function and a predicate. For every {@code x},
   * the generated predicate returns {@code predicate(function(x))}.
   *
   * @return the composition of the provided function and predicate.
   *
   * @see Functions#compose(Function, Function)
   */
  public static <A, B> Predicate<A> compose(
      Predicate<? super B> predicate,
      Function<? super A, ? extends B> function) {
    return new CompositionPredicate<A, B>(predicate, function);
  }

  /** @see Predicates#alwaysTrue() */
  private static class AlwaysTruePredicate
      implements Predicate<Object>, Serializable {
    private static final long serialVersionUID = 8759914710239461322L;
    public boolean apply(Object o) {
      return true;
    }
    private Object readResolve() {
      return INSTANCE; /* Preserve singleton property. */
    }
    @Override public int hashCode() {
      return -1; /* All bits on */
    }
    @Override public boolean equals(Object obj) {
      return obj == INSTANCE;
    }
    @Override public String toString() {
      return "AlwaysTrue";
    }
    static final AlwaysTruePredicate INSTANCE = new AlwaysTruePredicate();
  }

  /** @see Predicates#alwaysFalse() */
  private static class AlwaysFalsePredicate
      implements Predicate<Object>, Serializable {
    private static final long serialVersionUID = -565481022115659695L;
    public boolean apply(Object o) {
      return false;
    }
    private Object readResolve() {
      return INSTANCE; /* Preserve singleton property. */
    }
    @Override public int hashCode() {
      return 0; /* All bits off */
    }
    @Override public boolean equals(Object obj) {
      return obj == INSTANCE;
    }
    @Override public String toString() {
      return "AlwaysFalse";
    }
    static final AlwaysFalsePredicate INSTANCE = new AlwaysFalsePredicate();
  }

  /** @see Predicates#not(Predicate) */
  private static class NotPredicate<T>
      implements Predicate<T>, Serializable {
    private static final long serialVersionUID = -5113445916422049953L;
    private final Predicate<? super T> predicate;

    private NotPredicate(Predicate<? super T> predicate) {
      this.predicate = checkNotNull(predicate);
    }
    public boolean apply(T t) {
      return !predicate.apply(t);
    }
    @Override public int hashCode() {
      return ~predicate.hashCode(); /* Invert all bits. */
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof NotPredicate<?>) {
        NotPredicate<?> that = (NotPredicate<?>) obj;
        return predicate.equals(that.predicate);
      }
      return false;
    }
    @Override public String toString() {
      return "Not(" + predicate.toString() + ")";
    }
  }

  /** @see Predicates#and(Iterable) */
  private static class AndPredicate<T>
      implements Predicate<T>, Serializable {
    private static final long serialVersionUID = 1022358602593297546L;
    private final Iterable<? extends Predicate<? super T>> components;

    private AndPredicate(Iterable<? extends Predicate<? super T>> components) {
      this.components = checkContentsNotNull(components);
    }
    public boolean apply(T t) {
      for (Predicate<? super T> predicate : components) {
        if (!predicate.apply(t)) {
          return false;
        }
      }
      return true;
    }
    @Override public int hashCode() {
      int result = -1; /* Start with all bits on. */
      for (Predicate<? super T> predicate : components) {
        result &= predicate.hashCode();
      }
      return result;
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof AndPredicate<?>) {
        AndPredicate<?> that = (AndPredicate<?>) obj;
        return iterableElementsEqual(components, that.components);
      }
      return false;
    }
    @Override public String toString() {
      return "And(" + Join.join(",", components) + ")";
    }
  }

  /** @see Predicates#or(Iterable) */
  private static class OrPredicate<T>
      implements Predicate<T>, Serializable {
    private static final long serialVersionUID = -7942366790698074803L;
    private final Iterable<? extends Predicate<? super T>> components;

    private OrPredicate(Iterable<? extends Predicate<? super T>> components) {
      this.components = checkContentsNotNull(components);
    }
    public boolean apply(T t) {
      for (Predicate<? super T> predicate : components) {
        if (predicate.apply(t)) {
          return true;
        }
      }
      return false;
    }
    @Override public int hashCode() {
      int result = 0; /* Start with all bits off. */
      for (Predicate<? super T> predicate : components) {
        result |= predicate.hashCode();
      }
      return result;
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof OrPredicate<?>) {
        OrPredicate<?> that = (OrPredicate<?>) obj;
        return iterableElementsEqual(components, that.components);
      }
      return false;
    }
    @Override public String toString() {
      return "Or(" + Join.join(",", components) + ")";
    }
  }

  /** @see Predicates#isEqualTo(Object) */
  private static class IsEqualToPredicate<T>
      implements Predicate<T>, Serializable {
    private static final long serialVersionUID = 6457380537065200145L;
    private final T target;

    private IsEqualToPredicate(T target) {
      this.target = checkNotNull(target);
    }
    public boolean apply(T t) {
      return target.equals(t);
    }
    @Override public int hashCode() {
      return target.hashCode();
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof IsEqualToPredicate) {
        IsEqualToPredicate<?> that = (IsEqualToPredicate<?>) obj;
        return target.equals(that.target);
      }
      return false;
    }
    @Override public String toString() {
      return "IsEqualTo(" + target + ")";
    }
  }

  /** @see Predicates#isSameAs(Object) */
  private static class IsSameAsPredicate
      implements Predicate<Object>, Serializable {
    private static final long serialVersionUID = -6693499628919249233L;
    private final Object target;

    private IsSameAsPredicate(Object target) {
      this.target = checkNotNull(target);
    }
    public boolean apply(Object o) {
      return target == o;
    }
    @Override public int hashCode() {
      return target.hashCode();
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof IsSameAsPredicate) {
        IsSameAsPredicate that = (IsSameAsPredicate) obj;
        return target == that.target;
      }
      return false;
    }
    @Override public String toString() {
      return "IsSameAs(" + target + ")";
    }
  }

  /**
   * @see Predicates#isNull()
   */
  private static class IsNullPredicate
      implements Predicate<Object>, Serializable {
    private static final long serialVersionUID = -2507344851931204908L;
    public boolean apply(Object o) {
      return o == null;
    }
    private Object readResolve() {
      return INSTANCE; /* Preserve singleton property. */
    }
    @Override public int hashCode() {
      return (int) serialVersionUID; /* Arbitrary non-(0 or -1) hash code. */
    }
    @Override public boolean equals(Object obj) {
      return obj == INSTANCE;
    }
    @Override public String toString() {
      return "IsNull";
    }
    static final IsNullPredicate INSTANCE = new IsNullPredicate();
  }

  /**
   * @see Predicates#notNull()
   */
  private static class NotNullPredicate
      implements Predicate<Object>, Serializable {
    private static final long serialVersionUID = -1450999195742675143L;
    public boolean apply(Object o) {
      return o != null;
    }
    private Object readResolve() {
      return INSTANCE; /* Preserve singleton property. */
    }
    @Override public int hashCode() {
      return ~((int) IsNullPredicate.serialVersionUID);
    }
    @Override public boolean equals(Object obj) {
      return obj == INSTANCE;
    }
    @Override public String toString() {
      return "NotNull";
    }
    static final NotNullPredicate INSTANCE = new NotNullPredicate();
  }

  /**
   * @see Predicates#in(Collection)
   */
  private static class InPredicate<T>
      implements Predicate<T>, Serializable {
    private static final long serialVersionUID = 8423798306294600396L;

    private final Collection<T> target;

    private InPredicate(Collection<T> target) {
      this.target = checkNotNull(target);
    }

    public boolean apply(T t) {
      boolean result = false;
      try {
        result = target.contains(t);
      } catch (NullPointerException e) {
        result = false;
      } catch (ClassCastException e) {
        result = false;
      }
      return result;
    }
    @Override public boolean equals(Object obj) {
      if (obj instanceof InPredicate<?>) {
        InPredicate<?> that = (InPredicate<?>) obj;
        return target.equals(that.target);
      }
      return false;
    }
    @Override
    public int hashCode() {
      return target.hashCode();
    }
    @Override
    public String toString() {
      return "In(" + target + ")";
    }
  }

  /**
   * @see Predicates#compose(Predicate, Function)
   */
  private static class CompositionPredicate<A, B>
      implements Predicate<A>, Serializable {
    private static final long serialVersionUID = -6029206722887771572L;

    final Predicate<? super B> p;
    final Function<? super A, ? extends B> f;

    private CompositionPredicate(
        Predicate<? super B> p, Function<? super A, ? extends B> f) {
      this.p = checkNotNull(p);
      this.f = checkNotNull(f);
    }

    public boolean apply(A a) {
      return p.apply(f.apply(a));
    }

    @Override public boolean equals(Object obj) {
      if (obj instanceof CompositionPredicate<?, ?>) {
        CompositionPredicate<?, ?> that = (CompositionPredicate<?, ?>) obj;
        return f.equals(that.f) && p.equals(that.p);
      }
      return false;
    }

    @Override public int hashCode() {
      /*
       * TODO:  To leave the door open for future enhancement, this
       * calculation should be coordinated with the hashCode() method of the
       * corresponding composition method in Functions.  To construct the
       * composition:
       *    predicate(function2(function1(x)))
       *
       * There are two different ways of composing it:
       *    compose(predicate, compose(function2, function1))
       *    compose(compose(predicate, function2), function1)
       *
       * It would be nice if these could be equal.
       */
      return f.hashCode() ^ p.hashCode();
    }

    @Override public String toString() {
      return p.toString() + "(" + f.toString() + ")";
    }
  }

  /**
   * Determines whether the two Iterables contain equal elements. More
   * specifically, this method returns {@code true} if {@code iterable1} and
   * {@code iterable2} contain the same number of elements and every element of
   * {@code iterable1} is equal to the corresponding element of {@code
   * iterable2}.
   *
   * <p>This is not a general-purpose method; it assumes that the iterations
   * contain no {@code null} elements.
   */
  private static boolean iterableElementsEqual(
      Iterable<?> iterable1, Iterable<?> iterable2) {
    Iterator<?> iterator1 = iterable1.iterator();
    Iterator<?> iterator2 = iterable2.iterator();
    while (iterator1.hasNext()) {
      if (!iterator2.hasNext()) {
        return false;
      }
      if (!iterator1.next().equals(iterator2.next())) {
        return false;
      }
    }
    return !iterator2.hasNext();
  }
}
