package net.pistonmaster.pistonconfig.core;

import java.util.Set;

/**
 * Describes a concrete configuration format backend.
 */
public interface ConfigFormat {
  String name();

  Set<String> extensions();

  ConfigFormatCapabilities capabilities();

  ConfigLoader loader();
}
