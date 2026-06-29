package net.pistonmaster.pistonconfig.annotations;

import net.pistonmaster.pistonconfig.core.ConfigNode;

/// Custom serializer for one Java value type.
///
/// @param <T> Java value type
public interface ConfigSerializer<T> {
  /// Encodes a Java value into a config node.
  ///
  /// @param value value to encode
  /// @param context serialization context
  /// @return encoded config node
  ConfigNode encode(T value, ConfigSerializationContext context);

  /// Decodes a Java value from a config node.
  ///
  /// @param node source node
  /// @param context serialization context
  /// @return decoded Java value
  T decode(ConfigNode node, ConfigSerializationContext context);
}
