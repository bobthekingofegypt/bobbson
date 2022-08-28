package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBson;
import org.bson.*;

public class BsonValueConverters {
  public static void register(BobBson bobBson) {
    bobBson.registerConverter(BsonDocument.class, new BsonDocumentConverter(bobBson));
    bobBson.registerConverter(BsonString.class, new BsonStringConverter());
    bobBson.registerConverter(BsonNull.class, new BsonNullConverter());
    bobBson.registerConverter(BsonArray.class, new BsonArrayConverter(bobBson));
    bobBson.registerConverter(BsonNull.class, new BsonNullConverter());
    bobBson.registerConverter(BsonUndefined.class, new BsonUndefinedConverter());
    bobBson.registerConverter(BsonBoolean.class, new BsonBooleanConverter());
    bobBson.registerConverter(BsonInt32.class, new BsonInt32Converter());
    bobBson.registerConverter(BsonInt64.class, new BsonInt64Converter());
    bobBson.registerConverter(BsonBinary.class, new BsonBinaryConverter());
    bobBson.registerConverter(BsonDouble.class, new BsonDoubleConverter());
    bobBson.registerConverter(BsonDateTime.class, new BsonDateTimeConverter());
    bobBson.registerConverter(BsonTimestamp.class, new BsonTimestampConverter());
    bobBson.registerConverter(BsonSymbol.class, new BsonSymbolConverter());
    bobBson.registerConverter(BsonObjectId.class, new BsonObjectIdConverter());
    bobBson.registerConverter(BsonMaxKey.class, new BsonMaxKeyConverter());
    bobBson.registerConverter(BsonMinKey.class, new BsonMinKeyConverter());
    bobBson.registerConverter(BsonDecimal128.class, new BsonDecimal128Converter());
    bobBson.registerConverter(BsonDbPointer.class, new BsonDBPointerConverter());
    bobBson.registerConverter(BsonJavaScript.class, new BsonJavaScriptConverter());
    bobBson.registerConverter(
        BsonJavaScriptWithScope.class, new BsonJavaScriptWithScopeConverter(bobBson));
    bobBson.registerConverter(BsonRegularExpression.class, new BsonRegularExpressionConverter());
  }
}
