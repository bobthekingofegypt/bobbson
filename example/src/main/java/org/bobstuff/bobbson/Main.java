package org.bobstuff.bobbson;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bson.BsonBinaryWriter;
import org.bson.io.BasicOutputBuffer;

public class Main {
  public static void main(String... args) throws Exception {
    System.out.println("hello");

    byte[] buffer;
    try (BasicOutputBuffer output = new BasicOutputBuffer();
        BsonBinaryWriter bsonWriter = new BsonBinaryWriter(output)) {

      bsonWriter.writeStartDocument();
      bsonWriter.writeString("name", "freddy");
      bsonWriter.writeString("occupation", "doctor");
      bsonWriter.writeString("country", "USA");
      bsonWriter.writeInt32("age", 10);
      bsonWriter.writeStartArray("places");
      bsonWriter.writeString("france");
      bsonWriter.writeString("spain");
      bsonWriter.writeString("germany");
      bsonWriter.writeEndArray();
      bsonWriter.writeStartArray("scores");
      bsonWriter.writeString("12");
      bsonWriter.writeString("56");
      bsonWriter.writeString("29");
      bsonWriter.writeEndArray();
      bsonWriter.writeStartArray("aliases");
      bsonWriter.writeString("fred");
      bsonWriter.writeString("f");
      bsonWriter.writeString("Mr freddy");
      bsonWriter.writeEndArray();
      bsonWriter.writeStartArray("vacationSpots");
      bsonWriter.writeString("paris");
      bsonWriter.writeString("paris");
      bsonWriter.writeString("london");
      bsonWriter.writeEndArray();
      bsonWriter.writeStartDocument("counts");
      bsonWriter.writeInt32("houses", 2);
      bsonWriter.writeInt32("cars", 6);
      bsonWriter.writeInt32("children", 3);
      bsonWriter.writeEndDocument();
      bsonWriter.writeStartDocument("qualifications");
      bsonWriter.writeStartDocument("english");
      bsonWriter.writeString("type", "masters");
      bsonWriter.writeString("grade", "1");
      bsonWriter.writeEndDocument();
      bsonWriter.writeStartDocument("french");
      bsonWriter.writeString("type", "phd");
      bsonWriter.writeString("grade", "2");
      bsonWriter.writeEndDocument();
      bsonWriter.writeEndDocument();
      bsonWriter.writeEndDocument();

      bsonWriter.flush();

      buffer = new byte[output.getSize()];
      System.arraycopy(output.getInternalBuffer(), 0, buffer, 0, output.getSize());
    }

    BobBson bobBson = new BobBson();
    Person person =
        bobBson.deserialise(Person.class, new BsonReader(new ByteBufferBobBsonBuffer(buffer)));
    System.out.println(person);

    BasicObject bo = new BasicObject();
    bo.setName("Fred");
    bo.setAge(12);
    bo.setScore(1.4);
    var grades = new HashMap<String, Integer>();
    grades.put("maths", 1);
    grades.put("biology", 2);
    grades.put("french", 4);
    bo.setGrades(grades);

    var aliases = new HashMap<String, String>();
    aliases.put("key1", "value1");
    aliases.put("key2", "value2");
    aliases.put("key3", "value3");
    bo.setAliases(aliases);

    Qualification qualification = new Qualification();
    qualification.setGrade("A");
    qualification.setType("Computer Science");
    bo.setQualification(qualification);

    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new ByteBufferBobBsonBuffer(new byte[size]));
    DynamicBobBsonBuffer dynamicBuffer = new DynamicBobBsonBuffer(pool);

    org.bobstuff.bobbson.writer.BsonWriter bsonWriter =
        new org.bobstuff.bobbson.writer.BsonWriter(dynamicBuffer);

    bobBson.serialise(bo, BasicObject.class, bsonWriter);

    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    dynamicBuffer.pipe(bas);

    Files.write(Paths.get("/tmp/data.bin"), bas.toByteArray());

    BasicObject bo2 =
        bobBson.deserialise(
            BasicObject.class, new BsonReader(new ByteBufferBobBsonBuffer(bas.toByteArray())));
    System.out.println(bo2);

    testPersonSerializeThenDeserialise();
  }

  private static void testPersonSerializeThenDeserialise() throws Exception {
    BobBson bobBson = new BobBson();
    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new ByteBufferBobBsonBuffer(new byte[size]));
    DynamicBobBsonBuffer dynamicBuffer = new DynamicBobBsonBuffer(pool);

    Person p = new Person();
    p.setName("Bob");
    p.setOccupation("Programmer");
    p.setCountry("custom: Scotland");
    p.setAge(6);
    p.setWeight(3.1415);
    p.setPlaces(Arrays.asList("France", "Germany", "Greece"));
    p.setAliases(Arrays.asList("Jesus", "Christ", "Test", "Data", "Sucks"));
    p.setScores("custom: 12345");
    p.setVacationSpots(new HashSet<>(Arrays.asList("Hell", "Hogwarts")));
    Map<String, Integer> counts = new HashMap<>();
    counts.put("count1", 1);
    counts.put("count2", 2);
    p.setCounts(counts);
    Map<String, Qualification> qualifications = new HashMap<>();
    var q1 = new Qualification();
    q1.setType("degree");
    q1.setGrade("A");
    var q2 = new Qualification();
    q2.setType("degree");
    q2.setGrade("B");
    qualifications.put("french", q1);
    qualifications.put("english", q2);
    p.setQualifications(qualifications);

    org.bobstuff.bobbson.writer.BsonWriter bsonWriter =
        new org.bobstuff.bobbson.writer.BsonWriter(dynamicBuffer);

    bobBson.serialise(p, Person.class, bsonWriter);

    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    dynamicBuffer.pipe(bas);

    Files.write(Paths.get("/tmp/data.bin"), bas.toByteArray());

    Person p2 =
        bobBson.deserialise(
            Person.class, new BsonReader(new ByteBufferBobBsonBuffer(bas.toByteArray())));
    System.out.println(p);
    System.out.println(p2);

    if (!p.equals(p2)) {
      throw new Exception("objects didn't match");
    }
  }
}
