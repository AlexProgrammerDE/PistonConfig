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

  /// Returns an empty decoration value.
  ///
  /// @return empty decorations
  static ConfigNodeDecorations empty() {
    return ImmutableConfigNodeDecorations.builder().build();
  }

  /// Returns a copy with a different key comment.
  ///
  /// @param keyComment key comment
  /// @return updated decorations
  default ConfigNodeDecorations withKeyComment(ConfigComment keyComment) {
    return ImmutableConfigNodeDecorations.copyOf(this).withKeyComment(keyComment);
  }

  /// Returns a copy with a different key style.
  ///
  /// @param keyStyle key style
  /// @return updated decorations
  default ConfigNodeDecorations withKeyStyle(ConfigScalarStyle keyStyle) {
    return ImmutableConfigNodeDecorations.copyOf(this).withKeyStyle(keyStyle);
  }

  /// Returns a copy with a different collection style.
  ///
  /// @param collectionStyle collection style
  /// @return updated decorations
  default ConfigNodeDecorations withCollectionStyle(ConfigCollectionStyle collectionStyle) {
    return ImmutableConfigNodeDecorations.copyOf(this).withCollectionStyle(collectionStyle);
  }

  /// Returns a copy with a different scalar style.
  ///
  /// @param scalarStyle scalar style
  /// @return updated decorations
  default ConfigNodeDecorations withScalarStyle(ConfigScalarStyle scalarStyle) {
    return ImmutableConfigNodeDecorations.copyOf(this).withScalarStyle(scalarStyle);
  }

  /// Returns a copy with a different key location.
  ///
  /// @param keyLocation key source location
  /// @return updated decorations
  default ConfigNodeDecorations withKeyLocation(ConfigSourceLocation keyLocation) {
    return ImmutableConfigNodeDecorations.copyOf(this).withKeyLocation(keyLocation);
  }

  /// Returns a copy with a different value location.
  ///
  /// @param valueLocation value source location
  /// @return updated decorations
  default ConfigNodeDecorations withValueLocation(ConfigSourceLocation valueLocation) {
    return ImmutableConfigNodeDecorations.copyOf(this).withValueLocation(valueLocation);
  }

  /// Returns a copy with an added, replaced, or removed backend attribute.
  ///
  /// Passing `null` removes the key.
  ///
  /// @param key attribute key
  /// @param value attribute value, or `null` to remove it
  /// @return updated decorations
  default ConfigNodeDecorations withAttribute(String key, String value) {
    var nextAttributes = new java.util.LinkedHashMap<>(attributes());
    if (value == null) {
      nextAttributes.remove(key);
    } else {
      nextAttributes.put(key, value);
    }
    return ImmutableConfigNodeDecorations.copyOf(this).withAttributes(nextAttributes);
  }
}
