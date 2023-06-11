# BobBson Project

This is an experiment to create a faster bson library for the jvm. [BSON](https://bsonspec.org/) is short for binary JSON and is a binary-encoded serialization of JSON-like documents.  It is the data interchange format used by MongoDB.

BobBson can be used to convert BSON to Java objects and can also be used to convert Java objects into BSON.  It supports precompiled parsers using the annotation processor and also runtime support using reflection.

## Status

BobBson is a side project I started a while ago to get back into Java programming, it's something I work on occasionally when I have time.  There is still a lot of advanced features that are missing.

Currently supports:
* basic java beans
* basic enums
* column aliasing
* column ordering
* overriding of null handling
* additional type support through custom converters
* additional loading/detection through custom converter factories
* custom buffer types through BobBsonBuffer interface
* custom reader/writers through BsonReader/BsonWriter interfaces

## Download

Currently, BobBson is only available through [jitpack](https://jitpack.io/).  Follow the instruction on jitpack for setting up gradle to use the jitpack repository.

```
dependencies {
  annotationProcessor 'com.github.bobthekingofegypt.bobbson:processor:0.9.0'
  implementation 'com.github.bobthekingofegypt.bobbson:bobbson:0.9.0'
}
```

## Usage

* [User Guide](docs/userguide.md)

## Performance

Project contains a jmh benchmarks module.  Benchmarks can be run on your own machine by checking out the project and running:

```
./gradlew :benchmarks:jmh
```

In the output will be a link to a webpage displaying your results.

Sample output from my machine running test that mimicks [jvm serializers](https://github.com/eishay/jvm-serializers/wiki) (lower numbers are better):

![alt text](https://github.com/bobthekingofegypt/bobbson/raw/main/docs/images/jvmserializers-average.png "Average request time")

![alt text](https://github.com/bobthekingofegypt/bobbson/raw/main/docs/images/jvmserializers-gc.png "Normalised GC use")


## Requirements

Minimum Java version

bobbson 0.9 and newer: Java 17


## Other modules

* activeJ:
Module that supports using ActiveJ byte buffers for serialisation and deserialisation 
* bobbson-mongodb: Module that adds converters for mongodb bson types like BsonDocument, BsonArray, BsonString etc.



