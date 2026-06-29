package net.pistonmaster.pistonconfig.core;

import java.util.Map;
import org.immutables.value.Value;

/// Source decorations that are not part of the typed configuration value.
///
/// Decorations describe how a node appeared in its source file, such as key
/// comments, scalar style, collection style, and parser source locations.
@PistonStyle
@Value.Immutable
public interface ConfigNodeDecorations {
  /// Returns comments attached to the key token rather than the value.
  ///
  /// @return key comment
  @Value.Default
  default ConfigComment keyComment() {
    return ConfigComment.none();
  }

  /// Returns the scalar style used for an object key.
  ///
  /// @return key scalar style
  @Value.Default
  default ConfigScalarStyle keyStyle() {
    return ConfigScalarStyle.UNSPECIFIED;
  }

  /// Returns the collection style used for object or list values.
  ///
  /// @return collection style
  @Value.Default
  default ConfigCollectionStyle collectionStyle() {
    return ConfigCollectionStyle.UNSPECIFIED;
  }

  /// Returns the scalar style used for scalar values.
  ///
  /// @return scalar style
  @Value.Default
  default ConfigScalarStyle scalarStyle() {
    return ConfigScalarStyle.UNSPECIFIED;
  }

  /// Returns the source location of an object key.
  ///
  /// @return key source location
  @Value.Default
  default ConfigSourceLocation keyLocation() {
    return ConfigSourceLocation.unknown();
  }

  /// Returns the source location of a node value.
  ///
  /// @return value source location
  @Value.Default
  default ConfigSourceLocation valueLocation() {
    return ConfigSourceLocation.unknown();
  }

  /// Returns backend-specific decoration attributes.
  ///
  /// @return backend attributes
  Map<String, String> attributes();

  /// Creates an Immutables builder for node decorations.
  ///
  /// @return decoration builder
  static ImmutableConfigNodeDecorations.Builder builder() {
    return ImmutableConfigNodeDecorations.builder();
  }

  /// Returns an empty decoration value.
  ///
  /// @return empty decorations
  static ConfigNodeDecorations empty() {
    return ImmutableConfigNodeDecorations.builder().build();
  }
}
