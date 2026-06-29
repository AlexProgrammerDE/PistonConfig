package net.pistonmaster.pistonconfig.annotations;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.MergeCommentStrategy;
import net.pistonmaster.pistonconfig.core.MergeListStrategy;
import net.pistonmaster.pistonconfig.core.MergeOptions;
import net.pistonmaster.pistonconfig.core.MergeValueStrategy;

/// Options used by annotation-based typed config mapping.
public final class ConfigMapperOptions {
  private final ConfigNameFormatter nameFormatter;
  private final boolean inputNulls;
  private final boolean outputNulls;
  private final MergeCommentStrategy commentStrategy;
  private final ConfigUnknownKeyPolicy unknownKeyPolicy;
  private final MergeListStrategy listStrategy;
  private final MergeValueStrategy valueStrategy;
  private final ConfigScalarCoercion scalarCoercion;
  private final Map<Class<?>, ConfigSerializer<?>> serializers;
  private final Map<Class<?>, ConfigSerializerFactory<?>> serializerFactories;

  private ConfigMapperOptions(Builder builder) {
    nameFormatter = Objects.requireNonNull(builder.nameFormatter, "nameFormatter");
    inputNulls = builder.inputNulls;
    outputNulls = builder.outputNulls;
    commentStrategy = Objects.requireNonNull(builder.commentStrategy, "commentStrategy");
    unknownKeyPolicy = Objects.requireNonNull(builder.unknownKeyPolicy, "unknownKeyPolicy");
    listStrategy = Objects.requireNonNull(builder.listStrategy, "listStrategy");
    valueStrategy = Objects.requireNonNull(builder.valueStrategy, "valueStrategy");
    scalarCoercion = Objects.requireNonNull(builder.scalarCoercion, "scalarCoercion");
    serializers = Map.copyOf(builder.serializers);
    serializerFactories = Map.copyOf(builder.serializerFactories);
  }

  /// Creates options with conservative defaults.
  ///
  /// @return default options
  public static ConfigMapperOptions defaults() {
    return builder().build();
  }

  /// Creates an options builder.
  ///
  /// @return options builder
  public static Builder builder() {
    return new Builder();
  }

  /// Returns the configured Java-name formatter.
  ///
  /// @return name formatter
  public ConfigNameFormatter nameFormatter() {
    return nameFormatter;
  }

  /// Returns whether explicit `null` values may overwrite typed values.
  ///
  /// @return input null policy
  public boolean inputNulls() {
    return inputNulls;
  }

  /// Returns whether typed `null` values are written to documents.
  ///
  /// @return output null policy
  public boolean outputNulls() {
    return outputNulls;
  }

  /// Returns how generated comments merge with existing document comments during update.
  ///
  /// @return comment merge strategy
  public MergeCommentStrategy commentStrategy() {
    return commentStrategy;
  }

  /// Returns how unknown keys are handled during typed update.
  ///
  /// @return unknown key policy
  public ConfigUnknownKeyPolicy unknownKeyPolicy() {
    return unknownKeyPolicy;
  }

  /// Returns list merge behavior used during typed update.
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

  /// Returns scalar coercion behavior.
  ///
  /// @return scalar coercion behavior
  public ConfigScalarCoercion scalarCoercion() {
    return scalarCoercion;
  }

  /// Converts mapper options to document merge options.
  ///
  /// @return merge options
  public MergeOptions mergeOptions() {
    return MergeOptions.builder()
      .commentStrategy(commentStrategy)
      .removeUnknown(unknownKeyPolicy == ConfigUnknownKeyPolicy.DROP)
      .listStrategy(listStrategy)
      .valueStrategy(valueStrategy)
      .build();
  }

  Map<Class<?>, ConfigSerializer<?>> serializers() {
    return serializers;
  }

  Map<Class<?>, ConfigSerializerFactory<?>> serializerFactories() {
    return serializerFactories;
  }

  /// Builder for [ConfigMapperOptions].
  public static final class Builder {
    private ConfigNameFormatter nameFormatter = ConfigNameFormatters.IDENTITY;
    private boolean inputNulls;
    private boolean outputNulls;
    private MergeCommentStrategy commentStrategy = MergeCommentStrategy.FILL_MISSING;
    private ConfigUnknownKeyPolicy unknownKeyPolicy = ConfigUnknownKeyPolicy.PRESERVE;
    private MergeListStrategy listStrategy = MergeListStrategy.PRESERVE_EXISTING;
    private MergeValueStrategy valueStrategy = MergeValueStrategy.REPLACE_INVALID;
    private ConfigScalarCoercion scalarCoercion = ConfigScalarCoercion.STRICT;
    private final Map<Class<?>, ConfigSerializer<?>> serializers = new LinkedHashMap<>();
    private final Map<Class<?>, ConfigSerializerFactory<?>> serializerFactories = new LinkedHashMap<>();

    private Builder() {
    }

    /// Sets the Java-name formatter.
    ///
    /// @param nameFormatter name formatter
    /// @return this builder
    public Builder nameFormatter(ConfigNameFormatter nameFormatter) {
      this.nameFormatter = Objects.requireNonNull(nameFormatter, "nameFormatter");
      return this;
    }

    /// Sets whether explicit `null` values may overwrite typed values.
    ///
    /// @param inputNulls input null policy
    /// @return this builder
    public Builder inputNulls(boolean inputNulls) {
      this.inputNulls = inputNulls;
      return this;
    }

    /// Sets whether typed `null` values are written to documents.
    ///
    /// @param outputNulls output null policy
    /// @return this builder
    public Builder outputNulls(boolean outputNulls) {
      this.outputNulls = outputNulls;
      return this;
    }

    /// Sets how generated comments merge with existing document comments during update.
    ///
    /// @param commentStrategy comment merge strategy
    /// @return this builder
    public Builder commentStrategy(MergeCommentStrategy commentStrategy) {
      this.commentStrategy = Objects.requireNonNull(commentStrategy, "commentStrategy");
      return this;
    }

    /// Sets unknown key behavior during typed update.
    ///
    /// @param unknownKeyPolicy unknown key policy
    /// @return this builder
    public Builder unknownKeyPolicy(ConfigUnknownKeyPolicy unknownKeyPolicy) {
      this.unknownKeyPolicy = Objects.requireNonNull(unknownKeyPolicy, "unknownKeyPolicy");
      return this;
    }

    /// Sets list merge behavior during typed update.
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

    /// Sets scalar coercion behavior.
    ///
    /// @param scalarCoercion scalar coercion behavior
    /// @return this builder
    public Builder scalarCoercion(ConfigScalarCoercion scalarCoercion) {
      this.scalarCoercion = Objects.requireNonNull(scalarCoercion, "scalarCoercion");
      return this;
    }

    /// Registers a serializer for a raw Java type.
    ///
    /// @param type Java type
    /// @param serializer serializer
    /// @param <T> Java type
    /// @return this builder
    public <T> Builder serializer(Class<T> type, ConfigSerializer<? super T> serializer) {
      serializers.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(serializer, "serializer"));
      return this;
    }

    /// Registers a serializer factory for a raw Java type.
    ///
    /// @param type Java type
    /// @param serializerFactory serializer factory
    /// @param <T> Java type
    /// @return this builder
    public <T> Builder serializerFactory(Class<T> type, ConfigSerializerFactory<? super T> serializerFactory) {
      serializerFactories.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(serializerFactory, "serializerFactory"));
      return this;
    }

    /// Builds mapper options.
    ///
    /// @return mapper options
    public ConfigMapperOptions build() {
      return new ConfigMapperOptions(this);
    }
  }
}
