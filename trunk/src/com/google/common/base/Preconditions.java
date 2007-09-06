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

/**
 * Contains static methods that can be called at the start of your methods
 * to verify correct arguments and state.  Note that the standard Java idiom
 * is to use the following directly:
 * <pre>
 *   if (!<i>argumentAssumption</i>) {
 *     throw new IllegalArgumentException();
 *   }
 *   if (!<i>stateAssumption</i>) {
 *     throw new IllegalStateException();
 *   }
 *   if (<i>requiredArgument</i> == null) {
 *     throw new NullPointerException();
 *   }
 * </pre>
 * It is perfectly acceptable to stick to the standard idiom. There are two
 * primary reasons you might prefer to use this class instead. First, it is
 * significantly more compact (especially if you use static import, but even if
 * you don't).
 * <p>
 * Moreover, Preconditions goes to a little extra effort to highlight for you
 * what it thinks (heuristically) are the two key frames of the stack trace.
 * First is the line that identifies which precondition check failed.  Second
 * is the line of the most likely "offender", which is defined as the first
 * frame in the stack trace that comes from a different class from the class
 * checking the precondition.  Example message:
 * <pre>
 * java.lang.IllegalArgumentException: precondition failed: (your message here)
 *     failed check:   at somepackage.SomeClass.someMethod(SomeClass.java:101)
 *     offending call: at otherpackage.Caller.callingMethod(Caller.java:99)
 * </pre>
 * (Again, the above is only the exception <i>message</i>, and the full stack
 * trace is kept intact.)
 *
 * @author Kevin Bourrillion
 */
public final class Preconditions {
  private Preconditions() {}

  /**
   * Ensures that {@code expression} is {@code true}.
   *
   * @param expression any boolean expression involving an argument to the
   *     current method
   * @throws IllegalArgumentException if {@code expression} is {@code false}
   */
  public static void checkArgument(boolean expression) {
    if (!expression) {
      failArgument(null);
    }
  }

  /**
   * Ensures that {@code expression} is {@code true}.
   *
   * @param expression any boolean expression involving the state of the
   *     current instance (and not involving arguments)
   * @throws IllegalStateException if {@code expression} is {@code false}
   */
  public static void checkState(boolean expression) {
    if (!expression) {
      failState(null);
    }
  }

  /**
   * Ensures that {@code reference} is not {@code null}.
   *
   * @param reference an object reference that was passed as a parameter to the
   *     current method
   * @throws NullPointerException if {@code reference} is {@code null}
   */
  public static void checkNotNull(Object reference) {
    if (reference == null) {
      failNotNull(null);
    }
  }

  /**
   * Ensures that {@code expression} is {@code true}.
   *
   * @param expression any boolean expression involving an argument to the
   *     current method
   * @param message a message object which will be converted using
   *     {@code Object#toString} and included in the exception message if the
   *     check fails
   * @throws IllegalArgumentException if {@code expression} is {@code false}
   */
  public static void checkArgument(boolean expression, Object message) {
    if (!expression) {
      failArgument(message);
    }
  }
  
  /**
   * Ensures that {@code expression} is {@code true}.
   *
   * @param expression any boolean expression involving the state of the
   *     current instance (and not involving arguments)
   * @param message a message object which will be converted using
   *     {@code Object#toString} and included in the exception message if the
   *     check fails
   * @throws IllegalStateException if {@code expression} is {@code false}
   */
  public static void checkState(boolean expression, Object message) {
    if (!expression) {
      failState(message);
    }
  }

  /**
   * Ensures that {@code reference} is not {@code null}.
   *
   * @param reference an object reference that was passed as a parameter to the
   *     current method
   * @param message a message object which will be converted using
   *     {@code Object#toString} and included in the exception message if the
   *     check fails
   * @throws NullPointerException if {@code reference} is {@code null}
   */
  public static void checkNotNull(Object reference, Object message) {
    if (reference == null) {
      failNotNull(message);
    }
  }

  /**
   * Ensures that {@code expression} is {@code true}.
   *
   * @param expression any boolean expression involving an argument to the
   *     current method
   * @param errorFormat format of error message to produce if the check fails
   * @param args the arguments for {@code errorFormat}
   * @throws IllegalArgumentException if {@code expression} is {@code false}
   * 
   * @see <a href=
   *   "http://java.sun.com/javase/6/docs/api/java/util/Formatter.html#syntax">
   *   Format string syntax</a>
   */
  public static void checkArgument(
      boolean expression, String errorFormat, Object... args) {
    if (!expression) {
      failArgument(String.format(errorFormat, args));
    }
  }
  
  /**
   * Ensures that {@code expression} is {@code true}.
   *
   * @param expression any boolean expression involving the state of the
   *     current instance (and not involving arguments)
   * @param errorFormat format of error message to produce if the check fails
   * @param args the arguments for {@code errorFormat}
   * @throws IllegalStateException if {@code errorFormat} is {@code false}
   * 
   * @see <a href=
   *   "http://java.sun.com/javase/6/docs/api/java/util/Formatter.html#syntax">
   *   Format string syntax</a>
   */
  public static void checkState(
      boolean expression, String errorFormat, Object... args) {
    if (!expression) {
      failState(String.format(errorFormat, args));
    }
  }
  
  /**
   * Ensures that {@code reference} is not {@code null}.
   *
   * @param reference an object reference that was passed as a parameter to the
   *     current method
   * @param errorFormat format of error message to produce if the check fails
   * @param args the arguments for {@code errorFormat}
   * @throws NullPointerException if {@code reference} is {@code null}
   * 
   * @see <a href=
   *   "http://java.sun.com/javase/6/docs/api/java/util/Formatter.html#syntax">
   *   Format string syntax</a>
   */
  public static void checkNotNull(
      Object reference, String errorFormat, Object... args) {
    if (reference == null) {
      failNotNull(String.format(errorFormat, args));
    }
  }

  private static void failArgument(final Object description) {
    throw new IllegalArgumentException() {
      private static final long serialVersionUID = 1L;
      @Override public String getMessage() {
        return buildMessage(this, description);
      }
      @Override public String toString() {
        return buildString(this);
      }
    };
  }

  private static void failState(final Object description) {
    throw new IllegalStateException() {
      private static final long serialVersionUID = 1L;
      @Override public String getMessage() {
        return buildMessage(this, description);
      }
      @Override public String toString() {
        return buildString(this);
      }
    };
  }

  private static void failNotNull(final Object description) {
    throw new NullPointerException() {
      private static final long serialVersionUID = 1L;
      @Override public String getMessage() {
        return buildMessage(this, description);
      }
      @Override public String toString() {
        return buildString(this);
      }
    };
  }

  private static final int STACK_INDEX = 2;
  private static final String NL = System.getProperty("line.separator");

  private static String buildMessage(RuntimeException e, Object description) {
    StringBuilder sb = new StringBuilder(300).append("precondition failed");
    StackTraceElement[] trace = e.getStackTrace();
    StackTraceElement failedAt = trace[STACK_INDEX];

    if (description != null) {
      sb.append(": ").append(description);
    }
    sb.append(NL).append("    failed check:   at ").append(failedAt);

    for (int i = STACK_INDEX + 1; i < trace.length; i++) {
      if (!trace[i].getClassName().equals(failedAt.getClassName())) {
        sb.append(NL).append("    offending call: at ").append(trace[i]);
        break;
      }
    }
    return sb.toString();
  }

  private static String buildString(Exception e) {
    Class<?> superclass = e.getClass().getSuperclass();
    return superclass.getName() + ": " + e.getMessage();
  }
}
