package org.bobstuff.bobbson;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ByteRangeTest {
  @Test
  public void defaultCreate() {
    var sut = new BobBsonByteRange(new byte[4]);
  }

  @Test
  public void setValues() {
    var sut = new BobBsonByteRange(new byte[4]);
    sut.set(4, 10, 0);
  }

  @Test
  public void compareArrays() {
    var data = "hello there".getBytes(StandardCharsets.UTF_8);
    var nullTerminatedData = new byte[data.length + 1];
    System.arraycopy(data, 0, nullTerminatedData, 0, data.length);
    var sut = new BobBsonByteRange(nullTerminatedData);
    sut.set(0, nullTerminatedData.length, 0);

    Assertions.assertTrue(sut.equalsArray(data));
  }

  @Test
  public void compareArraysNotEqual() {
    var data = "hello there".getBytes(StandardCharsets.UTF_8);
    var nullTerminatedData = new byte[data.length + 1];
    System.arraycopy(data, 0, nullTerminatedData, 0, data.length);
    var sut = new BobBsonByteRange(nullTerminatedData);
    sut.set(0, nullTerminatedData.length, 0);

    Assertions.assertFalse(sut.equalsArray("not equal".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  public void compareArraysWeakHash() {
    var data = "hello there".getBytes(StandardCharsets.UTF_8);
    var nullTerminatedData = new byte[data.length + 1];
    System.arraycopy(data, 0, nullTerminatedData, 0, data.length);
    var sut = new BobBsonByteRange(nullTerminatedData);
    sut.set(0, nullTerminatedData.length, 49);

    Assertions.assertTrue(sut.equalsArray("hello there".getBytes(StandardCharsets.UTF_8), 49));
  }

  @Test
  public void compareArraysWeakHashEqualTextNotEqual() {
    var data = "hello there".getBytes(StandardCharsets.UTF_8);
    var nullTerminatedData = new byte[data.length + 1];
    System.arraycopy(data, 0, nullTerminatedData, 0, data.length);
    var sut = new BobBsonByteRange(nullTerminatedData);
    sut.set(0, nullTerminatedData.length, 49);

    Assertions.assertFalse(sut.equalsArray("not equal".getBytes(StandardCharsets.UTF_8), 49));
  }

  @Test
  public void compareArraysWeakHashNotEqual() {
    var data = "hello there".getBytes(StandardCharsets.UTF_8);
    var nullTerminatedData = new byte[data.length + 1];
    System.arraycopy(data, 0, nullTerminatedData, 0, data.length);
    var sut = new BobBsonByteRange(nullTerminatedData);
    sut.set(0, nullTerminatedData.length, 49);

    Assertions.assertFalse(sut.equalsArray("hello there".getBytes(StandardCharsets.UTF_8), 21));
  }
}
