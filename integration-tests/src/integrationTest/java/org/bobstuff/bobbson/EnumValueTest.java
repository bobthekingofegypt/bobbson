package org.bobstuff.bobbson;

import org.bobstuff.bobbson.buffer.BobBsonBuffer;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class EnumValueTest {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonComboProvider.class)
  public void testReadWriteBeanWithEnum(
      BobBsonComboProvider.ConfigurationProvider configurationProvider) throws Exception {
    BobBsonBuffer buffer = configurationProvider.getBuffer(2048);
    BobBson bobBson = configurationProvider.getBobBson();

    var beanWithEnum = new EnumValue();
    beanWithEnum.setValue(EnumValue.AnEnum.VALUE_ONE);

    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(beanWithEnum, EnumValue.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(EnumValue.class, reader);
    Assertions.assertEquals(beanWithEnum, result);
  }
}
