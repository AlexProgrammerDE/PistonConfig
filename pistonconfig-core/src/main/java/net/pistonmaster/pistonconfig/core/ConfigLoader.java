package net.pistonmaster.pistonconfig.core;

import java.io.Reader;
import java.io.Writer;

/**
 * Reads and writes configuration documents for a specific format.
 */
public interface ConfigLoader {
  ConfigDocument load(Reader reader);

  void save(ConfigDocument document, Writer writer);
}
