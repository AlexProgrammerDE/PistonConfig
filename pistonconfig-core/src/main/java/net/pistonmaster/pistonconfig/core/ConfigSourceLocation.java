package net.pistonmaster.pistonconfig.core;

import org.immutables.value.Value;

/// Best-effort source location reported by a parser.
@PistonStyle
@Value.Immutable
public interface ConfigSourceLocation {
  /// Returns the parser-specific source description.
  ///
  /// @return source description
  @Value.Default
  default String description() {
    return "";
  }

  /// Returns the zero-based source line when known.
  ///
  /// @return source line, or `-1` when unknown
  @Value.Default
  default int line() {
    return -1;
  }

  /// Returns the zero-based source column when known.
  ///
  /// @return source column, or `-1` when unknown
  @Value.Default
  default int column() {
    return -1;
  }

  /// Creates a source location.
  ///
  /// @param description parser-specific source description
  /// @param line zero-based source line, or `-1` when unknown
  /// @param column zero-based source column, or `-1` when unknown
  /// @return source location
  static ConfigSourceLocation of(String description, int line, int column) {
    return ImmutableConfigSourceLocation.builder()
      .description(description == null ? "" : description)
      .line(line)
      .column(column)
      .build();
  }

  /// Creates an unknown source location.
  ///
  /// @return unknown source location
  static ConfigSourceLocation unknown() {
    return ImmutableConfigSourceLocation.builder().build();
  }

  /// Returns whether any location field contains useful information.
  ///
  /// @return `true` when the location is at least partially known
  default boolean isKnown() {
    return !description().isBlank() || line() >= 0 || column() >= 0;
  }
}
