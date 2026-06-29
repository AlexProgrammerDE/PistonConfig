package net.pistonmaster.pistonconfig.annotations;

/// Applies application-specific cleanup after a typed config has been read.
///
/// @param <T> config type
@FunctionalInterface
public interface ConfigPostProcessor<T> {
  /// Processes a config object.
  ///
  /// @param config config object
  /// @return processed config object
  T process(T config);
}
