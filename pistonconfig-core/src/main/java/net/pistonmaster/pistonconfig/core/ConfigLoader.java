package net.pistonmaster.pistonconfig.core;

import java.io.Reader;
import java.io.Writer;

/// Reads and writes configuration documents for a specific format.
public interface ConfigLoader {
  /// Loads a document from a character stream.
  ///
  /// @param reader source reader
  /// @return loaded document
  ConfigDocument load(Reader reader);

  /// Saves a document to a character stream.
  ///
  /// @param document document to save
  /// @param writer destination writer
  void save(ConfigDocument document, Writer writer);
}
