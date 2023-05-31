package org.bobstuff.bobbson.reflection;

import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConfig;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.StackBsonWriter;

public class ReflectionExample {
  public static void main(String... args) throws Exception {
    var bobBson =
        new BobBson(BobBsonConfig.Builder.builder().withScanning(false).withReflection().build());

    var model = new SimpleModel();
    model.setHeight(2.34);
    model.setAge(24);
    model.setName("Fred");

    var buffer = new BobBufferBobBsonBuffer(new byte[2048], 0, 0);
    var writer = new StackBsonWriter(buffer);

    bobBson.serialise(model, SimpleModel.class, writer);

    var reader = new StackBsonReader(buffer);

    var result = bobBson.deserialise(SimpleModel.class, reader);

    System.out.println(model);
    System.out.println(result);
  }

  public static class SimpleModel {
    private String name;
    private int age;
    private double height;

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

    public double getHeight() {
      return height;
    }

    public void setHeight(double height) {
      this.height = height;
    }

    @Override
    public String toString() {
      return "SimpleModel{" + "name='" + name + '\'' + ", age=" + age + ", height=" + height + '}';
    }
  }
}
