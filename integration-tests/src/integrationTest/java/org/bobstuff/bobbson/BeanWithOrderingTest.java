package org.bobstuff.bobbson;

import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class BeanWithOrderingTest {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testReadWriteKeyAsByteArray(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    // this test case should just pass, no need to validate anything
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bean = new BeanWithOrdering();
    bean.setFieldOne("one");
    bean.setFieldTwo("two");
    bean.setFieldThree("three");
    bean.setFieldFour("four");
    bean.setFieldFive("five");
    bean.setFieldSix("six");

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, BeanWithOrdering.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var fieldName = reader.getFieldName();
    reader.readStartDocument();
    reader.readBsonType();
    Assertions.assertEquals("fieldOne", fieldName.value());
    reader.readString();
    reader.readBsonType();
    Assertions.assertEquals("fieldTwo", fieldName.value());
    reader.readString();
    reader.readBsonType();
    Assertions.assertEquals("fieldThree", fieldName.value());
    reader.readString();
    reader.readBsonType();
    Assertions.assertEquals("fieldFour", fieldName.value());
    reader.readString();
    reader.readBsonType();
    Assertions.assertEquals("fieldFive", fieldName.value());
    reader.readString();
    reader.readBsonType();
    Assertions.assertEquals("fieldSix", fieldName.value());
  }
}
