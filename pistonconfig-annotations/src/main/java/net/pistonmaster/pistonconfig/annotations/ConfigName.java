package net.pistonmaster.pistonconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Overrides the configuration key used for a field.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigName {
  /// Returns the path or key used for the annotated field.
  ///
  /// @return mapped field path
  String value();
}
