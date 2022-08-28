package org.bobstuff.bobbson.bsonCorpus;

public class BsonCorpusDecodeErrorCase {
  private String description;
  private String bson;
  private boolean ignore;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getBson() {
    return bson;
  }

  public void setBson(String bson) {
    this.bson = bson;
  }

  public boolean isIgnore() {
    return ignore;
  }

  public void setIgnore(boolean ignore) {
    this.ignore = ignore;
  }
}
