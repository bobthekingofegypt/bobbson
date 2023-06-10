package org.bobstuff.bobbson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bobstuff.bobbson.BobBsonConverter;

/**
 * Declare a custom converter for a bean field.
 *
 * <p>When custom behaviour is needed or a new unknown type exists a subclass of {@link
 * BobBsonConverter} can be created and configured using this annotation
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BsonConverter {
  /** @return implementation of {@code BobBsonConverter} to use for annotated field */
  Class<? extends BobBsonConverter<?>> value();
}
