package org.bobstuff.bobbson.records;

import java.util.Map;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public record RecordWithMap(Map<String, Integer> ages) {}
