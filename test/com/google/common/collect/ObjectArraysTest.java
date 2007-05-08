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

import static com.google.common.collect.helpers.MoreAsserts.assertContentsInOrder;
import com.google.common.collect.helpers.NullPointerTester;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

/**
 * Unit test for {@link ObjectArrays}.
 *
 * @author kevinb
 */
public class ObjectArraysTest extends TestCase {

  public void testNullPointerExceptions() throws Exception {
    NullPointerTester tester = new NullPointerTester();
    tester.testAllPublicStaticMethods(ObjectArrays.class);
  }

  public void testNewArrayEmpty() throws Exception {
    String[] empty = ObjectArrays.newArray(String.class, 0);
    assertEquals(String[].class, empty.getClass());
    assertEquals(0, empty.length);
  }

  public void testNewArrayNonempty() throws Exception {
    String[] array = ObjectArrays.newArray(String.class, 2);
    assertEquals(String[].class, array.getClass());
    assertEquals(2, array.length);
    assertNull(array[0]);
  }

  public void testNewArrayOfArray() throws Exception {
    String[][] array = ObjectArrays.newArray(String[].class, 1);
    assertEquals(String[][].class, array.getClass());
    assertEquals(1, array.length);
    assertNull(array[0]);
  }

  public void testConcatEmptyEmpty() throws Exception {
    String[] result
        = ObjectArrays.concat(new String[0], new String[0], String.class);
    assertEquals(String[].class, result.getClass());
    assertEquals(0, result.length);
  }

  public void testConcatEmptyNonempty() throws Exception {
    String[] result = ObjectArrays.concat(
        new String[0], new String[] { "a", "b" }, String.class);
    assertEquals(String[].class, result.getClass());
    assertContentsInOrder(Arrays.asList(result), "a", "b");
  }

  public void testConcatNonemptyEmpty() throws Exception {
    String[] result = ObjectArrays.concat(
        new String[] { "a", "b" }, new String[0], String.class);
    assertEquals(String[].class, result.getClass());
    assertContentsInOrder(Arrays.asList(result), "a", "b");
  }

  public void testConcatBasic() throws Exception {
    String[] result = ObjectArrays.concat(
        new String[] { "a", "b" }, new String[] { "c", "d" }, String.class);
    assertEquals(String[].class, result.getClass());
    assertContentsInOrder(Arrays.asList(result), "a", "b", "c", "d");
  }

  public void testConcatWithMoreGeneralType() throws Exception {
    Object[] result
        = ObjectArrays.concat(new String[0], new String[0], Object.class);
    assertEquals(Object[].class, result.getClass());
  }
  
  public void testToArrayImpl1() throws Exception {
    doTestToArrayImpl1(Lists.<Integer>newArrayList());
    doTestToArrayImpl1(Lists.newArrayList(1));
    doTestToArrayImpl1(Lists.newArrayList(1, null, 3));
  }

  private void doTestToArrayImpl1(List<Integer> list) {
    Object[] reference = list.toArray();
    Object[] target = ObjectArrays.toArrayImpl(list);
    assertEquals(reference.getClass(), target.getClass());
    assertTrue(Arrays.equals(reference, target));
  }

  public void testToArrayImpl2() throws Exception {
    doTestToArrayImpl2(Lists.<Integer>newArrayList(), new Integer[0], false);
    doTestToArrayImpl2(Lists.<Integer>newArrayList(), new Integer[1], true);

    doTestToArrayImpl2(Lists.newArrayList(1), new Integer[0], false);
    doTestToArrayImpl2(Lists.newArrayList(1), new Integer[1], true);
    doTestToArrayImpl2(Lists.newArrayList(1), new Integer[] { 2, 3 }, true);

    doTestToArrayImpl2(Lists.newArrayList(1, null, 3), new Integer[0], false);
    doTestToArrayImpl2(Lists.newArrayList(1, null, 3), new Integer[2], false);
    doTestToArrayImpl2(Lists.newArrayList(1, null, 3), new Integer[3], true);
  }

  private void doTestToArrayImpl2(List<Integer> list, Integer[] array1,
      boolean expectModify) {
    Integer[] starting = array1.clone();
    Integer[] array2 = array1.clone();
    Object[] reference = list.toArray(array1);

    Object[] target = ObjectArrays.toArrayImpl(list, array2);

    assertEquals(reference.getClass(), target.getClass());
    assertTrue(Arrays.equals(reference, target));
    assertTrue(Arrays.equals(reference, target));

    Object[] expectedArray1 = expectModify ? reference : starting;
    Object[] expectedArray2 = expectModify ? target : starting;
    assertTrue(Arrays.equals(expectedArray1, array1));
    assertTrue(Arrays.equals(expectedArray2, array2));
  }
}
