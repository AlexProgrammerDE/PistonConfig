package net.pistonmaster.pistonconfig.staticfields;

/// Controls how static config reads handle invalid values that are present in a
/// document.
public enum StaticInvalidValuePolicy {
  /// Throw when a present value cannot be decoded.
  STRICT,

  /// Use the property default and mark the value for rewrite.
  FALLBACK_AND_REWRITE
}
