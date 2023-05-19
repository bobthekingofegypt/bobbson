package org.bobstuff.bobbson.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonReaderStack;
import org.bson.BsonBinaryWriter;
import org.bson.io.BasicOutputBuffer;

public class MDBBsonWriter {
  public static BsonBinaryWriter writer() {
    var output = new BasicOutputBuffer();
    var writer = new BsonBinaryWriter(output);
    writer.writeStartDocument();
    return writer;
  }

  public static byte[] data(BsonBinaryWriter writer) {
    writer.writeEndDocument();
    writer.flush();

    var output = writer.getBsonOutput();
    byte[] buffer = new byte[output.getSize()];

    if (output instanceof BasicOutputBuffer) {
      System.arraycopy(
          ((BasicOutputBuffer) output).getInternalBuffer(), 0, buffer, 0, output.getSize());
    } else {
      throw new IllegalStateException(
          "BSONBinaryWriter should have been initialised with a BasicOutputBuffer");
    }

    try {
      Files.write(Paths.get("/tmp/t.bin"), buffer);
    } catch (Exception e) {
    }

    return buffer;
  }

  public static BsonReader reader(BsonBinaryWriter writer) {
    return new BsonReaderStack(ByteBuffer.wrap(data(writer)).order(ByteOrder.LITTLE_ENDIAN));
  }
}
