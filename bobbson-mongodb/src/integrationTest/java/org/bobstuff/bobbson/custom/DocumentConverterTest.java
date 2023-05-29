package org.bobstuff.bobbson.custom;

import java.io.ByteArrayOutputStream;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.converters.BsonValueConverters;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.utils.MDBBsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DocumentConverterTest {
  @Test
  public void testDocumentDeserialisation() throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeString("name", "fred");

    var reader = MDBBsonWriter.reader(bsonWriter);

    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    Document document = bobBson.deserialise(Document.class, reader);

    Assertions.assertEquals("fred", document.get("name"));
  }

  @Test
  public void testDocumentSerialisation() throws Exception {
    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    Document document = new Document();
    document.put("name", "bob");

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bobBson.serialise(document, Document.class, bsonWriter);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write(buffer.getArray(), 0, buffer.getTail());
    var data = bos.toByteArray();

    var reader = new StackBsonReader(new BobBufferBobBsonBuffer(data, 0, data.length));
    var bsonDoc = bobBson.deserialise(BsonDocument.class, reader);

    Assertions.assertEquals("bob", bsonDoc.getString("name").getValue());
  }
}
