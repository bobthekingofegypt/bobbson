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

public class BooleanBsonConverterTest {
  @Test
  public void testReadHandlesNull() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    var sut = new BooleanBsonConverter();
    Boolean value = sut.read(reader);

    verify(reader).readNull();
    assertNull(value);
  }

  @Test
  public void testReadHandlesNonBoolean() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT32);

    var sut = new BooleanBsonConverter();
    assertThrows(RuntimeException.class, () -> sut.read(reader));
  }

  @Test
  public void testReadHandlesBooleanTrue() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.BOOLEAN);
    when(reader.readBoolean()).thenReturn(true);

    var sut = new BooleanBsonConverter();
    assertEquals(Boolean.TRUE, sut.read(reader));
  }

  @Test
  public void testReadHandlesBooleanFalse() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.BOOLEAN);
    when(reader.readBoolean()).thenReturn(false);

    var sut = new BooleanBsonConverter();
    assertEquals(Boolean.FALSE, sut.read(reader));
  }

  @Test
  public void testWriteHandlesBooleanTrue() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new BooleanBsonConverter();
    sut.write(writer, "key".getBytes(StandardCharsets.UTF_8), true);

    verify(writer).writeBoolean("key".getBytes(StandardCharsets.UTF_8), true);
  }

  @Test
  public void testWriteHandlesBooleanTrueNullKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new BooleanBsonConverter();
    sut.write(writer, (byte[]) null, true);

    verify(writer).writeBoolean(true);
  }

  @Test
  public void testWriteHandlesBooleanTrueNoKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new BooleanBsonConverter();
    sut.write(writer, true);

    verify(writer).writeBoolean(true);
  }

  @Test
  public void testWriteHandlesBooleanTrueStringKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new BooleanBsonConverter();
    sut.write(writer, "key", true);

    verify(writer).writeBoolean("key".getBytes(StandardCharsets.UTF_8), true);
  }

  @Test
  public void testWriteHandlesBooleanFalse() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new BooleanBsonConverter();
    sut.write(writer, "key".getBytes(StandardCharsets.UTF_8), false);

    verify(writer).writeBoolean("key".getBytes(StandardCharsets.UTF_8), false);
  }

  @Test
  public void testWriteHandlesBooleanFalseNullKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new BooleanBsonConverter();
    sut.write(writer, (byte[]) null, false);

    verify(writer).writeBoolean(false);
  }

  @Test
  public void testWriteHandlesBooleanFalseNoKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new BooleanBsonConverter();
    sut.write(writer, false);

    verify(writer).writeBoolean(false);
  }

  @Test
  public void testWriteHandlesBooleanFalseStringKey() {
    var writer = Mockito.mock(StackBsonWriter.class);

    var sut = new BooleanBsonConverter();
    sut.write(writer, "key", false);

    verify(writer).writeBoolean("key".getBytes(StandardCharsets.UTF_8), false);
  }
}
