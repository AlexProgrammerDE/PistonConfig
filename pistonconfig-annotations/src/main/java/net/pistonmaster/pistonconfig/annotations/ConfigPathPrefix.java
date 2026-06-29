package net.pistonmaster.pistonconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Places all mapped fields for a type under a shared path.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigPathPrefix {
  /// Returns the path prefix applied to every mapped field in the type.
  ///
  /// @return mapped path prefix
  String value();
}
