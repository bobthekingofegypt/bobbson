package org.bobstuff.bobbson.bsonCorpus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class BsonCorpusProvider implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    ObjectMapper objectMapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    File resourceDirectory = new File(BsonCorpusProvider.class.getResource("/bson-corpus").toURI());
    var arguments = new ArrayList<Arguments>();
    for (var file : resourceDirectory.listFiles()) {
      BsonCorpus corpus = objectMapper.readValue(file, BsonCorpus.class);
      if (corpus.getValid() != null) {
        for (var i = 0; i < corpus.getValid().size(); ++i) {
          arguments.add(
              Arguments.of(
                  Named.named(file.getName() + ":" + corpus.getDescription(), corpus),
                  i,
                  BsonCorpus.BsonCorpusTestCaseType.VALID));
        }
      }
    }
    return arguments.stream();
  }

  public interface BsonCorpusBuilder {
    BsonCorpus build();
  }
}
