/*
 * Copyright 2008-present MongoDB, Inc.
 * Copyright 2022-present Bob.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bobstuff.bobbson.reader;

import static java.lang.String.format;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.BobBsonBuffer;
import org.bobstuff.bobbson.models.*;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FastBsonReader implements BsonReader {
  private static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
  public static final String NOT_TERMINATED_WITH_NULL_BYTE =
      "readString value was not terminated" + " with null byte";
  private BobBsonBuffer buffer;
  private BsonState state;
  private BsonType currentBsonType;

  private int[] contextStack;
  private int contextStackIndex;
  private int contextStackRemaining;
  private BsonContextType contextStackType;

  public FastBsonReader(@NonNull BobBsonBuffer buffer) {
    this.buffer = buffer;
    this.state = BsonState.INITIAL;
    this.currentBsonType = BsonType.NOT_SET;
    this.contextStack = new int[8];
    contextStackType = BsonContextType.TOP_LEVEL;
  }

  private void pushContext(BsonContextType type, int remaining) {
    if (contextStack.length == contextStackIndex - 1) {
      contextStack = Arrays.copyOf(contextStack, contextStack.length * 2);
    }
    contextStack[contextStackIndex] = (contextStackRemaining << 4) | contextStackType.ordinal();
    contextStackIndex += 1;
    contextStack[contextStackIndex] = (remaining << 4) | type.ordinal();
    contextStackRemaining = remaining;
    contextStackType = type;
  }

  private void popContext() {
    contextStackIndex -= 1;
    var value = contextStack[contextStackIndex];
    contextStackRemaining = value >> 4;
    var contextTypeValue = value & 0xf;
    contextStackType =
        switch (contextTypeValue) {
          case 0 -> BsonContextType.TOP_LEVEL;
          case 1 -> BsonContextType.DOCUMENT;
          case 2 -> BsonContextType.ARRAY;
          default -> throw new RuntimeException("Unknown context type");
        };
  }

  public void reset() {
    contextStackIndex = 0;
    contextStackRemaining = 0;
    contextStackType = BsonContextType.TOP_LEVEL;
  }

  @Override
  public ContextStack getContextStack() {
    throw new RuntimeException("Who calls get context stack");
  }

  @Override
  public BsonState getState() {
    return state;
  }

  //  public void reset(BobBsonBuffer buffer) {
  //    this.buffer = buffer;
  //    // TODO re-asses reset functionality
  //    // buffer.reset();
  //    contextStack.reset();
  //    state = BsonState.INITIAL;
  //  }

  @Override
  public void readStartDocument() {
    int length = buffer.getInt();
    if (contextStackIndex != 0) {
      contextStackRemaining -= length;
    }
    pushContext(BsonContextType.DOCUMENT, length - 4);
    state = BsonState.TYPE;
  }

  @Override
  public void readEndDocument() {
    if (state == BsonState.TYPE) {
      readBsonType();
    }

    popContext();

    setStateOnEnd();
  }

  @Override
  public void readStartArray() {
    int length = buffer.getInt();
    contextStackRemaining -= length;
    pushContext(BsonContextType.ARRAY, length - 4);
    state = BsonState.TYPE;
  }

  @Override
  public void readEndArray() {
    if (state == BsonState.TYPE) {
      readBsonType();
    }

    popContext();

    setStateOnEnd();
  }

  @Override
  public BsonType readBsonType() {
    if (state == BsonState.DONE) {
      return currentBsonType;
    }

    byte bsonTypeByte = buffer.getByte();
    contextStackRemaining -= 1;

    currentBsonType = BsonType.findByValue(bsonTypeByte);

    if (currentBsonType == BsonType.END_OF_DOCUMENT) {
      var currentContextBsonType = contextStackType;
      if (currentContextBsonType == BsonContextType.ARRAY) {
        state = BsonState.END_OF_ARRAY;
      } else if (currentContextBsonType == BsonContextType.DOCUMENT) {
        state = BsonState.END_OF_DOCUMENT;
      } else {
        throw new IllegalStateException(
            format(
                "BsonType.END_OF_DOCUMENT is not valid when context type is %s", currentBsonType));
      }

    } else {
      switch (contextStackType) {
        case DOCUMENT, ARRAY -> {
          readCString();
          state = BsonState.VALUE;
        }
        default -> throw new RuntimeException(
            format("Unexpected ContextType. %s", contextStackType));
      }
    }
    return currentBsonType;
  }

  private void readCString() {
    int bytes = buffer.readUntil((byte) 0);
    contextStackRemaining -= bytes;
  }

  @Override
  public void readStringRaw() {
    buffer.getInt();
    contextStackRemaining -= 4;
    readCString();
    state = getNextState();
  }

  @Override
  public BobBsonBuffer.ByteRangeComparator getFieldName() {
    return buffer.getByteRangeComparator();
  }

  @Override
  public String currentFieldName() {
    return buffer.getByteRangeComparator().value();
  }

  @Override
  public BsonType getCurrentBsonType() {
    return currentBsonType;
  }

  @Override
  public BsonContextType getCurrentContextType() {
    return contextStackType;
  }

  @Override
  public boolean readBoolean() {
    var value = buffer.getByte();
    contextStackRemaining -= 1;
    state = getNextState();
    if (value != 0 && value != 1) {
      throw new RuntimeException("invalid boolean value");
    }
    return value == 1;
  }

  @Override
  public RegexRaw readRegex() {
    int size = buffer.readUntil((byte) 0);
    String regex = buffer.getByteRangeComparator().value();
    size += buffer.readUntil((byte) 0);
    String options = buffer.getByteRangeComparator().value();
    contextStackRemaining -= size;
    state = getNextState();
    return new RegexRaw(regex, options);
  }

  @Override
  public DbPointerRaw readDbPointerRaw() {
    int size = buffer.getInt();
    String value = buffer.getString(size - 1);
    byte nullByte = buffer.getByte();
    if (nullByte != 0) {
      throw new IllegalStateException(NOT_TERMINATED_WITH_NULL_BYTE);
    }
    byte[] id = buffer.getBytes(12);
    contextStackRemaining -= size + 16;
    state = getNextState();
    return new DbPointerRaw(value, id);
  }

  @Override
  public CodeWithScopeRaw readCodeWithScope() {
    int size = buffer.getInt();
    int codeSize = buffer.getInt();
    String code = buffer.getString(codeSize - 1);
    byte nullByte = buffer.getByte();
    if (nullByte != 0) {
      throw new IllegalStateException(NOT_TERMINATED_WITH_NULL_BYTE);
    }

    int scopeSize = size - codeSize - 8;
    byte[] scope = buffer.getBytes(scopeSize);
    contextStackRemaining -= size;
    state = getNextState();
    return new CodeWithScopeRaw(code, scope);
  }

  @Override
  public BobBsonBinary readBinary() {
    int size = buffer.getInt();
    int bytesSize = size;
    byte subtype = buffer.getByte();

    if (subtype == (byte) 2) {
      var oldBinarySize = buffer.getInt();
      if (size - 4 != oldBinarySize) {
        throw new RuntimeException(
            String.format(
                "Old binary size %s inconsistent with binary size %s", oldBinarySize, size - 4));
      }
      bytesSize -= 4;
    }

    byte[] value = buffer.getBytes(bytesSize);
    contextStackRemaining -= size + 5;
    state = getNextState();
    return new BobBsonBinary(subtype, value);
  }

  @Override
  public byte[] readObjectId() {
    byte[] objectId = buffer.getBytes(12);
    contextStackRemaining -= 12;
    state = getNextState();
    return objectId;
  }

  @Override
  public Decimal128 readDecimal128() {
    long low = buffer.getLong();
    long high = buffer.getLong();
    contextStackRemaining -= 16;
    state = getNextState();
    return Decimal128.fromIEEE754BIDEncoding(high, low);
  }

  @Override
  public String readString() {
    int size = buffer.getInt();
    String value = buffer.getString(size - 1);
    byte nullByte = buffer.getByte();
    if (nullByte != 0) {
      throw new IllegalStateException(NOT_TERMINATED_WITH_NULL_BYTE);
    }
    contextStackRemaining -= size + 4;
    state = getNextState();
    return value;
  }

  @Override
  public int readInt32() {
    state = getNextState();
    var value = buffer.getInt();
    contextStackRemaining -= 4;
    state = getNextState();
    return value;
  }

  @Override
  public long readInt64() {
    state = getNextState();
    var value = buffer.getLong();
    contextStackRemaining -= 8;
    state = getNextState();
    return value;
  }

  @Override
  public long readDateTime() {
    state = getNextState();
    var value = buffer.getLong();
    contextStackRemaining -= 8;
    state = getNextState();
    return value;
  }

  @Override
  public void readNull() {
    state = getNextState();
  }

  @Override
  public void readUndefined() {
    state = getNextState();
  }

  @Override
  public double readDouble() {
    state = getNextState();
    var value = buffer.getDouble();
    contextStackRemaining -= 8;
    state = getNextState();
    return value;
  }

  // TODO re-look at this.  It was for making a faster mongodump
  //  public void readValue(OutputStream stream) {
  //    var size = buffer.readSizeValue(stream);
  //    state = getNextState();
  //    //    stream.write();
  //    //    buffer.read(size - 5);
  //    //    byte nullByte = buffer.get();
  //    //    if (nullByte != 0) {
  //    //      throw new IllegalStateException("object value was not terminated with null byte");
  //    //    }
  //    //    context.adjustRemaining(-(size));
  //    //    buffer.read(stream, )
  //  }

  @Override
  public void skipValue() {
    if (state != BsonState.VALUE) {
      throw new IllegalStateException(String.format("Cannot skip value when state is %s", state));
    }
    if (currentBsonType == null) {
      System.out.println(buffer.getHead());
    }
    switch (currentBsonType) {
      case INT32 -> {
        buffer.skipHead(4);
        contextStackRemaining -= 4;
      }
      case INT64, DOUBLE, DATE_TIME, TIMESTAMP -> {
        buffer.skipHead(8);
        contextStackRemaining -= 8;
      }
      case NULL -> {
        // NO OP
      }
      case BOOLEAN -> {
        buffer.skipHead(1);
        contextStackRemaining -= 1;
      }
      case OBJECT_ID -> {
        buffer.skipHead(12);
        contextStackRemaining -= 12;
      }
      case STRING -> {
        var size = buffer.getInt();
        buffer.skipHead(size - 1);
        byte nullByte = buffer.getByte();
        if (nullByte != 0) {
          throw new IllegalStateException(NOT_TERMINATED_WITH_NULL_BYTE);
        }
        contextStackRemaining -= size + 4;
      }
      case BINARY -> {
        var size = buffer.getInt();
        buffer.skipHead(size + 1);
        //        byte nullByte = buffer.getByte();
        //        if (nullByte != 0) {
        //          throw new IllegalStateException("readBinary value was not terminated with null
        // byte");
        //        }
        contextStackRemaining -= size + 5;
      }
      case DOCUMENT -> {
        var size = buffer.getInt();
        buffer.skipHead(size - 5);
        byte nullByte = buffer.getByte();
        if (nullByte != 0) {
          throw new IllegalStateException("object value was not terminated with null byte");
        }
        contextStackRemaining -= size;
      }
      case ARRAY -> {
        var size = buffer.getInt();
        buffer.skipHead(size - 5);
        byte nullByte = buffer.getByte();
        if (nullByte != 0) {
          throw new IllegalStateException("array value was not terminated with null byte");
        }
        contextStackRemaining -= size;
      }
      default -> throw new UnsupportedOperationException(
          format("todo add skip support for %s", currentBsonType));
    }

    state = getNextState();
  }

  /**
   * Skips to the end of the current context, if the context is a document it jumps to the end of
   * that specific document. If it is an array it jumps to the end of the array. If it is an array
   * of documents it just to the end of the current document if readStartDocument has been called or
   * the array otherwise
   */
  @Override
  public void skipContext() {
    var remaining = contextStackRemaining;
    buffer.skipHead(remaining - 1);
    contextStackRemaining = 1;
    state = BsonState.TYPE;
  }

  @Override
  public void skipToEnd() {
    while (contextStackIndex != 0) {
      skipContext();
      readEndDocument();
    }
    skipContext();
  }

  protected BsonState getNextState() {
    switch (contextStackType) {
      case ARRAY:
      case DOCUMENT:
        return BsonState.TYPE;
      case TOP_LEVEL:
        return BsonState.DONE;
      default:
        throw new RuntimeException(format("Unexpected ContextType %s.", contextStackType));
    }
  }

  private void setStateOnEnd() {
    switch (contextStackType) {
      case ARRAY:
      case DOCUMENT:
        state = BsonState.TYPE;
        break;
      case TOP_LEVEL:
        state = BsonState.DONE;
        break;
      default:
        throw new RuntimeException(format("Unexpected ContextType %s.", contextStackType));
    }
  }
}
