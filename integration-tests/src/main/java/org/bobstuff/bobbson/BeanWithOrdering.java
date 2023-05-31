package org.bobstuff.bobbson;

import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public class BeanWithOrdering {
    @BsonAttribute(order = 1)
    private String fieldOne;
    @BsonAttribute(order = 2)
    private String fieldTwo;
    @BsonAttribute(order = 3)
    private String fieldThree;
    @BsonAttribute(order = 5)
    private String fieldFive;
    @BsonAttribute(order = 6)
    private String fieldSix;
    @BsonAttribute(order = 4)
    private String fieldFour;

    public String getFieldOne() {
        return fieldOne;
    }

    public void setFieldOne(String fieldOne) {
        this.fieldOne = fieldOne;
    }

    public String getFieldTwo() {
        return fieldTwo;
    }

    public void setFieldTwo(String fieldTwo) {
        this.fieldTwo = fieldTwo;
    }

    public String getFieldThree() {
        return fieldThree;
    }

    public void setFieldThree(String fieldThree) {
        this.fieldThree = fieldThree;
    }

    public String getFieldFive() {
        return fieldFive;
    }

    public void setFieldFive(String fieldFive) {
        this.fieldFive = fieldFive;
    }

    public String getFieldSix() {
        return fieldSix;
    }

    public void setFieldSix(String fieldSix) {
        this.fieldSix = fieldSix;
    }

    public String getFieldFour() {
        return fieldFour;
    }

    public void setFieldFour(String fieldFour) {
        this.fieldFour = fieldFour;
    }
}
