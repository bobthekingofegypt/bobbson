package org.bobstuff.bobbson;

import java.util.ArrayList;
import java.util.List;
import org.bobstuff.bobbson.converters.BobBsonConvertersModule;
import org.bobstuff.bobbson.reflection.BobBsonReflectionModule;

public class BobBsonConfig {
  private boolean scanning;
  private List<BobBsonConvertersModule> modules;

  public BobBsonConfig(boolean scanning, List<BobBsonConvertersModule> modules) {
    this.scanning = scanning;
    this.modules = modules;
  }

  public boolean isScanning() {
    return scanning;
  }

  public List<BobBsonConvertersModule> getModules() {
    return modules;
  }

  public static class Builder {
    private boolean scanning = true;
    private List<BobBsonConvertersModule> modules = new ArrayList<>();

    public static Builder builder() {
      return new Builder();
    }

    public BobBsonConfig build() {
      return new BobBsonConfig(scanning, modules);
    }

    public Builder withScanning(boolean scanning) {
      this.scanning = scanning;
      return this;
    }

    public Builder withReflection() {
      modules.add(new BobBsonReflectionModule());
      return this;
    }

    public Builder withModule(BobBsonConvertersModule module) {
      modules.add(module);
      return this;
    }
  }
}
