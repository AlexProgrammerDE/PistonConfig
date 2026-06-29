package net.pistonmaster.pistonconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Registers aliases for subtypes of a [ConfigPolymorphic] type.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigPolymorphicTypes {
  /// Returns subtype alias declarations.
  ///
  /// @return subtype alias declarations
  Type[] value();

  /// One subtype alias declaration.
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface Type {
    /// Returns the subtype.
    ///
    /// @return subtype
    Class<?> type();

    /// Returns the alias. A blank alias falls back to the class name.
    ///
    /// @return subtype alias
    String alias() default "";
  }
}
