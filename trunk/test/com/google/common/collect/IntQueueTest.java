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

import com.google.common.collect.helpers.MoreAsserts;
import java.util.NoSuchElementException;
import java.util.Random;
import junit.framework.TestCase;

/**
 * Unit test for {@code IntQueue}.
 *
 * @author jjb@google.com (Josh Bloch)
 */
public class IntQueueTest extends TestCase {

  private static final int NUM_ITERATIONS = 1000000;

  public void testIntQueue() {
    Random rnd = new Random(7);
    IntQueue queue = new IntQueue();
    int nextAdd = 0;
    int nextRemove = 0;

    // Perform a random sequence of adds and removes
    for (int i = 0; i < NUM_ITERATIONS; i++) {
      if (rnd.nextBoolean()) {
        queue.add(nextAdd++);
      } else { // remove an element
        if (nextAdd == nextRemove) { // queue empty
          int result = -1;
          boolean threw = false;
          try {
            result = queue.remove();
          } catch(NoSuchElementException e) {
            threw = true;
          }

          assertTrue("Remove should have failed: "+result, threw);
        } else { // queue nonempty
          int result = queue.remove();
          boolean mustEqual = (result == nextRemove++);
          assertTrue("Removed " + result + " expecting " + (nextRemove - 1),
                     mustEqual);
        }
      }
    }

    // Drain queue
    while (nextRemove != nextAdd) {
      int result = queue.remove();
      boolean mustEqual = (result == nextRemove++);
      assertTrue("Removed " + result + " expecting " + (nextRemove - 1),
                 mustEqual);
    }
  }

  public void testToArray() {
    IntQueue queue = new IntQueue();
    MoreAsserts.assertEquals(new int[] {}, queue.toArray());
    queue.add(1);
    MoreAsserts.assertEquals(new int[] { 1 }, queue.toArray());
    queue.add(2);
    queue.add(3);
    MoreAsserts.assertEquals(new int[] { 1, 2, 3 }, queue.toArray());
    queue.toArray()[1] = 4; // toArray returns a copy
    MoreAsserts.assertEquals(new int[] { 1, 2, 3 }, queue.toArray());
  }

  public void testToString() {
    IntQueue queue = new IntQueue();
    assertEquals("[]", queue.toString());
    queue.add(1);
    assertEquals("[1]", queue.toString());
    queue.add(2);
    queue.add(3);
    assertEquals("[1, 2, 3]", queue.toString());
  }

  public void testClone() {
    IntQueue queue = new IntQueue();
    queue.add(1);
    IntQueue clone = queue.clone();
    assertEquals("[1]", clone.toString());
    assertEquals(1, queue.remove());
    assertEquals("[1]", clone.toString());
    assertEquals(1, clone.remove());
  }
}
