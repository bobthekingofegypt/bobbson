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

public class StringBsonConverterTest {
  @Test
  public void testReadHandlesNull() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    var sut = new StringBsonConverter();
    String value = sut.read(reader);

    verify(reader).readNull();
    assertNull(value);
  }

  @Test
  public void testReadHandlesString() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.STRING);
    when(reader.readString()).thenReturn("bob");

    var sut = new StringBsonConverter();
    assertEquals("bob", sut.read(reader));
  }

  @Test
  public void testReadThrowsOnIncompatibleType() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT32);

    var sut = new StringBsonConverter();
    assertThrows(RuntimeException.class, () -> sut.read(reader));
  }

  @Test
  public void testWriteString() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new StringBsonConverter();
    sut.write(writer, "bob", "bob");

    verify(writer).writeString("bob".getBytes(StandardCharsets.UTF_8), "bob");
  }

  @Test
  public void testWriteStringNoKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new StringBsonConverter();
    sut.write(writer, "bob");

    verify(writer).writeString("bob");
  }

  @Test
  public void testWriteStringNullKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new StringBsonConverter();
    sut.write(writer, (byte[]) null, "bob");

    verify(writer).writeString("bob");
  }

  @Test
  public void testWriteStringByteKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new StringBsonConverter();
    sut.write(writer, "bob".getBytes(StandardCharsets.UTF_8), "bob");

    verify(writer).writeString("bob".getBytes(StandardCharsets.UTF_8), "bob");
  }
}
