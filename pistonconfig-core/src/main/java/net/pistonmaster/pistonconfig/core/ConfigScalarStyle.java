package net.pistonmaster.pistonconfig.core;

/**
 * Source-level scalar style, independent of a specific backend library.
 */
public enum ConfigScalarStyle {
  UNSPECIFIED,
  PLAIN,
  SINGLE_QUOTED,
  DOUBLE_QUOTED,
  LITERAL,
  FOLDED,
  MULTILINE,
  BINARY,
  OCTAL,
  DECIMAL,
  HEX,
  TIMESTAMP,
  SUBSTITUTION,
  UNKNOWN
}
