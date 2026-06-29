package net.pistonmaster.pistonconfig.annotations;

/// Controls scalar coercion during typed config reads.
public enum ConfigScalarCoercion {
  /// Accept only native scalar shapes, except safe numeric conversions.
  STRICT,

  /// Also parse strings for booleans, numbers, enums, and value types.
  STRING
}
