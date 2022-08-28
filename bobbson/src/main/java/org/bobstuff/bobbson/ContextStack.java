package org.bobstuff.bobbson;

import java.util.ArrayList;
import java.util.List;

public class ContextStack {
  private final List<BsonContext> stack;
  private int currentContextIndex;
  public BsonContext context;

  public ContextStack() {
    this.stack = new ArrayList<>(10);
    this.context = new BsonContext();
    this.stack.add(context);
    this.currentContextIndex = 0;
  }

  // TODO this is really only for testing
  public List<BsonContext> getStack() {
    return stack;
  }

  public int getCurrentContextIndex() {
    return currentContextIndex;
  }

  public BsonContextType getCurrentBsonType() {
    return context.getCurrentBsonType();
  }

  public int getCurrentStartPosition() {
    return context.getStartPosition();
  }

  public int getRemaining() {
    return context.getRemaining();
  }

  public void reset() {
    if (currentContextIndex != 0) {
      this.currentContextIndex = 0;
      context = stack.get(0);
    }
  }

  public void adjustRemaining(int adjustment) {
    this.context.adjustRemaining(-adjustment);
  }

  public boolean isRootContext() {
    return currentContextIndex == 0;
  }

  public void pop() {
    if (context.getRemaining() != 0) {
      throw new RuntimeException(
          "Popping context from stack with " + context.getRemaining() + " remaining bytes to read");
    }
    currentContextIndex -= 1;
    context = stack.get(currentContextIndex);
  }

  public BsonContext add(int length, BsonContextType type) {
    if (currentContextIndex + 1 == stack.size()) {
      context = new BsonContext();
      stack.add(context);
    } else {
      context = stack.get(currentContextIndex + 1);
    }
    currentContextIndex += 1;
    context.set(length, type);
    return context;
  }

  public BsonContext add(int length, int startPosition, BsonContextType type) {
    if (currentContextIndex + 1 == stack.size()) {
      context = new BsonContext();
      stack.add(context);
    } else {
      context = stack.get(currentContextIndex + 1);
    }
    currentContextIndex += 1;
    context.set(length, startPosition, type);
    return context;
  }
}
