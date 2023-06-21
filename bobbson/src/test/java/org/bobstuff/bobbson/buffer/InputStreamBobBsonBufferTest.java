package org.bobstuff.bobbson.buffer;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InputStreamBobBsonBufferTest {
  @Test
  public void testReadStream() {
    byte[] data = new byte[2048 * 2];
    var buffer = new BobBufferBobBsonBuffer(data, 0, 0);
    var writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeString("key1", "value 1");
    writer.writeString("key2", "value 2");
    writer.writeString("key3", "value 3");
    writer.writeString("key4", "value 4");
    writer.writeString("key5", "value 5");
    writer.writeEndDocument();

    var stream = new ByteArrayInputStream(buffer.toByteArray());

    var input = new InputStreamBobBsonBuffer(stream);
    var reader = new StackBsonReader(input);
    reader.readStartDocument();
    reader.readBsonType();
    Assertions.assertEquals("value 1", reader.readString());
    reader.readBsonType();
    Assertions.assertEquals("value 2", reader.readString());
    reader.readBsonType();
    Assertions.assertEquals("value 3", reader.readString());
    reader.readBsonType();
    Assertions.assertEquals("value 4", reader.readString());
    reader.readBsonType();
    Assertions.assertEquals("value 5", reader.readString());
    reader.readEndDocument();

    Assertions.assertEquals(0, input.getReadRemaining());
  }

  @Test
  public void testReadStreamTinyBuffer() {
    byte[] data = new byte[2048 * 2];
    var buffer = new BobBufferBobBsonBuffer(data, 0, 0);
    var writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeString("key1", "value 1");
    writer.writeString("key2", "value 2");
    writer.writeString("key3", "value 3");
    writer.writeString("key4", "value 4");
    writer.writeString("key5", "value 5");
    writer.writeEndDocument();

    var stream = new ByteArrayInputStream(buffer.toByteArray());

    var input = new InputStreamBobBsonBuffer(stream, 16);
    var reader = new StackBsonReader(input);
    reader.readStartDocument();
    reader.readBsonType();
    Assertions.assertEquals("value 1", reader.readString());
    reader.readBsonType();
    Assertions.assertEquals("value 2", reader.readString());
    reader.readBsonType();
    Assertions.assertEquals("value 3", reader.readString());
    reader.readBsonType();
    Assertions.assertEquals("value 4", reader.readString());
    reader.readBsonType();
    Assertions.assertEquals("value 5", reader.readString());
    reader.readEndDocument();

    Assertions.assertEquals(0, input.getReadRemaining());
  }

  @Test
  public void testGetNumbers() {
    byte[] data = new byte[2048 * 2];
    var buffer = new BobBufferBobBsonBuffer(data, 0, 0);
    var writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeString("key1", "value 1");
    writer.writeInteger("key2", 2);
    writer.writeLong("key3", 445348957934L);
    writer.writeDouble("key4", 2345.343D);
    writer.writeEndDocument();

    var stream = new ByteArrayInputStream(buffer.toByteArray());

    var input = new InputStreamBobBsonBuffer(stream, 16);
    var reader = new StackBsonReader(input);
    reader.readStartDocument();
    reader.readBsonType();
    Assertions.assertEquals("value 1", reader.readString());
    reader.readBsonType();
    Assertions.assertEquals(2, reader.readInt32());
    reader.readBsonType();
    Assertions.assertEquals(445348957934L, reader.readInt64());
    reader.readBsonType();
    Assertions.assertEquals(2345.343D, reader.readDouble());
    reader.readEndDocument();

    Assertions.assertEquals(0, input.getReadRemaining());
  }

  @Test
  public void testReadStringToBigForTinyBuffer() {
    byte[] data = new byte[2048 * 2];
    var buffer = new BobBufferBobBsonBuffer(data, 0, 0);
    var writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeString("key1", "value 1 is longer than 16 bytes");
    writer.writeEndDocument();

    var stream = new ByteArrayInputStream(buffer.toByteArray());

    var input = new InputStreamBobBsonBuffer(stream, 16);
    var reader = new StackBsonReader(input);
    reader.readStartDocument();
    reader.readBsonType();
    Assertions.assertEquals("value 1 is longer than 16 bytes", reader.readString());
    reader.readEndDocument();

    Assertions.assertEquals(0, input.getReadRemaining());
  }

  @Test
  public void testReadBytes() {
    byte[] data = new byte[2048 * 2];
    var buffer = new BobBufferBobBsonBuffer(data, 0, 0);
    var writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeBinary(
        "key1",
        (byte) 2,
        "value 1 is longer than 16 bytes indeed".getBytes(StandardCharsets.UTF_8));
    writer.writeEndDocument();

    var stream = new ByteArrayInputStream(buffer.toByteArray());

    var input = new InputStreamBobBsonBuffer(stream, 16);
    var reader = new StackBsonReader(input);
    reader.readStartDocument();
    reader.readBsonType();
    Assertions.assertEquals(
        "value 1 is longer than 16 bytes indeed",
        new String(reader.readBinary().getData(), StandardCharsets.UTF_8));
    reader.readEndDocument();

    Assertions.assertEquals(0, input.getReadRemaining());
  }
  @Test
  public void testReadBytesRequiresRollButFitsBuffer() {
    byte[] data = new byte[2048 * 2];
    var buffer = new BobBufferBobBsonBuffer(data, 0, 0);
    var writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeLong("names", 23L);
    writer.writeBinary(
            "key1",
            (byte) 2,
            "value 12".getBytes(StandardCharsets.UTF_8));
    writer.writeEndDocument();

    var stream = new ByteArrayInputStream(buffer.toByteArray());

    var input = new InputStreamBobBsonBuffer(stream, 16);
    var reader = new StackBsonReader(input);
    reader.readStartDocument();
    reader.readBsonType();
    Assertions.assertEquals(
            23L,
            reader.readInt64());

    reader.readBsonType();
    Assertions.assertEquals(
            "value 12",
            new String(reader.readBinary().getData(), StandardCharsets.UTF_8));
    reader.readEndDocument();

    Assertions.assertEquals(0, input.getReadRemaining());
  }

  @Test
  public void testSkipForward() {
    byte[] data = new byte[2048 * 2];
    var buffer = new BobBufferBobBsonBuffer(data, 0, 0);
    var writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeString("key1", "value 1 is longer than 16 bytes indeed");
    writer.writeEndDocument();

    var stream = new ByteArrayInputStream(buffer.toByteArray());

    var input = new InputStreamBobBsonBuffer(stream, 16);
    var reader = new StackBsonReader(input);
    reader.readStartDocument();
    reader.readBsonType();
    reader.skipValue();
    reader.readEndDocument();

    Assertions.assertEquals(0, input.getReadRemaining());
  }
}
