package net.pistonmaster.pistonconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Enables runtime subtype serialization for an abstract class or interface.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigPolymorphic {
  /// Default property used to store subtype information.
  String DEFAULT_PROPERTY = "type";

  /// Returns the object property that stores subtype information.
  ///
  /// @return type discriminator property
  String property() default DEFAULT_PROPERTY;
}
