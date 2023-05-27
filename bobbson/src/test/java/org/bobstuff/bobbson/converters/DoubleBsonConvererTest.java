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

public class DoubleBsonConvererTest {
  @Test
  public void testReadHandlesNull() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    var sut = new DoubleBsonConverter();
    Double value = sut.read(reader);

    verify(reader).readNull();
    assertNull(value);
  }

  @Test
  public void testReadHandlesDouble() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.DOUBLE);
    when(reader.readDouble()).thenReturn(2.4);

    var sut = new DoubleBsonConverter();
    assertEquals(2.4, sut.read(reader));
  }

  @Test
  public void testReadHandlesInt32() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT32);
    when(reader.readInt32()).thenReturn(2);

    var sut = new DoubleBsonConverter();
    assertEquals(2, sut.read(reader));
  }

  @Test
  public void testReadHandlesInt64() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT64);
    when(reader.readInt64()).thenReturn(25L);

    var sut = new DoubleBsonConverter();
    assertEquals(25, sut.read(reader));
  }

  @Test
  public void testReadThrowsOnIncompatibleType() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.STRING);

    var sut = new DoubleBsonConverter();
    assertThrows(RuntimeException.class, () -> sut.read(reader));
  }

  @Test
  public void testWriteDouble() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new DoubleBsonConverter();
    sut.write(writer, "bob", 2.3);

    verify(writer).writeName("bob");
    verify(writer).writeDouble(2.3);
  }

  @Test
  public void testWriteDoubleNoKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new DoubleBsonConverter();
    sut.write(writer, 2.3);

    verify(writer).writeDouble(2.3);
  }

  @Test
  public void testWriteDoubleNullKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new DoubleBsonConverter();
    sut.write(writer, (byte[]) null, 2.3);

    verify(writer).writeDouble(2.3);
  }

  @Test
  public void testWriteDoubleByteKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new DoubleBsonConverter();
    sut.write(writer, "bob".getBytes(StandardCharsets.UTF_8), 2.3);

    verify(writer).writeName("bob".getBytes(StandardCharsets.UTF_8));
    verify(writer).writeDouble(2.3);
  }
}
