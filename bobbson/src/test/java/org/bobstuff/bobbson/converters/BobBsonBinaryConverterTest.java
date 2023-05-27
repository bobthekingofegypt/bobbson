package org.bobstuff.bobbson.converters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bobstuff.bobbson.BobBsonBinary;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BobBsonBinaryConverterTest {
  @Test
  public void testReadHandlesNull() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    var sut = new BobBsonBinaryBsonConverter();
    BobBsonBinary value = sut.read(reader);

    verify(reader).readNull();
    assertNull(value);
  }

  @Test
  public void testReadHandlesBobBsonBinary() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.BINARY);
    var result = new BobBsonBinary(new byte[4]);
    when(reader.readBinary()).thenReturn(result);

    var sut = new BobBsonBinaryBsonConverter();
    BobBsonBinary value = sut.read(reader);

    assertEquals(result, value);
  }

  @Test
  public void testReadHandlesWrongType() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.STRING);

    var sut = new BobBsonBinaryBsonConverter();

    assertThrows(RuntimeException.class, () -> sut.read(reader));
  }

  @Test
  public void testWriteNotImplemented() {
    var writer = Mockito.mock(BsonWriter.class);
    var sut = new BobBsonBinaryBsonConverter();

    assertThrows(
        UnsupportedOperationException.class,
        () -> sut.write(writer, "test", new BobBsonBinary(new byte[10])));
  }
}
