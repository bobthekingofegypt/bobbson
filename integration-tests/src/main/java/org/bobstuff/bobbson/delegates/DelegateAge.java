package org.bobstuff.bobbson.delegates;

import java.util.Objects;

public class DelegateAge {
  private int age;
  private boolean old;

  public boolean isOld() {
    return old;
  }

  public void setOld(boolean old) {
    this.old = old;
  }

  public int getAge() {
    return age;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DelegateAge that = (DelegateAge) o;
    return age == that.age && old == that.old;
  }

  @Override
  public int hashCode() {
    return Objects.hash(age, old);
  }

  public void setAge(int age) {
    this.age = age;
  }
}
