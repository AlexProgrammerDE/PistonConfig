package net.pistonmaster.pistonconfig.core;

/// Converts between Java values and [ConfigNode] instances.
///
/// @param <T> Java type handled by this codec
public interface ConfigCodec<T> {
  /// Encodes a Java value into a configuration node.
  ///
  /// @param value value to encode
  /// @param registry registry available for nested or delegated values
  /// @return encoded node
  ConfigNode encode(T value, ConfigCodecRegistry registry);

  /// Decodes a Java value from a configuration node.
  ///
  /// @param node source node
  /// @param registry registry available for nested or delegated values
  /// @return decoded value
  T decode(ConfigNode node, ConfigCodecRegistry registry);
}
