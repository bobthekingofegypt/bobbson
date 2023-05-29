package org.bobstuff.bobbson.bsonCorpus;

import com.google.common.io.BaseEncoding;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.converters.BsonValueConverters;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class BsonCorpusRunner {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonCorpusProvider.class)
  public void runTestFile(BsonCorpus bsonCorpus, int index, BsonCorpus.BsonCorpusTestCaseType type)
      throws Exception {

    System.out.println(bsonCorpus.getDescription());
    System.out.println(index);
    if (type == BsonCorpus.BsonCorpusTestCaseType.VALID) {
      executeValidTest(bsonCorpus.getValid().get(index));
    } else if (type == BsonCorpus.BsonCorpusTestCaseType.DECODE_ERROR) {
      executeDecodeErrorTest(bsonCorpus.getDecodeErrors().get(index));
    }
  }

  private void executeDecodeErrorTest(BsonCorpusDecodeErrorCase decodeErrorCase) throws Exception {
    ByteBuffer readBuffer =
        ByteBuffer.wrap(BaseEncoding.base16().decode(decodeErrorCase.getBson().toUpperCase()))
            .order(ByteOrder.LITTLE_ENDIAN);
    BsonReader reader = new StackBsonReader(readBuffer);
    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    System.out.println(decodeErrorCase.getDescription());

    if (decodeErrorCase.isIgnore()) {
      // some tests don't make sense to run.  Invalid utf-8 is allowed in java it just replaces it
      // with
      // a replacement character.  And not consuming entire buffer when document length is correct
      // makes
      // no sense so we ignore these types of tests by marking the source file as ignore.
      return;
    }

    Assertions.assertThrows(
        Exception.class,
        () -> {
          bobBson.deserialise(BsonDocument.class, reader);
          if (readBuffer.remaining() != 0) {
            throw new Exception("garbage left in buffer after consuming valid data");
          }
        });
  }

  private void executeValidTest(BsonCorpusValidCase validCase) throws Exception {
    if (validCase.getCanonicalBson() == null) {
      throw new RuntimeException("canonical bson is undefined");
    }
    var canonicalBson = validCase.getCanonicalBson();

    ByteBuffer readBuffer =
        ByteBuffer.wrap(BaseEncoding.base16().decode(canonicalBson.toUpperCase()))
            .order(ByteOrder.LITTLE_ENDIAN);
    BsonReader reader = new StackBsonReader(readBuffer);
    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    BsonDocument document = bobBson.deserialise(BsonDocument.class, reader);

    if (readBuffer.remaining() != 0) {
      throw new Exception("WTF");
    }

    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[size]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BsonWriter writer = new StackBsonWriter(buffer);

    bobBson.serialise(document, BsonDocument.class, writer);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    buffer.pipe(os);
    os.flush();
    os.close();
    byte[] bytes = os.toByteArray();

    Assertions.assertEquals(
        validCase.getCanonicalBson().toUpperCase(), BaseEncoding.base16().encode(bytes));

    System.out.println(document);
  }
}
