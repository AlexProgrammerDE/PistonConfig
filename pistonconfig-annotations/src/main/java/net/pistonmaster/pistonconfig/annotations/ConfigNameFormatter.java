package net.pistonmaster.pistonconfig.annotations;

/// Formats Java field and record component names into configuration keys.
@FunctionalInterface
public interface ConfigNameFormatter {
  /// Formats one Java member name.
  ///
  /// @param javaName source Java member name
  /// @return configuration key
  String format(String javaName);
}
