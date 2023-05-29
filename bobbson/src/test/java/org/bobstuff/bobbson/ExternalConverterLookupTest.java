package org.bobstuff.bobbson;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ExternalConverterLookupTest {
  public static class TestConverterRegister implements BobBsonConverterRegister {
    public static int registerCallCount;

    public TestConverterRegister() {
      registerCallCount = 0;
    }

    @Override
    public void register(BobBson bson) {
      registerCallCount += 1;
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testRegistersConverterFromClasspath() throws Exception {
    var mockCl = Mockito.mock(ClassLoader.class);
    var bobBson = Mockito.mock(BobBson.class);

    Mockito.when(
            mockCl.loadClass(
                "org.bobstuff.bobbson._ExternalConverterLookupTest$TestConverterRegister_BobBsonConverterRegister"))
        .thenReturn((Class) TestConverterRegister.class);

    var sut = new ExternalConverterLookup(List.of(mockCl));

    assertTrue(sut.lookupFromClasspath(TestConverterRegister.class, bobBson));

    assertEquals(1, TestConverterRegister.registerCallCount);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testRegistersConverterFromClasspathNonConverter() throws Exception {
    var mockCl = Mockito.mock(ClassLoader.class);
    var bobBson = Mockito.mock(BobBson.class);

    Mockito.when(
            mockCl.loadClass(
                "org.bobstuff.bobbson._ExternalConverterLookupTest$TestConverterRegister_BobBsonConverterRegister"))
        .thenReturn((Class) Object.class);

    var sut = new ExternalConverterLookup(List.of(mockCl));

    assertFalse(sut.lookupFromClasspath(TestConverterRegister.class, bobBson));
  }
}
