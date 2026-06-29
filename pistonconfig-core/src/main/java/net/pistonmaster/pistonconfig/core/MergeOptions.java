package net.pistonmaster.pistonconfig.core;

/// Options for merging a default document into a current document.
///
/// @param updateComments whether comments from defaults replace current comments
/// @param removeUnknown whether object keys missing from defaults are removed
/// @param listStrategy strategy used when both nodes are lists
public record MergeOptions(
  boolean updateComments,
  boolean removeUnknown,
  MergeListStrategy listStrategy
) {
  /// Creates merge options and defaults a missing list strategy.
  public MergeOptions {
    if (listStrategy == null) {
      listStrategy = MergeListStrategy.PRESERVE_EXISTING;
    }
  }

  /// Returns options that add missing defaults and refresh comments.
  ///
  /// @return conservative merge options
  public static MergeOptions conservative() {
    return new MergeOptions(true, false, MergeListStrategy.PRESERVE_EXISTING);
  }

  /// Returns options that make the target match the default schema closely.
  ///
  /// @return exact-default merge options
  public static MergeOptions exactDefaults() {
    return new MergeOptions(true, true, MergeListStrategy.REPLACE);
  }
}
