package org.bobstuff.bobbson;

import java.util.stream.Stream;
import org.bobstuff.bobbson.activej.ActiveJBufferData;
import org.bobstuff.bobbson.buffer.BobBsonBuffer;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class BsonDataProvider implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    BufferDataBuilder activeJ = ActiveJBufferData::new;
    BufferDataBuilder bytesBuffer = ByteBufferBobBsonBuffer::new;
    return Stream.of(
        Arguments.of(Named.named("ByteBuffer", bytesBuffer)),
        Arguments.of(Named.named("ActiveJ", activeJ)));
  }

  public interface BufferDataBuilder {
    BobBsonBuffer build(byte[] data);
  }
}
