package net.pistonmaster.pistonconfig.annotations;

/// Creates serializers with access to the current serialization context.
///
/// @param <T> Java value type
@FunctionalInterface
public interface ConfigSerializerFactory<T> {
  /// Creates a serializer for the current type context.
  ///
  /// @param context current serialization context
  /// @return serializer
  ConfigSerializer<? extends T> create(ConfigSerializationContext context);
}
