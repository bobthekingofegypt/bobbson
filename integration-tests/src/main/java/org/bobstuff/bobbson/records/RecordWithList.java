package org.bobstuff.bobbson.records;

import java.util.List;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public record RecordWithList(List<String> names) {}
