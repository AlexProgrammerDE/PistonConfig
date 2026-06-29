package net.pistonmaster.pistonconfig.annotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigLoader;
import net.pistonmaster.pistonconfig.core.ConfigLoaders;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/// Format-agnostic store for one typed config model.
///
/// @param <T> config type
public final class ConfigStore<T> {
  private final Class<T> type;
  private final ConfigLoader loader;
  private final AnnotatedConfigMapper mapper;
  private final List<UnaryOperator<ConfigDocument>> readOverrides;
  private final List<ConfigPostProcessor<T>> postProcessors;
  private final List<ConfigValidator<T>> validators;

  private ConfigStore(Builder<T> builder) {
    type = builder.type;
    loader = Objects.requireNonNull(builder.loader, "A ConfigStore requires a format or loader.");
    mapper = new AnnotatedConfigMapper(builder.options);
    readOverrides = List.copyOf(builder.readOverrides);
    postProcessors = List.copyOf(builder.postProcessors);
    validators = List.copyOf(builder.validators);
  }

  /// Creates a store builder for a config type.
  ///
  /// @param type config type
  /// @param <T> config type
  /// @return store builder
  public static <T> Builder<T> builder(Class<T> type) {
    return new Builder<>(type);
  }

  /// Returns the config type handled by this store.
  ///
  /// @return config type
  public Class<T> type() {
    return type;
  }

  /// Returns the mapper used by this store.
  ///
  /// @return mapper
  public AnnotatedConfigMapper mapper() {
    return mapper;
  }

  /// Creates a default config instance.
  ///
  /// @return default config instance
  public T defaultConfig() {
    return mapper.createDefault(type);
  }

  /// Creates a defaults document.
  ///
  /// @return defaults document
  public ConfigDocument defaults() {
    return mapper.writeDefaults(type);
  }

  /// Reads a typed config from an existing document.
  ///
  /// Read overrides are applied to a copy and are not written back into the source document.
  ///
  /// @param document source document
  /// @return typed config
  public T read(ConfigDocument document) {
    Objects.requireNonNull(document, "document");
    return finishRead(mapper.read(applyReadOverrides(document), type));
  }

  /// Loads and reads a typed config from a path.
  ///
  /// @param path config path
  /// @return typed config
  public T load(Path path) {
    return read(ConfigLoaders.load(path, loader));
  }

  /// Saves a typed config to a path.
  ///
  /// @param path config path
  /// @param config config object
  public void save(Path path, T config) {
    Objects.requireNonNull(config, "config");
    ConfigLoaders.save(path, loader, mapper.write(config));
  }

  /// Creates or updates a config file with typed defaults, then reads it.
  ///
  /// Unknown-key, list, and comment behavior comes from [ConfigMapperOptions].
  /// Read overrides are used only for the returned config object and are not
  /// persisted to disk.
  ///
  /// @param path config path
  /// @return typed config
  public T update(Path path) {
    Objects.requireNonNull(path, "path");

    var defaults = defaults();
    var current = Files.exists(path) ? ConfigLoaders.load(path, loader) : ConfigDocument.empty();
    current.mergeDefaults(defaults, mapper.options().mergeOptions());
    ConfigLoaders.save(path, loader, current);

    return read(current);
  }

  /// Normalizes a typed config through mapper serialization while preserving unknown keys.
  ///
  /// This method is useful when callers want typed post-processing changes to be
  /// written back explicitly after calling [update].
  ///
  /// @param path config path
  /// @param config typed config
  public void rewrite(Path path, T config) {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(config, "config");

    var serialized = mapper.write(config);
    if (mapper.options().unknownKeyPolicy() == ConfigUnknownKeyPolicy.DROP || !Files.exists(path)) {
      ConfigLoaders.save(path, loader, serialized);
      return;
    }

    var current = ConfigLoaders.load(path, loader);
    overlayTypedValues(current, serialized);
    ConfigLoaders.save(path, loader, current);
  }

  private T finishRead(T config) {
    var processed = config;
    for (ConfigPostProcessor<T> postProcessor : postProcessors) {
      processed = Objects.requireNonNull(postProcessor.process(processed), "Config post-processors must not return null.");
    }
    for (ConfigValidator<T> validator : validators) {
      validator.validate(processed);
    }
    return processed;
  }

  private ConfigDocument applyReadOverrides(ConfigDocument document) {
    var copy = document.copy();
    for (UnaryOperator<ConfigDocument> readOverride : readOverrides) {
      copy = Objects.requireNonNull(readOverride.apply(copy), "Read overrides must not return null.");
    }
    return copy;
  }

  private static void overlayTypedValues(ConfigDocument target, ConfigDocument serialized) {
    for (ConfigPath path : leafPaths(serialized.root())) {
      var replacement = serialized.find(path).orElseThrow().copy();
      target.setNodePreservingSource(path, replacement);
    }
  }

  private static List<ConfigPath> leafPaths(ConfigNode node) {
    var paths = new ArrayList<ConfigPath>();
    collectLeafPaths(ConfigPath.root(), node, paths);
    return paths;
  }

  private static void collectLeafPaths(ConfigPath path, ConfigNode node, List<ConfigPath> paths) {
    if (!node.isObject() || node.objectChildren().isEmpty()) {
      paths.add(path);
      return;
    }

    for (var entry : node.objectChildren().entrySet()) {
      var child = path.isRoot() ? ConfigPath.of(entry.getKey()) : path.child(entry.getKey());
      collectLeafPaths(child, entry.getValue(), paths);
    }
  }

  /// Builder for [ConfigStore].
  ///
  /// @param <T> config type
  public static final class Builder<T> {
    private final Class<T> type;
    private ConfigLoader loader;
    private ConfigMapperOptions options = ConfigMapperOptions.defaults();
    private final List<UnaryOperator<ConfigDocument>> readOverrides = new ArrayList<>();
    private final List<ConfigPostProcessor<T>> postProcessors = new ArrayList<>();
    private final List<ConfigValidator<T>> validators = new ArrayList<>();

    private Builder(Class<T> type) {
      this.type = Objects.requireNonNull(type, "type");
    }

    /// Uses a format backend.
    ///
    /// @param format config format
    /// @return this builder
    public Builder<T> format(ConfigFormat format) {
      return loader(Objects.requireNonNull(format, "format").loader());
    }

    /// Uses a loader backend.
    ///
    /// @param loader config loader
    /// @return this builder
    public Builder<T> loader(ConfigLoader loader) {
      this.loader = Objects.requireNonNull(loader, "loader");
      return this;
    }

    /// Uses mapper options.
    ///
    /// @param options mapper options
    /// @return this builder
    public Builder<T> options(ConfigMapperOptions options) {
      this.options = Objects.requireNonNull(options, "options");
      return this;
    }

    /// Adds a read-only document override.
    ///
    /// Overrides are applied after defaults are merged and before typed reading.
    /// They are not persisted by update operations.
    ///
    /// @param readOverride read-only document override
    /// @return this builder
    public Builder<T> readOverride(UnaryOperator<ConfigDocument> readOverride) {
      readOverrides.add(Objects.requireNonNull(readOverride, "readOverride"));
      return this;
    }

    /// Adds a config post-processor.
    ///
    /// @param postProcessor post-processor
    /// @return this builder
    public Builder<T> postProcessor(ConfigPostProcessor<T> postProcessor) {
      postProcessors.add(Objects.requireNonNull(postProcessor, "postProcessor"));
      return this;
    }

    /// Adds a config validator.
    ///
    /// @param validator validator
    /// @return this builder
    public Builder<T> validator(ConfigValidator<T> validator) {
      validators.add(Objects.requireNonNull(validator, "validator"));
      return this;
    }

    /// Builds the store.
    ///
    /// @return config store
    public ConfigStore<T> build() {
      if (loader == null) {
        throw new ConfigException("A ConfigStore requires a format or loader.");
      }
      return new ConfigStore<>(this);
    }
  }
}
