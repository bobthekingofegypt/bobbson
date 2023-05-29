package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.converters.IntegerBsonConverter;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class MapConverterFactoryTest {
  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateHashMap() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new MapConverterFactory<Integer, HashMap<String, Integer>>();
    var converter =
        sut.tryCreate(new HashMap<String, Integer>() {}.getClass().getGenericSuperclass(), bobBson);

    var map = new HashMap<String, Integer>();
    map.put("fred", 4);
    map.put("bob", 12);

    validateConverter(converter, map);
  }

  @Test
  public void testTryCreateLinkedHashMapNoValueConverter() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class)).thenReturn(null);
    var sut = new MapConverterFactory<Integer, HashMap<String, Integer>>();
    var converter =
        sut.tryCreate(
            new LinkedHashMap<String, Integer>() {}.getClass().getGenericSuperclass(), bobBson);

    Assertions.assertNull(converter);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateLinkedHashMap() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new MapConverterFactory<Integer, HashMap<String, Integer>>();
    var converter =
        sut.tryCreate(
            new LinkedHashMap<String, Integer>() {}.getClass().getGenericSuperclass(), bobBson);

    var map = new LinkedHashMap<String, Integer>();
    map.put("fred", 4);
    map.put("bob", 12);

    validateConverter(converter, map);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateMapNonStringKey() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new MapConverterFactory<Integer, HashMap<String, Integer>>();
    var converter =
        sut.tryCreate(
            new LinkedHashMap<Double, Integer>() {}.getClass().getGenericSuperclass(), bobBson);

    Assertions.assertNull(converter);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateNonMapParamaterizedType() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new MapConverterFactory<Integer, HashMap<String, Integer>>();
    var converter =
        sut.tryCreate(new ArrayList<Integer>() {}.getClass().getGenericSuperclass(), bobBson);

    Assertions.assertNull(converter);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreateNonMap() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) Integer.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new MapConverterFactory<Integer, HashMap<String, Integer>>();
    var converter = sut.tryCreate(String.class, bobBson);

    Assertions.assertNull(converter);
  }


  @SuppressWarnings("unchecked")
  private void validateConverter(MapConverter converter, Map<String, Integer> value) {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    converter.write(bsonWriter, "ages", value);
    bsonWriter.writeEndDocument();

    var reader = new StackBsonReader(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    var result = (Map<String, Integer>) converter.read(reader);
    assertEquals(2, result.size());
    assertEquals(4, result.get("fred"));
    assertEquals(12, result.get("bob"));
  }
}
