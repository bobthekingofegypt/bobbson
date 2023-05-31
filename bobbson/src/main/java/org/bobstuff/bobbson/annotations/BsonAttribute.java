package org.bobstuff.bobbson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Identify and configure an attribute as a bean field. */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BsonAttribute {
  String DEFAULT_NON_VALID_ALIAS = "~**&-defaultfield";
  /**
   * @return an alias for the attribute if the value in your bson differs from the value in your
   *     model use this to map them
   */
  String value() default DEFAULT_NON_VALID_ALIAS;

  /**
   * @return control the order of the attribute as it should appear in the bson data, lower numbered
   *     values appear first
   */
  int order() default Integer.MAX_VALUE;

  boolean ignore() default false;
}
