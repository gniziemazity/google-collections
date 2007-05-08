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
import java.util.List;
import junit.framework.TestCase;

/**
 * Test suite for PrimitiveArrays.
 *
 * @author djlee@google.com (DJ Lee)
 */
public class PrimitiveArraysTest extends TestCase {

  /**
   * Test operations on collections of Shorts.
   */
  public void testToShortArray() {
    List<Short> none = Lists.newArrayList();
    assertTrue(Arrays.equals(new short[0],
        PrimitiveArrays.toShortArray(none)));

    List<Short> one = Lists.newArrayList((short) 0);
    assertTrue(Arrays.equals(new short[]{ (short) 0 },
        PrimitiveArrays.toShortArray(one)));

    List<Short> three = Lists.newArrayList((short) 0, (short) 1, (short) -37);
    assertTrue(Arrays.equals(new short[]{ (short) 0, (short) 1, (short) -37 },
        PrimitiveArrays.toShortArray(three)));
  }

  /**
   * Test operations on collections of Integers.
   */
  public void testToIntArray() {
    List<Integer> none = Lists.newArrayList();
    assertTrue(Arrays.equals(new int[0],
        PrimitiveArrays.toIntArray(none)));

    List<Integer> one = Lists.newArrayList(0);
    assertTrue(Arrays.equals(new int[]{ 0 },
        PrimitiveArrays.toIntArray(one)));

    List<Integer> three = Lists.newArrayList(0, 1, 0xdeadbeef);
    assertTrue(Arrays.equals(new int[]{ 0, 1, 0xdeadbeef },
        PrimitiveArrays.toIntArray(three)));
  }

  /**
   * Test operations on collections of Doubles.
   */
  public void testToDoubleArray() {
    List<Double> none = Lists.newArrayList();
    assertTrue(Arrays.equals(new double[0],
        PrimitiveArrays.toDoubleArray(none)));

    List<Double> one = Lists.newArrayList(0.0);
    assertTrue(Arrays.equals(new double[]{ 0.0 },
        PrimitiveArrays.toDoubleArray(one)));

    List<Double> three = Lists.newArrayList(0.0, 1.0, 3.1415926538979);
    assertTrue(Arrays.equals(new double[]{ 0.0, 1.0, 3.1415926538979 },
        PrimitiveArrays.toDoubleArray(three)));
  }

  /**
   * Test operations on collections of Floats.
   */
  public void testToFloatArray() {
    List<Float> none = Lists.newArrayList();
    assertTrue(Arrays.equals(new float[0],
        PrimitiveArrays.toFloatArray(none)));

    List<Float> one = Lists.newArrayList(0.0f);
    assertTrue(Arrays.equals(new float[]{ 0.0f },
        PrimitiveArrays.toFloatArray(one)));

    List<Float> three = Lists.newArrayList(0.0f, 1.0f, 3.1415927f);
    assertTrue(Arrays.equals(new float[]{ 0.0f, 1.0f, 3.1415927f },
        PrimitiveArrays.toFloatArray(three)));
  }

  /**
   * Test operations on collections of Longs.
   */
  public void testToLongArray() {
    List<Long> none = Lists.newArrayList();
    assertTrue(Arrays.equals(new long[0],
        PrimitiveArrays.toLongArray(none)));

    List<Long> one = Lists.newArrayList(0L);
    assertTrue(Arrays.equals(new long[]{ 0L },
        PrimitiveArrays.toLongArray(one)));

    List<Long> three = Lists.newArrayList(0L, 1L, 9876543210L);
    assertTrue(Arrays.equals(new long[]{ 0L, 1L, 9876543210L },
        PrimitiveArrays.toLongArray(three)));
  }

  /**
   * Test operations on collections of Characters.
   */
  public void testToCharArray() {
    List<Character> none = Lists.newArrayList();
    assertTrue(Arrays.equals(new char[0],
        PrimitiveArrays.toCharArray(none)));

    List<Character> one = Lists.newArrayList('a');
    assertTrue(Arrays.equals(new char[]{ 'a' },
        PrimitiveArrays.toCharArray(one)));

    List<Character> three = Lists.newArrayList('a', '\n', '\007');
    assertTrue(Arrays.equals(new char[]{ 'a', '\n', '\007' },
        PrimitiveArrays.toCharArray(three)));
  }

  /**
   * Test operations on collections of Booleans.
   */
  public void testToBooleanArray() {
    List<Boolean> none = Lists.newArrayList();
    assertTrue(Arrays.equals(new boolean[0],
        PrimitiveArrays.toBooleanArray(none)));

    List<Boolean> one = Lists.newArrayList(true);
    assertTrue(Arrays.equals(new boolean[]{ true },
        PrimitiveArrays.toBooleanArray(one)));

    List<Boolean> three = Lists.newArrayList(true, false, true);
    assertTrue(Arrays.equals(new boolean[]{ true, false, true },
        PrimitiveArrays.toBooleanArray(three)));
  }

  /**
   * Test operations on collections of Bytes.
   */
  public void testToByteArray() {
    List<Byte> none = Lists.newArrayList();
    assertTrue(Arrays.equals(new byte[0],
        PrimitiveArrays.toByteArray(none)));

    List<Byte> one = Lists.newArrayList((byte) 0);
    assertTrue(Arrays.equals(new byte[]{ (byte) 0 },
        PrimitiveArrays.toByteArray(one)));

    List<Byte> three = Lists.newArrayList((byte) 0, (byte) 1, (byte) 0xff);
    assertTrue(Arrays.equals(new byte[]{ (byte) 0, (byte) 1, (byte) 0xff },
        PrimitiveArrays.toByteArray(three)));
  }
}
