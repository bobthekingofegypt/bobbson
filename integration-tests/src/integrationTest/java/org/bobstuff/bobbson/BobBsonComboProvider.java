package org.bobstuff.bobbson;

import java.util.stream.Stream;
import org.bobstuff.bobbson.activej.ActiveJBufferData;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.BobBufferPool;
import org.bobstuff.bobbson.reflection.CollectionConverterFactory;
import org.bobstuff.bobbson.reflection.EnumConverterFactory;
import org.bobstuff.bobbson.reflection.ObjectConverterFactory;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class BobBsonComboProvider implements ArgumentsProvider {
  public interface BobBsonProvider {
    BobBson provide();
  }

  public interface BobBsonBufferProvider {
    BobBsonBuffer provide(int size);
  }

  public class BobBsonCompiledProvider implements BobBsonProvider {
    public BobBson provide() {
      return new BobBson();
    }
  }

  public class BobBsonRelflectionProvider implements BobBsonProvider {
    public BobBson provide() {
      var bobBson = new BobBson(new BobBsonConfig(false));
      bobBson.registerFactory(new EnumConverterFactory());
      bobBson.registerFactory(new ObjectConverterFactory());
      bobBson.registerFactory(new CollectionConverterFactory());

      return bobBson;
    }
  }

  public class ActiveJBufferProvider implements BobBsonBufferProvider {
    @Override
    public BobBsonBuffer provide(int size) {
      return new ActiveJBufferData(new byte[size], 0, 0);
    }
  }

  public class BobBufferBobBsonBufferProvider implements BobBsonBufferProvider {
    @Override
    public BobBsonBuffer provide(int size) {
      return new BobBufferBobBsonBuffer(new byte[size], 0, 0);
    }
  }

  public class ByteBufferBobBsonBufferProvider implements BobBsonBufferProvider {
    @Override
    public BobBsonBuffer provide(int size) {
      return new ByteBufferBobBsonBuffer(new byte[size], 0, 0);
    }
  }

  public class DynamicBobBsonBufferProvider implements BobBsonBufferProvider {
    @Override
    public BobBsonBuffer provide(int size) {
      return new DynamicBobBsonBuffer(new BobBufferPool());
    }
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    var bobBsonCompiledProvider = new BobBsonCompiledProvider();
    var bobBsonReflectionProvider = new BobBsonRelflectionProvider();
    var activeJBufferProvider = new ActiveJBufferProvider();
    var bobBufferBobBsonBufferProvider = new BobBufferBobBsonBufferProvider();
    var byteBufferBobBsonBufferProvider = new ByteBufferBobBsonBufferProvider();
    var dynamicBobBsonBufferProvider = new DynamicBobBsonBufferProvider();

    ConfigurationProvider compiledActiveJ =
        new ConfigurationProvider(bobBsonCompiledProvider, activeJBufferProvider);
    ConfigurationProvider compiledBobBson =
        new ConfigurationProvider(bobBsonCompiledProvider, bobBufferBobBsonBufferProvider);
    ConfigurationProvider compiledByte =
        new ConfigurationProvider(bobBsonCompiledProvider, byteBufferBobBsonBufferProvider);
    ConfigurationProvider compiledDynamic =
        new ConfigurationProvider(bobBsonCompiledProvider, dynamicBobBsonBufferProvider);
    ConfigurationProvider reflectionActiveJ =
        new ConfigurationProvider(bobBsonReflectionProvider, activeJBufferProvider);
    ConfigurationProvider reflectionBobBson =
        new ConfigurationProvider(bobBsonReflectionProvider, bobBufferBobBsonBufferProvider);
    ConfigurationProvider reflectionByte =
        new ConfigurationProvider(bobBsonReflectionProvider, byteBufferBobBsonBufferProvider);
    ConfigurationProvider reflectionDynamic =
        new ConfigurationProvider(bobBsonReflectionProvider, dynamicBobBsonBufferProvider);

    return Stream.of(
        Arguments.of(Named.named("BobBsonCompiled_activeJ", compiledActiveJ)),
        Arguments.of(Named.named("BobBsonCompiled_bobBson", compiledBobBson)),
        Arguments.of(Named.named("BobBsonCompiled_byte", compiledByte)),
        Arguments.of(Named.named("BobBsonCompiled_dynamic", compiledDynamic)),
        Arguments.of(Named.named("BobBsonReflection_activeJ", reflectionActiveJ)),
        Arguments.of(Named.named("BobBsonReflection_bobBson", reflectionBobBson)),
        Arguments.of(Named.named("BobBsonReflection_byte", reflectionByte)),
        Arguments.of(Named.named("BobBsonReflection_dynamic", reflectionDynamic)));
  }

  public class ConfigurationProvider {
    private BobBsonProvider bobBsonProvider;
    private BobBsonBufferProvider bufferProvider;

    public ConfigurationProvider(
        BobBsonProvider bobBsonProvider, BobBsonBufferProvider bufferProvider) {
      this.bobBsonProvider = bobBsonProvider;
      this.bufferProvider = bufferProvider;
    }

    public BobBson getBobBson() {
      return bobBsonProvider.provide();
    }

    public BobBsonBuffer getBuffer(int size) {
      return bufferProvider.provide(size);
    }
  }
}
