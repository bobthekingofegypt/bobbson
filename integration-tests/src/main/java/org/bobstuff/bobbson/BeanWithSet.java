package org.bobstuff.bobbson;

import java.util.Set;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class BeanWithSet {
  private Set<String> theSet;

  public Set<String> getTheSet() {
    return theSet;
  }

  public void setTheSet(Set<String> theSet) {
    this.theSet = theSet;
  }
}
