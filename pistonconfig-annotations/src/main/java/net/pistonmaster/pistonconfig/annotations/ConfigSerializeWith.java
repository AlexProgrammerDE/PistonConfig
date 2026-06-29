package net.pistonmaster.pistonconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Selects a custom serializer for a field, record component, type, or meta-annotation.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface ConfigSerializeWith {
  /// Returns the serializer type.
  ///
  /// @return serializer type
  Class<? extends ConfigSerializer<?>> value();

  /// Returns the nesting level where this serializer applies.
  ///
  /// Root member nesting is `0`; collection element nesting increments from there.
  ///
  /// @return nesting level
  int nesting() default 0;
}
