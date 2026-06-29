package net.pistonmaster.pistonconfig.yaml;

/// Core decoration attribute keys used by the YAML backend.
public final class YamlMetadataKeys {
  /// Decoration attribute storing the YAML tag for a node value.
  public static final String TAG = "yaml.tag";
  /// Decoration attribute storing the YAML anchor for a node value.
  public static final String ANCHOR = "yaml.anchor";
  /// Decoration attribute storing the YAML tag for a mapping key.
  public static final String KEY_TAG = "yaml.key.tag";
  /// Metadata key storing the original scalar text reported by SnakeYAML.
  public static final String SCALAR_RAW = "yaml.scalar.raw";

  private YamlMetadataKeys() {
  }
}
