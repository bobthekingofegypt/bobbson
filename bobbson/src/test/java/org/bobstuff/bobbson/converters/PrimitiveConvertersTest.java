package org.bobstuff.bobbson.converters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.bobstuff.bobbson.BsonReaderStack;
import org.bobstuff.bobbson.BsonType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PrimitiveConvertersTest {
  @Test
  public void testParseIntegerNullThrows() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.NULL);

    Assertions.assertThrows(RuntimeException.class, () -> PrimitiveConverters.parseInteger(reader));
  }

  @Test
  public void testParseInteger() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT32);
    when(reader.readInt32()).thenReturn(24);

    Assertions.assertEquals(24, PrimitiveConverters.parseInteger(reader));
  }

  @Test
  public void testParseLongFromInt32() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT32);
    when(reader.readInt32()).thenReturn(24);

    Assertions.assertEquals(24, PrimitiveConverters.parseLong(reader));
  }

  @Test
  public void testParseLongFromInt64() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.INT64);
    when(reader.readInt64()).thenReturn(24L);

    Assertions.assertEquals(24, PrimitiveConverters.parseLong(reader));
  }

  @Test
  public void testParseLongFromDouble() {
    var reader = Mockito.mock(BsonReaderStack.class);
    when(reader.getCurrentBsonType()).thenReturn(BsonType.DOUBLE);
    when(reader.readDouble()).thenReturn(24d);

    Assertions.assertEquals(24, PrimitiveConverters.parseLong(reader));
  }
}
