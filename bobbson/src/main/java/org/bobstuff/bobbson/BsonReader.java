package org.bobstuff.bobbson;

public interface BsonReader {
  ContextStack getContextStack();

  BsonState getState();

  void readStartDocument();

  void readEndDocument();

  void readStartArray();

  void readEndArray();

  BsonType readBsonType();

  void readStringRaw();

  BobBsonBuffer.ByteRangeComparitor getFieldName();

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
