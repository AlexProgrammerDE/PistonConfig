package net.pistonmaster.pistonconfig.staticfields;

import java.util.Objects;
import net.pistonmaster.pistonconfig.core.MergeCommentStrategy;
import net.pistonmaster.pistonconfig.core.MergeListStrategy;
import net.pistonmaster.pistonconfig.core.MergeOptions;
import net.pistonmaster.pistonconfig.core.MergeValueStrategy;

/// Options used by static config stores.
public final class StaticConfigStoreOptions {
  private final MergeCommentStrategy commentStrategy;
  private final StaticUnknownKeyPolicy unknownKeyPolicy;
  private final MergeListStrategy listStrategy;
  private final MergeValueStrategy valueStrategy;
  private final StaticInvalidValuePolicy invalidValuePolicy;

  private StaticConfigStoreOptions(Builder builder) {
    commentStrategy = Objects.requireNonNull(builder.commentStrategy, "commentStrategy");
    unknownKeyPolicy = Objects.requireNonNull(builder.unknownKeyPolicy, "unknownKeyPolicy");
    listStrategy = Objects.requireNonNull(builder.listStrategy, "listStrategy");
    valueStrategy = Objects.requireNonNull(builder.valueStrategy, "valueStrategy");
    invalidValuePolicy = Objects.requireNonNull(builder.invalidValuePolicy, "invalidValuePolicy");
  }

  /// Creates options with conservative defaults.
  ///
  /// @return default options
  public static StaticConfigStoreOptions defaults() {
    return builder().build();
  }

  /// Creates an options builder.
  ///
  /// @return options builder
  public static Builder builder() {
    return new Builder();
  }

  /// Returns how generated comments merge with existing comments during update.
  ///
  /// @return comment merge strategy
  public MergeCommentStrategy commentStrategy() {
    return commentStrategy;
  }

  /// Returns unknown key behavior.
  ///
  /// @return unknown key policy
  public StaticUnknownKeyPolicy unknownKeyPolicy() {
    return unknownKeyPolicy;
  }

  /// Returns list merge behavior.
  ///
  /// @return list merge strategy
  public MergeListStrategy listStrategy() {
    return listStrategy;
  }

  /// Returns how existing values are merged with generated defaults.
  ///
  /// @return value merge strategy
  public MergeValueStrategy valueStrategy() {
    return valueStrategy;
  }

  /// Returns invalid value behavior.
  ///
  /// @return invalid value policy
  public StaticInvalidValuePolicy invalidValuePolicy() {
    return invalidValuePolicy;
  }

  /// Converts store options to document merge options.
  ///
  /// @return merge options
  public MergeOptions mergeOptions() {
    return MergeOptions.builder()
      .commentStrategy(commentStrategy)
      .removeUnknown(unknownKeyPolicy == StaticUnknownKeyPolicy.DROP)
      .listStrategy(listStrategy)
      .valueStrategy(valueStrategy)
      .build();
  }

  /// Builder for [StaticConfigStoreOptions].
  public static final class Builder {
    private MergeCommentStrategy commentStrategy = MergeCommentStrategy.FILL_MISSING;
    private StaticUnknownKeyPolicy unknownKeyPolicy = StaticUnknownKeyPolicy.PRESERVE;
    private MergeListStrategy listStrategy = MergeListStrategy.PRESERVE_EXISTING;
    private MergeValueStrategy valueStrategy = MergeValueStrategy.REPLACE_INVALID;
    private StaticInvalidValuePolicy invalidValuePolicy = StaticInvalidValuePolicy.STRICT;

    private Builder() {
    }

    /// Sets how generated comments merge with existing comments during update.
    ///
    /// @param commentStrategy comment merge strategy
    /// @return this builder
    public Builder commentStrategy(MergeCommentStrategy commentStrategy) {
      this.commentStrategy = Objects.requireNonNull(commentStrategy, "commentStrategy");
      return this;
    }

    /// Sets unknown key behavior.
    ///
    /// @param unknownKeyPolicy unknown key policy
    /// @return this builder
    public Builder unknownKeyPolicy(StaticUnknownKeyPolicy unknownKeyPolicy) {
      this.unknownKeyPolicy = Objects.requireNonNull(unknownKeyPolicy, "unknownKeyPolicy");
      return this;
    }

    /// Sets list merge behavior.
    ///
    /// @param listStrategy list merge strategy
    /// @return this builder
    public Builder listStrategy(MergeListStrategy listStrategy) {
      this.listStrategy = Objects.requireNonNull(listStrategy, "listStrategy");
      return this;
    }

    /// Sets how existing values are merged with generated defaults.
    ///
    /// @param valueStrategy value merge strategy
    /// @return this builder
    public Builder valueStrategy(MergeValueStrategy valueStrategy) {
      this.valueStrategy = Objects.requireNonNull(valueStrategy, "valueStrategy");
      return this;
    }

    /// Sets invalid value behavior.
    ///
    /// @param invalidValuePolicy invalid value policy
    /// @return this builder
    public Builder invalidValuePolicy(StaticInvalidValuePolicy invalidValuePolicy) {
      this.invalidValuePolicy = Objects.requireNonNull(invalidValuePolicy, "invalidValuePolicy");
      return this;
    }

    /// Builds options.
    ///
    /// @return store options
    public StaticConfigStoreOptions build() {
      return new StaticConfigStoreOptions(this);
    }
  }
}
