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
 * Unit test for {@link IntStack}.
 *
 * @author jjb@google.com (Josh Bloch)
 */
public class IntStackTest extends TestCase {
  private static final int NUM_ITERATIONS = 1000000;

  private IntStack stack;
  private int size;

  public void setUp() throws Exception {
    super.setUp();
    stack = new IntStack();
    size = 0;
  }

  public void testStack() {
    Random rnd = new Random(7);

    // Perform a random sequence of adds and removes
    for (int i = 0; i < NUM_ITERATIONS; i++) {
      if (rnd.nextBoolean()) {
        stack.add(size++);
      } else {
        if (size == 0) {
          checkEmpty();
        } else {
          int result = stack.remove();
          assertTrue("Removed " + result + ", expecting " + size,
                     (result == --size));
        }
      }
      checkSize();
    }

    // Test clear method
    stack.clear();
    size = 0;
    checkSize();
    checkEmpty();
  }

  public void testToArray() {
    MoreAsserts.assertEquals(new int[] {}, stack.toArray());
    stack.add(1);
    MoreAsserts.assertEquals(new int[] { 1 }, stack.toArray());
    stack.add(2);
    stack.add(3);
    MoreAsserts.assertEquals(new int[] { 3, 2, 1 }, stack.toArray());
    stack.toArray()[1] = 4; // toArray returns a copy
    MoreAsserts.assertEquals(new int[] { 3, 2, 1 }, stack.toArray());
  }

  public void testToString() {
    assertEquals("[]", stack.toString());
    stack.add(1);
    assertEquals("[1]", stack.toString());
    stack.add(2);
    stack.add(3);
    assertEquals("[3, 2, 1]", stack.toString());
  }

  public void testClone() {
    stack.add(1);
    IntStack clone = stack.clone();
    assertEquals("[1]", clone.toString());
    assertEquals(1, stack.remove());
    assertEquals("[1]", clone.toString());
    assertEquals(1, clone.remove());
  }

  private void checkEmpty() {
    int result = -1;
    boolean threw = false;
    try {
      result = stack.remove();
    } catch(NoSuchElementException e) {
      threw = true;
    }
    assertTrue("Remove should have failed: " + result, threw);
    assertTrue("isEmpty should have returned false", stack.isEmpty());
  }

  private void checkSize() {
    assertTrue("Size: " + stack.size() + " != " + size, stack.size() == size);
  }
}
