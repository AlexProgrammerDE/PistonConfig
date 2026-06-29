package net.pistonmaster.pistonconfig.core;

/// Source marker used to write a comment line in a concrete format.
public enum ConfigCommentMarker {
  /// Hash marker, as in `# comment`.
  HASH,
  /// Double slash marker, as in `// comment`.
  DOUBLE_SLASH,
  /// Slash-star block marker, as in `/* comment */`.
  SLASH_STAR,
  /// Exclamation marker used by Java properties files.
  EXCLAMATION,
  /// Semicolon marker used by some properties-like formats.
  SEMICOLON,
  /// No source marker, usually for synthetic blank lines.
  NONE,
  /// The backend reported a marker that does not have a core equivalent.
  UNKNOWN
}
