package net.pistonmaster.pistonconfig.core;

/// Controls how list nodes behave when a current document is merged with defaults.
public enum MergeListStrategy {
  /// Keep the current list exactly as it is.
  PRESERVE_EXISTING,
  /// Replace the current list with the default list.
  REPLACE,
  /// Append default entries that are beyond the current list length.
  APPEND_MISSING
}
