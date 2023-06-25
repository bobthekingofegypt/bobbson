package org.bobstuff.bobbson;

import java.util.Arrays;
import java.util.List;
import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class BeanWithMethodNoField {
  @BsonAttribute(value = "notname", order = 1)
  public String getName() {
    return "Fred";
  }

  public void setName(String name) {
    // no-op
  }

  @BsonAttribute(order = 2)
  public List<String> getNames() {
    return Arrays.asList("bob", "scott");
  }

  public void setNames(List<String> names) {
    // no-op
  }
}
