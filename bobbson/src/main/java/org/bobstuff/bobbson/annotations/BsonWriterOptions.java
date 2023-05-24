package org.bobstuff.bobbson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configure the options the {@link org.bobstuff.bobbson.writer.BsonWriter} will use when writing the Bson data
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BsonWriterOptions {
  /**
   * @return true if {@code BsonWriter} should write nulls to output
   */
  boolean writeNull() default true;
}
