package org.bobstuff.bobbson;

import org.bobstuff.bobbson.annotations.CompiledBson;

import java.util.Objects;

@CompiledBson
public class EnumValue {
  @CompiledBson
  public static enum AnEnum {
    VALUE_ONE,
    VALUE_TWO,
    VALUE_THREE
  }

  private AnEnum value;

  public AnEnum getValue() {
    return value;
  }

  public void setValue(AnEnum value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EnumValue enumValue = (EnumValue) o;
    return value == enumValue.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
