package org.bobstuff.bobbson;

import java.util.concurrent.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassLoadFromThread {
  @Test
  public void testClassLoad() throws Exception {
    BobBson bobBson = new BobBson();
    bobBson.tryFindConverter(SimpleModel.class);
  }

  @Test
  public void testClassLoadThread() throws Exception {
    BobBson bobBson = new BobBson();
    FutureTask<Void> future =
        new FutureTask<>(
            () -> {
              Assertions.assertNotNull(bobBson.tryFindConverter(SimpleModel.class));
              return null;
            });
    future.run();
    future.get();
  }

  @Test
  public void testClassLoadExecutor() throws Exception {
    BobBson bobBson = new BobBson();
    ExecutorService service = Executors.newFixedThreadPool(1);
    CompletionService<Void> completionService = new ExecutorCompletionService<>(service);
    completionService.submit(
        () -> {
          Assertions.assertNotNull(bobBson.tryFindConverter(SimpleModel.class));
          return null;
        });

    Future<Void> f = completionService.take();
    f.get();
    service.shutdown();
  }
}
