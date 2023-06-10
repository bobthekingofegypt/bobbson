package org.bobstuff.bobbson.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonDocumentConverter implements BobBsonConverter<BsonDocument> {
  public static final Map<BsonType, Class<?>> bsonTypeClassMap = new HashMap<>();

  static {
    bsonTypeClassMap.put(BsonType.STRING, BsonString.class);
    bsonTypeClassMap.put(BsonType.NULL, BsonNull.class);
    bsonTypeClassMap.put(BsonType.ARRAY, BsonArray.class);
    bsonTypeClassMap.put(BsonType.BOOLEAN, BsonBoolean.class);
    bsonTypeClassMap.put(BsonType.INT32, BsonInt32.class);
    bsonTypeClassMap.put(BsonType.INT64, BsonInt64.class);
    bsonTypeClassMap.put(BsonType.BINARY, BsonBinary.class);
    bsonTypeClassMap.put(BsonType.DOCUMENT, BsonDocument.class);
    bsonTypeClassMap.put(BsonType.DOUBLE, BsonDouble.class);
    bsonTypeClassMap.put(BsonType.DATE_TIME, BsonDateTime.class);
    bsonTypeClassMap.put(BsonType.TIMESTAMP, BsonTimestamp.class);
    bsonTypeClassMap.put(BsonType.SYMBOL, BsonSymbol.class);
    bsonTypeClassMap.put(BsonType.OBJECT_ID, BsonObjectId.class);
    bsonTypeClassMap.put(BsonType.MAX_KEY, BsonMaxKey.class);
    bsonTypeClassMap.put(BsonType.MIN_KEY, BsonMinKey.class);
    bsonTypeClassMap.put(BsonType.DECIMAL128, BsonDecimal128.class);
    bsonTypeClassMap.put(BsonType.DB_POINTER, BsonDbPointer.class);
    bsonTypeClassMap.put(BsonType.JAVASCRIPT, BsonJavaScript.class);
    bsonTypeClassMap.put(BsonType.REGULAR_EXPRESSION, BsonRegularExpression.class);
    bsonTypeClassMap.put(BsonType.UNDEFINED, BsonUndefined.class);
    bsonTypeClassMap.put(BsonType.JAVASCRIPT_WITH_SCOPE, BsonJavaScriptWithScope.class);
  }

  private BobBson bobBson;

  public BsonDocumentConverter(BobBson bobBson) {
    this.bobBson = bobBson;
  }

  @Override
  @SuppressWarnings("PMD.AssignmentInOperand")
  public @Nullable BsonDocument readValue(BsonReader bsonReader, BsonType outerType) {
    List<BsonElement> keyValuePairs = new ArrayList<BsonElement>();

    bsonReader.readStartDocument();
    BsonType type;
    while ((type = bsonReader.readBsonType()) != BsonType.END_OF_DOCUMENT) {
      String fieldName = bsonReader.currentFieldName();
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
      keyValuePairs.add(new BsonElement(fieldName, (BsonValue) value));
    }
    bsonReader.readEndDocument();

    return new BsonDocument(keyValuePairs);
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonDocument value) {
    bsonWriter.writeStartDocument();
    for (Map.Entry<String, BsonValue> entry : value.entrySet()) {
      bsonWriter.writeName(entry.getKey());
      var clazz = entry.getValue().getClass();
      var converter = (BobBsonConverter) bobBson.tryFindConverter(clazz);
      if (converter == null) {
        throw new IllegalStateException(
            String.format("No converter registered for %s", clazz.getSimpleName()));
      }
      converter.write(bsonWriter, (BsonValue) entry.getValue());
    }
    bsonWriter.writeEndDocument();
  }
}
