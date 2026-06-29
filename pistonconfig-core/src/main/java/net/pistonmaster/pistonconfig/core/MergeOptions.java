package net.pistonmaster.pistonconfig.core;

import org.immutables.value.Value;

/// Options for merging a default document into a current document.
@PistonStyle
@Value.Immutable
public interface MergeOptions {
  /// Returns how comments and presentation-oriented decorations are merged.
  ///
  /// @return comment merge strategy
  @Value.Default
  default MergeCommentStrategy commentStrategy() {
    return MergeCommentStrategy.FILL_MISSING;
  }

  /// Returns whether object keys missing from defaults are removed.
  ///
  /// @return `true` when unknown target keys should be removed
  @Value.Default
  default boolean removeUnknown() {
    return false;
  }

  /// Returns the strategy used when both nodes are lists.
  ///
  /// @return list merge strategy
  @Value.Default
  default MergeListStrategy listStrategy() {
    return MergeListStrategy.PRESERVE_EXISTING;
  }

  /// Returns when existing values are replaced by defaults.
  ///
  /// @return value merge strategy
  @Value.Default
  default MergeValueStrategy valueStrategy() {
    return MergeValueStrategy.REPLACE_INVALID;
  }

  /// Creates an Immutables builder for merge options.
  ///
  /// @return merge options builder
  static ImmutableMergeOptions.Builder builder() {
    return ImmutableMergeOptions.builder();
  }

  /// Returns options that add missing defaults, fill missing comments, and repair invalid shapes.
  ///
  /// @return conservative merge options
  static MergeOptions conservative() {
    return builder().build();
  }

  /// Returns options that make the target match the default schema closely.
  ///
  /// @return exact-default merge options
  static MergeOptions exactDefaults() {
    return builder()
      .commentStrategy(MergeCommentStrategy.REPLACE)
      .removeUnknown(true)
      .listStrategy(MergeListStrategy.REPLACE)
      .valueStrategy(MergeValueStrategy.REPLACE_EXISTING)
      .build();
  }
}
