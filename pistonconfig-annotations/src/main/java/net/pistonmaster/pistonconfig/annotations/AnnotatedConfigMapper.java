package net.pistonmaster.pistonconfig.annotations;

import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/// Maps annotation-based Java configs to and from [ConfigDocument].
///
/// The mapper supports records, POJOs with no-args constructors, inherited
/// fields, nested configs, arrays, lists, sets, maps, enums, scalar value types,
/// custom serializers, and polymorphic abstract/interface members.
public final class AnnotatedConfigMapper {
  private final ConfigMapperOptions options;
  private final ConfigTypeMapper typeMapper;

  /// Creates a mapper with default options.
  public AnnotatedConfigMapper() {
    this(ConfigMapperOptions.defaults());
  }

  /// Creates a mapper with caller-provided options.
  ///
  /// @param options mapper options
  public AnnotatedConfigMapper(ConfigMapperOptions options) {
    this.options = Objects.requireNonNull(options, "options");
    typeMapper = new ConfigTypeMapper(options);
  }

  /// Returns mapper options.
  ///
  /// @return mapper options
  public ConfigMapperOptions options() {
    return options;
  }

  /// Creates a default instance of a config type.
  ///
  /// Classes need a no-args constructor. Records use a no-args constructor when
  /// present, otherwise primitive defaults and `null` reference defaults.
  ///
  /// @param type config type
  /// @param <T> config type
  /// @return default config instance
  public <T> T createDefault(Class<T> type) {
    return typeMapper.newDefaultInstance(type);
  }

  /// Writes a default instance of a config type into a new document.
  ///
  /// @param type config type
  /// @param <T> config type
  /// @return default document
  public <T> ConfigDocument writeDefaults(Class<T> type) {
    return write(createDefault(type));
  }

  /// Writes the supplied config object into a new document.
  ///
  /// @param config config object
  /// @return document containing encoded config values
  public ConfigDocument write(Object config) {
    Objects.requireNonNull(config, "config");
    return ConfigDocument.of(typeMapper.encodeRoot(config));
  }

  /// Writes the supplied config object into an existing document, replacing its root.
  ///
  /// @param document target document
  /// @param config config object
  public void writeInto(ConfigDocument document, Object config) {
    Objects.requireNonNull(document, "document");
    document.setNode(ConfigPath.root(), typeMapper.encodeRoot(config));
  }

  /// Instantiates and reads a typed config from a document.
  ///
  /// @param document source document
  /// @param type config type
  /// @param <T> config type
  /// @return populated config object
  public <T> T read(ConfigDocument document, Class<T> type) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(type, "type");
    return typeMapper.decodeRoot(document.root(), type);
  }

  /// Reads matching document values into an existing config object.
  ///
  /// Records cannot be used with this method because they are immutable.
  ///
  /// @param document source document
  /// @param target config object
  public void readInto(ConfigDocument document, Object target) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(target, "target");
    typeMapper.decodeInto(document.root(), target);
  }
}
