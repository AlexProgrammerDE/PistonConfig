package net.pistonmaster.pistonconfig.core;

/// Source-level scalar style, independent of a specific backend library.
public enum ConfigScalarStyle {
  /// The backend did not report a concrete scalar style.
  UNSPECIFIED,
  /// Plain, unquoted scalar style.
  PLAIN,
  /// Single-quoted scalar style.
  SINGLE_QUOTED,
  /// Double-quoted scalar style.
  DOUBLE_QUOTED,
  /// Literal block scalar style.
  LITERAL,
  /// Folded block scalar style.
  FOLDED,
  /// Generic multiline scalar style.
  MULTILINE,
  /// Binary numeric or literal style.
  BINARY,
  /// Octal numeric style.
  OCTAL,
  /// Decimal numeric style.
  DECIMAL,
  /// Hexadecimal numeric style.
  HEX,
  /// Timestamp scalar style.
  TIMESTAMP,
  /// HOCON substitution style.
  SUBSTITUTION,
  /// The backend reported a style that does not have a core equivalent.
  UNKNOWN
}
