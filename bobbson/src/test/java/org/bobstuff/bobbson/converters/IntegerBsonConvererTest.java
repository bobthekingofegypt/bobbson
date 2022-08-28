package org.bobstuff.bobbson.converters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class IntegerBsonConvererTest {
  @Test
  public void testReadHandlesNull() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    var sut = new IntegerBsonConverter();
    Integer value = sut.read(reader);

    verify(reader).readNull();
    assertNull(value);
  }

  @Test
  public void testReadHandlesInteger() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT32);
    when(reader.readInt32()).thenReturn(24);

    var sut = new IntegerBsonConverter();
    assertEquals(24, sut.read(reader));
  }

  @Test
  public void testReadHandlesLong() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT64);
    when(reader.readInt64()).thenReturn(24L);

    var sut = new IntegerBsonConverter();
    assertEquals(24, sut.read(reader));
  }

  @Test
  public void testReadHandlesDouble() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.DOUBLE);
    when(reader.readDouble()).thenReturn(24.34);

    var sut = new IntegerBsonConverter();
    assertEquals(24, sut.read(reader));
  }

  @Test
  public void testReadThrowsOnIncompatibleType() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.STRING);

    var sut = new IntegerBsonConverter();
    assertThrows(RuntimeException.class, () -> sut.read(reader));
  }

  @Test
  public void testWriteInt32() {
    var writer = Mockito.mock(BsonWriter.class);

    var sut = new IntegerBsonConverter();
    sut.write(writer, "bob", 23);

    verify(writer).writeInteger("bob".getBytes(StandardCharsets.UTF_8), 23);
  }

  @Test
  public void testWriteInt32NoKey() {
    var writer = Mockito.mock(BsonWriter.class);

    var sut = new IntegerBsonConverter();
    sut.write(writer, 23);

    verify(writer).writeInteger(23);
  }

  @Test
  public void testWriteInt32NullKey() {
    var writer = Mockito.mock(BsonWriter.class);

    var sut = new IntegerBsonConverter();
    sut.write(writer, (byte[]) null, 23);

    verify(writer).writeInteger(23);
  }

  @Test
  public void testWriteInt32ByteKey() {
    var writer = Mockito.mock(BsonWriter.class);

    var sut = new IntegerBsonConverter();
    sut.write(writer, "bob".getBytes(StandardCharsets.UTF_8), 23);

    verify(writer).writeInteger("bob".getBytes(StandardCharsets.UTF_8), 23);
  }
}
