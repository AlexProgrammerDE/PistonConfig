package net.pistonmaster.pistonconfig.core;

/**
 * Source-level object/list style, independent of a specific backend library.
 */
public enum ConfigCollectionStyle {
  UNSPECIFIED,
  BLOCK,
  FLOW,
  INLINE,
  TABLE,
  ARRAY_TABLE,
  UNKNOWN
}
