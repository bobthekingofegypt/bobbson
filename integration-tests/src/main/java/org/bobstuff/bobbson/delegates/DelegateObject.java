package org.bobstuff.bobbson.delegates;

import java.util.Objects;
import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class DelegateObject {
  private String name;
  private DelegateAge delegateAge = new DelegateAge();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAge(int age) {
    delegateAge.setAge(4);
  }

  @BsonAttribute("old")
  public boolean isOld() {
    return delegateAge.isOld();
  }

  public void setOld(boolean old) {
    delegateAge.setOld(old);
  }

  @BsonAttribute("age")
  public int getAge() {
    return delegateAge.getAge();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DelegateObject that = (DelegateObject) o;
    return name.equals(that.name) && delegateAge.equals(that.delegateAge);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, delegateAge);
  }
}
