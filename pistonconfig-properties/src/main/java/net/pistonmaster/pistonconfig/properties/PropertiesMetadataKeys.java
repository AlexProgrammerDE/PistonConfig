package net.pistonmaster.pistonconfig.properties;

/// Core decoration attribute keys used by the properties backend.
public final class PropertiesMetadataKeys {
  /// Decoration attribute storing the key-value separator for an entry.
  public static final String SEPARATOR = "properties.separator";
  /// Decoration attribute storing whether repeated values should be written on one line.
  public static final String SINGLE_LINE = "properties.singleLine";
  /// Decoration attribute storing blank lines before an entry.
  public static final String BLANK_LINES_BEFORE = "properties.blankLinesBefore";

  private PropertiesMetadataKeys() {
  }
}
