package net.pistonmaster.pistonconfig.annotations;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import net.pistonmaster.pistonconfig.core.ConfigNode;

/// Context passed to custom config serializers.
public interface ConfigSerializationContext {
  /// Returns mapper options.
  ///
  /// @return mapper options
  ConfigMapperOptions options();

  /// Returns the currently handled annotated type.
  ///
  /// @return annotated type
  AnnotatedType annotatedType();

  /// Returns the currently handled generic type.
  ///
  /// @return generic type
  default Type type() {
    return annotatedType().getType();
  }

  /// Returns current nesting depth.
  ///
  /// Root value nesting is `0`; collection element nesting increments from there.
  ///
  /// @return nesting depth
  int nesting();

  /// Encodes a nested value with the mapper.
  ///
  /// @param value Java value
  /// @param type nested annotated type
  /// @return encoded node
  ConfigNode encode(Object value, AnnotatedType type);

  /// Decodes a nested value with the mapper.
  ///
  /// @param node source node
  /// @param type nested annotated type
  /// @return decoded value
  Object decode(ConfigNode node, AnnotatedType type);
}
