package org.bobstuff.bobbson.unicode;

import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class UnicodeChars {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonComboProvider.class)
  public void testReadWriteStringWithUnicode(
      BobBsonComboProvider.ConfigurationProvider configurationProvider) throws Exception {
    BobBsonBuffer buffer = configurationProvider.getBuffer(2048);
    System.out.println(buffer.getClass());
    BobBson bobBson = configurationProvider.getBobBson();

    var beanWithString = new BeanWithString();
    beanWithString.setValue("Steve Jobs스");

    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(beanWithString, BeanWithString.class, writer);

    BsonReader reader = new BsonReaderStack(buffer);
    var result = bobBson.deserialise(BeanWithString.class, reader);
    System.out.println(result);
    System.out.println(beanWithString);
    Assertions.assertEquals(beanWithString, result);
  }
}
