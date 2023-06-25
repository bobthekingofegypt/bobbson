package org.bobstuff.bobbson.records;

import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public record RecordWithAttribute(
    @BsonAttribute("notname") String name, @BsonAttribute(order = 12) int age) {}
