package org.bobstuff.bobbson.buffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.base.Strings;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DynamicBobBsonBufferTest {
  public static final String TEST_STRING_1 = "this is a test string value of certain length";
  public static final String TEST_STRING_2 = "this is a different text string of certain length";
  public static final int TEST_STRING_1_LENGTH = TEST_STRING_1.getBytes(UTF_8).length;
  public static final int TEST_STRING_2_LENGTH = TEST_STRING_2.getBytes(UTF_8).length;
  DynamicBobBsonBuffer writeSut;
  BobBsonBufferPool exceptionPool =
      new NoopBobBsonBufferPool(
          (size) -> {
            throw new RuntimeException("shouldn't be called");
          });
  BobBsonBufferPool smallBufferPool =
      new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[50], 0, 0));

  @BeforeEach
  public void setup() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[50], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    writeSut.writeString(TEST_STRING_1);
    writeSut.writeInteger(30);
    writeSut.writeInteger(48);
    writeSut.writeString(TEST_STRING_2);
  }

  @Test
  public void testReadContentRollsBuffers() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool(
            (size) -> {
              throw new RuntimeException("shouldn't be called");
            });
    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), pool);
    assertEquals(0, readSut.getHead());
    assertEquals(TEST_STRING_1, readSut.getString(TEST_STRING_1.getBytes(UTF_8).length));
    assertEquals(30, readSut.getInt());
    assertEquals(48, readSut.getInt());
    assertEquals(TEST_STRING_2, readSut.getString(TEST_STRING_2.getBytes(UTF_8).length));
  }

  @Test
  public void testSkipHeadRollsBuffers() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool(
            (size) -> {
              throw new RuntimeException("shouldn't be called");
            });
    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), pool);
    readSut.skipHead(53);
    assertEquals(TEST_STRING_2, readSut.getString(TEST_STRING_2.getBytes(UTF_8).length));
    readSut.skipHead(-TEST_STRING_2.getBytes(UTF_8).length);
    assertEquals(TEST_STRING_2, readSut.getString(TEST_STRING_2.getBytes(UTF_8).length));
  }

  @Test
  public void testSkipTailRollsBuffers() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(pool);
    readSut.skipTail(53);
    assertEquals(53, readSut.getTail());
    assertEquals(6, readSut.getBuffers().size());
    assertEquals(3, readSut.getBuffers().get(readSut.getBuffers().size() - 1).getTail());
    readSut.skipTail(-53);
    assertEquals(0, readSut.getTail());
    assertEquals(1, readSut.getBuffers().size());
    assertEquals(0, readSut.getBuffers().get(0).getTail());
  }

  @Test
  public void testSkipTailRollsBuffersDontResetToZero() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(pool);
    readSut.skipTail(53);
    assertEquals(53, readSut.getTail());
    assertEquals(6, readSut.getBuffers().size());
    assertEquals(3, readSut.getBuffers().get(readSut.getBuffers().size() - 1).getTail());
    readSut.skipTail(-49);
    assertEquals(4, readSut.getTail());
    assertEquals(1, readSut.getBuffers().size());
    assertEquals(4, readSut.getBuffers().get(0).getTail());
  }

  @Test
  public void testSkipTailRollsBuffersEndsOnBufferX() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(pool);
    readSut.skipTail(53);
    assertEquals(53, readSut.getTail());
    assertEquals(6, readSut.getBuffers().size());
    assertEquals(3, readSut.getBuffers().get(readSut.getBuffers().size() - 1).getTail());
    readSut.skipTail(-40);
    assertEquals(13, readSut.getTail());
    assertEquals(2, readSut.getBuffers().size());
    assertEquals(3, readSut.getBuffers().get(1).getTail());
  }

  @Test
  public void testGetReadRemaining() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool(
            (size) -> {
              throw new RuntimeException("shouldn't be called");
            });
    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), pool);
    assertEquals(TEST_STRING_1_LENGTH + TEST_STRING_2_LENGTH + 8, readSut.getReadRemaining());
  }

  @Test
  public void testGetReadRemainingAfterSkip() {
    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    readSut.skipHead(TEST_STRING_1_LENGTH);
    assertEquals(TEST_STRING_2_LENGTH + 8, readSut.getReadRemaining());
  }

  @Test
  public void testGetIntRollsBuffers() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[50], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    for (var i = 0; i < 1000; i += 1) {
      writeSut.writeInteger(i);
    }

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    for (var i = 0; i < 1000; i += 1) {
      assertEquals(i, readSut.getInt());
    }

    assertEquals(1000 * 4, readSut.getHead());
  }

  @Test
  public void testGetByteRollsBuffers() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    for (var i = 0; i < 1000; i += 1) {
      writeSut.writeByte((byte) i);
    }

    assertEquals(1000, writeSut.getTail());

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    for (var i = 0; i < 1000; i += 1) {
      assertEquals((byte) i, readSut.getByte());
    }

    assertEquals(1000, readSut.getHead());
  }

  @Test
  public void testGetLongRollsBuffers() {
    writeSut = new DynamicBobBsonBuffer(smallBufferPool);
    for (var i = 0; i < 1000; i += 1) {
      writeSut.writeLong(i);
    }

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    for (var i = 0; i < 1000; i += 1) {
      assertEquals(i, readSut.getLong());
    }

    assertEquals(1000 * 8, readSut.getHead());
  }

  @Test
  public void testGetDoubleRollsBuffers() {
    writeSut = new DynamicBobBsonBuffer(smallBufferPool);
    for (var i = 0; i < 1000; i += 1) {
      writeSut.writeDouble(i);
    }

    for (var i = 0; i < writeSut.getBuffers().size() - 1; i += 1) {
      assertEquals(50, writeSut.getBuffers().get(i).getTail());
    }

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    for (var i = 0; i < 1000; i += 1) {
      assertEquals(i, readSut.getDouble());
    }

    assertEquals(1000 * 8, readSut.getHead());
  }

  @Test
  public void testGetBytesFitsBuffer() {
    var words = "words";
    var wordsBytes = words.getBytes(UTF_8);

    writeSut = new DynamicBobBsonBuffer(smallBufferPool);
    writeSut.writeBytes(wordsBytes);

    assertEquals(wordsBytes.length, writeSut.getTail());

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    byte[] data = readSut.getBytes(wordsBytes.length);

    assertArrayEquals(wordsBytes, data);

    assertEquals(wordsBytes.length, readSut.getHead());
    assertEquals(5, readSut.getHead());
  }

  @Test
  public void testGetBytesRollsBuffers() {
    var words = Strings.repeat("words are scary", 200);
    var wordsBytes = words.getBytes(UTF_8);

    writeSut = new DynamicBobBsonBuffer(smallBufferPool);
    writeSut.writeBytes(wordsBytes);

    assertEquals(wordsBytes.length, writeSut.getTail());

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    byte[] data = readSut.getBytes(wordsBytes.length);

    assertArrayEquals(wordsBytes, data);

    assertEquals(wordsBytes.length, readSut.getHead());
    assertEquals(3000, readSut.getHead());
  }

  @Test
  public void testGetStringRollsBuffers() {
    var words = Strings.repeat("words are scary", 200);

    writeSut = new DynamicBobBsonBuffer(smallBufferPool);
    writeSut.writeString(words);

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    String data = readSut.getString(words.length());

    assertEquals(words, data);

    assertEquals(words.length(), readSut.getHead());
  }

  @Test
  public void testGetWriteRemaining() {
    assertEquals(Integer.MAX_VALUE, writeSut.getWriteRemaining());
  }

  @Test
  public void testReadUntilDoesntFindOnRollsBuffers() {
    var data =
        new byte[] {
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        };
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    writeSut.writeBytes(data);

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);

    assertThrows(IllegalStateException.class, () -> readSut.readUntil((byte) 0));
  }

  @Test
  public void testReadUntilRollsBuffers() {
    var data =
        new byte[] {
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          0
        };
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    writeSut.writeBytes(data);

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    int count = readSut.readUntil((byte) 0);

    assertEquals(31, count);
    assertEquals(31, readSut.getHead());
  }

  @Test
  public void testGetFieldNameRollsBuffers() {
    var dataString = "this is a very long string that is used to test that name crosses buffers";
    var data = dataString.getBytes(UTF_8);
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    writeSut.writeBytes(data);
    writeSut.writeByte((byte) 0);

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    int count = readSut.readUntil((byte) 0);
    BobBsonBuffer.ByteRangeComparator comparitor = readSut.getByteRangeComparator();
    Assertions.assertEquals(dataString, comparitor.value());
    assertEquals(dataString.length() + 1, count);

    readSut.setHead(4);
    Assertions.assertEquals(dataString, comparitor.value());
    Assertions.assertEquals(4, readSut.getHead());
  }

  @Test
  public void testComparitorEqualsArrayWhenRollsBuffers() {
    var dataString = "this is a very long string that is used to test that name crosses buffers";
    var data = dataString.getBytes(UTF_8);
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    writeSut.writeBytes(data);
    writeSut.writeByte((byte) 0);

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    BobBsonBuffer.ByteRangeComparator comparitor = readSut.getByteRangeComparator();
    int count = readSut.readUntil((byte) 0);
    assertTrue(comparitor.equalsArray(data));
    // try starting somewhere in middle of stream
    readSut.setHead(4);
    assertTrue(comparitor.equalsArray(data));
    Assertions.assertEquals(4, readSut.getHead());
    assertFalse(comparitor.equalsArray(new byte[] {1, 2, 3, 4}));
  }

  @Test
  public void testComparitorEqualsArrayWeakHashWhenRollsBuffers() {
    var dataString = "this is a very long string that is used to test that name crosses buffers";
    var data = dataString.getBytes(UTF_8);
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    writeSut.writeBytes(data);
    writeSut.writeByte((byte) 0);

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    BobBsonBuffer.ByteRangeComparator comparitor = readSut.getByteRangeComparator();
    int count = readSut.readUntil((byte) 0);
    assertTrue(comparitor.equalsArray(data, 23));
    // try starting somewhere in middle of stream
    readSut.setHead(4);
    assertTrue(comparitor.equalsArray(data, 23));
    Assertions.assertEquals(4, readSut.getHead());
  }

  @Test
  public void testSetTail() {
    var dataString = "this is a very long string that is used to test that name crosses buffers";
    var data = dataString.getBytes(UTF_8);
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    writeSut.writeBytes(data);
    writeSut.writeByte((byte) 0);

    DynamicBobBsonBuffer readSut = new DynamicBobBsonBuffer(writeSut.getBuffers(), exceptionPool);
    BobBsonBuffer.ByteRangeComparator comparitor = readSut.getByteRangeComparator();
    readSut.setTail(data.length - 7);
    readSut.writeBytes("bananas".getBytes(UTF_8));
    writeSut.writeByte((byte) 0);

    int count = readSut.readUntil((byte) 0);
    System.out.println(comparitor.value());
  }

  @Test
  public void testReadAndWrite() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    writeSut.writeInteger(14);
    writeSut.writeInteger(25);

    assertEquals(14, writeSut.getInt());

    writeSut.writeBytes("hello".getBytes(UTF_8));

    assertEquals(25, writeSut.getInt());

    writeSut.writeByte((byte) 0);

    BobBsonBuffer.ByteRangeComparator comparator = writeSut.getByteRangeComparator();
    writeSut.readUntil((byte) 0);

    assertEquals("hello", comparator.value());
  }

  @Test
  public void testReadBeyondTailFails() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    Assertions.assertThrows(IllegalStateException.class, () -> writeSut.getInt());
  }

  @Test
  public void testReadBeyondTailAfterTailSetFails() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    writeSut.writeBytes("this is some text to make the buffer roll over somewhere".getBytes(UTF_8));
    writeSut.setTail(2);
    Assertions.assertThrows(IllegalStateException.class, () -> writeSut.getInt());
    Assertions.assertThrows(IllegalStateException.class, () -> writeSut.getLong());
    Assertions.assertThrows(IllegalStateException.class, () -> writeSut.getDouble());
    Assertions.assertThrows(IllegalStateException.class, () -> writeSut.getString(5));
    Assertions.assertThrows(IllegalStateException.class, () -> writeSut.getBytes(5));
    writeSut.getByte();
    writeSut.getByte();
    Assertions.assertThrows(IllegalStateException.class, () -> writeSut.getByte());
  }

  @Test
  public void testWriteBytesWithinRemainingLimit() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[1000], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);
    String text = "this is some text to make the buffer roll over somewhere";
    writeSut.writeBytes(text.getBytes(UTF_8));
    writeSut.writeByte((byte) 0);

    writeSut.readUntil((byte) 0);
    assertEquals(text, writeSut.getByteRangeComparator().value());
  }

  @Test
  public void testGetLimit() {
    assertEquals(Integer.MAX_VALUE, writeSut.getLimit());
  }

  @Test
  public void testWriteIntegerAtPosition() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);

    writeSut.skipTail(4);
    writeSut.writeDouble(2.3);
    writeSut.writeDouble(5.3);
    writeSut.writeInteger(0, 34);

    assertEquals(34, writeSut.getInt());
    assertEquals(2.3, writeSut.getDouble());
    assertEquals(5.3, writeSut.getDouble());
  }

  @Test
  public void testWriteIntegerAtRollsBuffer() {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);

    writeSut.writeDouble(2.3);
    writeSut.skipTail(4);
    writeSut.writeDouble(5.3);
    writeSut.writeInteger(8, 34);

    assertEquals(2.3, writeSut.getDouble());
    assertEquals(34, writeSut.getInt());
    assertEquals(5.3, writeSut.getDouble());
  }

  @Test
  public void testWriteReadUnicodeCharsRolls() {
    var text =
        "оЂ мЎ Боↁ іт'ѕ а ъцисЂ оf сѓаzЎ циісоↁэ тэхт ₕ ₘy gₒd ᵢₜ'ₛ ₐ bᵤₙcₕ ₒf cᵣₐzy ᵤₙᵢcₒdₑ ₜₑₓₜ"
            + " öḧ ṁÿ ġöḋ ïẗ'ṡ ä ḅüṅċḧ öḟ ċṛäżÿ üṅïċöḋë ẗëẍẗ \uD808\uDC04 \uD808\uDC2A";
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[10], 0, 0));
    writeSut = new DynamicBobBsonBuffer(pool);

    writeSut.writeInteger(4);
    writeSut.writeString(text);
    writeSut.writeByte((byte) 0);

    assertEquals(4, writeSut.getInt());
    int length = writeSut.readUntil((byte) 0);
    assertEquals(text, writeSut.getByteRangeComparator().value());
  }

  @Test
  public void testGetArray() {
    Assertions.assertNull(writeSut.getArray());
  }
}
