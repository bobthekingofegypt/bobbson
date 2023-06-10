package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.converters.IntegerBsonConverter;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CollectionConverterFactoryTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateList() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new CollectionConverterFactory();
    var converter =
        sut.tryCreate(new ArrayList<Integer>() {}.getClass().getGenericSuperclass(), bobBson);

    validateConverter(converter);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateSet() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new CollectionConverterFactory();
    var converter =
        sut.tryCreate(new HashSet<Integer>() {}.getClass().getGenericSuperclass(), bobBson);

    validateConverter(converter);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateSetInterface() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new CollectionConverterFactory();
    var converter = sut.tryCreate(new TypeToken<Set<Integer>>() {}.getType(), bobBson);

    validateConverter(converter);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateListInterface() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new CollectionConverterFactory();
    var converter = sut.tryCreate(new TypeToken<List<Integer>>() {}.getType(), bobBson);

    validateConverter(converter);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateQueueInterface() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new CollectionConverterFactory();
    var converter = sut.tryCreate(new TypeToken<Queue<Integer>>() {}.getType(), bobBson);

    validateConverter(converter);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateNonCollectionInterface() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new CollectionConverterFactory();
    var converter = sut.tryCreate(new TypeToken<Map<String, Integer>>() {}.getType(), bobBson);

    assertNull(converter);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateNonGenericType() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new CollectionConverterFactory();
    var converter = sut.tryCreate(new TypeToken<List>() {}.getType(), bobBson);

    assertNull(converter);
  }

  private void validateConverter(CollectionConverter<?, ?> converter) {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartArray("bob");
    bsonWriter.writeInteger(3);
    bsonWriter.writeInteger(4);
    bsonWriter.writeEndArray();
    bsonWriter.writeEndDocument();

    var reader = new StackBsonReader(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    var result = converter.read(reader);
    assertEquals(2, result.size());
    assertTrue(result.contains(3));
    assertTrue(result.contains(4));
  }
}
