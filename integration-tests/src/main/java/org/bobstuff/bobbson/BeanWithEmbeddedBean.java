package org.bobstuff.bobbson;

import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class BeanWithEmbeddedBean {
  @GenerateBobBsonConverter
  public static class Embedded {
    private String banana;
    private String bob;

    public String getBanana() {
      return banana;
    }

    public void setBanana(String banana) {
      this.banana = banana;
    }

    public String getBob() {
      return bob;
    }

    public void setBob(String bob) {
      this.bob = bob;
    }
  }

  @BsonAttribute(order = 1)
  private Embedded embedded;

  private String name;

  public Embedded getEmbedded() {
    return embedded;
  }

  public void setEmbedded(Embedded embedded) {
    this.embedded = embedded;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
