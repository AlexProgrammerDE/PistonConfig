package net.pistonmaster.pistonconfig.core;

/// Controls when existing values are replaced during default merging.
public enum MergeValueStrategy {
  /// Keep existing values even when their shape differs from the defaults.
  PRESERVE_EXISTING,
  /// Replace existing values whose node kind differs from the default node kind.
  REPLACE_INVALID,
  /// Replace existing values declared by the defaults.
  REPLACE_EXISTING
}
