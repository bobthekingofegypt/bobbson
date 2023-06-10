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

import java.nio.ByteBuffer;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.BobBsonBuffer;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.models.*;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A bson reader that maintains an internal stack to track states as a documents indentation
 * increases.
 */
public class StackBsonReader implements BsonReader {
  public static final String NOT_TERMINATED_WITH_NULL_BYTE =
      "readString value was not terminated" + " with null byte";
  private final BobBsonBuffer buffer;
  private BsonState state;
  private BsonType currentBsonType;
  private final ContextStack contextStack;

  public StackBsonReader(@NonNull ByteBuffer buffer) {
    this(buffer, new ContextStack());
  }

  public StackBsonReader(@NonNull ByteBuffer buffer, ContextStack contextStack) {
    this(new ByteBufferBobBsonBuffer(buffer), contextStack);
  }

  public StackBsonReader(@NonNull BobBsonBuffer buffer) {
    this(buffer, new ContextStack());
  }

  public StackBsonReader(@NonNull BobBsonBuffer buffer, ContextStack contextStack) {
    this.contextStack = contextStack;
    this.buffer = buffer;
    this.state = BsonState.INITIAL;
    this.currentBsonType = BsonType.NOT_SET;
  }

  @Override
  public ContextStack getContextStack() {
    return contextStack;
  }

  @Override
  public BsonState getState() {
    return state;
  }

  @Override
  public void readStartDocument() {
    int length = buffer.getInt();
    if (!contextStack.isRootContext()) {
      contextStack.adjustRemaining(length);
    }
    contextStack.add(length - 4, BsonContextType.DOCUMENT);
    state = BsonState.TYPE;
  }

  @Override
  public void readEndDocument() {
    if (state == BsonState.TYPE) {
      readBsonType();
    }

    contextStack.pop();

    setStateOnEnd();
  }

  @Override
  public void readStartArray() {
    int length = buffer.getInt();
    contextStack.adjustRemaining(length);
    contextStack.add(length - 4, BsonContextType.ARRAY);
    state = BsonState.TYPE;
  }

  @Override
  public void readEndArray() {
    if (state == BsonState.TYPE) {
      readBsonType();
    }

    contextStack.pop();

    setStateOnEnd();
  }

  @Override
  public BsonType readBsonType() {
    if (state == BsonState.DONE) {
      return currentBsonType;
    }

    byte bsonTypeByte = buffer.getByte();
    contextStack.adjustRemaining(1);

    currentBsonType = BsonType.findByValue(bsonTypeByte);

    if (currentBsonType == BsonType.END_OF_DOCUMENT) {
      var currentContextBsonType = contextStack.getCurrentBsonType();
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
      switch (contextStack.getCurrentBsonType()) {
        case DOCUMENT, ARRAY -> {
          readCString();
          state = BsonState.VALUE;
        }
        default -> throw new RuntimeException(
            format("Unexpected ContextType. %s", contextStack.getCurrentBsonType()));
      }
    }
    return currentBsonType;
  }

  private void readCString() {
    int bytes = buffer.readUntil((byte) 0);
    contextStack.adjustRemaining(bytes);
  }

  @Override
  public void readStringRaw() {
    var i = buffer.getInt();
    contextStack.adjustRemaining(4);
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
    return contextStack.getCurrentBsonType();
  }

  @Override
  public boolean readBoolean() {
    var value = buffer.getByte();
    contextStack.adjustRemaining(1);
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
    contextStack.adjustRemaining(size);
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
    contextStack.adjustRemaining(size + 16);
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
    contextStack.adjustRemaining(size);
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
    contextStack.adjustRemaining(size + 5);
    state = getNextState();
    return new BobBsonBinary(subtype, value);
  }

  @Override
  public byte[] readObjectId() {
    byte[] objectId = buffer.getBytes(12);
    contextStack.adjustRemaining(12);
    state = getNextState();
    return objectId;
  }

  @Override
  public Decimal128 readDecimal128() {
    long low = buffer.getLong();
    long high = buffer.getLong();
    contextStack.adjustRemaining(16);
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
    contextStack.adjustRemaining(size + 4);
    state = getNextState();
    return value;
  }

  @Override
  public int readInt32() {
    state = getNextState();
    var value = buffer.getInt();
    contextStack.adjustRemaining(4);
    state = getNextState();
    return value;
  }

  @Override
  public long readInt64() {
    state = getNextState();
    var value = buffer.getLong();
    contextStack.adjustRemaining(8);
    state = getNextState();
    return value;
  }

  @Override
  public long readDateTime() {
    state = getNextState();
    var value = buffer.getLong();
    contextStack.adjustRemaining(8);
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
    contextStack.adjustRemaining(8);
    state = getNextState();
    return value;
  }

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
        contextStack.adjustRemaining(4);
      }
      case INT64, DOUBLE, DATE_TIME, TIMESTAMP -> {
        buffer.skipHead(8);
        contextStack.adjustRemaining(8);
      }
      case NULL -> {
        // NO OP
      }
      case BOOLEAN -> {
        buffer.skipHead(1);
        contextStack.adjustRemaining(1);
      }
      case OBJECT_ID -> {
        buffer.skipHead(12);
        contextStack.adjustRemaining(12);
      }
      case STRING -> {
        var size = buffer.getInt();
        buffer.skipHead(size - 1);
        byte nullByte = buffer.getByte();
        if (nullByte != 0) {
          throw new IllegalStateException(NOT_TERMINATED_WITH_NULL_BYTE);
        }
        contextStack.adjustRemaining(size + 4);
      }
      case BINARY -> {
        var size = buffer.getInt();
        buffer.skipHead(size + 1);
        contextStack.adjustRemaining(size + 5);
      }
      case DOCUMENT -> {
        var size = buffer.getInt();
        buffer.skipHead(size - 5);
        byte nullByte = buffer.getByte();
        if (nullByte != 0) {
          throw new IllegalStateException("object value was not terminated with null byte");
        }
        contextStack.adjustRemaining(size);
      }
      case ARRAY -> {
        var size = buffer.getInt();
        buffer.skipHead(size - 5);
        byte nullByte = buffer.getByte();
        if (nullByte != 0) {
          throw new IllegalStateException("array value was not terminated with null byte");
        }
        contextStack.adjustRemaining(size);
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
    var remaining = contextStack.getRemaining();
    buffer.skipHead(remaining - 1);
    contextStack.adjustRemaining(remaining - 1);
    state = BsonState.TYPE;
  }

  @Override
  public void skipToEnd() {
    while (!contextStack.isRootContext()) {
      skipContext();
      readEndDocument();
    }
    skipContext();
  }

  protected BsonState getNextState() {
    switch (contextStack.getCurrentBsonType()) {
      case ARRAY:
      case DOCUMENT:
        return BsonState.TYPE;
      case TOP_LEVEL:
        return BsonState.DONE;
      default:
        throw new RuntimeException(
            format("Unexpected ContextType %s.", contextStack.getCurrentBsonType()));
    }
  }

  private void setStateOnEnd() {
    switch (contextStack.getCurrentBsonType()) {
      case ARRAY:
      case DOCUMENT:
        state = BsonState.TYPE;
        break;
      case TOP_LEVEL:
        state = BsonState.DONE;
        break;
      default:
        throw new RuntimeException(
            format("Unexpected ContextType %s.", contextStack.getCurrentBsonType()));
    }
  }
}
