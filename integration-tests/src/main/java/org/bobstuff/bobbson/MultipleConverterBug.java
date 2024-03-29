package org.bobstuff.bobbson;

import java.util.List;
import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;
import org.checkerframework.checker.nullness.qual.Nullable;

@GenerateBobBsonConverter
public class MultipleConverterBug<TModel> {
  @BsonAttribute("q")
  private @Nullable String filter;

  @BsonAttribute("u")
  private @Nullable List<String> updates;

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public List<String> getUpdates() {
    return updates;
  }

  public void setUpdates(List<String> updates) {
    this.updates = updates;
  }
}
