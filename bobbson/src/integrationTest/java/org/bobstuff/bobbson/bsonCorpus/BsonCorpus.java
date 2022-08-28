package org.bobstuff.bobbson.bsonCorpus;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class BsonCorpus {
  public enum BsonCorpusTestCaseType {
    VALID;
  }

  private String description;

  @JsonProperty("bson_type")
  private String bsonType;

  @JsonProperty("test_key")
  private String testKey;

  private List<BsonCorpusValidCase> valid;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getBsonType() {
    return bsonType;
  }

  public void setBsonType(String bsonType) {
    this.bsonType = bsonType;
  }

  public String getTestKey() {
    return testKey;
  }

  public void setTestKey(String testKey) {
    this.testKey = testKey;
  }

  public List<BsonCorpusValidCase> getValid() {
    return valid;
  }

  public void setValid(List<BsonCorpusValidCase> valid) {
    this.valid = valid;
  }
}
