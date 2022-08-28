package org.bobstuff.bobbson;

import java.util.Objects;
import org.bobstuff.bobbson.annotations.CompiledBson;

@CompiledBson
public class Qualification {
  private String type;
  private String grade;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getGrade() {
    return grade;
  }

  public void setGrade(String grade) {
    this.grade = grade;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Qualification that = (Qualification) o;
    return Objects.equals(type, that.type) && Objects.equals(grade, that.grade);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, grade);
  }

  @Override
  public String toString() {
    return "Qualification{" + "type='" + type + '\'' + ", grade='" + grade + '\'' + '}';
  }
}
