package org.bobstuff.bobbson;

import org.bobstuff.bobbson.annotations.CompiledBson;

@CompiledBson
public class SimpleModel {
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
