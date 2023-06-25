package org.bobstuff.bobbson.records;

import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public record RecordPlain(String name, int age, double iq) {}
