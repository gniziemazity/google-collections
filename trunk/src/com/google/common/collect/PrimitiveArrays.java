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

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

/**
 * Static utility methods pertaining to arrays of Java primitives.
 *
 * @author DJ Lee
 * @author Michael Parker
 */
public final class PrimitiveArrays {
  private PrimitiveArrays() {}

  /**
   * Converts a Collection of {@code Short} instances (wrapper objects) into a
   * new array of primitive shorts.
   *
   * @param collection a Collection of Shorts.
   * @return an array containing the same shorts as {@code collection}, in the
   *     same order, converted to primitives.
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
   * Returns a fixed-size list backed by the specified array, similar to {@link
   * java.util.Arrays#asList}. The only additional restriction of the returned
   * list is that {@code null} cannot be assigned to any element via {@link
   * List#set(int,Object)}.
   *
   * @param backingArray the array to back the list
   * @return a list view of the array
   */
  public static List<Short> asList(short[] backingArray) {
    return new ShortArray(backingArray);
  }

  private static class ShortArray extends AbstractList<Short> implements
      RandomAccess, Serializable {
    final short[] array;

    ShortArray(short[] array) {
      checkNotNull(array);
      this.array = array;
    }

    @Override public Short get(int index) {
      return array[index];
    }

    @Override public int size() {
      return array.length;
    }

    @Override public boolean contains(Object o) {
      return o != null && super.contains(o);
    }

    @Override public Short set(int index, Short element) {
      checkNotNull(element);
      Short oldValue = array[index];
      array[index] = element;
      return oldValue;
    }

    @Override public int indexOf(Object o) {
      return (o == null) ? -1 : super.indexOf(o);
    }

    @Override public int lastIndexOf(Object o) {
      return (o == null) ? -1 : super.lastIndexOf(o);
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof ShortArray) {
        ShortArray otherShortArray = (ShortArray) o;
        return Arrays.equals(array, otherShortArray.array);
      }
      return super.equals(o);
    }

    @Override public int hashCode() {
      return Arrays.hashCode(array);
    }

    @Override public String toString() {
      return Arrays.toString(array);
    }

    private static final long serialVersionUID = -8154916222509568718L;
  }

  /**
   * Converts a Collection of {@code Integer} instances (wrapper objects) into a
   * new array of primitive ints.
   *
   * @param collection a Collection of Integers.
   * @return an array containing the same ints as {@code collection}, in the
   *     same order, converted to primitives.
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
   * Returns a fixed-size list backed by the specified array, similar to {@link
   * java.util.Arrays#asList}. The only additional restriction of the returned
   * list is that {@code null} cannot be assigned to any element via {@link
   * List#set(int,Object)}.
   *
   * @param backingArray the array to back the list
   * @return a list view of the array
   */
  public static List<Integer> asList(int[] backingArray) {
    return new IntegerArray(backingArray);
  }

  private static class IntegerArray extends AbstractList<Integer>
      implements RandomAccess, Serializable {
    final int[] array;

    IntegerArray(int[] array) {
      checkNotNull(array);
      this.array = array;
    }

    @Override public Integer get(int index) {
      return array[index];
    }

    @Override public int size() {
      return array.length;
    }

    @Override public boolean contains(Object o) {
      return o != null && super.contains(o);
    }

    @Override public Integer set(int index, Integer element) {
      checkNotNull(element);
      Integer oldValue = array[index];
      array[index] = element;
      return oldValue;
    }

    @Override public int indexOf(Object o) {
      return (o == null) ? -1 : super.indexOf(o);
    }

    @Override public int lastIndexOf(Object o) {
      return (o == null) ? -1 : super.lastIndexOf(o);
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof IntegerArray) {
        IntegerArray otherIntArray = (IntegerArray) o;
        return Arrays.equals(array, otherIntArray.array);
      }
      return super.equals(o);
    }

    @Override public int hashCode() {
      return Arrays.hashCode(array);
    }

    @Override public String toString() {
      return Arrays.toString(array);
    }

    private static final long serialVersionUID = -4822892581373843939L;
  }

  /**
   * Converts a Collection of {@code Double} instances (wrapper objects) into a
   * new array of primitive doubles.
   *
   * @param collection a Collection of Doubles.
   * @return an array containing the same doubles as {@code collection}, in the
   *     same order, converted to primitives.
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
   * Returns a fixed-size list backed by the specified array, similar to {@link
   * java.util.Arrays#asList}. The only additional restriction of the returned
   * list is that {@code null} cannot be assigned to any element via {@link
   * List#set(int,Object)}.
   *
   * @param backingArray the array to back the list
   * @return a list view of the array
   */
  public static List<Double> asList(double[] backingArray) {
    return new DoubleArray(backingArray);
  }

  private static class DoubleArray extends AbstractList<Double> implements
      RandomAccess, Serializable {
    final double[] array;

    DoubleArray(double[] array) {
      checkNotNull(array);
      this.array = array;
    }

    @Override public Double get(int index) {
      return array[index];
    }

    @Override public int size() {
      return array.length;
    }

    @Override public boolean contains(Object o) {
      return o != null && super.contains(o);
    }

    @Override public Double set(int index, Double element) {
      checkNotNull(element);
      Double oldValue = array[index];
      array[index] = element;
      return oldValue;
    }

    @Override public int indexOf(Object o) {
      return (o == null) ? -1 : super.indexOf(o);
    }

    @Override public int lastIndexOf(Object o) {
      return (o == null) ? -1 : super.lastIndexOf(o);
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof DoubleArray) {
        DoubleArray otherDoubleArray = (DoubleArray) o;
        return Arrays.equals(array, otherDoubleArray.array);
      }
      return super.equals(o);
    }

    @Override public int hashCode() {
      return Arrays.hashCode(array);
    }

    @Override public String toString() {
      return Arrays.toString(array);
    }

    private static final long serialVersionUID = 8653552962092585491L;
  }

  /**
   * Converts a Collection of {@code Float} instances (wrapper objects) into a
   * new array of primitive floats.
   *
   * @param collection a Collection of Floats.
   * @return an array containing the same floats as {@code collection}, in the
   *     same order, converted to primitives.
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
   * Returns a fixed-size list backed by the specified array, similar to {@link
   * java.util.Arrays#asList}. The only additional restriction of the returned
   * list is that {@code null} cannot be assigned to any element via {@link
   * List#set(int,Object)}.
   *
   * @param backingArray the array to back the list
   * @return a list view of the array
   */
  public static List<Float> asList(float[] backingArray) {
    return new FloatArray(backingArray);
  }

  private static class FloatArray extends AbstractList<Float> implements
      RandomAccess, Serializable {
    final float[] array;

    FloatArray(float[] array) {
      checkNotNull(array);
      this.array = array;
    }

    @Override public Float get(int index) {
      return array[index];
    }

    @Override public int size() {
      return array.length;
    }

    @Override public boolean contains(Object o) {
      return o != null && super.contains(o);
    }

    @Override public Float set(int index, Float element) {
      checkNotNull(element);
      Float oldValue = array[index];
      array[index] = element;
      return oldValue;
    }

    @Override public int indexOf(Object o) {
      return (o == null) ? -1 : super.indexOf(o);
    }

    @Override public int lastIndexOf(Object o) {
      return (o == null) ? -1 : super.lastIndexOf(o);
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof FloatArray) {
        FloatArray otherFloatArray = (FloatArray) o;
        return Arrays.equals(array, otherFloatArray.array);
      }
      return super.equals(o);
    }

    @Override public int hashCode() {
      return Arrays.hashCode(array);
    }

    @Override public String toString() {
      return Arrays.toString(array);
    }

    private static final long serialVersionUID = -2012656098077210382L;
  }

  /**
   * Converts a Collection of {@code Long} instances (wrapper objects) into a
   * new array of primitive longs.
   *
   * @param collection a Collection of Longs.
   * @return an array containing the same longs as {@code collection}, in the
   *     same order, converted to primitives.
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
   * Returns a fixed-size list backed by the specified array, similar to {@link
   * java.util.Arrays#asList}. The only additional restriction of the returned
   * list is that {@code null} cannot be assigned to any element via {@link
   * List#set(int,Object)}.
   *
   * @param backingArray the array to back the list
   * @return a list view of the array
   */
  public static List<Long> asList(long[] backingArray) {
    return new LongArray(backingArray);
  }

  private static class LongArray extends AbstractList<Long> implements
      RandomAccess, Serializable {
    final long[] array;

    LongArray(long[] array) {
      checkNotNull(array);
      this.array = array;
    }

    @Override public Long get(int index) {
      return array[index];
    }

    @Override public int size() {
      return array.length;
    }

    @Override public boolean contains(Object o) {
      return o != null && super.contains(o);
    }

    @Override public Long set(int index, Long element) {
      checkNotNull(element);
      Long oldValue = array[index];
      array[index] = element;
      return oldValue;
    }

    @Override public int indexOf(Object o) {
      return (o == null) ? -1 : super.indexOf(o);
    }

    @Override public int lastIndexOf(Object o) {
      return (o == null) ? -1 : super.lastIndexOf(o);
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof LongArray) {
        LongArray otherLongArray = (LongArray) o;
        return Arrays.equals(array, otherLongArray.array);
      }
      return super.equals(o);
    }

    @Override public int hashCode() {
      return Arrays.hashCode(array);
    }

    @Override public String toString() {
      return Arrays.toString(array);
    }

    private static final long serialVersionUID = -1906354782954286455L;
  }

  /**
   * Converts a Collection of {@code Character} instances (wrapper objects) into
   * a new array of primitive chars.
   *
   * @param collection a Collection of Characters.
   * @return an array containing the same chars as {@code collection}, in the
   *     same order, converted to primitives.
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
   * Returns a fixed-size list backed by the specified array, similar to {@link
   * java.util.Arrays#asList}. The only additional restriction of the returned
   * list is that {@code null} cannot be assigned to any element via {@link
   * List#set(int,Object)}.
   *
   * @param backingArray the array to back the list
   * @return a list view of the array
   */
  public static List<Character> asList(char[] backingArray) {
    return new CharacterArray(backingArray);
  }

  private static class CharacterArray extends AbstractList<Character>
      implements RandomAccess, Serializable {
    final char[] array;

    CharacterArray(char[] array) {
      checkNotNull(array);
      this.array = array;
    }

    @Override public Character get(int index) {
      return array[index];
    }

    @Override public int size() {
      return array.length;
    }

    @Override public boolean contains(Object o) {
      return o != null && super.contains(o);
    }

    @Override public Character set(int index, Character element) {
      checkNotNull(element);
      Character oldValue = array[index];
      array[index] = element;
      return oldValue;
    }

    @Override public int indexOf(Object o) {
      return (o == null) ? -1 : super.indexOf(o);
    }

    @Override public int lastIndexOf(Object o) {
      return (o == null) ? -1 : super.lastIndexOf(o);
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof CharacterArray) {
        CharacterArray otherCharArray = (CharacterArray) o;
        return Arrays.equals(array, otherCharArray.array);
      }
      return super.equals(o);
    }

    @Override public int hashCode() {
      return Arrays.hashCode(array);
    }

    @Override public String toString() {
      return Arrays.toString(array);
    }

    private static final long serialVersionUID = 3842631130130459235L;
  }

  /**
   * Converts a Collection of {@code Boolean} instances (wrapper objects) into a
   * new array of primitive booleans.
   *
   * @param collection a Collection of Booleans.
   * @return an array containing the same booleans as {@code collection}, in the
   *     same order, converted to primitives.
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
   * Returns a fixed-size list backed by the specified array, similar to {@link
   * java.util.Arrays#asList}. The only additional restriction of the returned
   * list is that {@code null} cannot be assigned to any element via {@link
   * List#set(int,Object)}.
   *
   * @param backingArray the array to back the list
   * @return a list view of the array
   */
  public static List<Boolean> asList(boolean[] backingArray) {
    return new BooleanArray(backingArray);
  }

  private static class BooleanArray extends AbstractList<Boolean>
      implements RandomAccess, Serializable {
    final boolean[] array;

    BooleanArray(boolean[] array) {
      checkNotNull(array);
      this.array = array;
    }

    @Override public Boolean get(int index) {
      return array[index];
    }

    @Override public int size() {
      return array.length;
    }

    @Override public boolean contains(Object o) {
      return o != null && super.contains(o);
    }

    @Override public Boolean set(int index, Boolean element) {
      checkNotNull(element);
      Boolean oldValue = array[index];
      array[index] = element;
      return oldValue;
    }

    @Override public int indexOf(Object o) {
      return (o == null) ? -1 : super.indexOf(o);
    }

    @Override public int lastIndexOf(Object o) {
      return (o == null) ? -1 : super.lastIndexOf(o);
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof BooleanArray) {
        BooleanArray otherBoolArray = (BooleanArray) o;
        return Arrays.equals(array, otherBoolArray.array);
      }
      return super.equals(o);
    }

    @Override public int hashCode() {
      return Arrays.hashCode(array);
    }

    @Override public String toString() {
      return Arrays.toString(array);
    }

    private static final long serialVersionUID = 843073596901468312L;
  }

  /**
   * Converts a Collection of {@code Byte} instances (wrapper objects) into a
   * new array of primitive bytes.
   *
   * @param collection a Collection of Bytes.
   * @return an array containing the same bytes as {@code collection}, in the
   *     same order, converted to primitives.
   */
  public static byte[] toByteArray(Collection<Byte> collection) {
    int counter = 0;
    byte[] array = new byte[collection.size()];
    for (Byte x : collection) {
      array[counter++] = x;
    }
    return array;
  }

  /**
   * Returns a fixed-size list backed by the specified array, similar to {@link
   * java.util.Arrays#asList}. The only additional restriction of the returned
   * list is that {@code null} cannot be assigned to any element via {@link
   * List#set(int,Object)}.
   *
   * @param backingArray the array to back the list
   * @return a list view of the array
   */
  public static List<Byte> asList(byte[] backingArray) {
    return new ByteArray(backingArray);
  }

  private static class ByteArray extends AbstractList<Byte> implements
      RandomAccess, Serializable {
    final byte[] array;

    ByteArray(byte[] array) {
      checkNotNull(array);
      this.array = array;
    }

    @Override public Byte get(int index) {
      return array[index];
    }

    @Override public int size() {
      return array.length;
    }

    @Override public boolean contains(Object o) {
      return o != null && super.contains(o);
    }

    @Override public Byte set(int index, Byte element) {
      checkNotNull(element);
      Byte oldValue = array[index];
      array[index] = element;
      return oldValue;
    }

    @Override public int indexOf(Object o) {
      return (o == null) ? -1 : super.indexOf(o);
    }

    @Override public int lastIndexOf(Object o) {
      return (o == null) ? -1 : super.lastIndexOf(o);
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof ByteArray) {
        ByteArray otherByteArray = (ByteArray) o;
        return Arrays.equals(array, otherByteArray.array);
      }
      return super.equals(o);
    }

    @Override public int hashCode() {
      return Arrays.hashCode(array);
    }

    @Override public String toString() {
      return Arrays.toString(array);
    }

    private static final long serialVersionUID = 4227122860714750651L;
  }
}
