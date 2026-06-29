package net.pistonmaster.pistonconfig.staticfields;

/// Validates a static config session after it has been loaded or updated.
@FunctionalInterface
public interface StaticConfigValidator {
  /// Validates a session.
  ///
  /// @param session static config session
  void validate(StaticConfigSession session);
}
