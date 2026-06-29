package net.pistonmaster.pistonconfig.core;

import org.immutables.value.Value;

/// Options for merging a default document into a current document.
@PistonStyle
@Value.Immutable
public interface MergeOptions {
  /// Returns whether comments from defaults replace current comments.
  ///
  /// @return `true` when default comments should be copied to the target
  boolean updateComments();

  /// Returns whether object keys missing from defaults are removed.
  ///
  /// @return `true` when unknown target keys should be removed
  boolean removeUnknown();

  /// Returns the strategy used when both nodes are lists.
  ///
  /// @return list merge strategy
  @Value.Default
  default MergeListStrategy listStrategy() {
    return MergeListStrategy.PRESERVE_EXISTING;
  }

  /// Creates an Immutables staged builder for merge options.
  ///
  /// @return merge options builder
  static ImmutableMergeOptions.UpdateCommentsBuildStage builder() {
    return ImmutableMergeOptions.builder();
  }

  /// Returns options that add missing defaults and refresh comments.
  ///
  /// @return conservative merge options
  static MergeOptions conservative() {
    return builder()
      .updateComments(true)
      .removeUnknown(false)
      .build();
  }

  /// Returns options that make the target match the default schema closely.
  ///
  /// @return exact-default merge options
  static MergeOptions exactDefaults() {
    return builder()
      .updateComments(true)
      .removeUnknown(true)
      .listStrategy(MergeListStrategy.REPLACE)
      .build();
  }
}
