package org.bobstuff.bobbson.records;

import java.util.List;
import org.bobstuff.bobbson.CustomListConverterAlterStrings;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public record RecordWithConverter(
    @BsonConverter(CustomListConverterAlterStrings.class) List<String> names) {}
