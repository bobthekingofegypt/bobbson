package org.bobstuff.bobbson;

import java.util.Map;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class BasicObject {
  private String name;
  private int age;
  private double score;
  private Map<String, String> aliases;
  private Map<String, Integer> grades;
  private Qualification qualification;

  @Override
  public String toString() {
    return "BasicObject{"
        + "name='"
        + name
        + '\''
        + ", age="
        + age
        + ", score="
        + score
        + ", aliases="
        + aliases
        + ", grades="
        + grades
        + ", qualification="
        + qualification
        + '}';
  }

  public Qualification getQualification() {
    return qualification;
  }

  public void setQualification(Qualification qualification) {
    this.qualification = qualification;
  }

  public Map<String, String> getAliases() {
    return aliases;
  }

  public void setAliases(Map<String, String> aliases) {
    this.aliases = aliases;
  }

  public Map<String, Integer> getGrades() {
    return grades;
  }

  public void setGrades(Map<String, Integer> grades) {
    this.grades = grades;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }
}
