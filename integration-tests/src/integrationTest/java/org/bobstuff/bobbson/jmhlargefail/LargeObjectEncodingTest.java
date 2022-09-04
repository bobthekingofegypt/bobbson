package org.bobstuff.bobbson.jmhlargefail;

import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.reflection.CollectionConverterFactory;
import org.bobstuff.bobbson.reflection.ObjectConverterFactory;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;

public class LargeObjectEncodingTest {
    @Test
    public void testEncodingSucceeds() throws Exception {
        BufferDataPool pool =
                new NoopBufferDataPool((size) -> new BobBufferBobBsonBuffer(new byte[size], 0, 0));
        DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
        BobBson bobBson = new BobBson();

        org.bobstuff.bobbson.writer.BsonWriter bsonWriter =
                new org.bobstuff.bobbson.writer.BsonWriter(buffer);
        bobBson.serialise(Generator.newLargeObject(), LargeObject.class, bsonWriter);
        buffer.release();
    }

    @Test
    public void testRelfectionEncodingSucceeds() throws Exception {
        BufferDataPool pool =
                new NoopBufferDataPool((size) -> new BobBufferBobBsonBuffer(new byte[size], 0, 0));
        DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
        BobBson bobBson = new BobBson(new BobBsonConfig(false));
        bobBson.registerFactory(new CollectionConverterFactory());
        bobBson.registerFactory(new ObjectConverterFactory());

        org.bobstuff.bobbson.writer.BsonWriter bsonWriter =
                new org.bobstuff.bobbson.writer.BsonWriter(buffer);
        bobBson.serialise(Generator.newLargeObject(), LargeObject.class, bsonWriter);
        buffer.release();

    }
}
