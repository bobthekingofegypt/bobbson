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
    var type = reader.getCurrentBsonType();
    if (type == BsonType.NULL) {
      throw new RuntimeException("Attempting to read null into a primitive long type");
    }

    if (type == BsonType.INT64) {
      return reader.readInt64();
    } else if (type == BsonType.INT32) {
      return reader.readInt32();
    } else if (type == BsonType.DOUBLE) {
      return (long) reader.readDouble();
    }
    throw new RuntimeException("Attempting to read non int64 type into long field");
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
