package org.bobstuff.bobbson;

import org.checkerframework.checker.nullness.qual.NonNull;

public class BsonContext {
  private int documentLength;
  private int remaining;
  private int startPosition;
  private int arrayIndex;
  private @NonNull BsonContextType currentBsonType;

  public BsonContext() {
    this.documentLength = 0;
    this.remaining = 0;
    this.startPosition = 0;
    this.arrayIndex = 0;
    this.currentBsonType = BsonContextType.TOP_LEVEL;
  }

  public int getStartPosition() {
    return startPosition;
  }

  public int getRemaining() {
    return remaining;
  }

  public void adjustRemaining(int amount) {
    remaining += amount;
  }

  public int getAndIncrementArrayIndex() {
    var old = arrayIndex;
    arrayIndex += 1;
    return old;
  }

  public @NonNull BsonContextType getCurrentBsonType() {
    return currentBsonType;
  }

  public int getDocumentLength() {
    return documentLength;
  }

  public void set(int documentLength) {
    this.remaining = documentLength;
    this.documentLength = documentLength;
    this.arrayIndex = 0;
  }

  public void set(int documentLength, BsonContextType bsonType) {
    this.remaining = documentLength;
    this.documentLength = documentLength;
    this.currentBsonType = bsonType;
    this.arrayIndex = 0;
  }

  public void set(int documentLength, int startPosition, BsonContextType bsonType) {
    this.remaining = documentLength;
    this.documentLength = documentLength;
    this.currentBsonType = bsonType;
    this.startPosition = startPosition;
    this.arrayIndex = 0;
  }

  //  public void set(int documentLength, BsonContextType bsonType, @Nullable Mark mark) {
  //    this.remaining = documentLength;
  //    this.documentLength = documentLength;
  //    this.currentBsonType = bsonType;
  //    this.mark = mark;
  //  }
}
