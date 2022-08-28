package org.bobstuff.bobbson;

import static org.bobstuff.bobbson.BsonContextType.DOCUMENT;
import static org.bobstuff.bobbson.BsonContextType.TOP_LEVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * This should be a collection of tests to validate the context moving as the parser proceeds.
 * Contexts store what type of field you are current inside ie array or document and how long that
 * document is to allow you to just skip through it when you don't need any of the remaining data.
 */
public class BsonReaderContextTests {
  @Test
  public void testContextSizeIsSet_singleDoc() {
    ContextStack contextStack = new ContextStack();
    BobBsonBuffer buffer = BufferDataMockBuilder.builder().addDocumentLength(80).build();
    BsonReader reader = new BsonReader(buffer, contextStack);

    assertEquals(0, contextStack.getCurrentContextIndex());
    assertEquals(TOP_LEVEL, contextStack.context.getCurrentBsonType());
    assertEquals(0, contextStack.context.getRemaining());

    reader.readStartDocument();

    assertEquals(1, contextStack.getCurrentContextIndex());
    assertEquals(DOCUMENT, contextStack.context.getCurrentBsonType());
    assertEquals(76, contextStack.context.getRemaining());
  }

  @Test
  public void testContextSizeAfterSkipContext_isNullBytePositioned() {
    /*
    reader.skipContext should move the buffer forward till the null byte for that context so
    parser can then call readEndDocument as normal so skip context is no different than readInt
    or any other forwarding moving buffer operation.
     */
    ContextStack contextStack = new ContextStack();
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            .addDocumentLength(80)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReader(buffer, contextStack);

    reader.readStartDocument();
    reader.skipContext();

    assertEquals(1, contextStack.getCurrentContextIndex());
    assertEquals(DOCUMENT, contextStack.context.getCurrentBsonType());
    assertEquals(1, contextStack.context.getRemaining());

    reader.readEndDocument();

    /*
    the document context should have been popped and now we are back on the top level
     */
    assertEquals(0, contextStack.getStack().get(1).getRemaining());
    assertEquals(0, contextStack.context.getRemaining());
    assertEquals(0, contextStack.getCurrentContextIndex());
    assertEquals(TOP_LEVEL, contextStack.context.getCurrentBsonType());
  }

  @Test
  public void testContextSizeAfterSkipInt() {
    ContextStack contextStack = new ContextStack();
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            .addDocumentLength(80)
            .addType(BsonType.INT32)
            .addReadUntil(5)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReader(buffer, contextStack);

    reader.readStartDocument();

    assertEquals(76, contextStack.context.getRemaining());

    assertEquals(BsonType.INT32, reader.readBsonType());
    reader.skipValue();
    // 75 - key size (5) - bsontype (1) - size of int (4)
    assertEquals(76 - 6 - 4, contextStack.context.getRemaining());
  }

  @Test
  public void testContextSizeAfterSkipContextEmbeddedDocument() {
    ContextStack contextStack = new ContextStack();
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            .addDocumentLength(80)
            .addType(BsonType.DOCUMENT)
            .addDocumentLength(40)
            .addReadUntil(5)
            .addType(BsonType.END_OF_DOCUMENT)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReader(buffer, contextStack);

    reader.readStartDocument();

    assertEquals(1, contextStack.getCurrentContextIndex());
    assertEquals(76, contextStack.getRemaining());

    assertEquals(BsonType.DOCUMENT, reader.readBsonType());

    reader.readStartDocument();

    assertEquals(76 - 40 - 4 - 1 - 1, contextStack.getStack().get(1).getRemaining());
    assertEquals(2, contextStack.getCurrentContextIndex());
    assertEquals(36, contextStack.getRemaining());

    reader.skipContext();

    assertEquals(2, contextStack.getCurrentContextIndex());
    assertEquals(1, contextStack.getRemaining());

    reader.readEndDocument();

    assertEquals(1, contextStack.getCurrentContextIndex());
    assertEquals(76 - 40 - 4 - 1 - 1, contextStack.getRemaining());
  }

  @Test
  public void testContextSizeAfterSkipContextDocumentInsideArray() {
    ContextStack contextStack = new ContextStack();
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
            .addDocumentLength(36)
            //            .addType(BsonType.STRING)
            //            .addReadUntil(4)
            // SKIP WILL BE CALLED HERE
            .addType(BsonType.END_OF_DOCUMENT)
            .addType(BsonType.END_OF_DOCUMENT)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReader(buffer, contextStack);

    reader.readStartDocument();

    assertEquals(1, contextStack.getCurrentContextIndex());
    assertEquals(76, contextStack.getRemaining());

    assertEquals(BsonType.ARRAY, reader.readBsonType());

    reader.readStartArray();

    assertEquals(76 - 50 - 4 - 1 - 1, contextStack.getStack().get(1).getRemaining());
    assertEquals(2, contextStack.getCurrentContextIndex());
    assertEquals(46, contextStack.getRemaining());

    assertEquals(BsonType.DOCUMENT, reader.readBsonType());
    reader.readStartDocument();

    assertEquals(76 - 50 - 4 - 1 - 1, contextStack.getStack().get(1).getRemaining());
    assertEquals(46 - 8 - 36 - 1, contextStack.getStack().get(2).getRemaining());
    assertEquals(3, contextStack.getCurrentContextIndex());
    assertEquals(32, contextStack.getRemaining());

    reader.skipContext();
    reader.readEndDocument();

    assertEquals(2, contextStack.getCurrentContextIndex());
    assertEquals(46 - 8 - 36 - 1, contextStack.getRemaining());

    reader.readEndDocument();

    assertEquals(1, contextStack.getCurrentContextIndex());
    assertEquals(76 - 50 - 4 - 1 - 1, contextStack.getRemaining());
  }

  @Test
  public void testContextSizeAfterSkipValueDocumentInsideDocumentInsideArray() {
    /*
    Trying to recreate a bug on an array of documents where each document has an internal document
    that is to be skipped completely after the key read.  This would then mean all keys are read
    so the context should be skipped
     */
    ContextStack contextStack = new ContextStack();
    BobBsonBuffer buffer =
        BufferDataMockBuilder.builder()
            // TOP LEVEL DOC
            .addDocumentLength(180)
            // INTERNAL ARRAY
            .addType(BsonType.ARRAY)
            .addReadUntil(5)
            .addDocumentLength(100)
            // DOCUMENT INSIDE ARRAY
            .addType(BsonType.DOCUMENT)
            .addReadUntil(8)
            .addDocumentLength(35)
            .addType(BsonType.DOCUMENT)
            .addReadUntil(4)
            .addDocumentLength(16)
            // SKIP WILL BE CALLED HERE
            .addType(BsonType.END_OF_DOCUMENT)
            .addType(BsonType.END_OF_DOCUMENT)
            .addType(BsonType.END_OF_DOCUMENT)
            .build();
    BsonReader reader = new BsonReader(buffer, contextStack);

    reader.readStartDocument();

    assertEquals(1, contextStack.getCurrentContextIndex());
    assertEquals(176, contextStack.getRemaining());

    assertEquals(BsonType.ARRAY, reader.readBsonType());

    reader.readStartArray();

    assertEquals(176 - 100 - 5 - 1, contextStack.getStack().get(1).getRemaining());
    assertEquals(2, contextStack.getCurrentContextIndex());
    assertEquals(96, contextStack.getRemaining());

    assertEquals(BsonType.DOCUMENT, reader.readBsonType());
    reader.readStartDocument();

    assertEquals(176 - 100 - 5 - 1, contextStack.getStack().get(1).getRemaining());
    assertEquals(96 - 8 - 35 - 1, contextStack.getStack().get(2).getRemaining());
    assertEquals(3, contextStack.getCurrentContextIndex());
    assertEquals(31, contextStack.getRemaining());

    assertEquals(BsonType.DOCUMENT, reader.readBsonType());

    reader.skipValue();

    assertEquals(10, contextStack.getRemaining());

    reader.skipContext();
    reader.readEndDocument();

    assertEquals(2, contextStack.getCurrentContextIndex());
    assertEquals(96 - 8 - 35 - 1, contextStack.getRemaining());

    reader.skipContext();
    reader.readEndDocument();

    assertEquals(1, contextStack.getCurrentContextIndex());
    assertEquals(176 - 100 - 5 - 1, contextStack.getRemaining());
  }
}
