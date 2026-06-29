package net.pistonmaster.pistonconfig.core;

import org.immutables.value.Value;

/**
 * Best-effort source location reported by a parser.
 */
@PistonStyle
@Value.Immutable
public interface ConfigSourceLocation {
  @Value.Default
  default String description() {
    return "";
  }

  @Value.Default
  default int line() {
    return -1;
  }

  @Value.Default
  default int column() {
    return -1;
  }

  static ConfigSourceLocation of(String description, int line, int column) {
    return ImmutableConfigSourceLocation.builder()
      .description(description == null ? "" : description)
      .line(line)
      .column(column)
      .build();
  }

  static ConfigSourceLocation unknown() {
    return ImmutableConfigSourceLocation.builder().build();
  }

  default boolean isKnown() {
    return !description().isBlank() || line() >= 0 || column() >= 0;
  }
}
