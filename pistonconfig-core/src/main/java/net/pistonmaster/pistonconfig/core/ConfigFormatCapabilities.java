package net.pistonmaster.pistonconfig.core;

/// Declares what a backend can preserve when reading and writing a document.
///
/// @param blockComments whether the backend preserves standalone block comments
/// @param inlineComments whether the backend preserves inline comments
/// @param ordering whether the backend preserves key and list ordering
/// @param typedScalars whether the backend preserves scalar types beyond strings
/// @param lists whether the backend preserves list values
public record ConfigFormatCapabilities(
  boolean blockComments,
  boolean inlineComments,
  boolean ordering,
  boolean typedScalars,
  boolean lists
) {
  /// Returns capabilities for a backend that preserves every core feature.
  ///
  /// @return full capability set
  public static ConfigFormatCapabilities full() {
    return new ConfigFormatCapabilities(true, true, true, true, true);
  }
}
