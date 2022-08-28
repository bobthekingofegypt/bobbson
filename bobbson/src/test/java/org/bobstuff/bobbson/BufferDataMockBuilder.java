package org.bobstuff.bobbson;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;

public class BufferDataMockBuilder {
  private List<Integer> intResults;
  private List<Byte> byteResults;
  private List<Integer> readUntilResults;
  private List<String> readStringResults;

  public BufferDataMockBuilder() {
    intResults = new ArrayList<>();
    byteResults = new ArrayList<>();
    readUntilResults = new ArrayList<>();
    readStringResults = new ArrayList<>();
  }

  public static BufferDataMockBuilder builder() {
    var builder = new BufferDataMockBuilder();
    return builder;
  }

  public BufferDataMockBuilder addDocumentLength(int length) {
    intResults.add(length);
    return this;
  }

  public BufferDataMockBuilder addIntValue(int length) {
    intResults.add(length);
    return this;
  }

  public BufferDataMockBuilder addReadStringValue(String s) {
    intResults.add(s.length());
    readStringResults.add(s);
    byteResults.add((byte) 0);
    return this;
  }

  public BufferDataMockBuilder addType(BsonType type) {
    byteResults.add((byte) type.getValue());
    return this;
  }

  public BufferDataMockBuilder addReadUntil(int length) {
    readUntilResults.add(length);
    return this;
  }

  public BobBsonBuffer build() {
    ByteBufferBobBsonBuffer buffer = mock(ByteBufferBobBsonBuffer.class);
    doAnswer((invocation) -> intResults.remove(0)).when(buffer).getInt();
    doAnswer((invocation) -> byteResults.remove(0)).when(buffer).getByte();
    doAnswer((invocation) -> readUntilResults.remove(0)).when(buffer).readUntil((byte) 0);
    doAnswer((invocation) -> readStringResults.remove(0)).when(buffer).getString(Mockito.anyInt());
    return buffer;
  }
}
