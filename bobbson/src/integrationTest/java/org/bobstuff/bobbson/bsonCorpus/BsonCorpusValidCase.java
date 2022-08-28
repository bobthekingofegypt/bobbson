package org.bobstuff.bobbson.bsonCorpus;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BsonCorpusValidCase {
  private String description;

  @JsonProperty("canonical_bson")
  private String canonicalBson;

  @JsonProperty("degenerate_bson")
  private String degenerateBson;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCanonicalBson() {
    return canonicalBson;
  }

  public void setCanonicalBson(String canonicalBson) {
    this.canonicalBson = canonicalBson;
  }

  public String getDegenerateBson() {
    return degenerateBson;
  }

  public void setDegenerateBson(String degenerateBson) {
    this.degenerateBson = degenerateBson;
  }
}
