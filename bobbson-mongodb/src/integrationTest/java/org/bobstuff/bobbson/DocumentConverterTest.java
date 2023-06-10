package org.bobstuff.bobbson;

import java.util.List;
import org.bobstuff.bobbson.converters.BsonValueConverters;
import org.bobstuff.bobbson.utils.MDBBsonWriter;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DocumentConverterTest {
  //  @Test
  //  public void testBsonDocumentString() throws Exception {
  //    var bsonWriter = MDBBsonWriter.writer();
  //    bsonWriter.writeString("name", "fred");
  //
  //    var reader = MDBBsonWriter.reader(bsonWriter);
  //
  //    BobBson bobBson = new BobBson();
  //    BsonValueConverters.register(bobBson);
  //
  //    BsonDocument document = bobBson.deserialise(BsonDocument.class, reader);
  //
  //    Assertions.assertEquals("fred", document.getString("name").getValue());
  //  }
  //
  //  @Test
  //  public void testBsonDocumentNull() throws Exception {
  //    var bsonWriter = MDBBsonWriter.writer();
  //    bsonWriter.writeNull("name");
  //
  //    var reader = MDBBsonWriter.reader(bsonWriter);
  //
  //    BobBson bobBson = new BobBson();
  //    BsonValueConverters.register(bobBson);
  //
  //    BsonDocument document = bobBson.deserialise(BsonDocument.class, reader);
  //
  //    Assertions.assertTrue(document.get("name").isNull());
  //  }

  @Test
  public void testBsonDocumentArray() throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeStartDocument("data");
    bsonWriter.writeStartArray("names");
    bsonWriter.writeString("fred");
    bsonWriter.writeString("james");
    bsonWriter.writeEndArray();
    bsonWriter.writeEndDocument();

    var reader = MDBBsonWriter.reader(bsonWriter);

    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    var document = bobBson.deserialise(Document.class, reader);

    Assertions.assertNotNull(((Document) document.get("data")).get("names"));

    var values = (List) ((Document) document.get("data")).get("names");

    Assertions.assertEquals("fred", values.get(0));
    Assertions.assertEquals("james", values.get(1));
  }

  @Test
  public void testBsonDocumentArrayOfDocuments() throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeStartDocument("data");
    bsonWriter.writeStartArray("names");
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("name", "fred");
    bsonWriter.writeEndDocument();
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("name", "james");
    bsonWriter.writeEndDocument();
    bsonWriter.writeEndArray();
    bsonWriter.writeEndDocument();

    var reader = MDBBsonWriter.reader(bsonWriter);

    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    var document = bobBson.deserialise(Document.class, reader);

    Assertions.assertNotNull(((Document) document.get("data")).get("names"));

    var values = (List) ((Document) document.get("data")).get("names");
    var one = (Document) values.get(0);
    var two = (Document) values.get(1);

    Assertions.assertEquals("fred", one.get("name"));
    Assertions.assertEquals("james", two.get("name"));
  }
}
