package net.pistonmaster.pistonconfig.core;

/**
 * Declares what a backend can preserve when reading and writing a document.
 */
public record ConfigFormatCapabilities(
  boolean blockComments,
  boolean inlineComments,
  boolean ordering,
  boolean typedScalars,
  boolean lists
) {
  public static ConfigFormatCapabilities full() {
    return new ConfigFormatCapabilities(true, true, true, true, true);
  }
}
