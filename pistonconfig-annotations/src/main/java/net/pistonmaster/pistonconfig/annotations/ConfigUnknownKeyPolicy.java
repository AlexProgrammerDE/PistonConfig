package net.pistonmaster.pistonconfig.annotations;

/// Controls unknown object keys when a typed store updates a file.
public enum ConfigUnknownKeyPolicy {
  /// Keep keys that are not represented by the typed config model.
  PRESERVE,

  /// Drop keys that are not represented by the typed config model.
  DROP
}
