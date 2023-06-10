package org.bobstuff.bobbson.writer;

import org.bobstuff.bobbson.BsonContextType;
import org.bobstuff.bobbson.BsonState;
import org.bobstuff.bobbson.models.Decimal128;

/**
 * A writer that understands bson and can write the various types defined in the BSON specification.
 */
public interface BsonWriter {
  void writeStartDocument();

  void writeStartDocument(byte[] field);

  void writeStartDocument(String name);

  void writeStartArray(byte[] field);

  void writeStartArray(String field);

  void writeStartArray();

  void writeEndDocument();

  void writeEndArray();

  void writeName(String name);

  void writeName(byte[] name);

  void writeRegex(String field, String regex, String options);

  void writeRegex(byte[] field, String regex, String options);

  void writeRegex(String regex, String options);

  void writeBinary(String field, byte type, byte[] data);

  void writeBinary(byte[] field, byte type, byte[] data);

  void writeBinary(byte type, byte[] data);

  void writeDbPointer(String field, String namespace, byte[] objectId);

  void writeDbPointer(byte[] field, String namespace, byte[] objectId);

  void writeDbPointer(String namespace, byte[] objectId);

  void writeCodeWithScope(String field, String code, byte[] scope);

  void writeCodeWithScope(byte[] field, String code, byte[] scope);

  void writeCodeWithScope(String code, byte[] scope);

  void writeDecimal128(String field, long high, long low);

  void writeDecimal128(byte[] field, long high, long low);

  void writeDecimal128(long high, long low);

  void writeDecimal128(String field, Decimal128 value);

  void writeDecimal128(byte[] field, Decimal128 value);

  void writeDecimal128(Decimal128 value);

  void writeMaxKey(String field);

  void writeMaxKey(byte[] field);

  void writeMaxKey();

  void writeMinKey(String field);

  void writeMinKey(byte[] field);

  void writeMinKey();

  void writeNull(String field);

  void writeNull(byte[] field);

  void writeNull();

  void writeUndefined(String field);

  void writeUndefined(byte[] field);

  void writeUndefined();

  void writeSymbol(byte[] key, String value);

  void writeSymbol(String value);

  void writeSymbol(String field, String value);

  void writeJavascript(byte[] key, String value);

  void writeJavascript(String value);

  void writeJavascript(String field, String value);

  void writeString(byte[] key, byte[] value);

  void writeString(byte[] value);

  void writeString(String field, byte[] value);

  void writeString(byte[] key, String value);

  void writeString(String value);

  void writeString(String field, String value);

  void writeObjectId(byte[] key, byte[] value);

  void writeObjectId(byte[] value);

  void writeObjectId(String field, byte[] value);

  void writeDouble(String field, double value);

  void writeDouble(byte[] field, double value);

  void writeDouble(double value);

  void writeBoolean(String name, boolean value);

  void writeBoolean(byte[] name, boolean value);

  void writeBoolean(boolean value);

  void writeInteger(String field, int value);

  void writeInteger(byte[] field, int value);

  void writeInteger(int value);

  void writeLong(String field, long value);

  void writeLong(byte[] field, long value);

  void writeLong(long value);

  void writeDateTime(String field, Long value);

  void writeDateTime(byte[] field, Long value);

  void writeDateTime(Long value);

  void writeTimestamp(String field, Long value);

  void writeTimestamp(byte[] field, Long value);

  void writeTimestamp(Long value);

  BsonState getState();

  BsonContextType getCurrentBsonContext();
}
