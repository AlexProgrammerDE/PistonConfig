package net.pistonmaster.pistonconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Adds block comments to a generated configuration node.
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface ConfigComment {
  /// Returns comment lines to attach to the generated node.
  ///
  /// @return comment lines
  String[] value();
}
