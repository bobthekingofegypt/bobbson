package org.bobstuff.bobbson;

import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class BeanWithLowerCaseSingleLetter {
  private String aString;

  public String getaString() {
    return aString;
  }

  public void setaString(String aString) {
    this.aString = aString;
  }
}
