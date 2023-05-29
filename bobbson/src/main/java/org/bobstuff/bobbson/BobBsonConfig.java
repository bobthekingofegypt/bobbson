package org.bobstuff.bobbson;

public class BobBsonConfig {
  private boolean allowExternalLookup;

  public BobBsonConfig(boolean allowExternalLookup) {
    this.allowExternalLookup = allowExternalLookup;
  }

  public boolean isAllowExternalLookup() {
    return allowExternalLookup;
  }
  public void setAllowExternalLookup(boolean allowExternalLookup) {
    this.allowExternalLookup = allowExternalLookup;
  }
}
