package net.pistonmaster.pistonconfig.annotations;

/// Validates a typed config after it has been read.
///
/// @param <T> config type
@FunctionalInterface
public interface ConfigValidator<T> {
  /// Validates a config object.
  ///
  /// @param config config object
  void validate(T config);
}
