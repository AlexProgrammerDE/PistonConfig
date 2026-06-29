package net.pistonmaster.pistonconfig.core;

/// Controls how comments and presentation-oriented source decorations are merged.
public enum MergeCommentStrategy {
  /// Keep comments and presentation decorations already present in the target.
  KEEP_EXISTING,
  /// Copy default comments and presentation decorations only where the target has none.
  FILL_MISSING,
  /// Replace target comments and presentation decorations with the defaults.
  REPLACE
}
