package org.bobstuff.bobbson;

public interface BobBsonConverterRegister {
  /**
   * Configure library instance with appropriate readers/writers/etc...
   *
   * @param bson library instance
   */
  void register(BobBson bson);
}
