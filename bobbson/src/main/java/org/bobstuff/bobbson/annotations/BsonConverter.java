package org.bobstuff.bobbson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bobstuff.bobbson.BobBsonConverter;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BsonConverter {
  /**
   * For which class this converter applies.
   *
   * @return target type
   */
  Class<? extends BobBsonConverter> target();
}
