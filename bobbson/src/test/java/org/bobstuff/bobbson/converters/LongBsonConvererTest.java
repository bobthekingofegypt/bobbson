package org.bobstuff.bobbson.converters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.bobstuff.bobbson.BsonReaderStack;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LongBsonConvererTest {
  @Test
  public void testReadHandlesNull() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    var sut = new LongBsonConverter();
    Long value = sut.read(reader);

    verify(reader).readNull();
    assertNull(value);
  }

  @Test
  public void testReadHandlesInteger() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT32);
    when(reader.readInt32()).thenReturn(24);

    var sut = new LongBsonConverter();
    assertEquals(24, sut.read(reader));
  }

  @Test
  public void testReadHandlesLong() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT64);
    when(reader.readInt64()).thenReturn(24L);

    var sut = new LongBsonConverter();
    assertEquals(24L, sut.read(reader));
  }

  @Test
  public void testReadHandlesDouble() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.DOUBLE);
    when(reader.readDouble()).thenReturn(24.34);

    var sut = new LongBsonConverter();
    assertEquals(24L, sut.read(reader));
  }

  @Test
  public void testReadThrowsOnIncompatibleType() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.STRING);

    var sut = new LongBsonConverter();
    assertThrows(RuntimeException.class, () -> sut.read(reader));
  }

  @Test
  public void testWriteInt64() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new LongBsonConverter();
    sut.write(writer, "bob", 23L);

    verify(writer).writeLong("bob".getBytes(StandardCharsets.UTF_8), 23L);
  }

  @Test
  public void testWriteInt64NoKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new LongBsonConverter();
    sut.write(writer, 23L);

    verify(writer).writeLong(23L);
  }

  @Test
  public void testWriteInt64NullKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new LongBsonConverter();
    sut.write(writer, (byte[]) null, 23L);

    verify(writer).writeLong(23L);
  }

  @Test
  public void testWriteInt64ByteKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new LongBsonConverter();
    sut.write(writer, "bob".getBytes(StandardCharsets.UTF_8), 23L);

    verify(writer).writeLong("bob".getBytes(StandardCharsets.UTF_8), 23L);
  }
}
