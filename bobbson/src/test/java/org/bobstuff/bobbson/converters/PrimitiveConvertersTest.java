package org.bobstuff.bobbson.converters;

import static org.mockito.Mockito.when;

import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PrimitiveConvertersTest {
  @Test
  public void testParseIntegerNullThrows() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    Assertions.assertThrows(RuntimeException.class, () -> PrimitiveConverters.parseInteger(reader));
  }

  @Test
  public void testParseInteger() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT32);
    when(reader.readInt32()).thenReturn(24);

    Assertions.assertEquals(24, PrimitiveConverters.parseInteger(reader));
  }

  @Test
  public void testParseIntegerInvalidType() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.ARRAY);

    Assertions.assertThrows(RuntimeException.class, () -> PrimitiveConverters.parseInteger(reader));
  }

  @Test
  public void testParseLongFromInt32() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT32);
    when(reader.readInt32()).thenReturn(24);

    Assertions.assertEquals(24, PrimitiveConverters.parseLong(reader));
  }

  @Test
  public void testParseLongFromInt64() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT64);
    when(reader.readInt64()).thenReturn(24L);

    Assertions.assertEquals(24, PrimitiveConverters.parseLong(reader));
  }

  @Test
  public void testParseLongFromDouble() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.DOUBLE);
    when(reader.readDouble()).thenReturn(24d);

    Assertions.assertEquals(24, PrimitiveConverters.parseLong(reader));
  }

  @Test
  public void testParseLongNullThrows() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    Assertions.assertThrows(RuntimeException.class, () -> PrimitiveConverters.parseLong(reader));
  }

  @Test
  public void testParseLong() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT64);
    when(reader.readInt64()).thenReturn(24L);

    Assertions.assertEquals(24, PrimitiveConverters.parseLong(reader));
  }

  @Test
  public void testParseLongInvalidType() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.ARRAY);

    Assertions.assertThrows(RuntimeException.class, () -> PrimitiveConverters.parseLong(reader));
  }

  @Test
  public void testParseDoubleNullThrows() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    Assertions.assertThrows(RuntimeException.class, () -> PrimitiveConverters.parseDouble(reader));
  }

  @Test
  public void testParseDouble() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.DOUBLE);
    when(reader.readDouble()).thenReturn(24D);

    Assertions.assertEquals(24, PrimitiveConverters.parseDouble(reader));
  }

  @Test
  public void testParseDoubleInvalidType() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.ARRAY);

    Assertions.assertThrows(RuntimeException.class, () -> PrimitiveConverters.parseDouble(reader));
  }

  @Test
  public void testParseBooleanNullThrows() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    Assertions.assertThrows(RuntimeException.class, () -> PrimitiveConverters.parseBoolean(reader));
  }

  @Test
  public void testParseBoolean() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.BOOLEAN);
    when(reader.readBoolean()).thenReturn(true);

    Assertions.assertEquals(true, PrimitiveConverters.parseBoolean(reader));
  }

  @Test
  public void testParseBooleanInvalidType() {
    var reader = Mockito.mock(BsonReader.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.ARRAY);
    when(reader.readBoolean()).thenReturn(true);

    Assertions.assertThrows(RuntimeException.class, () -> PrimitiveConverters.parseBoolean(reader));
  }
}
