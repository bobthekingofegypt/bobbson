package org.bobstuff.bobbson;

public class BobBsonConfig {
  private boolean allowExternalLookup;
  private boolean writeNulls;

  public BobBsonConfig(boolean allowExternalLookup) {
    this.allowExternalLookup = allowExternalLookup;
  }

  public boolean isAllowExternalLookup() {
    return allowExternalLookup;
  }

  public boolean isWriteNulls() {
    return writeNulls;
  }

  public void setWriteNulls(boolean writeNulls) {
    this.writeNulls = writeNulls;
  }

  public void setAllowExternalLookup(boolean allowExternalLookup) {
    this.allowExternalLookup = allowExternalLookup;
  }
}
