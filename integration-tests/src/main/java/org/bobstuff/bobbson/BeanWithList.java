package org.bobstuff.bobbson;

import java.util.List;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class BeanWithList {
  private List<String> names;

  public List<String> getNames() {
    return names;
  }

  public void setNames(List<String> names) {
    this.names = names;
  }
}
