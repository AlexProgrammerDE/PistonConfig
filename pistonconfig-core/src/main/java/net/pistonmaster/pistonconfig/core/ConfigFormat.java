package net.pistonmaster.pistonconfig.core;

import java.util.Set;

/// Describes a concrete configuration format backend.
public interface ConfigFormat {
  /// Returns the stable backend name.
  ///
  /// @return backend name
  String name();

  /// Returns file extensions supported by this backend without leading dots.
  ///
  /// @return supported extensions
  Set<String> extensions();

  /// Returns the preservation capabilities of this backend.
  ///
  /// @return backend capabilities
  ConfigFormatCapabilities capabilities();

  /// Creates or returns a loader for this backend.
  ///
  /// @return backend loader
  ConfigLoader loader();
}
