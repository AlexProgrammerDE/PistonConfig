package net.pistonmaster.pistonconfig.hocon;

/// Core decoration attribute keys used by the HOCON backend.
public final class HoconMetadataKeys {
  /// Decoration attribute storing the Lightbend Config origin description.
  public static final String ORIGIN_DESCRIPTION = "hocon.origin.description";
  /// Decoration attribute storing the concise rendered source value.
  public static final String RENDERED = "hocon.rendered";

  private HoconMetadataKeys() {
  }
}
