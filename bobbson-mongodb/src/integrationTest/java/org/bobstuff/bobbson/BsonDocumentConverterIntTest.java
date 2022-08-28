package org.bobstuff.bobbson;

import org.bobstuff.bobbson.converters.BsonValueConverters;
import org.bobstuff.bobbson.utils.MDBBsonWriter;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BsonDocumentConverterIntTest {
  @Test
  public void testBsonDocumentString() throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeString("name", "fred");

    var reader = MDBBsonWriter.reader(bsonWriter);

    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    BsonDocument document = bobBson.deserialise(BsonDocument.class, reader);

    Assertions.assertEquals("fred", document.getString("name").getValue());
  }

  @Test
  public void testBsonDocumentNull() throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeNull("name");

    var reader = MDBBsonWriter.reader(bsonWriter);

    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    BsonDocument document = bobBson.deserialise(BsonDocument.class, reader);

    Assertions.assertTrue(document.get("name").isNull());
  }

  @Test
  public void testBsonDocumentArray() throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeStartArray("names");
    bsonWriter.writeString("fred");
    bsonWriter.writeString("james");
    bsonWriter.writeEndArray();

    var reader = MDBBsonWriter.reader(bsonWriter);

    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    BsonDocument document = bobBson.deserialise(BsonDocument.class, reader);

    Assertions.assertTrue(document.get("names").isArray());

    BsonArray values = document.getArray("names");

    Assertions.assertEquals("fred", values.get(0).asString().getValue());
    Assertions.assertEquals("james", values.get(1).asString().getValue());
  }
}
