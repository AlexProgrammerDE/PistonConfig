package net.pistonmaster.pistonconfig.core;

/**
 * Options for merging a default document into a current document.
 */
public record MergeOptions(
  boolean updateComments,
  boolean removeUnknown,
  MergeListStrategy listStrategy
) {
  public MergeOptions {
    if (listStrategy == null) {
      listStrategy = MergeListStrategy.PRESERVE_EXISTING;
    }
  }

  public static MergeOptions conservative() {
    return new MergeOptions(true, false, MergeListStrategy.PRESERVE_EXISTING);
  }

  public static MergeOptions exactDefaults() {
    return new MergeOptions(true, true, MergeListStrategy.REPLACE);
  }
}
