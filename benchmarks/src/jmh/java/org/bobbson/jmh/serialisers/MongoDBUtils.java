package org.bobbson.jmh.serialisers;

import com.mongodb.MongoClientSettings;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public class MongoDBUtils {
  public static CodecRegistry initialiseCodecRegistery() {
    CodecRegistry pojoCodecRegistry =
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
    return CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
  }

  public static CodecProvider initialiseCodecProvider() {
    return PojoCodecProvider.builder().automatic(true).build();
  }
}
