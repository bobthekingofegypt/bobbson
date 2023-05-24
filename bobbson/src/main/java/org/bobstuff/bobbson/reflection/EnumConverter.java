package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

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
  private Class<T> instanceClazz;

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
  @SuppressWarnings("unchecked")
  public void write(BsonWriter bsonWriter, byte @Nullable [] key, T instance) {
    if (instance == null) {
      if (key == null) {
        bsonWriter.writeNull();
      } else {
        bsonWriter.writeNull(key);
      }
      return;
    }

    if (key == null) {
      bsonWriter.writeString(instance.name());
    } else {
      bsonWriter.writeString(key, instance.name());
    }
  }

  @Override
  public void write(BsonWriter bsonWriter, T instance) {
    this.write(bsonWriter, (byte[]) null, instance);
  }

  @Override
  @SuppressWarnings("unchecked")
  public @Nullable T read(BsonReader bsonReader) {
    if (bsonReader.getCurrentBsonType() == BsonType.NULL) {
      return null;
    }

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