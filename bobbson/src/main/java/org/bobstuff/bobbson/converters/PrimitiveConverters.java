package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;

public class PrimitiveConverters {
  public static int parseInteger(BsonReader reader) {
    if (reader.getCurrentBsonType() == BsonType.NULL) {
      throw new RuntimeException("Attempting to read null into a primitive integer type");
    }
    if (reader.getCurrentBsonType() != BsonType.INT32) {
      throw new RuntimeException("Attempting to read non int32 type into int field");
    }
    return reader.readInt32();
  }

  public static long parseLong(BsonReader reader) {
    if (reader.getCurrentBsonType() == BsonType.NULL) {
      throw new RuntimeException("Attempting to read null into a primitive long type");
    }
    // TODO support casting of numeric types where safe
    if (reader.getCurrentBsonType() != BsonType.INT64) {
      throw new RuntimeException("Attempting to read non int64 type into long field");
    }
    return reader.readInt64();
  }

  public static double parseDouble(BsonReader reader) {
    if (reader.getCurrentBsonType() == BsonType.NULL) {
      throw new RuntimeException("Attempting to read null into a primitive double type");
    }
    if (reader.getCurrentBsonType() != BsonType.DOUBLE) {
      throw new RuntimeException("Attempting to read non double type into double field");
    }
    return reader.readDouble();
  }

  public static boolean parseBoolean(BsonReader reader) {
    if (reader.getCurrentBsonType() == BsonType.NULL) {
      throw new RuntimeException("Attempting to read null into a primitive double type");
    }
    if (reader.getCurrentBsonType() != BsonType.BOOLEAN) {
      throw new RuntimeException("Attempting to read non boolean type into boolean field");
    }
    return reader.readBoolean();
  }
}
