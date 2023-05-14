package org.bobstuff.bobbson;

import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.CompiledBson;

@CompiledBson
public class BeanWithByteArray {
  @BsonConverter(target = CustomObjectIdConverter.class)
  private byte[] key;

  public byte[] getKey() {
    return key;
  }

  public void setKey(byte[] key) {
    this.key = key;
  }
}
