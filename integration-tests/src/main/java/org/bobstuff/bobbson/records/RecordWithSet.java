package org.bobstuff.bobbson.records;

import java.util.Set;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@GenerateBobBsonConverter
public record RecordWithSet(Set<String> names) {}
