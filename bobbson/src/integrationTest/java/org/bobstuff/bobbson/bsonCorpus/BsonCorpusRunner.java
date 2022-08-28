package org.bobstuff.bobbson.bsonCorpus;

import com.google.common.io.BaseEncoding;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bobstuff.bobbson.BsonReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class BsonCorpusRunner {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonCorpusProvider.class)
  public void runTestFile(
      BsonCorpus bsonCorpus, int index, BsonCorpus.BsonCorpusTestCaseType type) {

    System.out.println(bsonCorpus.getDescription());
    System.out.println(index);
    if (type == BsonCorpus.BsonCorpusTestCaseType.VALID) {
      System.out.println("VALID TEST");
    }
  }

  private void executeValidTest(
      BsonCorpus bsonCorpus, int index, BsonCorpus.BsonCorpusTestCaseType type) {
    var validCase = bsonCorpus.getValid().get(index);

    if (validCase.getCanonicalBson() == null) {
      throw new RuntimeException("canonical bson is undefined");
    }
    var canonicalBson = validCase.getCanonicalBson();

    BsonReader reader =
        new BsonReader(
            ByteBuffer.wrap(BaseEncoding.base16().decode(canonicalBson.toUpperCase()))
                .order(ByteOrder.LITTLE_ENDIAN));
  }
}
