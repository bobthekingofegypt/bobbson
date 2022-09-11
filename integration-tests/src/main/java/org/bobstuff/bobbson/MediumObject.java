package org.bobstuff.bobbson;

import java.util.Objects;
import org.bobstuff.bobbson.annotations.CompiledBson;

@CompiledBson
public class MediumObject {
  private SmallObject smallObject;
  private String lotsOfText;
  private String moreText;
  private int number1;
  private int number2;
  private int number3;
  private int number4;
  private boolean option1;
  private boolean option2;
  private boolean option3;
  private boolean option4;
  private String string1;
  private String string2;
  private String string3;
  private String string4;

  public SmallObject getSmallObject() {
    return smallObject;
  }

  public void setSmallObject(SmallObject smallObject) {
    this.smallObject = smallObject;
  }

  public String getLotsOfText() {
    return lotsOfText;
  }

  public void setLotsOfText(String lotsOfText) {
    this.lotsOfText = lotsOfText;
  }

  public String getMoreText() {
    return moreText;
  }

  public void setMoreText(String moreText) {
    this.moreText = moreText;
  }

  public int getNumber1() {
    return number1;
  }

  public void setNumber1(int number1) {
    this.number1 = number1;
  }

  public int getNumber2() {
    return number2;
  }

  public void setNumber2(int number2) {
    this.number2 = number2;
  }

  public int getNumber3() {
    return number3;
  }

  public void setNumber3(int number3) {
    this.number3 = number3;
  }

  public int getNumber4() {
    return number4;
  }

  public void setNumber4(int number4) {
    this.number4 = number4;
  }

  public boolean isOption1() {
    return option1;
  }

  public void setOption1(boolean option1) {
    this.option1 = option1;
  }

  public boolean isOption2() {
    return option2;
  }

  public void setOption2(boolean option2) {
    this.option2 = option2;
  }

  public boolean isOption3() {
    return option3;
  }

  public void setOption3(boolean option3) {
    this.option3 = option3;
  }

  public boolean isOption4() {
    return option4;
  }

  public void setOption4(boolean option4) {
    this.option4 = option4;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MediumObject that = (MediumObject) o;
    return number1 == that.number1
        && number2 == that.number2
        && number3 == that.number3
        && number4 == that.number4
        && option1 == that.option1
        && option2 == that.option2
        && option3 == that.option3
        && option4 == that.option4
        && Objects.equals(smallObject, that.smallObject)
        && Objects.equals(lotsOfText, that.lotsOfText)
        && Objects.equals(moreText, that.moreText)
        && Objects.equals(string1, that.string1)
        && Objects.equals(string2, that.string2)
        && Objects.equals(string3, that.string3)
        && Objects.equals(string4, that.string4);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        smallObject,
        lotsOfText,
        moreText,
        number1,
        number2,
        number3,
        number4,
        option1,
        option2,
        option3,
        option4,
        string1,
        string2,
        string3,
        string4);
  }
}
