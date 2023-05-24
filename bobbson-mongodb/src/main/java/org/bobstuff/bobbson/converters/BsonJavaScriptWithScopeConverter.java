package org.bobstuff.bobbson.converters;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.bson.BsonDocument;
import org.bson.BsonJavaScriptWithScope;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonJavaScriptWithScopeConverter implements BobBsonConverter<BsonJavaScriptWithScope> {
  private BobBson bobBson;

  public BsonJavaScriptWithScopeConverter(BobBson bobBson) {
    this.bobBson = bobBson;
  }

  @Override
  public @Nullable BsonJavaScriptWithScope read(BsonReader bsonReader) {
    var codeWithScope = bsonReader.readCodeWithScope();
    BsonReader reader =
        new BsonReaderStack(
            ByteBuffer.wrap(codeWithScope.getScope()).order(ByteOrder.LITTLE_ENDIAN));
    BsonDocument document;
    try {
      document = bobBson.deserialise(BsonDocument.class, reader);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (document == null) {
      throw new IllegalStateException("document shouldn't be null");
    }

    return new BsonJavaScriptWithScope(codeWithScope.getCode(), document);
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter,
      byte @Nullable [] key,
      @NonNull BsonJavaScriptWithScope value) {
    var code = value.getCode();
    var scope = value.getScope();

    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[size]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BsonWriter writer = new StackBsonWriter(buffer);

    byte[] scopeBytes;

    try {
      bobBson.serialise(scope, BsonDocument.class, writer);

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      buffer.pipe(os);
      os.flush();
      os.close();
      scopeBytes = os.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    if (key == null) {
      bsonWriter.writeCodeWithScope(code, scopeBytes);
    } else {
      bsonWriter.writeCodeWithScope(key, code, scopeBytes);
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonJavaScriptWithScope value) {
    var code = value.getCode();
    var scope = value.getScope();

    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[size]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BsonWriter writer = new StackBsonWriter(buffer);

    byte[] scopeBytes;

    try {
      bobBson.serialise(scope, BsonDocument.class, writer);

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      buffer.pipe(os);
      os.flush();
      os.close();
      scopeBytes = os.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    bsonWriter.writeCodeWithScope(code, scopeBytes);
  }
}
