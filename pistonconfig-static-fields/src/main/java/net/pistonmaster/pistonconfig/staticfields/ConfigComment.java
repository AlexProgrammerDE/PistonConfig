package net.pistonmaster.pistonconfig.staticfields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares generated comments for a static [ConfigProperty] field.
///
/// Use an empty string to emit a blank comment line.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigComment {
  /// Comment lines.
  ///
  /// @return comment lines
  String[] value();
}
