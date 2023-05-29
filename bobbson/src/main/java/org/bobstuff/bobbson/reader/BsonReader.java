package org.bobstuff.bobbson.reader;

import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.BobBsonBuffer;
import org.bobstuff.bobbson.models.*;

/**
 * A reader that understands bson and can read the various types defined in the BSON specification.
 */
public interface BsonReader {
  ContextStack getContextStack();

  BsonState getState();

  void readStartDocument();

  void readEndDocument();

  void readStartArray();

  void readEndArray();

  BsonType readBsonType();

  void readStringRaw();

  BobBsonBuffer.ByteRangeComparator getFieldName();

  String currentFieldName();

  BsonType getCurrentBsonType();

  BsonContextType getCurrentContextType();

  boolean readBoolean();

  RegexRaw readRegex();

  DbPointerRaw readDbPointerRaw();

  CodeWithScopeRaw readCodeWithScope();

  BobBsonBinary readBinary();

  byte[] readObjectId();

  Decimal128 readDecimal128();

  String readString();

  int readInt32();

  long readInt64();

  long readDateTime();

  void readNull();

  void readUndefined();

  double readDouble();

  void skipValue();

  void skipContext();

  void skipToEnd();
}
