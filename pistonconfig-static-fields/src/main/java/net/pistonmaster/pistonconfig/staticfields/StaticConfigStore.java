package net.pistonmaster.pistonconfig.staticfields;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigLoader;
import net.pistonmaster.pistonconfig.core.ConfigLoaders;

/// Format-agnostic store for static config property definitions.
public final class StaticConfigStore {
  private final StaticConfigDefinition definition;
  private final ConfigLoader loader;
  private final ConfigCodecRegistry codecRegistry;
  private final StaticConfigStoreOptions options;
  private final List<UnaryOperator<ConfigDocument>> migrations;
  private final List<UnaryOperator<ConfigDocument>> readOverrides;
  private final List<StaticConfigValidator> validators;

  private StaticConfigStore(Builder builder) {
    definition = Objects.requireNonNull(builder.definition, "A StaticConfigStore requires a definition or holder classes.");
    loader = Objects.requireNonNull(builder.loader, "A StaticConfigStore requires a format or loader.");
    codecRegistry = Objects.requireNonNull(builder.codecRegistry, "codecRegistry");
    options = Objects.requireNonNull(builder.options, "options");
    migrations = List.copyOf(builder.migrations);
    readOverrides = List.copyOf(builder.readOverrides);
    validators = List.copyOf(builder.validators);
  }

  /// Creates a store builder.
  ///
  /// @return store builder
  public static Builder builder() {
    return new Builder();
  }

  /// Returns the static config definition.
  ///
  /// @return definition
  public StaticConfigDefinition definition() {
    return definition;
  }

  /// Returns the codec registry.
  ///
  /// @return codec registry
  public ConfigCodecRegistry codecRegistry() {
    return codecRegistry;
  }

  /// Returns store options.
  ///
  /// @return options
  public StaticConfigStoreOptions options() {
    return options;
  }

  /// Creates a defaults document.
  ///
  /// @return defaults document
  public ConfigDocument defaults() {
    return definition.defaults(codecRegistry);
  }

  /// Loads a config file without modifying it.
  ///
  /// @param path config path
  /// @return stateful session
  public StaticConfigSession load(Path path) {
    Objects.requireNonNull(path, "path");
    var document = Files.exists(path) ? ConfigLoaders.load(path, loader) : ConfigDocument.empty();
    return finishSession(path, document);
  }

  /// Creates or updates a config file with static defaults, then returns a session.
  ///
  /// @param path config path
  /// @return stateful session
  public StaticConfigSession update(Path path) {
    Objects.requireNonNull(path, "path");

    var document = Files.exists(path) ? ConfigLoaders.load(path, loader) : ConfigDocument.empty();
    document = applyMigrations(document);
    document.mergeDefaults(defaults(), options.mergeOptions());
    definition.rewriteInvalidValues(document, codecRegistry, options.invalidValuePolicy());
    ConfigLoaders.save(path, loader, document);
    return finishSession(path, document);
  }

  /// Saves a document through the configured loader.
  ///
  /// @param path config path
  /// @param document document to save
  public void save(Path path, ConfigDocument document) {
    ConfigLoaders.save(path, loader, document);
  }

  /// Rewrites known static property values while preserving unknown keys unless
  /// the store is configured to drop them.
  ///
  /// @param path config path
  /// @param document source document
  public void rewrite(Path path, ConfigDocument document) {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(document, "document");

    var serialized = ConfigDocument.empty();
    for (ConfigProperty<?> property : definition.properties()) {
      rewriteKnownValue(document, serialized, property);
    }

    if (options.unknownKeyPolicy() == StaticUnknownKeyPolicy.DROP || !Files.exists(path)) {
      ConfigLoaders.save(path, loader, serialized);
      return;
    }

    var current = ConfigLoaders.load(path, loader);
    for (ConfigProperty<?> property : definition.properties()) {
      serialized.find(property.path())
        .ifPresent(node -> current.setNodePreservingSource(property.path(), node));
    }
    ConfigLoaders.save(path, loader, current);
  }

  <T> T get(ConfigDocument document, ConfigProperty<T> property) {
    return definition.resolve(applyReadOverrides(document), property, codecRegistry, options.invalidValuePolicy()).value();
  }

  <T> StaticConfigValue<T> resolve(ConfigDocument document, ConfigProperty<T> property) {
    return definition.resolve(applyReadOverrides(document), property, codecRegistry, options.invalidValuePolicy());
  }

  <T> void set(ConfigDocument document, ConfigProperty<T> property, T value) {
    definition.set(document, property, value, codecRegistry);
  }

  ConfigDocument reloadDocument(Path path) {
    return Files.exists(path) ? ConfigLoaders.load(path, loader) : ConfigDocument.empty();
  }

  void saveDocument(Path path, ConfigDocument document) {
    ConfigLoaders.save(path, loader, document);
  }

  void validate(StaticConfigSession session) {
    for (StaticConfigValidator validator : validators) {
      validator.validate(session);
    }
  }

  private StaticConfigSession finishSession(Path path, ConfigDocument document) {
    var session = new StaticConfigSession(this, path, document);
    validate(session);
    return session;
  }

  private ConfigDocument applyMigrations(ConfigDocument document) {
    var migrated = document;
    for (UnaryOperator<ConfigDocument> migration : migrations) {
      migrated = Objects.requireNonNull(migration.apply(migrated), "Migrations must not return null.");
    }
    return migrated;
  }

  private ConfigDocument applyReadOverrides(ConfigDocument document) {
    var copy = document.copy();
    for (UnaryOperator<ConfigDocument> readOverride : readOverrides) {
      copy = Objects.requireNonNull(readOverride.apply(copy), "Read overrides must not return null.");
    }
    return copy;
  }

  private <T> void rewriteKnownValue(ConfigDocument source, ConfigDocument target, ConfigProperty<T> property) {
    var value = definition.resolve(source, property, codecRegistry, options.invalidValuePolicy());
    definition.set(target, property, value.value(), codecRegistry);
  }

  /// Builder for [StaticConfigStore].
  public static final class Builder {
    private StaticConfigDefinition definition;
    private ConfigLoader loader;
    private ConfigCodecRegistry codecRegistry = new ConfigCodecRegistry();
    private StaticConfigStoreOptions options = StaticConfigStoreOptions.defaults();
    private final List<UnaryOperator<ConfigDocument>> migrations = new ArrayList<>();
    private final List<UnaryOperator<ConfigDocument>> readOverrides = new ArrayList<>();
    private final List<StaticConfigValidator> validators = new ArrayList<>();

    private Builder() {
    }

    /// Uses holder classes as the static definition source.
    ///
    /// @param holderTypes holder classes
    /// @return this builder
    public Builder holders(Class<?>... holderTypes) {
      definition = StaticConfigDefinition.from(holderTypes);
      return this;
    }

    /// Uses an existing static definition.
    ///
    /// @param definition static definition
    /// @return this builder
    public Builder definition(StaticConfigDefinition definition) {
      this.definition = Objects.requireNonNull(definition, "definition");
      return this;
    }

    /// Uses a format backend.
    ///
    /// @param format config format
    /// @return this builder
    public Builder format(ConfigFormat format) {
      return loader(Objects.requireNonNull(format, "format").loader());
    }

    /// Uses a loader backend.
    ///
    /// @param loader config loader
    /// @return this builder
    public Builder loader(ConfigLoader loader) {
      this.loader = Objects.requireNonNull(loader, "loader");
      return this;
    }

    /// Uses a codec registry.
    ///
    /// @param codecRegistry codec registry
    /// @return this builder
    public Builder codecRegistry(ConfigCodecRegistry codecRegistry) {
      this.codecRegistry = Objects.requireNonNull(codecRegistry, "codecRegistry");
      return this;
    }

    /// Uses store options.
    ///
    /// @param options store options
    /// @return this builder
    public Builder options(StaticConfigStoreOptions options) {
      this.options = Objects.requireNonNull(options, "options");
      return this;
    }

    /// Adds a document migration hook used during store updates.
    ///
    /// @param migration migration hook
    /// @return this builder
    public Builder migration(UnaryOperator<ConfigDocument> migration) {
      migrations.add(Objects.requireNonNull(migration, "migration"));
      return this;
    }

    /// Adds a read-only document override used by session reads.
    ///
    /// @param readOverride read override
    /// @return this builder
    public Builder readOverride(UnaryOperator<ConfigDocument> readOverride) {
      readOverrides.add(Objects.requireNonNull(readOverride, "readOverride"));
      return this;
    }

    /// Adds a session validator.
    ///
    /// @param validator validator
    /// @return this builder
    public Builder validator(StaticConfigValidator validator) {
      validators.add(Objects.requireNonNull(validator, "validator"));
      return this;
    }

    /// Builds the store.
    ///
    /// @return static config store
    public StaticConfigStore build() {
      if (definition == null) {
        throw new ConfigException("A StaticConfigStore requires a definition or holder classes.");
      }
      if (loader == null) {
        throw new ConfigException("A StaticConfigStore requires a format or loader.");
      }
      return new StaticConfigStore(this);
    }
  }
}
