// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.Collection;

/**
 * Static utility methods pertaining to arrays of Java primitives.
 *
 * @author djlee@google.com (DJ Lee)
 */
public final class PrimitiveArrays {
  private PrimitiveArrays() {}

  /**
   * Converts a Collection of {@code Short} instances (wrapper objects)
   * into a new array of primitive shorts.
   * @param collection a Collection of Shorts.
   * @return an array containing the same shorts as {@code collection},
   * in the same order, converted to primitives.
   */
  public static short[] toShortArray(Collection<Short> collection) {
    int counter = 0;
    short[] array = new short[collection.size()];
    for (Short x : collection) {
      array[counter++] = x;
    }
    return array;
  }

  /**
   * Converts a Collection of {@code Integer} instances (wrapper objects)
   * into a new array of primitive ints.
   * @param collection a Collection of Integers.
   * @return an array containing the same ints as {@code collection},
   * in the same order, converted to primitives.
   */
  public static int[] toIntArray(Collection<Integer> collection) {
    int counter = 0;
    int[] array = new int[collection.size()];
    for (Integer x : collection) {
      array[counter++] = x;
    }
    return array;
  }

  /**
   * Converts a Collection of {@code Double} instances (wrapper objects)
   * into a new array of primitive doubles.
   * @param collection a Collection of Doubles.
   * @return an array containing the same doubles as {@code collection},
   * in the same order, converted to primitives.
   */
  public static double[] toDoubleArray(Collection<Double> collection) {
    int counter = 0;
    double[] array = new double[collection.size()];
    for (Double x : collection) {
      array[counter++] = x;
    }
    return array;
  }

  /**
   * Converts a Collection of {@code Float} instances (wrapper objects)
   * into a new array of primitive floats.
   * @param collection a Collection of Floats.
   * @return an array containing the same floats as {@code collection},
   * in the same order, converted to primitives.
   */
  public static float[] toFloatArray(Collection<Float> collection) {
    int counter = 0;
    float[] array = new float[collection.size()];
    for (Float x : collection) {
      array[counter++] = x;
    }
    return array;
  }

  /**
   * Converts a Collection of {@code Long} instances (wrapper objects)
   * into a new array of primitive longs.
   * @param collection a Collection of Longs.
   * @return an array containing the same longs as {@code collection},
   * in the same order, converted to primitives.
   */
  public static long[] toLongArray(Collection<Long> collection) {
    int counter = 0;
    long[] array = new long[collection.size()];
    for (Long x : collection) {
      array[counter++] = x;
    }
    return array;
  }

  /**
   * Converts a Collection of {@code Character} instances (wrapper objects)
   * into a new array of primitive chars.
   * @param collection a Collection of Characters.
   * @return an array containing the same chars as {@code collection},
   * in the same order, converted to primitives.
   */
  public static char[] toCharArray(Collection<Character> collection) {
    int counter = 0;
    char[] array = new char[collection.size()];
    for (Character x : collection) {
      array[counter++] = x;
    }
    return array;
  }

  /**
   * Converts a Collection of {@code Boolean} instances (wrapper objects)
   * into a new array of primitive booleans.
   * @param collection a Collection of Booleans.
   * @return an array containing the same booleans as {@code collection},
   * in the same order, converted to primitives.
   */
  public static boolean[] toBooleanArray(Collection<Boolean> collection) {
    int counter = 0;
    boolean[] array = new boolean[collection.size()];
    for (Boolean x : collection) {
      array[counter++] = x;
    }
    return array;
  }

  /**
   * Converts a Collection of {@code Byte} instances (wrapper objects)
   * into a new array of primitive bytes.
   * @param collection a Collection of Bytes.
   * @return an array containing the same bytes as {@code collection},
   * in the same order, converted to primitives.
   */
  public static byte[] toByteArray(Collection<Byte> collection) {
    int counter = 0;
    byte[] array = new byte[collection.size()];
    for (Byte x : collection) {
      array[counter++] = x;
    }
    return array;
  }
}
