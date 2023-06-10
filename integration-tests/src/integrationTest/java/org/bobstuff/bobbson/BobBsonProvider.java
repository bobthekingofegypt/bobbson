package org.bobstuff.bobbson;

import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class BobBsonProvider implements ArgumentsProvider {
  public interface BobBsonImplProvider {
    BobBson provide();
  }

  public class BobBsonCompiledProvider implements BobBsonImplProvider {
    public BobBson provide() {
      return new BobBson();
    }
  }

  public class BobBsonRelflectionProvider implements BobBsonImplProvider {
    public BobBson provide() {
      var bobBson =
          new BobBson(BobBsonConfig.Builder.builder().withScanning(false).withReflection().build());

      return bobBson;
    }
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    var bobBsonCompiledProvider = new BobBsonCompiledProvider();
    var bobBsonReflectionProvider = new BobBsonRelflectionProvider();

    return Stream.of(
        Arguments.of(Named.named("BobBsonCompiled", bobBsonCompiledProvider)),
        Arguments.of(Named.named("BobBsonReflectionReflection", bobBsonReflectionProvider)));
  }
}
