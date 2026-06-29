package net.pistonmaster.pistonconfig.staticfields;

import java.util.Objects;
import net.pistonmaster.pistonconfig.core.MergeListStrategy;
import net.pistonmaster.pistonconfig.core.MergeOptions;

/// Options used by static config stores.
public final class StaticConfigStoreOptions {
  private final boolean updateComments;
  private final StaticUnknownKeyPolicy unknownKeyPolicy;
  private final MergeListStrategy listStrategy;
  private final StaticInvalidValuePolicy invalidValuePolicy;

  private StaticConfigStoreOptions(Builder builder) {
    updateComments = builder.updateComments;
    unknownKeyPolicy = Objects.requireNonNull(builder.unknownKeyPolicy, "unknownKeyPolicy");
    listStrategy = Objects.requireNonNull(builder.listStrategy, "listStrategy");
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

  /// Returns whether generated comments refresh existing comments during update.
  ///
  /// @return comment update policy
  public boolean updateComments() {
    return updateComments;
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
      .updateComments(updateComments)
      .removeUnknown(unknownKeyPolicy == StaticUnknownKeyPolicy.DROP)
      .listStrategy(listStrategy)
      .build();
  }

  /// Builder for [StaticConfigStoreOptions].
  public static final class Builder {
    private boolean updateComments = true;
    private StaticUnknownKeyPolicy unknownKeyPolicy = StaticUnknownKeyPolicy.PRESERVE;
    private MergeListStrategy listStrategy = MergeListStrategy.PRESERVE_EXISTING;
    private StaticInvalidValuePolicy invalidValuePolicy = StaticInvalidValuePolicy.STRICT;

    private Builder() {
    }

    /// Sets whether generated comments refresh existing comments during update.
    ///
    /// @param updateComments comment update policy
    /// @return this builder
    public Builder updateComments(boolean updateComments) {
      this.updateComments = updateComments;
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
