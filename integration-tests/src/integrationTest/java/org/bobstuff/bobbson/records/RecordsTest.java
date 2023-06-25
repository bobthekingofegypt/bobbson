package org.bobstuff.bobbson.records;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonProvider;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class RecordsTest {
  BobBsonBufferPool pool;
  DynamicBobBsonBuffer buffer;

  @BeforeEach
  public void setUp() {
    pool = new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    buffer = new DynamicBobBsonBuffer(pool);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testRecordNormal(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    var bean = new RecordPlain("bob", 12, 23.4);

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, RecordPlain.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(RecordPlain.class, reader);
    Assertions.assertEquals(bean, result);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testRecordList(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    var bean = new RecordWithList(Arrays.asList("bob", "john", "fred"));

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, RecordWithList.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(RecordWithList.class, reader);
    Assertions.assertEquals(bean, result);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testRecordNullList(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    var bean = new RecordWithList(null);

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, RecordWithList.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(RecordWithList.class, reader);
    Assertions.assertEquals(bean, result);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testRecordSet(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    var bean = new RecordWithSet(new HashSet<>(Arrays.asList("bob", "john", "fred")));

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, RecordWithSet.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(RecordWithSet.class, reader);
    Assertions.assertEquals(bean, result);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testRecordNullSet(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    var bean = new RecordWithSet(null);

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, RecordWithSet.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(RecordWithSet.class, reader);
    Assertions.assertEquals(bean, result);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testRecordMap(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    var m = new HashMap<String, Integer>();
    m.put("bob", 12);
    m.put("fred", 24);
    var bean = new RecordWithMap(m);

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, RecordWithMap.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(RecordWithMap.class, reader);
    Assertions.assertEquals(bean, result);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testRecordNullMap(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    var bean = new RecordWithMap(null);

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, RecordWithMap.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(RecordWithMap.class, reader);
    Assertions.assertEquals(bean, result);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testRecordWithConverter(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    var bean = new RecordWithConverter(Arrays.asList("bob", "john", "fred"));

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, RecordWithConverter.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader verifyReader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    verifyReader.readStartDocument();
    Assertions.assertEquals(BsonType.ARRAY, verifyReader.readBsonType());
    verifyReader.readStartArray();
    Assertions.assertEquals(BsonType.STRING, verifyReader.readBsonType());
    Assertions.assertEquals("!bob", verifyReader.readString());
    Assertions.assertEquals(BsonType.STRING, verifyReader.readBsonType());
    Assertions.assertEquals("!john", verifyReader.readString());
    Assertions.assertEquals(BsonType.STRING, verifyReader.readBsonType());
    Assertions.assertEquals("!fred", verifyReader.readString());
    verifyReader.readEndArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(RecordWithConverter.class, reader);
    Assertions.assertEquals(bean, result);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testRecordWithAttribute(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    var bean = new RecordWithAttribute("bob", 32);

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, RecordWithAttribute.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader verifyReader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    verifyReader.readStartDocument();
    Assertions.assertEquals(BsonType.INT32, verifyReader.readBsonType());
    Assertions.assertEquals("age", verifyReader.currentFieldName());
    Assertions.assertEquals(32, verifyReader.readInt32());
    Assertions.assertEquals(BsonType.STRING, verifyReader.readBsonType());
    Assertions.assertEquals("notname", verifyReader.currentFieldName());
    Assertions.assertEquals("bob", verifyReader.readString());

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(RecordWithAttribute.class, reader);
    Assertions.assertEquals(bean, result);
  }
}
