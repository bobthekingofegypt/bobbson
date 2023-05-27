package org.bobstuff.bobbson;

import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * BobBsonConverters are the backbone of this bson library, each object type needs an assiciated
 * converter. The converter details how an object of a specific type should be writen and read from
 * bson.
 *
 * <p>There are default implementations of the top level read and write functions to handle null
 * values and writing of keys. If you need to override this default behaviour you can implement your
 * own versions of these functions.
 *
 * @param <T> Type the converter operates on
 */
public interface BobBsonConverter<T> {

  /**
   * Read value from the reader.
   *
   * <p>If current bson type in reader is null then null will be read from the reader and returned.
   *
   * @param bsonReader reader instance
   * @return value read from reader or null if BsonType is null
   */
  default @Nullable T read(BsonReader bsonReader) {
    BsonType type = bsonReader.getCurrentBsonType();

    if (type != BsonType.NULL) {
      return readValue(bsonReader, type);
    }

    bsonReader.readNull();
    return null;
  }

  /**
   * Read value from the given reader. Current bson type is provided to save multiple lookups.
   *
   * @param bsonReader reader instance
   * @param type current bson type from reader
   * @return value read from reader
   */
  @Nullable T readValue(BsonReader bsonReader, BsonType type);

  /**
   * Write value to give bson writer. If value is null then null will be written to the writer.
   *
   * @param bsonWriter writer instance
   * @param value value to be written
   */
  default void write(BsonWriter bsonWriter, @Nullable T value) {
    if (value != null) {
      writeValue(bsonWriter, value);
      return;
    }

    bsonWriter.writeNull();
  }

  /**
   * Write value to given writer, value will not be null at this point.
   *
   * @param bsonWriter writer instance
   * @param value object to be writen
   */
  void writeValue(BsonWriter bsonWriter, T value);

  /**
   * Write value to given writer referenced by the given key.
   *
   * <p>if value is null default implementation will write null and return
   *
   * @param bsonWriter writer instance
   * @param key entries key in the bson
   * @param value object to be writen
   */
  default void write(BsonWriter bsonWriter, byte[] key, @Nullable T value) {
    bsonWriter.writeName(key);

    if (value != null) {
      writeValue(bsonWriter, value);
      return;
    }

    bsonWriter.writeNull();
  }

  /**
   * Write value to given writer referenced by the given key.
   *
   * <p>if value is null default implementation will write null and return
   *
   * @param bsonWriter writer instance
   * @param key entries key in the bson
   * @param value object to be writen
   */
  default void write(BsonWriter bsonWriter, String key, @Nullable T value) {
    bsonWriter.writeName(key);

    if (value != null) {
      writeValue(bsonWriter, value);
      return;
    }

    bsonWriter.writeNull();
  }
}
