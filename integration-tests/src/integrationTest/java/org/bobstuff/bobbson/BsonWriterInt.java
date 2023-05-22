package org.bobstuff.bobbson;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BsonWriterInt {
  @Test
  public void testStringWrite() throws Exception {
    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new ByteBufferBobBsonBuffer(new byte[10]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    BsonWriter writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeString("name", "bob");
    writer.writeEndDocument();

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    buffer.pipe(os);
    os.flush();
    os.close();
    byte[] bytes = os.toByteArray();

    Files.write(Paths.get("/tmp/data.bin"), bytes);

    BsonReader reader = new BsonReaderStack(new ByteBufferBobBsonBuffer(bytes));
    reader.readStartDocument();
    Assertions.assertEquals(BsonType.STRING, reader.readBsonType());
    Assertions.assertEquals("name", reader.currentFieldName());
    Assertions.assertEquals("bob", reader.readString());
    reader.readEndDocument();
  }

  @Test
  public void testMultipleStringWrite() throws Exception {
    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new ByteBufferBobBsonBuffer(new byte[size]));
    ByteBufferBobBsonBuffer buffer = new ByteBufferBobBsonBuffer(new byte[2048]);

    BsonWriter writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeString("name", "bob");
    writer.writeString("job", "programmer");
    writer.writeString("address", "cupboard");
    writer.writeEndDocument();

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    buffer.pipe(os);
    os.flush();
    os.close();
    byte[] bytes = os.toByteArray();

    Files.write(Paths.get("/tmp/data.bin"), bytes);

    BsonReader reader = new BsonReaderStack(new ByteBufferBobBsonBuffer(bytes));
    reader.readStartDocument();
    Assertions.assertEquals(BsonType.STRING, reader.readBsonType());
    Assertions.assertEquals("name", reader.currentFieldName());
    Assertions.assertEquals("bob", reader.readString());
    Assertions.assertEquals(BsonType.STRING, reader.readBsonType());
    Assertions.assertEquals("job", reader.currentFieldName());
    Assertions.assertEquals("programmer", reader.readString());
    Assertions.assertEquals(BsonType.STRING, reader.readBsonType());
    Assertions.assertEquals("address", reader.currentFieldName());
    Assertions.assertEquals("cupboard", reader.readString());
    reader.readEndDocument();
  }

  @Test
  public void testMultipleStringWriteActiveJ() throws Exception {
    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new ByteBufferBobBsonBuffer(new byte[size]));
    ByteBufferBobBsonBuffer buffer = new ByteBufferBobBsonBuffer(new byte[2048]);

    BsonWriter writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeString("name", "bob");
    writer.writeString("job", "programmer");
    writer.writeString("address", "cupboard");
    writer.writeEndDocument();

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    buffer.pipe(os);
    os.flush();
    os.close();
    byte[] bytes = os.toByteArray();

    Files.write(Paths.get("/tmp/data.bin"), bytes);

    BsonReader reader = new BsonReaderStack(new ByteBufferBobBsonBuffer(bytes));
    reader.readStartDocument();
    Assertions.assertEquals(BsonType.STRING, reader.readBsonType());
    Assertions.assertEquals("name", reader.currentFieldName());
    Assertions.assertEquals("bob", reader.readString());
    Assertions.assertEquals(BsonType.STRING, reader.readBsonType());
    Assertions.assertEquals("job", reader.currentFieldName());
    Assertions.assertEquals("programmer", reader.readString());
    Assertions.assertEquals(BsonType.STRING, reader.readBsonType());
    Assertions.assertEquals("address", reader.currentFieldName());
    Assertions.assertEquals("cupboard", reader.readString());
    reader.readEndDocument();
  }
}
