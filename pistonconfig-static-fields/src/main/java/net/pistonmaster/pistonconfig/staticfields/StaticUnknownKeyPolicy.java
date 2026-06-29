package net.pistonmaster.pistonconfig.staticfields;

/// Controls unknown object keys when a static config store updates a file.
public enum StaticUnknownKeyPolicy {
  /// Keep keys that are not declared by the static definition.
  PRESERVE,

  /// Remove keys that are not declared by the static definition.
  DROP
}
