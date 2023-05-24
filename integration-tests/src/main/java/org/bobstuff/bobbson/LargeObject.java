package org.bobstuff.bobbson;

import java.util.List;
import java.util.Objects;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class LargeObject {
  private List<MediumObject> mediumObjects;
  private SmallObject smallObject;
  private double double1;
  private double double2;
  private double double3;
  private double double4;
  private double double5;
  private double double6;
  private double double7;
  private double double8;
  private double double9;

  private String string1;
  private String string2;
  private String string3;
  private String string4;
  private String string5;
  private String string6;
  private String string7;
  private String string8;
  private String string9;

  public List<MediumObject> getMediumObjects() {
    return mediumObjects;
  }

  public void setMediumObjects(List<MediumObject> mediumObjects) {
    this.mediumObjects = mediumObjects;
  }

  public SmallObject getSmallObject() {
    return smallObject;
  }

  public void setSmallObject(SmallObject smallObject) {
    this.smallObject = smallObject;
  }

  public double getDouble1() {
    return double1;
  }

  public void setDouble1(double double1) {
    this.double1 = double1;
  }

  public double getDouble2() {
    return double2;
  }

  public void setDouble2(double double2) {
    this.double2 = double2;
  }

  public double getDouble3() {
    return double3;
  }

  public void setDouble3(double double3) {
    this.double3 = double3;
  }

  public double getDouble4() {
    return double4;
  }

  public void setDouble4(double double4) {
    this.double4 = double4;
  }

  public double getDouble5() {
    return double5;
  }

  public void setDouble5(double double5) {
    this.double5 = double5;
  }

  public double getDouble6() {
    return double6;
  }

  public void setDouble6(double double6) {
    this.double6 = double6;
  }

  public double getDouble7() {
    return double7;
  }

  public void setDouble7(double double7) {
    this.double7 = double7;
  }

  public double getDouble8() {
    return double8;
  }

  public void setDouble8(double double8) {
    this.double8 = double8;
  }

  public double getDouble9() {
    return double9;
  }

  public void setDouble9(double double9) {
    this.double9 = double9;
  }

  public String getString1() {
    return string1;
  }

  public void setString1(String string1) {
    this.string1 = string1;
  }

  public String getString2() {
    return string2;
  }

  public void setString2(String string2) {
    this.string2 = string2;
  }

  public String getString3() {
    return string3;
  }

  public void setString3(String string3) {
    this.string3 = string3;
  }

  public String getString4() {
    return string4;
  }

  public void setString4(String string4) {
    this.string4 = string4;
  }

  public String getString5() {
    return string5;
  }

  public void setString5(String string5) {
    this.string5 = string5;
  }

  public String getString6() {
    return string6;
  }

  public void setString6(String string6) {
    this.string6 = string6;
  }

  public String getString7() {
    return string7;
  }

  public void setString7(String string7) {
    this.string7 = string7;
  }

  public String getString8() {
    return string8;
  }

  public void setString8(String string8) {
    this.string8 = string8;
  }

  public String getString9() {
    return string9;
  }

  public void setString9(String string9) {
    this.string9 = string9;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LargeObject that = (LargeObject) o;
    return Double.compare(that.double1, double1) == 0
        && Double.compare(that.double2, double2) == 0
        && Double.compare(that.double3, double3) == 0
        && Double.compare(that.double4, double4) == 0
        && Double.compare(that.double5, double5) == 0
        && Double.compare(that.double6, double6) == 0
        && Double.compare(that.double7, double7) == 0
        && Double.compare(that.double8, double8) == 0
        && Double.compare(that.double9, double9) == 0
        && Objects.equals(mediumObjects, that.mediumObjects)
        && Objects.equals(smallObject, that.smallObject)
        && Objects.equals(string1, that.string1)
        && Objects.equals(string2, that.string2)
        && Objects.equals(string3, that.string3)
        && Objects.equals(string4, that.string4)
        && Objects.equals(string5, that.string5)
        && Objects.equals(string6, that.string6)
        && Objects.equals(string7, that.string7)
        && Objects.equals(string8, that.string8)
        && Objects.equals(string9, that.string9);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        mediumObjects,
        smallObject,
        double1,
        double2,
        double3,
        double4,
        double5,
        double6,
        double7,
        double8,
        double9,
        string1,
        string2,
        string3,
        string4,
        string5,
        string6,
        string7,
        string8,
        string9);
  }
}
