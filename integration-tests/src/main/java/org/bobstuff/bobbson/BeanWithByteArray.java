package org.bobstuff.bobbson;

import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class BeanWithByteArray {
  @BsonConverter(value = CustomObjectIdConverter.class)
  private byte[] key;

  public byte[] getKey() {
    return key;
  }

  public void setKey(byte[] key) {
    this.key = key;
  }
}
