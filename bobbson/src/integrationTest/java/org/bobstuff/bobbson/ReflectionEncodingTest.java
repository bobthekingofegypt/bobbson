package org.bobstuff.bobbson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.*;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.reflection.CollectionConverterFactory;
import org.bobstuff.bobbson.reflection.MapConverterFactory;
import org.bobstuff.bobbson.reflection.ObjectConverterFactory;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.junit.jupiter.api.Test;

public class ReflectionEncodingTest {

  @Test
  public void testReflectionBasedEncoding() throws Exception {
    var bobBson = new BobBson();
    bobBson.registerFactory(new ObjectConverterFactory());

    var simple = new Simple();
    simple.setAge(3);
    simple.setName("bob");

    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new BobBufferBobBsonBuffer(new byte[size], 0, 0));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bsonWriter = new BsonWriter(buffer);
    bobBson.serialise(simple, Simple.class, bsonWriter);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    buffer.pipe(bos);
    var data = bos.toByteArray();

    var reader = new BsonReader(new BobBufferBobBsonBuffer(data, 0, data.length));
    var result = bobBson.deserialise(Simple.class, reader);

    assertEquals("bob", result.getName());
    assertEquals(3, result.getAge());
  }

  @Test
  public void testReflectionBasedEncodingCollections() throws Exception {
    var bobBson = new BobBson();
    bobBson.registerFactory(new CollectionConverterFactory());
    bobBson.registerFactory(new MapConverterFactory());
    bobBson.registerFactory(new ObjectConverterFactory());

    var simple = new SimpleCollections();
    var names = List.of("bob", "jim", "tom");
    var numbers = new HashSet<Integer>(List.of(1, 2, 3));
    var tags = new HashMap<String, Integer>();
    tags.put("happy", 5);
    tags.put("sad", 40);
    simple.setNames(names);
    simple.setNumbers(numbers);
    simple.setTags(tags);

    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new BobBufferBobBsonBuffer(new byte[size], 0, 0));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bsonWriter = new BsonWriter(buffer);
    bobBson.serialise(simple, SimpleCollections.class, bsonWriter);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    buffer.pipe(bos);
    var data = bos.toByteArray();

    var reader = new BsonReader(new BobBufferBobBsonBuffer(data, 0, data.length));
    var result = bobBson.deserialise(SimpleCollections.class, reader);

    assertEquals(names, result.getNames());
    assertEquals(tags, result.getTags());
    assertEquals(numbers, result.getNumbers());
  }

  @Test
  public void testReflectionBasedEncodingRecursiveMaps() throws Exception {
    var bobBson = new BobBson();
    bobBson.registerFactory(new CollectionConverterFactory());
    bobBson.registerFactory(new MapConverterFactory());
    bobBson.registerFactory(new ObjectConverterFactory());

    var simple = new RecursiveMaps();
    var holder2 = new HashMap<String, Map<String, Map<String, String>>>();
    var holder = new HashMap<String, Map<String, String>>();
    var map = new HashMap<String, String>();

    map.put("name", "bob");
    holder.put("holder", map);
    holder2.put("holder2", holder);
    simple.setRecursive(holder2);

    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new BobBufferBobBsonBuffer(new byte[size], 0, 0));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bsonWriter = new BsonWriter(buffer);
    bobBson.serialise(simple, RecursiveMaps.class, bsonWriter);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    buffer.pipe(bos);
    var data = bos.toByteArray();

    var reader = new BsonReader(new BobBufferBobBsonBuffer(data, 0, data.length));
    var result = bobBson.deserialise(RecursiveMaps.class, reader);

    assertEquals("bob", result.getRecursive().get("holder2").get("holder").get("name"));
  }

  public static class SimpleCollections {
    private List<String> names;
    private Set<Integer> numbers;
    private Map<String, Integer> tags;

    public List<String> getNames() {
      return names;
    }

    public void setNames(List<String> names) {
      this.names = names;
    }

    public Set<Integer> getNumbers() {
      return numbers;
    }

    public void setNumbers(Set<Integer> numbers) {
      this.numbers = numbers;
    }

    public Map<String, Integer> getTags() {
      return tags;
    }

    public void setTags(Map<String, Integer> tags) {
      this.tags = tags;
    }
  }

  public static class Simple {
    private String name;
    private int age;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }
  }

  public static class RecursiveMaps {
    private Map<String, Map<String, Map<String, String>>> recursive;

    public Map<String, Map<String, Map<String, String>>> getRecursive() {
      return recursive;
    }

    public void setRecursive(Map<String, Map<String, Map<String, String>>> recursive) {
      this.recursive = recursive;
    }
  }
}