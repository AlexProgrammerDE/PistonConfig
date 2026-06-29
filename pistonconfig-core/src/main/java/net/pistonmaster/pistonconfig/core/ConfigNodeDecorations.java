package net.pistonmaster.pistonconfig.core;

import java.util.Map;
import org.immutables.value.Value;

/**
 * Source decorations that are not part of the typed configuration value.
 */
@PistonStyle
@Value.Immutable
public interface ConfigNodeDecorations {
  @Value.Default
  default ConfigComment keyComment() {
    return ConfigComment.none();
  }

  @Value.Default
  default ConfigScalarStyle keyStyle() {
    return ConfigScalarStyle.UNSPECIFIED;
  }

  @Value.Default
  default ConfigCollectionStyle collectionStyle() {
    return ConfigCollectionStyle.UNSPECIFIED;
  }

  @Value.Default
  default ConfigScalarStyle scalarStyle() {
    return ConfigScalarStyle.UNSPECIFIED;
  }

  @Value.Default
  default ConfigSourceLocation keyLocation() {
    return ConfigSourceLocation.unknown();
  }

  @Value.Default
  default ConfigSourceLocation valueLocation() {
    return ConfigSourceLocation.unknown();
  }

  Map<String, String> attributes();

  static ConfigNodeDecorations empty() {
    return ImmutableConfigNodeDecorations.builder().build();
  }

  default ConfigNodeDecorations withKeyComment(ConfigComment keyComment) {
    return ImmutableConfigNodeDecorations.copyOf(this).withKeyComment(keyComment);
  }

  default ConfigNodeDecorations withKeyStyle(ConfigScalarStyle keyStyle) {
    return ImmutableConfigNodeDecorations.copyOf(this).withKeyStyle(keyStyle);
  }

  default ConfigNodeDecorations withCollectionStyle(ConfigCollectionStyle collectionStyle) {
    return ImmutableConfigNodeDecorations.copyOf(this).withCollectionStyle(collectionStyle);
  }

  default ConfigNodeDecorations withScalarStyle(ConfigScalarStyle scalarStyle) {
    return ImmutableConfigNodeDecorations.copyOf(this).withScalarStyle(scalarStyle);
  }

  default ConfigNodeDecorations withKeyLocation(ConfigSourceLocation keyLocation) {
    return ImmutableConfigNodeDecorations.copyOf(this).withKeyLocation(keyLocation);
  }

  default ConfigNodeDecorations withValueLocation(ConfigSourceLocation valueLocation) {
    return ImmutableConfigNodeDecorations.copyOf(this).withValueLocation(valueLocation);
  }

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
