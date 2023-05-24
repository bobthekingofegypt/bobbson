package org.bobstuff.bobbson.writer;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BsonWriterTest {
  private static final byte NULL_BYTE = (byte) 0;
  private BsonWriter writer;
  private ByteBufferBobBsonBuffer bsonBuffer;
  private byte[] buffer;

  @BeforeEach
  public void setUp() {
    buffer = new byte[1000];
    bsonBuffer = new ByteBufferBobBsonBuffer(buffer);
    writer = new StackBsonWriter(bsonBuffer);
    writer.writeStartDocument();
  }

  @Test
  public void testWriteStartDocumentNoArgs() {
    assertEquals(4, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(0, bsonBuffer.getInt());
  }

  @Test
  public void testWriteStartDocumentWriteName() {
    writer.writeName("bob");
    writer.writeStartDocument();
    validateStartDocument();
  }

  @Test
  public void testWriteStartDocumentWriteNameBytes() {
    writer.writeName("bob".getBytes(StandardCharsets.UTF_8));
    writer.writeStartDocument();
    validateStartDocument();
  }

  @Test
  public void testWriteStartDocumentWriteNameReuse() {
    writer.writeName("fred");
    writer.writeStartDocument("bob");
    validateStartDocument();
    writer.writeStartDocument();

    assertEquals(BsonType.DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("fred", bsonBuffer.getString(4));
    assertEquals(BsonType.END_OF_DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals(0, bsonBuffer.getInt());
  }

  @Test
  public void testWriteStartDocumentWithName() {
    writer.writeStartDocument("bob");
    validateStartDocument();
  }

  @Test
  public void testWriteStartDocumentWithNameBytes() {
    writer.writeStartDocument("bob".getBytes(StandardCharsets.UTF_8));
    validateStartDocument();
  }

  private void validateStartDocument() {
    assertEquals(13, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("bob", bsonBuffer.getString(3));
    assertEquals(BsonType.END_OF_DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals(0, bsonBuffer.getInt());
  }

  @Test
  public void testWriteStartArrayNoName() {
    assertThrows(IllegalStateException.class, () -> writer.writeStartArray());
  }

  @Test
  public void testWriteStartArrayWriteName() {
    writer.writeName("bob");
    writer.writeStartArray();
    validateStartArray();
  }

  @Test
  public void testWriteStartArrayWriteNameBytes() {
    writer.writeName("bob".getBytes(StandardCharsets.UTF_8));
    writer.writeStartArray();
    validateStartArray();
  }

  @Test
  public void testWriteStartArrayName() {
    writer.writeStartArray("bob");
    validateStartArray();
  }

  @Test
  public void testWriteStartArrayNameBytes() {
    writer.writeStartArray("bob".getBytes(StandardCharsets.UTF_8));
    validateStartArray();
  }

  private void validateStartArray() {
    assertEquals(13, bsonBuffer.getTail());
    assertEquals(BsonState.VALUE, writer.getState());
    assertEquals(BsonContextType.ARRAY, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.ARRAY, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("bob", bsonBuffer.getString(3));
    assertEquals(BsonType.END_OF_DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals(0, bsonBuffer.getInt());
  }

  @Test
  public void testWriteEndDocument() {
    writer.writeStartDocument("bob");
    validateStartDocument();
    writer.writeInteger("int", 54);
    writer.writeEndDocument();

    assertEquals(23, bsonBuffer.getTail());
    assertEquals(BsonType.INT32, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("int", bsonBuffer.getString(3));
    assertEquals(BsonType.END_OF_DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals(54, bsonBuffer.getInt());
    assertEquals(BsonType.END_OF_DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    bsonBuffer.setHead(9);
    assertEquals(14, bsonBuffer.getInt());
  }

  @Test
  public void testWriteEndParentDocument() {
    writer.writeInteger("int", 40);
    writer.writeEndDocument();

    assertEquals(BsonState.DONE, writer.getState());
    assertEquals(14, bsonBuffer.getInt());
    assertEquals(14, bsonBuffer.getTail());
    assertEquals(BsonType.INT32, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("int", bsonBuffer.getString(3));
    assertEquals(BsonType.END_OF_DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals(40, bsonBuffer.getInt());
    assertEquals(BsonType.END_OF_DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals(BsonContextType.TOP_LEVEL, writer.getCurrentBsonContext());
  }

  @Test
  public void testWriteEndArray() {
    writer.writeStartArray("bob");
    validateStartArray();
    writer.writeInteger(54);
    writer.writeInteger(445);
    writer.writeEndArray();

    assertEquals(28, bsonBuffer.getTail());
    assertEquals(BsonType.INT32, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("0", bsonBuffer.getString(1));
    assertEquals(BsonType.END_OF_DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals(54, bsonBuffer.getInt());
    assertEquals(BsonType.INT32, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("1", bsonBuffer.getString(1));
    assertEquals(BsonType.END_OF_DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals(445, bsonBuffer.getInt());
    assertEquals(BsonType.END_OF_DOCUMENT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    bsonBuffer.setHead(9);
    assertEquals(19, bsonBuffer.getInt());
  }

  @Test
  public void testWriteRegexName() {
    writer.writeRegex("aregex", "\s+", "g");
    validateRegex();
  }

  @Test
  public void testWriteRegexNameBytes() {
    writer.writeRegex("aregex".getBytes(StandardCharsets.UTF_8), "\s+", "g");
    validateRegex();
  }

  @Test
  public void testWriteRegexWriteNameBytes() {
    writer.writeName("aregex".getBytes(StandardCharsets.UTF_8));
    writer.writeRegex("\s+", "g");
    validateRegex();
  }

  @Test
  public void testWriteRegexWriteName() {
    writer.writeName("aregex");
    writer.writeRegex("\s+", "g");
    validateRegex();
  }

  private void validateRegex() {
    assertEquals(17, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.REGULAR_EXPRESSION, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("aregex", bsonBuffer.getString(6));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals("\s+", bsonBuffer.getString(2));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals("g", bsonBuffer.getString(1));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
  }

  @Test
  public void testWriteBinaryName() {
    writer.writeBinary("abinary", (byte) 3, new byte[] {1, 2, 3});
    validateBinary();
  }

  @Test
  public void testWriteBinaryNameBytes() {
    writer.writeBinary("abinary".getBytes(StandardCharsets.UTF_8), (byte) 3, new byte[] {1, 2, 3});
    validateBinary();
  }

  @Test
  public void testWriteBinaryWriteNameBytes() {
    writer.writeName("abinary".getBytes(StandardCharsets.UTF_8));
    writer.writeBinary((byte) 3, new byte[] {1, 2, 3});
    validateBinary();
  }

  @Test
  public void testWriteBinaryWriteName() {
    writer.writeName("abinary");
    writer.writeBinary((byte) 3, new byte[] {1, 2, 3});
    validateBinary();
  }

  @Test
  public void testWriteBinaryTypeTwo() {
    writer.writeName("abinary");
    writer.writeBinary((byte) 2, new byte[] {1, 2, 3});
    assertEquals(25, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.BINARY, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("abinary", bsonBuffer.getString(7));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(7, bsonBuffer.getInt());
    assertEquals((byte) 2, bsonBuffer.getByte());
    assertEquals(3, bsonBuffer.getInt());
    assertArrayEquals(new byte[] {1, 2, 3}, bsonBuffer.getBytes(3));
  }

  private void validateBinary() {
    assertEquals(21, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.BINARY, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("abinary", bsonBuffer.getString(7));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(3, bsonBuffer.getInt());
    assertEquals((byte) 3, bsonBuffer.getByte());
    assertArrayEquals(new byte[] {1, 2, 3}, bsonBuffer.getBytes(3));
  }

  @Test
  public void testWriteDbPointerName() {
    writer.writeDbPointer(
        "adbpointer", "namespace", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    validateDbPointer();
  }

  @Test
  public void testWriteDbPointerNameBytes() {
    writer.writeDbPointer(
        "adbpointer".getBytes(StandardCharsets.UTF_8),
        "namespace",
        new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    validateDbPointer();
  }

  @Test
  public void testWriteDbPointerWriteNameBytes() {
    writer.writeName("adbpointer".getBytes(StandardCharsets.UTF_8));
    writer.writeDbPointer("namespace", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    validateDbPointer();
  }

  @Test
  public void testWriteDbPointerWriteName() {
    writer.writeName("adbpointer");
    writer.writeDbPointer("namespace", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    validateDbPointer();
  }

  private void validateDbPointer() {
    assertEquals(42, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.DB_POINTER, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("adbpointer", bsonBuffer.getString(10));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(10, bsonBuffer.getInt());
    assertEquals("namespace", bsonBuffer.getString(9));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, bsonBuffer.getBytes(12));
  }

  @Test
  public void testWriteCodeWithScopeName() {
    writer.writeCodeWithScope("field", "const b=3;", new byte[] {1, 2, 3});
    validateCodeWithScope();
  }

  @Test
  public void testWriteCodeWithScopeNameBytes() {
    writer.writeCodeWithScope(
        "field".getBytes(StandardCharsets.UTF_8), "const b=3;", new byte[] {1, 2, 3});
    validateCodeWithScope();
  }

  @Test
  public void testWriteCodeWithScopeWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeCodeWithScope("const b=3;", new byte[] {1, 2, 3});
    validateCodeWithScope();
  }

  @Test
  public void testWriteCodeWithScopeWriteName() {
    writer.writeName("field");
    writer.writeCodeWithScope("const b=3;", new byte[] {1, 2, 3});
    validateCodeWithScope();
  }

  private void validateCodeWithScope() {
    assertEquals(33, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.JAVASCRIPT_WITH_SCOPE, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(22, bsonBuffer.getInt());
    assertEquals(11, bsonBuffer.getInt());
    assertEquals("const b=3;", bsonBuffer.getString(10));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertArrayEquals(new byte[] {1, 2, 3}, bsonBuffer.getBytes(3));
  }

  @Test
  public void testWriteDecimal128Name() {
    writer.writeDecimal128("field", new Decimal128(123));
    validateDecimal128();
  }

  @Test
  public void testWriteDecimal128NameBytes() {
    writer.writeDecimal128("field".getBytes(StandardCharsets.UTF_8), new Decimal128(123));
    validateDecimal128();
  }

  @Test
  public void testWriteDecimal128WriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeDecimal128(new Decimal128(123));
    validateDecimal128();
  }

  @Test
  public void testWriteDecimal128WriteName() {
    writer.writeName("field");
    writer.writeDecimal128(new Decimal128(123));
    validateDecimal128();
  }

  @Test
  public void testWriteDecimal128PartsName() {
    var dec = new Decimal128(123);
    writer.writeDecimal128("field", dec.getHigh(), dec.getLow());
    validateDecimal128();
  }

  @Test
  public void testWriteDecimal128PartsNameBytes() {
    var dec = new Decimal128(123);
    writer.writeDecimal128("field".getBytes(StandardCharsets.UTF_8), dec.getHigh(), dec.getLow());
    validateDecimal128();
  }

  @Test
  public void testWriteDecimal128PartsWriteNameBytes() {
    var dec = new Decimal128(123);
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeDecimal128(dec.getHigh(), dec.getLow());
    validateDecimal128();
  }

  @Test
  public void testWriteDecimal128PartsWriteName() {
    var dec = new Decimal128(123);
    writer.writeName("field");
    writer.writeDecimal128(dec.getHigh(), dec.getLow());
    validateDecimal128();
  }

  private void validateDecimal128() {
    assertEquals(27, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.DECIMAL128, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(123, bsonBuffer.getLong());
    assertEquals(3476778912330022912L, bsonBuffer.getLong());
  }

  @Test
  public void testWriteMaxKeyName() {
    writer.writeMaxKey("field");
    validateMaxKey();
  }

  @Test
  public void testWriteMaxKeyNameBytes() {
    writer.writeMaxKey("field".getBytes(StandardCharsets.UTF_8));
    validateMaxKey();
  }

  @Test
  public void testWriteMaxKeyWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeMaxKey();
    validateMaxKey();
  }

  @Test
  public void testWriteMaxKeyWriteName() {
    writer.writeName("field");
    writer.writeMaxKey();
    validateMaxKey();
  }

  private void validateMaxKey() {
    assertEquals(11, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.MAX_KEY, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
  }

  @Test
  public void testWriteMinKeyName() {
    writer.writeMinKey("field");
    validateMinKey();
  }

  @Test
  public void testWriteMinKeyNameBytes() {
    writer.writeMinKey("field".getBytes(StandardCharsets.UTF_8));
    validateMinKey();
  }

  @Test
  public void testWriteMinKeyWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeMinKey();
    validateMinKey();
  }

  @Test
  public void testWriteMinKeyWriteName() {
    writer.writeName("field");
    writer.writeMinKey();
    validateMinKey();
  }

  private void validateMinKey() {
    assertEquals(11, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.MIN_KEY, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
  }

  @Test
  public void testWriteNullName() {
    writer.writeNull("field");
    validateNull();
  }

  @Test
  public void testWriteNullNameBytes() {
    writer.writeNull("field".getBytes(StandardCharsets.UTF_8));
    validateNull();
  }

  @Test
  public void testWriteNullWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeNull();
    validateNull();
  }

  @Test
  public void testWriteNullWriteName() {
    writer.writeName("field");
    writer.writeNull();
    validateNull();
  }

  private void validateNull() {
    assertEquals(11, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.NULL, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
  }

  @Test
  public void testWriteUndefinedName() {
    writer.writeUndefined("field");
    validateUndefined();
  }

  @Test
  public void testWriteUndefinedNameBytes() {
    writer.writeUndefined("field".getBytes(StandardCharsets.UTF_8));
    validateUndefined();
  }

  @Test
  public void testWriteUndefinedWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeUndefined();
    validateUndefined();
  }

  @Test
  public void testWriteUndefinedWriteName() {
    writer.writeName("field");
    writer.writeUndefined();
    validateUndefined();
  }

  private void validateUndefined() {
    assertEquals(11, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.UNDEFINED, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
  }

  @Test
  public void testWriteSymbolName() {
    writer.writeSymbol("field", "a symbol");
    validateSymbol();
  }

  @Test
  public void testWriteSymbolNameBytes() {
    writer.writeSymbol("field".getBytes(StandardCharsets.UTF_8), "a symbol");
    validateSymbol();
  }

  @Test
  public void testWriteSymbolWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeSymbol("a symbol");
    validateSymbol();
  }

  @Test
  public void testWriteSymbolWriteName() {
    writer.writeName("field");
    writer.writeSymbol("a symbol");
    validateSymbol();
  }

  private void validateSymbol() {
    assertEquals(24, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.SYMBOL, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(9, bsonBuffer.getInt());
    assertEquals("a symbol", bsonBuffer.getString(8));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
  }

  @Test
  public void testWriteJavascriptName() {
    writer.writeJavascript("field", "a js man");
    validateJavascript();
  }

  @Test
  public void testWriteJavascriptNameBytes() {
    writer.writeJavascript("field".getBytes(StandardCharsets.UTF_8), "a js man");
    validateJavascript();
  }

  @Test
  public void testWriteJavascriptWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeJavascript("a js man");
    validateJavascript();
  }

  @Test
  public void testWriteJavascriptWriteName() {
    writer.writeName("field");
    writer.writeJavascript("a js man");
    validateJavascript();
  }

  private void validateJavascript() {
    assertEquals(24, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.JAVASCRIPT, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(9, bsonBuffer.getInt());
    assertEquals("a js man", bsonBuffer.getString(8));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
  }

  @Test
  public void testWriteStringName() {
    writer.writeString("field", "a string");
    validateString();
  }

  @Test
  public void testWriteStringNameBytes() {
    writer.writeString("field".getBytes(StandardCharsets.UTF_8), "a string");
    validateString();
  }

  @Test
  public void testWriteStringWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeString("a string");
    validateString();
  }

  @Test
  public void testWriteStringWriteName() {
    writer.writeName("field");
    writer.writeString("a string");
    validateString();
  }

  private void validateString() {
    assertEquals(24, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.STRING, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(9, bsonBuffer.getInt());
    assertEquals("a string", bsonBuffer.getString(8));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
  }

  @Test
  public void testWriteObjectIdName() {
    writer.writeObjectId("field", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    validateObjectId();
  }

  @Test
  public void testWriteObjectIdNameBytes() {
    writer.writeObjectId(
        "field".getBytes(StandardCharsets.UTF_8),
        new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    validateObjectId();
  }

  @Test
  public void testWriteObjectIdWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeObjectId(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    validateObjectId();
  }

  @Test
  public void testWriteObjectIdWriteName() {
    writer.writeName("field");
    writer.writeObjectId(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    validateObjectId();
  }

  private void validateObjectId() {
    assertEquals(23, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.OBJECT_ID, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, bsonBuffer.getBytes(12));
  }

  @Test
  public void testWriteDoubleName() {
    writer.writeDouble("field", 2.3);
    validateDouble();
  }

  @Test
  public void testWriteDoubleNameBytes() {
    writer.writeDouble("field".getBytes(StandardCharsets.UTF_8), 2.3);
    validateDouble();
  }

  @Test
  public void testWriteDoubleWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeDouble(2.3);
    validateDouble();
  }

  @Test
  public void testWriteDoubleWriteName() {
    writer.writeName("field");
    writer.writeDouble(2.3);
    validateDouble();
  }

  private void validateDouble() {
    assertEquals(19, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.DOUBLE, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(2.3, bsonBuffer.getDouble());
  }

  @Test
  public void testWriteBooleanName() {
    writer.writeBoolean("field", true);
    validateBoolean();
  }

  @Test
  public void testWriteBooleanNameBytes() {
    writer.writeBoolean("field".getBytes(StandardCharsets.UTF_8), true);
    validateBoolean();
  }

  @Test
  public void testWriteBooleanWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeBoolean(true);
    validateBoolean();
  }

  @Test
  public void testWriteBooleanWriteName() {
    writer.writeName("field");
    writer.writeBoolean(true);
    validateBoolean();
  }

  private void validateBoolean() {
    assertEquals(12, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.BOOLEAN, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals((byte) 1, bsonBuffer.getByte());
  }

  @Test
  public void testWriteBooleanFalseName() {
    writer.writeBoolean("field", false);
    validateBooleanFalse();
  }

  @Test
  public void testWriteBooleanFalseNameBytes() {
    writer.writeBoolean("field".getBytes(StandardCharsets.UTF_8), false);
    validateBooleanFalse();
  }

  @Test
  public void testWriteBooleanFalseWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeBoolean(false);
    validateBooleanFalse();
  }

  @Test
  public void testWriteBooleanFalseWriteName() {
    writer.writeName("field");
    writer.writeBoolean(false);
    validateBooleanFalse();
  }

  private void validateBooleanFalse() {
    assertEquals(12, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.BOOLEAN, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals((byte) 0, bsonBuffer.getByte());
  }

  @Test
  public void testWriteIntegerName() {
    writer.writeInteger("field", 2);
    validateInteger();
  }

  @Test
  public void testWriteIntegerNameBytes() {
    writer.writeInteger("field".getBytes(StandardCharsets.UTF_8), 2);
    validateInteger();
  }

  @Test
  public void testWriteIntegerWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeInteger(2);
    validateInteger();
  }

  @Test
  public void testWriteIntegerWriteName() {
    writer.writeName("field");
    writer.writeInteger(2);
    validateInteger();
  }

  private void validateInteger() {
    assertEquals(15, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.INT32, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(2, bsonBuffer.getInt());
  }

  @Test
  public void testWriteLongName() {
    writer.writeLong("field", 245L);
    validateLong();
  }

  @Test
  public void testWriteLongNameBytes() {
    writer.writeLong("field".getBytes(StandardCharsets.UTF_8), 245L);
    validateLong();
  }

  @Test
  public void testWriteLongWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeLong(245L);
    validateLong();
  }

  @Test
  public void testWriteLongWriteName() {
    writer.writeName("field");
    writer.writeLong(245L);
    validateLong();
  }

  private void validateLong() {
    assertEquals(19, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.INT64, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(245L, bsonBuffer.getLong());
  }

  @Test
  public void testWriteDatetimeName() {
    writer.writeDateTime("field", 245L);
    validateDatetime();
  }

  @Test
  public void testWriteDatetimeNameBytes() {
    writer.writeDateTime("field".getBytes(StandardCharsets.UTF_8), 245L);
    validateDatetime();
  }

  @Test
  public void testWriteDatetimeWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeDateTime(245L);
    validateDatetime();
  }

  @Test
  public void testWriteDatetimeWriteName() {
    writer.writeName("field");
    writer.writeDateTime(245L);
    validateDatetime();
  }

  private void validateDatetime() {
    assertEquals(19, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.DATE_TIME, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(245L, bsonBuffer.getLong());
  }

  @Test
  public void testWriteTimestampName() {
    writer.writeTimestamp("field", 245L);
    validateTimestamp();
  }

  @Test
  public void testWriteTimestampNameBytes() {
    writer.writeTimestamp("field".getBytes(StandardCharsets.UTF_8), 245L);
    validateTimestamp();
  }

  @Test
  public void testWriteTimestampWriteNameBytes() {
    writer.writeName("field".getBytes(StandardCharsets.UTF_8));
    writer.writeTimestamp(245L);
    validateTimestamp();
  }

  @Test
  public void testWriteTimestampWriteName() {
    writer.writeName("field");
    writer.writeTimestamp(245L);
    validateTimestamp();
  }

  private void validateTimestamp() {
    assertEquals(19, bsonBuffer.getTail());
    assertEquals(BsonState.NAME, writer.getState());
    assertEquals(BsonContextType.DOCUMENT, writer.getCurrentBsonContext());

    assertEquals(0, bsonBuffer.getInt());
    assertEquals(BsonType.TIMESTAMP, BsonType.findByValue(bsonBuffer.getByte()));
    assertEquals("field", bsonBuffer.getString(5));
    assertEquals(NULL_BYTE, bsonBuffer.getByte());
    assertEquals(245L, bsonBuffer.getLong());
  }
}
