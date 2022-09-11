package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BasicBSONObject;
import org.bson.BsonNull;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BasicBSONObjectConverter implements BobBsonConverter<BasicBSONObject> {
  public static final Map<BsonType, Class<?>> bsonTypeClassMap = new HashMap<>();

  static {
    bsonTypeClassMap.put(BsonType.STRING, String.class);
    bsonTypeClassMap.put(BsonType.NULL, BsonNull.class);
    bsonTypeClassMap.put(BsonType.ARRAY, List.class);
    bsonTypeClassMap.put(BsonType.BOOLEAN, Boolean.class);
    bsonTypeClassMap.put(BsonType.INT32, Integer.class);
    bsonTypeClassMap.put(BsonType.INT64, Long.class);
    bsonTypeClassMap.put(BsonType.BINARY, Binary.class);
    bsonTypeClassMap.put(BsonType.DOCUMENT, Document.class);
    bsonTypeClassMap.put(BsonType.DOUBLE, Double.class);
    bsonTypeClassMap.put(BsonType.DATE_TIME, Date.class);
    bsonTypeClassMap.put(BsonType.TIMESTAMP, BsonTimestamp.class);
//    bsonTypeClassMap.put(BsonType.SYMBOL, BsonSymbol.class);
    bsonTypeClassMap.put(BsonType.OBJECT_ID, ObjectId.class);
//    bsonTypeClassMap.put(BsonType.MAX_KEY, BsonMaxKey.class);
//    bsonTypeClassMap.put(BsonType.MIN_KEY, BsonMinKey.class);
    bsonTypeClassMap.put(BsonType.DECIMAL128, Decimal128.class);
//    bsonTypeClassMap.put(BsonType.DB_POINTER, DbPointer.class);
    bsonTypeClassMap.put(BsonType.JAVASCRIPT, Code.class);
//    bsonTypeClassMap.put(BsonType.REGULAR_EXPRESSION, BsonRegularExpression.class);
//    bsonTypeClassMap.put(BsonType.UNDEFINED, Undefined.class);
//    bsonTypeClassMap.put(BsonType.JAVASCRIPT_WITH_SCOPE, BsonJavaScriptWithScope.class);
  }

  private BobBson bobBson;

  public BasicBSONObjectConverter(BobBson bobBson) {
    this.bobBson = bobBson;
  }

//  private static StringDecoderCache cache = new StringDecoderCache();
//  static {
//    cache.setMaxCacheEntries(100);
//    cache.setMaxCacheLength(100);
//  }
  private static Map<Integer, String> cache = new HashMap<Integer, String>();

  @Override
  @SuppressWarnings("PMD.AssignmentInOperand")
  public @Nullable BasicBSONObject read(BsonReader bsonReader) {
    var document = new BasicBSONObject();

    bsonReader.readStartDocument();
    BsonType type;
    BobBsonBuffer.ByteRangeComparitor rangeComparitor = bsonReader.getFieldName();
    while ((type = bsonReader.readBsonType()) != BsonType.END_OF_DOCUMENT) {

      String fieldName = cache.get(rangeComparitor.weakHash());
      if (fieldName == null) {
        fieldName = rangeComparitor.name();
        cache.put(rangeComparitor.weakHash(), fieldName);
      }

//      bsonReader.currentFieldName();
      Class<?> clazz = bsonTypeClassMap.get(type);
      if (clazz == null) {
        throw new IllegalStateException(
            String.format(
                "No BSONValue type map entry registered for BSONValue type %s", type.toString()));
      }
      var converter = bobBson.tryFindConverter(clazz);
      if (converter == null) {
        throw new IllegalStateException(
            String.format("No converter registered for %s", clazz.getSimpleName()));
      }
      var value = converter.read(bsonReader);
      if (value == null) {
        throw new IllegalStateException(String.format("Decoded BSONValue value is null"));
      }
      document.put(fieldName, value);
    }
    bsonReader.readEndDocument();

    return document;
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BasicBSONObject value) {
    if (key == null) {
      bsonWriter.writeStartDocument();
    } else {
      bsonWriter.writeStartDocument(key);
    }
    for (Map.Entry<String, Object> entry : value.entrySet()) {
      bsonWriter.writeName(entry.getKey());
      var clazz = entry.getValue().getClass();
      var converter = (BobBsonConverter) bobBson.tryFindConverter(clazz);
      if (converter == null) {
        throw new IllegalStateException(
            String.format("No converter registered for %s", clazz.getSimpleName()));
      }
      converter.write(bsonWriter, entry.getValue());
    }
    bsonWriter.writeEndDocument();
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BasicBSONObject value) {
    bsonWriter.writeStartDocument();
    for (Map.Entry<String, Object> entry : value.entrySet()) {
      bsonWriter.writeName(entry.getKey());
      var clazz = entry.getValue().getClass();
      var converter = (BobBsonConverter) bobBson.tryFindConverter(clazz);
      if (converter == null) {
        throw new IllegalStateException(
            String.format("No converter registered for %s", clazz.getSimpleName()));
      }
      converter.write(bsonWriter, entry.getValue());
    }
    bsonWriter.writeEndDocument();
  }
}