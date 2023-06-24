package org.bobstuff.bobbson;

import java.util.Map;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class BeanWithMap {
  public Map<String, String> getTheMap() {
    return theMap;
  }

  public void setTheMap(Map<String, String> theMap) {
    this.theMap = theMap;
  }

  private Map<String, String> theMap;
}
