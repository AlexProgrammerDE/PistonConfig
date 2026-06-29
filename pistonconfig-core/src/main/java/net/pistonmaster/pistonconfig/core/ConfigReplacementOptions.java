package net.pistonmaster.pistonconfig.core;

/// Options for replacing nodes while preserving selected source metadata from an existing node.
public record ConfigReplacementOptions(
  boolean preserveComments,
  boolean preserveDecorations,
  boolean preserveMetadata
) {
  /// Replaces the target node without preserving source metadata.
  ///
  /// @return replacement options
  public static ConfigReplacementOptions none() {
    return new ConfigReplacementOptions(false, false, false);
  }

  /// Preserves comments and common source decorations from the existing node.
  ///
  /// @return replacement options
  public static ConfigReplacementOptions source() {
    return new ConfigReplacementOptions(true, true, false);
  }

  /// Preserves comments, common source decorations, and backend metadata from the existing node.
  ///
  /// @return replacement options
  public static ConfigReplacementOptions sourceAndMetadata() {
    return new ConfigReplacementOptions(true, true, true);
  }
}
