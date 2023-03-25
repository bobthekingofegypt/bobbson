package org.bobstuff.bobbson;

import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.CompiledBson;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

@CompiledBson
public class MultipleConverterBug<TModel> {
    @BsonAttribute("q")
    private @Nullable String filter;

    @BsonAttribute("u")
    private @Nullable List<String> updates;


    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<String> getUpdates() {
        return updates;
    }

    public void setUpdates(List<String> updates) {
        this.updates = updates;
    }
}
