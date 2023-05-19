package org.bobstuff.bobbson;

import static org.junit.jupiter.api.Assertions.*;

import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class BsonReaderTest {
  @Test
  public void testReadStartDocument() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeInteger("int", 4);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();

    assertEquals(2, sut.getContextStack().getStack().size());
    var context = sut.getContextStack().context;
    assertEquals(BsonContextType.DOCUMENT, context.getCurrentBsonType());
    // type + key + null + int32 + null
    assertEquals(10, context.getRemaining());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadStartDocumentEmbeddedDoc() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartDocument("doc");
    bsonWriter.writeInteger("int", 4);
    bsonWriter.writeEndDocument();
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.DOCUMENT, sut.readBsonType());
    assertEquals("doc", sut.currentFieldName());
    sut.readStartDocument();

    assertEquals(3, sut.getContextStack().getStack().size());
    var context = sut.getContextStack().context;
    assertEquals(BsonContextType.DOCUMENT, context.getCurrentBsonType());
    // type + key + null + int32 + null
    assertEquals(10, context.getRemaining());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadEndDocument() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    sut.readEndDocument();

    assertEquals(0, sut.getContextStack().getCurrentContextIndex());
    var context = sut.getContextStack().context;
    assertEquals(BsonContextType.TOP_LEVEL, context.getCurrentBsonType());
    assertEquals(0, context.getRemaining());
    assertEquals(BsonState.DONE, sut.getState());
  }

  @Test
  public void testReadEndDocumentEmbeddedDoc() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartDocument("doc");
    bsonWriter.writeInteger("int", 4);
    bsonWriter.writeEndDocument();
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.DOCUMENT, sut.readBsonType());
    assertEquals("doc", sut.currentFieldName());
    sut.readStartDocument();
    assertEquals(BsonType.INT32, sut.readBsonType());
    assertEquals("int", sut.currentFieldName());
    sut.readInt32();
    sut.readEndDocument();

    assertEquals(1, sut.getContextStack().getCurrentContextIndex());
    var context = sut.getContextStack().context;
    assertEquals(BsonContextType.DOCUMENT, context.getCurrentBsonType());
    // null byte for enclosing doc
    assertEquals(1, context.getRemaining());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadStartArray() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartArray("bob");
    bsonWriter.writeInteger(3);
    bsonWriter.writeInteger(9);
    bsonWriter.writeEndArray();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.ARRAY, sut.readBsonType());
    assertEquals("bob", sut.currentFieldName());
    sut.readStartArray();

    assertEquals(2, sut.getContextStack().getCurrentContextIndex());
    var context = sut.getContextStack().context;
    assertEquals(BsonContextType.ARRAY, context.getCurrentBsonType());
    // (type + key + null + int32) * 2 + null
    assertEquals(15, context.getRemaining());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadEndArray() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartArray("bob");
    bsonWriter.writeInteger(3);
    bsonWriter.writeInteger(9);
    bsonWriter.writeEndArray();
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.ARRAY, sut.readBsonType());
    assertEquals("bob", sut.currentFieldName());
    sut.readStartArray();
    assertEquals(BsonType.INT32, sut.readBsonType());
    assertEquals(3, sut.readInt32());
    assertEquals(BsonType.INT32, sut.readBsonType());
    assertEquals(9, sut.readInt32());
    sut.readEndArray();

    assertEquals(1, sut.getContextStack().getCurrentContextIndex());
    var context = sut.getContextStack().context;
    assertEquals(BsonContextType.DOCUMENT, context.getCurrentBsonType());
    assertEquals(1, context.getRemaining());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadBsonType() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeInteger("bob", 3);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    var context = sut.getContextStack().context;
    assertEquals(10, context.getRemaining());
    assertEquals(BsonType.INT32, sut.readBsonType());
    assertEquals(5, context.getRemaining());
    assertEquals(BsonState.VALUE, sut.getState());
  }

  @Test
  public void testReadBsonTypeEOD() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    var context = sut.getContextStack().context;
    assertEquals(1, context.getRemaining());
    assertEquals(BsonType.END_OF_DOCUMENT, sut.readBsonType());
    assertEquals(0, context.getRemaining());
    assertEquals(BsonState.END_OF_DOCUMENT, sut.getState());
  }

  @Test
  public void testReadBsonTypeEndOfArray() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartArray("data");
    bsonWriter.writeEndArray();
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    sut.readBsonType();
    sut.readStartArray();
    var context = sut.getContextStack().context;
    assertEquals(1, context.getRemaining());
    assertEquals(BsonType.END_OF_DOCUMENT, sut.readBsonType());
    assertEquals(0, context.getRemaining());
    assertEquals(BsonState.END_OF_ARRAY, sut.getState());
  }

  @Test
  public void testFieldNameRead() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartArray("data");
    bsonWriter.writeEndArray();
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    sut.readBsonType();

    BobBsonBuffer.ByteRangeComparitor comparitor = sut.getFieldName();
    assertEquals("data", comparitor.name());
    assertEquals("data", comparitor.name());
    assertEquals("data", comparitor.name());
  }

  @Test
  public void testGetCurrentBsonType() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartArray("data");
    bsonWriter.writeInteger(6);
    bsonWriter.writeEndArray();
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    sut.readBsonType();
    assertEquals(13, sut.getContextStack().getRemaining());
    assertEquals(BsonType.ARRAY, sut.getCurrentBsonType());
    assertEquals(13, sut.getContextStack().getRemaining());
    assertEquals(BsonType.ARRAY, sut.getCurrentBsonType());
    assertEquals(13, sut.getContextStack().getRemaining());
    assertEquals(BsonType.ARRAY, sut.getCurrentBsonType());
    assertEquals(13, sut.getContextStack().getRemaining());
    sut.readStartArray();
    sut.readBsonType();
    assertEquals(BsonType.INT32, sut.getCurrentBsonType());
    assertEquals(BsonType.INT32, sut.getCurrentBsonType());
    assertEquals(BsonType.INT32, sut.getCurrentBsonType());
  }

  @Test
  public void testReadBoolean() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeBoolean("bool1", true);
    bsonWriter.writeBoolean("bool2", false);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.BOOLEAN, sut.readBsonType());
    assertEquals("bool1", sut.currentFieldName());
    assertEquals(10, sut.getContextStack().getRemaining());
    assertTrue(sut.readBoolean());
    assertEquals(9, sut.getContextStack().getRemaining());
    assertEquals(BsonType.BOOLEAN, sut.readBsonType());
    assertEquals(2, sut.getContextStack().getRemaining());
    assertEquals("bool2", sut.currentFieldName());
    assertFalse(sut.readBoolean());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadRegex() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeRegex("regex1", "/test", "g");
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.REGULAR_EXPRESSION, sut.readBsonType());
    assertEquals("regex1", sut.currentFieldName());
    assertEquals(9, sut.getContextStack().getRemaining());
    var regex = sut.readRegex();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals("/test", regex.getRegex());
    assertEquals("g", regex.getOptions());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadDbPointer() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    var bytes = new ObjectId().toByteArray();
    bsonWriter.writeDbPointer("dbpointer", "namespace", bytes);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.DB_POINTER, sut.readBsonType());
    assertEquals("dbpointer", sut.currentFieldName());
    assertEquals(27, sut.getContextStack().getRemaining());
    var dbPointer = sut.readDbPointerRaw();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals("namespace", dbPointer.getNamespace());
    assertArrayEquals(bytes, dbPointer.getObjectId());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadCodeWithScope() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    var bytes = new ObjectId().toByteArray();
    bsonWriter.writeCodeWithScope("cws", "var i=0", bytes);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.JAVASCRIPT_WITH_SCOPE, sut.readBsonType());
    assertEquals("cws", sut.currentFieldName());
    assertEquals(29, sut.getContextStack().getRemaining());
    var codeWithScope = sut.readCodeWithScope();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals("var i=0", codeWithScope.getCode());
    assertArrayEquals(bytes, codeWithScope.getScope());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadBinary() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    var bytes = new ObjectId().toByteArray();
    bsonWriter.writeBinary("binary", (byte) 1, bytes);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.BINARY, sut.readBsonType());
    assertEquals("binary", sut.currentFieldName());
    assertEquals(18, sut.getContextStack().getRemaining());
    var binary = sut.readBinary();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals((byte) 1, binary.getType());
    assertArrayEquals(bytes, binary.getData());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadBinarySpecialCaseOldBinary() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    var bytes = new ObjectId().toByteArray();
    bsonWriter.writeBinary("binary", (byte) 2, bytes);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.BINARY, sut.readBsonType());
    assertEquals("binary", sut.currentFieldName());
    assertEquals(22, sut.getContextStack().getRemaining());
    var binary = sut.readBinary();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals((byte) 2, binary.getType());
    assertArrayEquals(bytes, binary.getData());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadObjectId() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    var bytes = new ObjectId().toByteArray();
    bsonWriter.writeObjectId("oid", bytes);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.OBJECT_ID, sut.readBsonType());
    assertEquals("oid", sut.currentFieldName());
    assertEquals(13, sut.getContextStack().getRemaining());
    var value = sut.readObjectId();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertArrayEquals(bytes, value);
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadDecimal128() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    var decimal = new Decimal128(123);
    bsonWriter.writeDecimal128("value", decimal);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.DECIMAL128, sut.readBsonType());
    assertEquals("value", sut.currentFieldName());
    assertEquals(17, sut.getContextStack().getRemaining());
    var value = sut.readDecimal128();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals(decimal, value);
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadString() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("value", "bob rocks");
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.STRING, sut.readBsonType());
    assertEquals("value", sut.currentFieldName());
    assertEquals(15, sut.getContextStack().getRemaining());
    var value = sut.readString();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals("bob rocks", value);
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadInt32() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeInteger("value", 23);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.INT32, sut.readBsonType());
    assertEquals("value", sut.currentFieldName());
    assertEquals(5, sut.getContextStack().getRemaining());
    var value = sut.readInt32();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals(23, value);
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadInt64() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeLong("value", 64L);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.INT64, sut.readBsonType());
    assertEquals("value", sut.currentFieldName());
    assertEquals(9, sut.getContextStack().getRemaining());
    var value = sut.readInt64();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals(64L, value);
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadDateTime() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    var datetime = System.currentTimeMillis();
    bsonWriter.writeDateTime("value", datetime);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.DATE_TIME, sut.readBsonType());
    assertEquals("value", sut.currentFieldName());
    assertEquals(9, sut.getContextStack().getRemaining());
    var value = sut.readDateTime();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals(datetime, value);
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadNull() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeNull("value");
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.NULL, sut.readBsonType());
    assertEquals("value", sut.currentFieldName());
    assertEquals(1, sut.getContextStack().getRemaining());
    sut.readNull();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadUndefined() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeUndefined("value");
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.UNDEFINED, sut.readBsonType());
    assertEquals("value", sut.currentFieldName());
    assertEquals(1, sut.getContextStack().getRemaining());
    sut.readUndefined();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testReadDouble() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeDouble("value", 23.23);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    assertEquals(BsonType.DOUBLE, sut.readBsonType());
    assertEquals("value", sut.currentFieldName());
    assertEquals(9, sut.getContextStack().getRemaining());
    var value = sut.readDouble();
    assertEquals(23.23, value);
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testSkipNull() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeNull("value");
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    sut.readBsonType();

    assertEquals(1, sut.getContextStack().getRemaining());
    sut.skipValue();
    assertEquals(1, sut.getContextStack().getRemaining());

    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testSkipBoolean() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeBoolean("value", true);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    sut.readBsonType();

    assertEquals(2, sut.getContextStack().getRemaining());
    sut.skipValue();
    assertEquals(1, sut.getContextStack().getRemaining());

    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testSkipObjectId() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeObjectId("value", new ObjectId().toByteArray());
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    sut.readBsonType();

    assertEquals(13, sut.getContextStack().getRemaining());
    sut.skipValue();
    assertEquals(1, sut.getContextStack().getRemaining());

    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testSkipBinary() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeBinary("value", (byte) 1, new byte[12]);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    sut.readBsonType();

    assertEquals(18, sut.getContextStack().getRemaining());
    sut.skipValue();
    assertEquals(1, sut.getContextStack().getRemaining());

    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testSkipBinaryOldType() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeBinary("value", (byte) 2, new byte[12]);
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    sut.readBsonType();

    assertEquals(22, sut.getContextStack().getRemaining());
    sut.skipValue();
    assertEquals(1, sut.getContextStack().getRemaining());

    assertEquals(BsonState.TYPE, sut.getState());
  }

  @Test
  public void testSkipArray() {
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartArray("bob");
    bsonWriter.writeInteger(3);
    bsonWriter.writeInteger(4);
    bsonWriter.writeEndArray();
    bsonWriter.writeEndDocument();

    var sut = new BsonReaderStack(buffer);
    sut.readStartDocument();
    sut.readBsonType();

    assertEquals(1, sut.getContextStack().getCurrentContextIndex());
    assertEquals(20, sut.getContextStack().getRemaining());
    sut.skipValue();
    assertEquals(1, sut.getContextStack().getRemaining());
    assertEquals(1, sut.getContextStack().getCurrentContextIndex());

    assertEquals(BsonState.TYPE, sut.getState());
  }

  // TODO Test cases for invalid writing and reading orders should generate useful error messages

  @Test
  public void skipInteger() {
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            .addDocumentLength(10)
            .addType(BsonType.INT32)
            .addReadUntil(5)
            .build();
    BsonReader reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    reader.skipValue();

    Mockito.verify(buffer).skipHead(4);
    assertEquals(BsonContextType.DOCUMENT, reader.getCurrentContextType());
  }

  @Test
  public void skipInteger64() {
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            .addDocumentLength(10)
            .addType(BsonType.INT64)
            .addReadUntil(5)
            .build();
    BsonReader reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    reader.skipValue();

    Mockito.verify(buffer).skipHead(8);
    assertEquals(BsonContextType.DOCUMENT, reader.getCurrentContextType());
  }

  @Test
  public void skipString() {
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            .addDocumentLength(10)
            .addType(BsonType.STRING)
            .addReadUntil(5)
            .addIntValue(10)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    reader.skipValue();

    Mockito.verify(buffer).skipHead(9);
    assertEquals(BsonContextType.DOCUMENT, reader.getCurrentContextType());
  }

  @Test
  public void skipToEndOfDocument() {
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            .addDocumentLength(40)
            .addType(BsonType.INT32)
            .addReadUntil(5)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();
    reader.skipContext();
    reader.readEndDocument();

    Mockito.verify(buffer).skipHead(29);
    assertEquals(BsonContextType.TOP_LEVEL, reader.getCurrentContextType());
  }

  @Test
  public void skipToEndOfSubDocument() {
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            .addDocumentLength(52)
            .addType(BsonType.DOCUMENT)
            .addReadUntil(5)
            .addDocumentLength(30)
            .addType(BsonType.STRING)
            .addReadUntil(8)
            .addType(BsonType.END_OF_DOCUMENT)
            .addType(BsonType.INT32)
            .addReadUntil(6)
            .addIntValue(14)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    assertEquals(BsonType.DOCUMENT, reader.readBsonType());
    reader.readStartDocument();
    assertEquals(BsonType.STRING, reader.readBsonType());
    reader.skipContext();
    reader.readEndDocument();

    Mockito.verify(buffer).skipHead(16);

    assertEquals(BsonContextType.DOCUMENT, reader.getCurrentContextType());
    assertEquals(BsonType.INT32, reader.readBsonType());
    assertEquals(14, reader.readInt32());
    reader.readEndDocument();
    assertEquals(BsonContextType.TOP_LEVEL, reader.getCurrentContextType());
  }

  @Test
  public void skipToEndOfMultipleSubDocument() {
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            .addDocumentLength(80)
            .addType(BsonType.DOCUMENT)
            .addReadUntil(5)
            .addDocumentLength(30)
            .addType(BsonType.STRING)
            .addReadUntil(8)
            .addType(BsonType.END_OF_DOCUMENT)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    assertEquals(BsonType.DOCUMENT, reader.readBsonType());
    reader.readStartDocument();
    assertEquals(BsonType.STRING, reader.readBsonType());
    reader.skipContext();
    reader.readEndDocument();

    InOrder inOrder = Mockito.inOrder(buffer);

    inOrder.verify(buffer).skipHead(16);

    reader.skipContext();
    reader.readEndDocument();
    inOrder.verify(buffer).skipHead(39);
    assertEquals(BsonContextType.TOP_LEVEL, reader.getCurrentContextType());
  }

  @Test
  public void skipToEndOfSimpleArray() {
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            .addDocumentLength(52)
            .addType(BsonType.ARRAY)
            .addReadUntil(5)
            .addDocumentLength(30)
            .addType(BsonType.STRING)
            .addReadUntil(8)
            .addType(BsonType.END_OF_DOCUMENT)
            .addType(BsonType.INT32)
            .addReadUntil(6)
            .addIntValue(14)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    assertEquals(BsonType.ARRAY, reader.readBsonType());
    reader.readStartArray();
    assertEquals(BsonType.STRING, reader.readBsonType());
    reader.skipContext();

    Mockito.verify(buffer).skipHead(16);

    reader.readEndArray();
    assertEquals(BsonContextType.DOCUMENT, reader.getCurrentContextType());
    assertEquals(BsonType.INT32, reader.readBsonType());
    assertEquals(14, reader.readInt32());
    reader.readEndDocument();
    assertEquals(BsonContextType.TOP_LEVEL, reader.getCurrentContextType());
  }

  @Test
  public void skipToEndOfDocumentInsideArray() {
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            // TOP LEVEL DOC
            .addDocumentLength(80)
            // INTERNAL ARRAY
            .addType(BsonType.ARRAY)
            .addReadUntil(5)
            .addDocumentLength(58)
            // DOCUMENT INSIDE ARRAY
            .addType(BsonType.DOCUMENT)
            .addReadUntil(8)
            .addDocumentLength(15)
            .addType(BsonType.STRING)
            .addReadUntil(4)
            // SKIP WILL BE CALLED HERE
            .addType(BsonType.END_OF_DOCUMENT)
            // SECOND DOCUMENT INSIDE ARRAY
            .addType(BsonType.DOCUMENT)
            .addReadUntil(8)
            .addDocumentLength(20)
            .addType(BsonType.STRING)
            .addReadUntil(4)
            .addReadStringValue("words!")
            .addType(BsonType.END_OF_DOCUMENT) // document end
            .addType(BsonType.END_OF_DOCUMENT) // array end
            .addType(BsonType.INT32)
            .addReadUntil(6)
            .addIntValue(14)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    assertEquals(BsonType.ARRAY, reader.readBsonType());
    reader.readStartArray();
    assertEquals(BsonType.DOCUMENT, reader.readBsonType());
    reader.readStartDocument();
    assertEquals(BsonType.STRING, reader.readBsonType());
    reader.skipContext();
    reader.readEndDocument();

    Mockito.verify(buffer).skipHead(5);

    assertEquals(BsonContextType.ARRAY, reader.getCurrentContextType());
    assertEquals(BsonType.DOCUMENT, reader.readBsonType());
    reader.readStartDocument();
    assertEquals(BsonType.STRING, reader.readBsonType());
    assertEquals("words!", reader.readString());
    assertEquals(BsonType.END_OF_DOCUMENT, reader.readBsonType());
    reader.readEndDocument();
    assertEquals(BsonType.END_OF_DOCUMENT, reader.readBsonType());
    reader.readEndArray();
    assertEquals(BsonType.INT32, reader.readBsonType());
    assertEquals(14, reader.readInt32());
    reader.readEndDocument();
    assertEquals(BsonContextType.TOP_LEVEL, reader.getCurrentContextType());
  }

  @Test
  public void skipToEndFromDocumentInsideArray() {
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            // TOP LEVEL DOC
            .addDocumentLength(80)
            // INTERNAL ARRAY
            .addType(BsonType.ARRAY)
            .addReadUntil(5)
            .addDocumentLength(50)
            // DOCUMENT INSIDE ARRAY
            .addType(BsonType.DOCUMENT)
            .addReadUntil(8)
            .addDocumentLength(15)
            .addType(BsonType.STRING)
            .addReadUntil(4)
            // SKIP WILL BE CALLED HERE
            .addType(BsonType.END_OF_DOCUMENT)
            .addType(BsonType.END_OF_DOCUMENT)
            .addType(BsonType.END_OF_DOCUMENT)
            //                             .addType(BsonType.END_OF_DOCUMENT)
            .build();

    BsonReader reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    assertEquals(BsonType.ARRAY, reader.readBsonType());
    reader.readStartArray();
    assertEquals(BsonType.DOCUMENT, reader.readBsonType());
    reader.readStartDocument();
    assertEquals(BsonType.STRING, reader.readBsonType());
    reader.skipToEnd();
    //    reader.readEndDocument();

    InOrder inOrder = Mockito.inOrder(buffer);
    inOrder.verify(buffer).skipHead(5);
    inOrder.verify(buffer).skipHead(21);
    inOrder.verify(buffer).skipHead(19);

    assertEquals(BsonContextType.TOP_LEVEL, reader.getCurrentContextType());
  }
}
