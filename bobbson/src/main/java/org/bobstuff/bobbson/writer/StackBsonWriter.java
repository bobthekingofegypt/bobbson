package org.bobstuff.bobbson.writer;

import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.*;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("PMD.NullAssignment")
public class StackBsonWriter implements BsonWriter {
  private BobBsonBuffer buffer;
  private ContextStack contextStack;
  private @Nullable String name;
  private byte @Nullable [] nameBytes;
  private BsonState state;
  private byte[] indexNumberCache = new byte[32];

  public StackBsonWriter(BufferDataPool bufferDataPool) {
    this(bufferDataPool, new ContextStack());
  }

  public StackBsonWriter(BufferDataPool bufferDataPool, ContextStack contextStack) {
    this(new ByteBufferBobBsonBuffer(new byte[1024]), contextStack);
  }

  public StackBsonWriter(BobBsonBuffer buffer) {
    this(buffer, new ContextStack());
  }

  public StackBsonWriter(BobBsonBuffer buffer, ContextStack contextStack) {
    this.contextStack = contextStack;
    this.buffer = buffer;
    this.name = null;
    this.nameBytes = null;
    this.state = BsonState.INITIAL;
  }

  public void reset() {
    this.contextStack.reset();
    this.state = BsonState.INITIAL;
  }

  @Override
  public void writeStartDocument() {
    if (this.state == BsonState.VALUE || this.state == BsonState.NAME) {
      buffer.writeByte((byte) BsonType.DOCUMENT.getValue());
      writeNameValue();
    }
    contextStack.add(0, buffer.getTail(), BsonContextType.DOCUMENT);
    buffer.skipTail(4);
    state = BsonState.NAME;
  }

  @Override
  public void writeStartDocument(byte[] field) {
    buffer.writeByte((byte) BsonType.DOCUMENT.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    contextStack.add(0, buffer.getTail(), BsonContextType.DOCUMENT);
    buffer.skipTail(4);
    state = BsonState.NAME;
  }

  @Override
  public void writeStartDocument(String name) {
    buffer.writeByte((byte) BsonType.DOCUMENT.getValue());
    buffer.writeBytes(name.getBytes(StandardCharsets.UTF_8));
    buffer.writeByte((byte) 0);
    contextStack.add(0, buffer.getTail(), BsonContextType.DOCUMENT);
    buffer.skipTail(4);
    state = BsonState.NAME;
  }

  @Override
  public void writeStartArray(byte[] field) {
    buffer.writeByte((byte) BsonType.ARRAY.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    contextStack.add(0, buffer.getTail(), BsonContextType.ARRAY);
    buffer.skipTail(4);
    state = BsonState.VALUE;
  }

  @Override
  public void writeStartArray(String field) {
    buffer.writeByte((byte) BsonType.ARRAY.getValue());
    buffer.writeBytes(field.getBytes(StandardCharsets.UTF_8));
    buffer.writeByte((byte) 0);
    contextStack.add(0, buffer.getTail(), BsonContextType.ARRAY);
    buffer.skipTail(4);
    state = BsonState.VALUE;
  }

  @Override
  public void writeStartArray() {
    buffer.writeByte((byte) BsonType.ARRAY.getValue());
    writeNameValue();
    contextStack.add(0, buffer.getTail(), BsonContextType.ARRAY);
    buffer.skipTail(4);
    state = BsonState.VALUE;
  }

  @Override
  public void writeEndDocument() {
    buffer.writeByte((byte) 0);
    int startPosition = contextStack.getCurrentStartPosition();
    contextStack.pop();
    buffer.writeInteger(startPosition, buffer.getTail() - startPosition);

    if (contextStack.isRootContext()) {
      state = BsonState.DONE;
    } else {
      setNextState();
    }
  }

  @Override
  public void writeEndArray() {
    writeEndDocument();
  }

  private void setNextState() {
    if (contextStack.getCurrentBsonType() == BsonContextType.ARRAY) {
      state = BsonState.VALUE;
    } else {
      state = BsonState.NAME;
    }
  }

  private void writeNameValue() {
    if (this.nameBytes != null) {
      buffer.writeBytes(this.nameBytes);
      buffer.writeByte((byte) 0);
      this.nameBytes = null;
    } else if (this.name != null) {
      buffer.writeString(this.name);
      buffer.writeByte((byte) 0);
      this.name = null;
    } else if (contextStack.context.getCurrentBsonType() == BsonContextType.ARRAY) {
      var index = contextStack.context.getAndIncrementArrayIndex();
      var size = stringSize(index);
      var c = getChars(index, size, indexNumberCache);
//      buffer.writeString((contextStack.context.getAndIncrementArrayIndex()));
      buffer.writeBytes(indexNumberCache, 0, size);
      buffer.writeByte((byte) 0);
    } else {
      throw new IllegalStateException("write name value is confused");
    }
  }
  static final byte[] DigitOnes = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57};
  static final byte[] DigitTens = new byte[]{48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 52, 52, 52, 52, 52, 52, 52, 52, 52, 52, 53, 53, 53, 53, 53, 53, 53, 53, 53, 53, 54, 54, 54, 54, 54, 54, 54, 54, 54, 54, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 57, 57, 57, 57, 57, 57, 57, 57, 57, 57};

  static int getChars(int i, int index, byte[] buf) {
    int charPos = index;
    boolean negative = i < 0;
    if (!negative) {
      i = -i;
    }

    int q;
    int r;
    while(i <= -100) {
      q = i / 100;
      r = q * 100 - i;
      i = q;
      --charPos;
      buf[charPos] = DigitOnes[r];
      --charPos;
      buf[charPos] = DigitTens[r];
    }

    q = i / 10;
    r = q * 10 - i;
    --charPos;
    buf[charPos] = (byte)(48 + r);
    if (q < 0) {
      --charPos;
      buf[charPos] = (byte)(48 - q);
    }

    if (negative) {
      --charPos;
      buf[charPos] = 45;
    }

    return charPos;
  }

  static int stringSize(int x) {
    int d = 1;
    if (x >= 0) {
      d = 0;
      x = -x;
    }

    int p = -10;

    for(int i = 1; i < 10; ++i) {
      if (x > p) {
        return i + d;
      }

      p = 10 * p;
    }

    return 10 + d;
  }

  @Override
  public void writeName(String name) {
    this.name = name;
    nameBytes = null;
    state = BsonState.VALUE;
  }

  @Override
  public void writeName(byte[] name) {
    this.name = null;
    nameBytes = name;
    state = BsonState.VALUE;
  }

  @Override
  public void writeRegex(String field, String regex, String options) {
    buffer.writeByte((byte) BsonType.REGULAR_EXPRESSION.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    buffer.writeString(regex);
    buffer.writeByte((byte) 0);
    buffer.writeString(options);
    buffer.writeByte((byte) 0);
    setNextState();
  }

  @Override
  public void writeRegex(byte[] field, String regex, String options) {
    buffer.writeByte((byte) BsonType.REGULAR_EXPRESSION.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    buffer.writeString(regex);
    buffer.writeByte((byte) 0);
    buffer.writeString(options);
    buffer.writeByte((byte) 0);
    setNextState();
  }

  @Override
  public void writeRegex(String regex, String options) {
    buffer.writeByte((byte) BsonType.REGULAR_EXPRESSION.getValue());
    writeNameValue();
    buffer.writeString(regex);
    buffer.writeByte((byte) 0);
    buffer.writeString(options);
    buffer.writeByte((byte) 0);
    setNextState();
  }

  @Override
  public void writeBinary(String field, byte type, byte[] data) {
    buffer.writeByte((byte) BsonType.BINARY.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeByte(type);
    if (type == (byte) 2) {
      buffer.writeInteger(data.length);
    }
    buffer.writeBytes(data);
    buffer.writeInteger(mark, buffer.getTail() - mark - 5);
    setNextState();
  }

  @Override
  public void writeBinary(byte[] field, byte type, byte[] data) {
    buffer.writeByte((byte) BsonType.BINARY.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeByte(type);
    if (type == (byte) 2) {
      buffer.writeInteger(data.length);
    }
    buffer.writeBytes(data);
    buffer.writeInteger(mark, buffer.getTail() - mark - 5);
    setNextState();
  }

  @Override
  public void writeBinary(byte type, byte[] data) {
    buffer.writeByte((byte) BsonType.BINARY.getValue());
    writeNameValue();
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeByte(type);
    if (type == (byte) 2) {
      buffer.writeInteger(data.length);
    }
    buffer.writeBytes(data);
    buffer.writeInteger(mark, buffer.getTail() - mark - 5);
    setNextState();
  }

  @Override
  public void writeDbPointer(String field, String namespace, byte[] objectId) {
    buffer.writeByte((byte) BsonType.DB_POINTER.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(namespace);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    buffer.writeBytes(objectId);
    setNextState();
  }

  @Override
  public void writeDbPointer(byte[] field, String namespace, byte[] objectId) {
    buffer.writeByte((byte) BsonType.DB_POINTER.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(namespace);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    buffer.writeBytes(objectId);
    setNextState();
  }

  @Override
  public void writeDbPointer(String namespace, byte[] objectId) {
    buffer.writeByte((byte) BsonType.DB_POINTER.getValue());
    writeNameValue();
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(namespace);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    buffer.writeBytes(objectId);
    setNextState();
  }

  @Override
  public void writeCodeWithScope(String field, String code, byte[] scope) {
    buffer.writeByte((byte) BsonType.JAVASCRIPT_WITH_SCOPE.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    var stringMark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(code);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(stringMark, buffer.getTail() - stringMark - 4);
    buffer.writeBytes(scope);
    //    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark);
    setNextState();
  }

  @Override
  public void writeCodeWithScope(byte[] field, String code, byte[] scope) {
    buffer.writeByte((byte) BsonType.JAVASCRIPT_WITH_SCOPE.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    var stringMark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(code);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(stringMark, buffer.getTail() - stringMark - 4);
    buffer.writeBytes(scope);
    //    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark);
    setNextState();
  }

  @Override
  public void writeCodeWithScope(String code, byte[] scope) {
    buffer.writeByte((byte) BsonType.JAVASCRIPT_WITH_SCOPE.getValue());
    writeNameValue();
    var mark = buffer.getTail();
    buffer.skipTail(4);
    var stringMark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(code);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(stringMark, buffer.getTail() - stringMark - 4);
    buffer.writeBytes(scope);
    //    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark);
    setNextState();
  }

  @Override
  public void writeDecimal128(String field, long high, long low) {
    buffer.writeByte((byte) BsonType.DECIMAL128.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    buffer.writeLong(low);
    buffer.writeLong(high);
    setNextState();
  }

  @Override
  public void writeDecimal128(byte[] field, long high, long low) {
    buffer.writeByte((byte) BsonType.DECIMAL128.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    buffer.writeLong(low);
    buffer.writeLong(high);
    setNextState();
  }

  @Override
  public void writeDecimal128(long high, long low) {
    buffer.writeByte((byte) BsonType.DECIMAL128.getValue());
    writeNameValue();
    buffer.writeLong(low);
    buffer.writeLong(high);
    setNextState();
    //    buffer.writeByte((byte) 0);
  }

  @Override
  public void writeDecimal128(String field, Decimal128 value) {
    buffer.writeByte((byte) BsonType.DECIMAL128.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    buffer.writeLong(value.getLow());
    buffer.writeLong(value.getHigh());
    setNextState();
  }

  @Override
  public void writeDecimal128(byte[] field, Decimal128 value) {
    buffer.writeByte((byte) BsonType.DECIMAL128.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    buffer.writeLong(value.getLow());
    buffer.writeLong(value.getHigh());
    setNextState();
  }

  @Override
  public void writeDecimal128(Decimal128 value) {
    buffer.writeByte((byte) BsonType.DECIMAL128.getValue());
    writeNameValue();
    buffer.writeLong(value.getLow());
    buffer.writeLong(value.getHigh());
    setNextState();
    //    buffer.writeByte((byte) 0);
  }

  @Override
  public void writeMaxKey(String field) {
    buffer.writeByte((byte) BsonType.MAX_KEY.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    setNextState();
    //    buffer.writeByte((byte) 0);
  }

  @Override
  public void writeMaxKey(byte[] field) {
    buffer.writeByte((byte) BsonType.MAX_KEY.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    setNextState();
    //    buffer.writeByte((byte) 0);
  }

  @Override
  public void writeMaxKey() {
    buffer.writeByte((byte) BsonType.MAX_KEY.getValue());
    writeNameValue();
    setNextState();
    //    buffer.writeByte((byte) 0);
  }

  @Override
  public void writeMinKey(String field) {
    buffer.writeByte((byte) BsonType.MIN_KEY.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    setNextState();
    //    buffer.writeByte((byte) 0);
  }

  @Override
  public void writeMinKey(byte[] field) {
    buffer.writeByte((byte) BsonType.MIN_KEY.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    setNextState();
    //    buffer.writeByte((byte) 0);
  }

  @Override
  public void writeMinKey() {
    buffer.writeByte((byte) BsonType.MIN_KEY.getValue());
    writeNameValue();
    setNextState();
    //    buffer.writeByte((byte) 0);
  }

  @Override
  public void writeNull(String field) {
    buffer.writeByte((byte) BsonType.NULL.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    setNextState();
  }

  @Override
  public void writeNull(byte[] field) {
    buffer.writeByte((byte) BsonType.NULL.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    setNextState();
  }

  @Override
  public void writeNull() {
    buffer.writeByte((byte) BsonType.NULL.getValue());
    writeNameValue();
    setNextState();
  }

  @Override
  public void writeUndefined(String field) {
    buffer.writeByte((byte) BsonType.UNDEFINED.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    setNextState();
  }

  @Override
  public void writeUndefined(byte[] field) {
    buffer.writeByte((byte) BsonType.UNDEFINED.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    setNextState();
  }

  @Override
  public void writeUndefined() {
    buffer.writeByte((byte) BsonType.UNDEFINED.getValue());
    writeNameValue();
    setNextState();
  }

  @Override
  public void writeSymbol(byte[] key, String value) {
    buffer.writeByte((byte) BsonType.SYMBOL.getValue());
    buffer.writeBytes(key);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(value);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    setNextState();
  }

  @Override
  public void writeSymbol(String value) {
    buffer.writeByte((byte) BsonType.SYMBOL.getValue());
    writeNameValue();
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(value);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    setNextState();
  }

  @Override
  public void writeSymbol(String field, String value) {
    buffer.writeByte((byte) BsonType.SYMBOL.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(value);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    setNextState();
  }

  @Override
  public void writeJavascript(byte[] key, String value) {
    buffer.writeByte((byte) BsonType.JAVASCRIPT.getValue());
    buffer.writeBytes(key);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(value);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    setNextState();
  }

  @Override
  public void writeJavascript(String value) {
    buffer.writeByte((byte) BsonType.JAVASCRIPT.getValue());
    writeNameValue();
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(value);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    setNextState();
  }

  @Override
  public void writeJavascript(String field, String value) {
    buffer.writeByte((byte) BsonType.JAVASCRIPT.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(value);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    setNextState();
  }

  @Override
  public void writeString(byte[] key, byte[] value) {
    buffer.writeByte((byte) BsonType.STRING.getValue());
    buffer.writeBytes(key);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(value.length + 1);
    buffer.writeBytes(value);
    buffer.writeByte((byte) 0);
    setNextState();
  }

  @Override
  public void writeString(byte[] value) {
    buffer.writeByte((byte) BsonType.STRING.getValue());
    writeNameValue();
    buffer.skipTail(value.length + 1);
    buffer.writeBytes(value);
    buffer.writeByte((byte) 0);
    setNextState();
  }

  @Override
  public void writeString(String field, byte[] value) {
    buffer.writeByte((byte) BsonType.STRING.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    buffer.skipTail(value.length + 1);
    buffer.writeBytes(value);
    buffer.writeByte((byte) 0);
    setNextState();
  }

  @Override
  public void writeString(byte[] key, String value) {
    buffer.writeByte((byte) BsonType.STRING.getValue());
    buffer.writeBytes(key);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(value);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    setNextState();
  }

  @Override
  public void writeString(String value) {
    buffer.writeByte((byte) BsonType.STRING.getValue());
    writeNameValue();
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(value);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    setNextState();
  }

  @Override
  public void writeString(String field, String value) {
    buffer.writeByte((byte) BsonType.STRING.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    var mark = buffer.getTail();
    buffer.skipTail(4);
    buffer.writeString(value);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(mark, buffer.getTail() - mark - 4);
    setNextState();
  }

  @Override
  public void writeObjectId(byte[] key, byte[] value) {
    buffer.writeByte((byte) BsonType.OBJECT_ID.getValue());
    buffer.writeBytes(key);
    buffer.writeByte((byte) 0);
    buffer.writeBytes(value);
    setNextState();
  }

  @Override
  public void writeObjectId(byte[] value) {
    buffer.writeByte((byte) BsonType.OBJECT_ID.getValue());
    writeNameValue();
    buffer.writeBytes(value);
    setNextState();
  }

  @Override
  public void writeObjectId(String field, byte[] value) {
    buffer.writeByte((byte) BsonType.OBJECT_ID.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    buffer.writeBytes(value);
    setNextState();
  }
  @Override
  public void writeDouble(String field, double value) {
    buffer.writeByte((byte) BsonType.DOUBLE.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    buffer.writeDouble(value);
    setNextState();
  }

  @Override
  public void writeDouble(byte[] field, double value) {
    buffer.writeByte((byte) BsonType.DOUBLE.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    buffer.writeDouble(value);
    setNextState();
  }

  @Override
  public void writeDouble(double value) {
    buffer.writeByte((byte) BsonType.DOUBLE.getValue());
    writeNameValue();
    buffer.writeDouble(value);
    setNextState();
  }

  @Override
  public void writeBoolean(String name, boolean value) {
    buffer.writeByte((byte) BsonType.BOOLEAN.getValue());
    buffer.writeString(name);
    buffer.writeByte((byte) 0);
    if (value) {
      buffer.writeByte((byte) 1);
    } else {
      buffer.writeByte((byte) 0);
    }
    setNextState();
  }

  @Override
  public void writeBoolean(byte[] name, boolean value) {
    buffer.writeByte((byte) BsonType.BOOLEAN.getValue());
    buffer.writeBytes(name);
    buffer.writeByte((byte) 0);
    if (value) {
      buffer.writeByte((byte) 1);
    } else {
      buffer.writeByte((byte) 0);
    }
    setNextState();
  }

  @Override
  public void writeBoolean(boolean value) {
    buffer.writeByte((byte) BsonType.BOOLEAN.getValue());
    writeNameValue();
    if (value) {
      buffer.writeByte((byte) 1);
    } else {
      buffer.writeByte((byte) 0);
    }
    setNextState();
  }

  @Override
  public void writeInteger(String field, int value) {
    buffer.writeByte((byte) BsonType.INT32.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(value);
    setNextState();
  }

  @Override
  public void writeInteger(byte[] field, int value) {
    buffer.writeByte((byte) BsonType.INT32.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    buffer.writeInteger(value);
    setNextState();
  }

  @Override
  public void writeInteger(int value) {
    buffer.writeByte((byte) BsonType.INT32.getValue());
    writeNameValue();
    buffer.writeInteger(value);
    setNextState();
  }

  @Override
  public void writeLong(String field, long value) {
    buffer.writeByte((byte) BsonType.INT64.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    buffer.writeLong(value);
    setNextState();
  }

  @Override
  public void writeLong(byte[] field, long value) {
    buffer.writeByte((byte) BsonType.INT64.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    buffer.writeLong(value);
    setNextState();
  }

  @Override
  public void writeLong(long value) {
    buffer.writeByte((byte) BsonType.INT64.getValue());
    writeNameValue();
    buffer.writeLong(value);
    setNextState();
  }

  @Override
  public void writeDateTime(String field, Long value) {
    buffer.writeByte((byte) BsonType.DATE_TIME.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    buffer.writeLong(value);
    setNextState();
  }

  @Override
  public void writeDateTime(byte[] field, Long value) {
    buffer.writeByte((byte) BsonType.DATE_TIME.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    buffer.writeLong(value);
    setNextState();
  }

  @Override
  public void writeDateTime(Long value) {
    buffer.writeByte((byte) BsonType.DATE_TIME.getValue());
    writeNameValue();
    buffer.writeLong(value);
    setNextState();
  }

  @Override
  public void writeTimestamp(String field, Long value) {
    buffer.writeByte((byte) BsonType.TIMESTAMP.getValue());
    buffer.writeString(field);
    buffer.writeByte((byte) 0);
    buffer.writeLong(value);
    setNextState();
  }

  @Override
  public void writeTimestamp(byte[] field, Long value) {
    buffer.writeByte((byte) BsonType.TIMESTAMP.getValue());
    buffer.writeBytes(field);
    buffer.writeByte((byte) 0);
    buffer.writeLong(value);
    setNextState();
  }

  @Override
  public void writeTimestamp(Long value) {
    buffer.writeByte((byte) BsonType.TIMESTAMP.getValue());
    writeNameValue();
    buffer.writeLong(value);
    setNextState();
  }

  @Override
  public BsonState getState() {
    return state;
  }

  @Override
  public BsonContextType getCurrentBsonContext() {
    return contextStack.getCurrentBsonType();
  }
}
