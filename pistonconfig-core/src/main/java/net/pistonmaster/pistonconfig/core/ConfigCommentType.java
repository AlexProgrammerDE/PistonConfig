package net.pistonmaster.pistonconfig.core;

/// Logical shape of a comment line as represented by a backend.
public enum ConfigCommentType {
  /// A comment that appears before or after a node as its own line.
  BLOCK,
  /// A comment that appears on the same logical line as a node.
  INLINE,
  /// A blank comment line used to preserve spacing.
  BLANK
}
