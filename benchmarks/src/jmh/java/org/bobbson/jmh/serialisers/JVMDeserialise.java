package org.bobbson.jmh.serialisers;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConfig;
import org.bobstuff.bobbson.activej.ActiveJBufferData;
import org.bobstuff.bobbson.buffer.BobBsonBuffer;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.InputStreamBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.ConcurrentBobBsonBufferPool;
import org.bson.BsonBinaryReader;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Test suite that mimics exact data from jvm-serializers test runner. Uses the same model object
 * and runs it against bobbson/mongo/bson4jackson/dsl etc
 */
@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime})
@Measurement(iterations = 200000, time = 20000, timeUnit = MILLISECONDS)
@OutputTimeUnit(NANOSECONDS)
public class JVMDeserialise {
  private CodecRegistry codecRegistry;
  private CodecProvider codecProvider;
  private BobBson bobBsonCompiled;
  private BobBson bobBsonReflection;
  private ObjectMapper jacksonMapper;
  private BobBsonBufferPool bobBsonBufferPool;
  private MediaContent data;

  private byte[] bsonData;
  private ActiveJBufferData activeJBuffer;
  private BobBsonBuffer isb;
  private ByteArrayInputStream is;

  @Setup
  public void setup() throws Throwable {
    codecProvider = MongoDBUtils.initialiseCodecProvider();
    codecRegistry = MongoDBUtils.initialiseCodecRegistery();

    bobBsonCompiled = new BobBson(BobBsonConfig.Builder.builder().withScanning(true).build());
    bobBsonReflection =
        new BobBson(BobBsonConfig.Builder.builder().withReflection().withScanning(false).build());

    jacksonMapper = new ObjectMapper(new BsonFactory());

    bobBsonBufferPool = new ConcurrentBobBsonBufferPool();

    data = TestData.standard();

    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(bobBsonBufferPool);

    org.bobstuff.bobbson.writer.BsonWriter bsonWriter =
        new org.bobstuff.bobbson.writer.StackBsonWriter(buffer);
    bobBsonCompiled.serialise(data, MediaContent.class, bsonWriter);
    bsonData = buffer.toByteArray();
    buffer.release();

    activeJBuffer = new ActiveJBufferData(bsonData, 0, bsonData.length);
    is = new ByteArrayInputStream(bsonData);
    isb = new InputStreamBobBsonBuffer(is);
  }

  @Benchmark
  public void bbCompiledInputStream(Blackhole bh) throws Exception {
    is.reset();
    org.bobstuff.bobbson.reader.BsonReader bsonReader =
        new org.bobstuff.bobbson.reader.StackBsonReader(isb);
    var result = bobBsonCompiled.deserialise(MediaContent.class, bsonReader);
    bh.consume(result);
  }

  @Benchmark
  public void bbCompiledActiveJRe(Blackhole bh) throws Exception {
    activeJBuffer.process(bsonData, 0, bsonData.length);
    org.bobstuff.bobbson.reader.BsonReader bsonReader =
        new org.bobstuff.bobbson.reader.StackBsonReader(activeJBuffer);
    var result = bobBsonCompiled.deserialise(MediaContent.class, bsonReader);
    bh.consume(result);
  }

  @Benchmark
  public void bbCompiled(Blackhole bh) throws Exception {
    var buffer = new BobBufferBobBsonBuffer(bsonData, 0, bsonData.length);
    org.bobstuff.bobbson.reader.BsonReader bsonReader =
        new org.bobstuff.bobbson.reader.StackBsonReader(buffer);
    var result = bobBsonCompiled.deserialise(MediaContent.class, bsonReader);
    bh.consume(result);
  }

  @Benchmark
  public void bbReflection(Blackhole bh) throws Exception {
    activeJBuffer.process(bsonData, 0, bsonData.length);
    org.bobstuff.bobbson.reader.BsonReader bsonReader =
        new org.bobstuff.bobbson.reader.StackBsonReader(activeJBuffer);
    var result = bobBsonReflection.deserialise(MediaContent.class, bsonReader);
    bh.consume(result);
  }

  @Benchmark
  public void mongo(Blackhole bh) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bsonData).order(ByteOrder.LITTLE_ENDIAN);
    org.bson.BsonReader reader = new BsonBinaryReader(byteBuffer);

    var codec = codecProvider.get(MediaContent.class, codecRegistry);
    DecoderContext context = DecoderContext.builder().build();
    bh.consume(codec.decode(reader, context));
  }

  @Benchmark
  public void bson4jackson(Blackhole bh) throws Exception {
    bh.consume(jacksonMapper.readValue(bsonData, MediaContent.class));
  }
}
