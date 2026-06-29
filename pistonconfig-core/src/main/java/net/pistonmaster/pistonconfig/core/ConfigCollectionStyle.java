package net.pistonmaster.pistonconfig.core;

/// Source-level object or list style, independent of a specific backend library.
public enum ConfigCollectionStyle {
  /// The backend did not report a concrete collection style.
  UNSPECIFIED,
  /// A block-style collection, such as a YAML block mapping.
  BLOCK,
  /// A flow-style collection, such as a YAML inline map or sequence.
  FLOW,
  /// An inline collection representation.
  INLINE,
  /// A TOML table-style collection.
  TABLE,
  /// A TOML array-of-tables collection.
  ARRAY_TABLE,
  /// The backend reported a style that does not have a core equivalent.
  UNKNOWN
}
