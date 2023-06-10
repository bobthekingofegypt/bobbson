package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Reflection converter to read/write basic enums using their name field.
 *
 * @param <T> type of enum
 */
public class EnumConverter<T extends Enum<T>> implements BobBsonConverter<T> {
  private class EnumValue {
    public byte[] nameBytes;
    public int nameWeakHash;
    public T value;

    public EnumValue(T value) {
      var name = value.name();
      nameBytes = name.getBytes(StandardCharsets.UTF_8);
      var total = 0;
      for (var b : nameBytes) {
        total += b;
      }
      nameWeakHash = total;
      this.value = value;
    }
  }

  private EnumValue[] values;
  private final Class<T> instanceClazz;

  /**
   * Construct enum converter for the given enum class and its array of name values
   *
   * @param instanceClazz enum class
   * @param constants array of name values
   */
  @SuppressWarnings("unchecked")
  public EnumConverter(Class<T> instanceClazz, T[] constants) {
    this.instanceClazz = instanceClazz;
    var values = (EnumValue[]) Array.newInstance(EnumValue.class, constants.length);
    var i = 0;
    for (var constant : constants) {
      values[i++] = new EnumValue(constant);
    }
    this.values = values;
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, T instance) {
    bsonWriter.writeString(instance.name());
  }

  @Override
  public @Nullable T readValue(BsonReader bsonReader, BsonType type) {
    var fieldName = bsonReader.getFieldName();
    bsonReader.readStringRaw();

    for (EnumValue value : values) {
      if (fieldName.equalsArray(value.nameBytes, value.nameWeakHash)) {
        return value.value;
      }
    }
    return Enum.valueOf(instanceClazz, fieldName.value());
  }
}
