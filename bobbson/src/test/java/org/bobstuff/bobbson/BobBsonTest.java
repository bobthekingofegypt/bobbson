package org.bobstuff.bobbson;

import java.net.URL;

import org.bobstuff.bobbson.models.BobBsonBinary;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BobBsonTest {
  @Test
  public void testLongRegistered() {
    var sut = new BobBson();
    Assertions.assertNotNull(sut.tryFindConverter(Long.class));
    Assertions.assertNotNull(sut.tryFindConverter(long.class));
  }

  @Test
  public void testDoubleRegistered() {
    var sut = new BobBson();
    Assertions.assertNotNull(sut.tryFindConverter(Double.class));
    Assertions.assertNotNull(sut.tryFindConverter(double.class));
  }

  @Test
  public void testIntRegistered() {
    var sut = new BobBson();
    Assertions.assertNotNull(sut.tryFindConverter(Integer.class));
    Assertions.assertNotNull(sut.tryFindConverter(int.class));
  }

  @Test
  public void testBooleanRegistered() {
    var sut = new BobBson();
    Assertions.assertNotNull(sut.tryFindConverter(Boolean.class));
    Assertions.assertNotNull(sut.tryFindConverter(boolean.class));
  }

  @Test
  public void testStringRegistered() {
    var sut = new BobBson();
    Assertions.assertNotNull(sut.tryFindConverter(String.class));
  }

  @Test
  public void testBobBsonBinaryRegistered() {
    var sut = new BobBson();
    Assertions.assertNotNull(sut.tryFindConverter(BobBsonBinary.class));
  }

  @Test
  public void testSomethingNotRegistered() {
    var sut = new BobBson();
    Assertions.assertThrows(RuntimeException.class, () -> sut.tryFindConverter(URL.class));
  }

  @Test
  public void testRegisterSomething() {
    var converter = Mockito.mock(BobBsonConverter.class);
    var sut = new BobBson();

    sut.registerConverter(URL.class, converter);

    Assertions.assertEquals(converter, sut.tryFindConverter(URL.class));
  }

  @Test
  public void testRegisteredFactory() {
    var converter = Mockito.mock(BobBsonConverter.class);
    var factory = Mockito.mock(BobBsonConverterFactory.class);
    Mockito.when(factory.tryCreate(Mockito.eq(URL.class), Mockito.any(BobBson.class)))
        .thenReturn(converter);

    var sut = new BobBson();
    sut.registerFactory(factory);

    Assertions.assertEquals(converter, sut.tryFindConverter(URL.class));
  }

  @Test
  public void testDeserialise() throws Exception {
    var converter = Mockito.mock(BobBsonConverter.class);
    var factory = Mockito.mock(BobBsonConverterFactory.class);
    var reader = Mockito.mock(StackBsonReader.class);
    Mockito.when(factory.tryCreate(Mockito.eq(URL.class), Mockito.any(BobBson.class)))
        .thenReturn(converter);

    var sut = new BobBson();
    sut.registerFactory(factory);

    var url = new URL("http://localhost");
    Mockito.when(converter.read(reader)).thenReturn(url);

    var result = sut.deserialise(URL.class, reader);
    Assertions.assertEquals(url, result);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDeserialiseNull() throws Exception {
    var reader = Mockito.mock(StackBsonReader.class);

    var sut = new BobBson();

    Assertions.assertThrows(IllegalArgumentException.class, () -> sut.deserialise(null, reader));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSerialise() throws Exception {
    var converter = Mockito.mock(BobBsonConverter.class);
    var factory = Mockito.mock(BobBsonConverterFactory.class);
    var writer = Mockito.mock(StackBsonWriter.class);
    Mockito.when(factory.tryCreate(Mockito.eq(URL.class), Mockito.any(BobBson.class)))
        .thenReturn(converter);

    var sut = new BobBson();
    sut.registerFactory(factory);

    var url = new URL("http://localhost");
    sut.serialise(url, URL.class, writer);

    Mockito.verify(converter).write(writer, url);
  }
}
