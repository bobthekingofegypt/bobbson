package org.bobbson.jmh.serialisers;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.internal.connection.ByteBufferBsonOutput;
import com.mongodb.internal.connection.PowerOfTwoBufferPool;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConfig;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.activej.ActiveJBufferData;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.ConcurrentBobBsonBufferPool;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.codecs.EncoderContext;
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
@Measurement(iterations = 200, time = 6000, timeUnit = MILLISECONDS)
@OutputTimeUnit(NANOSECONDS)
public class JVMSerialise {
  private CodecRegistry codecRegistry;
  private CodecProvider codecProvider;

  private PowerOfTwoBufferPool mongodbPool;
  private BobBson bobBsonCompiled;
  private BobBson bobBsonReflection;
  private ObjectMapper jacksonMapper;
  private BobBsonBufferPool bobBsonBufferPool;
  private MediaContent data;
  private BobBsonConverter<MediaContent> bbConverter;
  private StackBsonWriter oldBsonWriter;
  private ActiveJBufferData activeJBuffer;

  @Setup
  public void setup() throws Throwable {
    codecProvider = MongoDBUtils.initialiseCodecProvider();
    codecRegistry = MongoDBUtils.initialiseCodecRegistery();

    bobBsonCompiled = new BobBson(BobBsonConfig.Builder.builder().withScanning(true).build());
    bobBsonReflection =
        new BobBson(BobBsonConfig.Builder.builder().withReflection().withScanning(false).build());

    jacksonMapper = new ObjectMapper(new BsonFactory());

    bobBsonBufferPool = new ConcurrentBobBsonBufferPool();

    mongodbPool = new PowerOfTwoBufferPool(32);

    data = TestData.standard();

    activeJBuffer = new ActiveJBufferData(new byte[4096], 0, 0);
    oldBsonWriter = new StackBsonWriter(activeJBuffer);
    bbConverter = bobBsonCompiled.tryFindConverter(MediaContent.class);
  }

  @Benchmark
  public void bbCompiledActiveJRe(Blackhole bh) throws Exception {
    activeJBuffer.setTail(0);
    oldBsonWriter.reset();
    bbConverter.write(oldBsonWriter, data);
    bh.consume(activeJBuffer.toByteArray().length);
  }

  @Benchmark
  public void bbCompiled(Blackhole bh) throws Exception {
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(bobBsonBufferPool);

    org.bobstuff.bobbson.writer.BsonWriter bsonWriter =
        new org.bobstuff.bobbson.writer.StackBsonWriter(buffer);
    bobBsonCompiled.serialise(data, MediaContent.class, bsonWriter);
    bh.consume(buffer.toByteArray());
    buffer.release();
  }

  @Benchmark
  public void bbReflection(Blackhole bh) throws Exception {
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(bobBsonBufferPool);

    org.bobstuff.bobbson.writer.BsonWriter bsonWriter =
        new org.bobstuff.bobbson.writer.StackBsonWriter(buffer);
    bobBsonReflection.serialise(data, MediaContent.class, bsonWriter);
    bh.consume(buffer.toByteArray());
    buffer.release();
  }

  @Benchmark
  public void mongoPool(Blackhole bh) {
    ByteBufferBsonOutput output = new ByteBufferBsonOutput(mongodbPool);
    BsonWriter bsonWriter = new BsonBinaryWriter(output);

    var codec = codecProvider.get(MediaContent.class, codecRegistry);
    EncoderContext context = EncoderContext.builder().build();

    codec.encode(bsonWriter, data, context);

    bh.consume(output.toByteArray());
    output.close();
  }

  @Benchmark
  public void bson4jackson(Blackhole bh) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    jacksonMapper.writeValue(bos, data);
    bh.consume(bos.toByteArray());
  }
}
