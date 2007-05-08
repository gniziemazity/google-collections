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

import java.util.Arrays;
import java.util.Iterator;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * A utility for testing an Iterator implementation by comparing its behavior
 * to that of a "known good" reference implementation. In order to accomplish
 * this, it's important to test a great variety of sequences of the
 * {@link Iterator#next}, {@link Iterator#hasNext} and {@link Iterator#remove}
 * operations. This utility takes the brute-force approach of trying
 * <i>all</i> possible sequences of these operations, up to a given number of
 * steps. So, if the caller specifies to use <i>n</i> steps, a total of
 * <i>3^n</i> tests are actually performed.
 *
 * For instance, if <i>steps</i> is 5, one example sequence that will be tested
 * is:
 *
 * <ol>
 * <li>remove();
 * <li>hasNext()
 * <li>hasNext();
 * <li>remove();
 * <li>next();
 * </ol>
 *
 * This particular order of operations may be unrealistic, and testing all 3^5
 * of them may be thought of as overkill; however, it's difficult to determine
 * which proper subset of this massive set would be sufficient to expose any
 * possible bug.  Brute force is simpler.
 * <p>
 * To use this class the concrete subclass must implement the
 * {@link IteratorTester#newReferenceIterator} and
 * {@link IteratorTester#newTargetIterator} methods.  This is because it's
 * impossible to test an Iterator without changing its state, so the tester
 * needs a steady supply of fresh Iterators.
 * <p>
 * Despite this attempt at exhaustiveness, this class still does not test
 * everything that could go wrong with your Iterator.  For example, it has no
 * way to verify that after {@code remove()} is called, the item truly was
 * removed from its backing collection.
 * <p>
 * TODO(kevinb): Right now, when one of these tests fails, it's not all that
 * easy to diagnose what happened.  Improvements should be made.
 *
 * @author kevinb
 */
public abstract class IteratorTester {

  /**
   * JDK 6 currently has a bug where some iterators get into a undefined state
   * when next() throws a NoSuchElementException. The correct behavior is for
   * remove() to remove the last element returned by next, even if a subsequent
   * next() call threw an exception; however JDK 6's HashMap and related
   * classes throw an IllegalStateException in this case.
   *
   * <p>This flag, if true, causes the iterator tester to abort a given
   * stimulus sequence whenever an exception is thrown by an iterator, avoiding
   * the JDK 6 bugs. This flag should be removed when the JDK is fixed.
   */
  private static final boolean abortOnException =
      System.getProperty("java.version").startsWith("1.6.");

  Stimulus[] stimuli;

  /**
   * Creates an IteratorTester.
   *
   * @param steps how many operations to test for each tested pair of iterators
   */
  protected IteratorTester(int steps) {
    stimuli = new Stimulus[steps];
  }

  /**
   * Returns a new reference iterator each time it's called.  The reference
   * iterator should be a "known good" iterator, such as
   * {@link java.util.ArrayList#iterator}.  This and
   * {@link IteratorTester#newTargetIterator} must return Iterators that
   * reference equivalent objects and in the same order. Warning: it is not
   * enough to simply pull multiple iterators from the same source Iterable,
   * unless that Iterator is unmodifiable.
   */
  protected abstract Iterator<?> newReferenceIterator();

  /**
   * Returns a new target iterator each time it's called. This is the
   * iterator you are trying to test. This and
   * {@link IteratorTester#newReferenceIterator} must return Iterators that
   * reference equivalent objects and in the same order. Warning: it is not
   * enough to simply pull multiple iterators from the same source Iterable,
   * unless that Iterator is unmodifiable.
   */
  protected abstract Iterator<?> newTargetIterator();

  /**
   * Executes the test.
   */
  public final void test() throws Exception {
    try {
      recurse(0);
    } catch (Exception e) {
      throw new Exception(Arrays.toString(stimuli), e);
    }
  }

  private void recurse(int level) throws Exception {
    // We're going to reuse the stimuli array 3^steps times by overwriting it
    // in a recursive loop.  Sneaky.
    if (level == stimuli.length) {
      // We've filled the array.
      compareResultsForThisListOfStimuli();
    } else {
      // Keep recursing to fill the array.
      for (Stimulus stimulus : Stimulus.values()) {
        stimuli[level] = stimulus;
        recurse(level + 1);
      }
    }
  }

  private void compareResultsForThisListOfStimuli() {
    Iterator<?> reference = newReferenceIterator();
    Iterator<?> target = newTargetIterator();
    for (Stimulus stimulus : stimuli) {
      if (stimulus.executeAndCompare(reference, target)
          && abortOnException) {
        return;
      }
    }
  }

  enum Stimulus {
    HAS_NEXT {
      boolean executeAndCompare(Iterator<?> reference, Iterator<?> target) {
        // return only if both are true or both are false
        assertEquals(reference.hasNext(), target.hasNext());
        return false;
      }
    },
    NEXT {
      boolean executeAndCompare(Iterator<?> reference, Iterator<?> target) {
        Object obj = null;
        try {
          obj = reference.next();
        } catch (RuntimeException referenceException) {
          // Reference iterator threw an exception, so we should expect the
          // same exception from the target
          try {
            target.next();
          } catch (RuntimeException targetException) {
            assertEquals(referenceException.getClass(),
                targetException.getClass());
            return true;
          }
          fail("no exception thrown");
        }
        // Reference iterator returned a value, so we should expect the same
        // value from the target
        assertEquals(obj, target.next());
        return false;
      }
    },
    REMOVE {
      boolean executeAndCompare(Iterator<?> reference, Iterator<?> target) {
        try {
          reference.remove();
        } catch (RuntimeException referenceException) {
          // Reference iterator threw an exception, so we should expect the
          // same exception from the target
          try {
            target.remove();
          } catch (RuntimeException targetException) {
            Class<?> referenceExClass = referenceException.getClass();
            Class<?> targetExClass = targetException.getClass();
            assertTrue(referenceExClass.isAssignableFrom(targetExClass));
            return true;
          }
          fail("no exception thrown");
        }
        // Reference iterator returned normally, so the target should as well
        target.remove();
        return false;
      }
    };

    /**
     * Send this stimulus to both iterators and return normally only if both
     * produce the same response.
     *
     * @return {@code true} if an exception was thrown by the iterators.
     */
    abstract boolean executeAndCompare(Iterator<?> reference,
        Iterator<?> target);
  }
}
