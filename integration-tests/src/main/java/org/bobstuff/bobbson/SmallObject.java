package org.bobstuff.bobbson;

import org.bobstuff.bobbson.annotations.CompiledBson;

@CompiledBson
public class SmallObject {
    private String name;
    private String description;
    private int number1;
    private double number2;
    private boolean option;
    private long number3;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumber1() {
        return number1;
    }

    public void setNumber1(int number1) {
        this.number1 = number1;
    }

    public double getNumber2() {
        return number2;
    }

    public void setNumber2(double number2) {
        this.number2 = number2;
    }

    public boolean isOption() {
        return option;
    }

    public void setOption(boolean option) {
        this.option = option;
    }

    public long getNumber3() {
        return number3;
    }

    public void setNumber3(long number3) {
        this.number3 = number3;
    }
}
