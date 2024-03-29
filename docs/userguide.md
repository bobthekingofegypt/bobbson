# BobBson User Guide

1. [Using BobBson](#using-bobbson)
   * [Using BobBson with gradle](#using-bobbson-with-gradle)
   * [Annotation Processor](#annotation-processor)
   * [BobBson Instance](#bobbson-instance)
   * [Buffers](#buffers)
   * [BsonReader](#bsonreader)
   * [BsonWriter](#bsonwriter)
   * [BobBsonConverter](#bobbsonconverter)
   * [Reflection](#reflection)
   * [@BsonAttribute](#bsonattribute)
   * [@BsonConverter](#bsonconverter)

## Using BobBson

### Using BobBson with gradle

BobBson is available through jitpack, you will need to add jitpack to your repository list and then you can load releases using this projects github address.

```gradle
repositories {
    mavenCentral()
    maven {
        url "https://jitpack.io"
    }
}
```

```gradle
dependencies {
  annotationProcessor 'com.github.bobthekingofegypt.bobbson:processor:0.9.0'
  implementation 'com.github.bobthekingofegypt.bobbson:bobbson:0.9.0'
}
```

### Annotation Processor

BobBson supports the generation of efficient serialise/deserialise code by using an annotation processor.  The annotation processor will scan classes marked with the *@GenerateBobBsonConverter* annotation.  The processor will attempt to identify the bean fields on the class and generate custom code for BobBson to use when encountering instances of the class.

```java
@GenerateBobBsonConverter
public class Person {
    private String name;
    private int age;

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
}
```

The processor will read this class, identify name and age as bean properties and create a parser to read/write this object from/to BSON without you having to write any parsers.

Use of the processor is not required, BobBson can use reflection to understand how to read/write the object.  Reflection is disabled by default but can be enabled in the BobBsonConfig when creating an instance of BobBson.

### BobBson Instance

The entry point for BobBson is the BobBson class.  This class controls the currently registered converters and factories.  It has methods to perform serialise and deserialise operations.

#### Basic BobBson Example 

```java
var person = new Person();
person.setName("Bob");
person.setAge(10);

var bobBson = new BobBson();

var buffer = new BobBufferBobBsonBuffer(new byte[2048], 0, 0);
var writer = new StackBsonWriter(buffer);
bobBson.serialise(person, Person.class, writer);

var data = buffer.toByteArray();

var readBuffer = new BobBufferBobBsonBuffer(data, 0, data.length);
var reader = new StackBsonReader(readBuffer);
var personResult = bobBson.deserialise(Person.class, reader);
```

By default BobBson will try to find converters generated by @GenerateBobBsonConverter marked models and pre-register them.  It also contains default implementations to handle String,Integer,Double,Long,Boolean.

This example shows the use of buffers,writers,readers and how they interact with BobBson.

### Buffers

BobBson has multiple buffers built in, they vary in their purpose and performance characteristics.  The basic BobBson example showed the use of BobBufferBobBsonBuffer, this is a custom buffer implementation in BobBson.  It is fast for most uses but for writing it does not support dynamic resizing so can only be used if you know your data will fit into its buffer.

Other buffers available:
* _ByteBufferBobBsonBuffer_ wrapper around the Java ByteBuffer class, useful if you already have ByteBuffer instances.
* _DynamicBobBsonBuffer_ a buffer that internally contains multiple buffers and supports allocating more buffer segments as data expands beyond its initial size.  Useful for write operations when size of data to be writen is unknown.  Slower than other implementations when size of data is known, no recommended to use during read operations.  Supports buffer pooling.
* _ActiveJBobBsonBuffer_ wrapper around active-j bytebuffs.  Currently, the fastest implementation available.
* _InputStreamBobBsonBuffer_ supports reading only, useful when you have an InputStream to read from.

Custom buffer implementations can be created by implementing the BobBsonBuffer interface.

### BsonReader

BsonReader is the interface that readers implement.  A readers responsibility is to provide implementations of functions that can read all the Bson specification types. Currently, provided by default is the StackBsonReader.  This implementation internally maintains a context stack tracking where you are in the document.  This class if not threadsafe, it can be reused as long as no errors occured during a previous read.  (A reset concept may be added later). 

In the experimental module there may be other implementations of BsonReader but for now there is only StackBsonReader in core.

Readers can be used directly to read BSON data step by step

```java

var readBuffer = new BobBufferBobBsonBuffer(data, 0, data.length);
var reader = new StackBsonReader(readBuffer);
reader.readStartDocument();
var type = reader.readBsonType();
if (type != BsonType.STRING || !"name".equals(reader.currentFieldName())) {
    throw new RuntimeException("Field should be string");
}
...
```

### BsonWriter

BsonWriter is the interface that writers implement.  A writers responsibility is to provide implementations of functions that can write all the Bson specification types.  Currently provided by default is the StackBsonWriter.  This class is not threadsafe, it can be reused as long as no error occurred during a previous write.  (A reset concept may be added later)

In the experimental module there may be other implementations of BsonReader but for now there is only StackBsonReader in core.

Writers can be used to write Bson data step by step.

```java
BsonWriter writer = new StackBsonWriter(buffer);
writer.writeStartDocument();
writer.writeObjectId("key", id);
writer.writeString("name", "bob");
writer.writeEndDocument();
```

### BobBsonConverter

A converter is a class that knows how to read and write a specific type to bson.

An example for String could be:
```java
public class StringBsonConverter implements BobBsonConverter<String> {
    @Override
    public String readValue(BsonReader bsonReader, BsonType type) {
        if (type == BsonType.STRING) {
            return bsonReader.readString();
        }

        throw new RuntimeException(format("Attempting to read %s bson type as a string", type));
    }

    @Override
    public void writeValue(BsonWriter bsonWriter, String value) {
        bsonWriter.writeString(value);
    }
}
```

This is the minimum interface that must be implemented to create a converter.  If you want to implement key writing or null handling differently than the default implementation you would have to override the default implementation of the read/write interface.  With just these methods keys are efficiently writen and null values are writen and read as null.

Converters can be used directly but it would be more common to register your converter with BobBson.

```java
bobBson.registerConverter(String.class, new StringBsonConverter());
```

Now your converter is available to BobBson when it is reading/writing more complex objects that contain strings.  Your converter can also be retrieved through BobBson

```java
var converter = bobBson.tryFindConverter(String.class);
```

Using this method you can add support for new types or override built in support of types.

### Reflection

When you don't have access to all the code to add your own annotations you may require the use of reflection.  BobBson has basic reflection support currently, it needs more work to add more capabilities for builders, records, complex generics.

Reflection is enabled when creating the BobBson instance

```java
var bobBson = new BobBson(BobBsonConfig.Builder.builder().withReflection().build());
```

That is all that is required.

### @BsonAttribute

Sometimes when converting a bson field to java object you may want to handle field names that don't conform to camel case standard.  Or your field has a different name in the bson.  To do this you would mark the field with the @BsonAttribute annotation setting the value to its alias.

```java
@BsonAttribute("date_created")
private Instant dateCreated;
```

Ignoring a field in a java bean when writing/reading can be handle with the ignore flag

```java
@BsonAttribute(ignore = true)
private Instant dateCreated;
```

The order that fields are writen in, or read back in can be controlled with the order flag.  This can speed up parsing when you know the exact order the data is going to be in as you wouldn't have to compare the field key against every field each time as you would know what field is next.

```java
@BsonAttribute(order = 1)
private Instant dateCreated;

@BsonAttribute(order = 100)
private Instant afterDateCreated;
```

### @BsonConverter

To handle reading/writing a specific instance of a field differently than it's type would normally expect can be handled by marking the field with @BsonConverter and the class of a custom converter.  For example you could have a String in an object that is a date but you don't want Date objects by default to expect a Bson timestamp.

```java
@BsonConverter(DateFromStringConverter.class)
private Instant dateCreated;
```
