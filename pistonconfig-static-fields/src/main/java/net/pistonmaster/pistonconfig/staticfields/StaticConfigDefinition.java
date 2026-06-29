package net.pistonmaster.pistonconfig.staticfields;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.MergeOptions;

/// Collection of static [ConfigProperty] declarations.
///
/// A definition can be built from one or more holder classes that expose static
/// properties. Holder scanning includes inherited fields in parent-to-child
/// order and preserves grouped declaration order.
public final class StaticConfigDefinition {
  private final List<ConfigProperty<?>> properties;
  private final Map<ConfigPath, net.pistonmaster.pistonconfig.core.ConfigComment> comments;
  private final net.pistonmaster.pistonconfig.core.ConfigComment rootComment;

  private StaticConfigDefinition(
    List<ConfigProperty<?>> properties,
    Map<ConfigPath, net.pistonmaster.pistonconfig.core.ConfigComment> comments,
    net.pistonmaster.pistonconfig.core.ConfigComment rootComment
  ) {
    this.properties = List.copyOf(properties);
    this.comments = Collections.unmodifiableMap(new LinkedHashMap<>(comments));
    this.rootComment = Objects.requireNonNull(rootComment, "rootComment");
  }

  /// Returns property declarations in grouped declaration order.
  ///
  /// @return property declarations
  public List<ConfigProperty<?>> properties() {
    return properties;
  }

  /// Returns generated section comments.
  ///
  /// @return generated comments by path
  public Map<ConfigPath, net.pistonmaster.pistonconfig.core.ConfigComment> comments() {
    return comments;
  }

  /// Returns root comments.
  ///
  /// @return root comments
  public net.pistonmaster.pistonconfig.core.ConfigComment rootComment() {
    return rootComment;
  }

  /// Creates a builder for static config definitions.
  ///
  /// @return definition builder
  public static Builder builder() {
    return new Builder();
  }

  /// Reads all static [ConfigProperty] fields from holder classes.
  ///
  /// @param holderTypes classes containing static property fields
  /// @return static config definition
  public static StaticConfigDefinition from(Class<?>... holderTypes) {
    Objects.requireNonNull(holderTypes, "holderTypes");
    return from(Arrays.asList(holderTypes));
  }

  /// Reads all static [ConfigProperty] fields from holder classes.
  ///
  /// @param holderTypes classes containing static property fields
  /// @return static config definition
  public static StaticConfigDefinition from(Iterable<Class<?>> holderTypes) {
    Objects.requireNonNull(holderTypes, "holderTypes");
    var collector = new PropertyCollector();
    var comments = new StaticConfigCommentRegistry();

    for (Class<?> holderType : holderTypes) {
      collectHolder(Objects.requireNonNull(holderType, "holderType"), collector, comments);
    }

    var collectedComments = new LinkedHashMap<>(comments.comments());
    return new StaticConfigDefinition(collector.create(), collectedComments, comments.rootComment());
  }

  /// Builds a document containing every declared default value.
  ///
  /// @param codecRegistry registry used to encode default values
  /// @return defaults document
  public ConfigDocument defaults(ConfigCodecRegistry codecRegistry) {
    Objects.requireNonNull(codecRegistry, "codecRegistry");

    var document = ConfigDocument.empty();
    document.root().setComment(rootComment);

    for (ConfigProperty<?> property : properties) {
      setDefault(document, property, codecRegistry);
    }

    for (var entry : comments.entrySet()) {
      var target = document.root().getOrCreate(entry.getKey());
      if (target.comment().isEmpty()) {
        target.setComment(entry.getValue());
      }
    }

    return document;
  }

  /// Merges declared defaults into an existing document.
  ///
  /// @param document target document
  /// @param codecRegistry registry used to encode default values
  /// @return the same document for chaining
  public ConfigDocument applyDefaults(ConfigDocument document, ConfigCodecRegistry codecRegistry) {
    return applyDefaults(document, codecRegistry, MergeOptions.conservative());
  }

  /// Merges declared defaults into an existing document.
  ///
  /// @param document target document
  /// @param codecRegistry registry used to encode default values
  /// @param options merge behavior
  /// @return the same document for chaining
  public ConfigDocument applyDefaults(ConfigDocument document, ConfigCodecRegistry codecRegistry, MergeOptions options) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(options, "options");
    document.mergeDefaults(defaults(codecRegistry), options);
    return document;
  }

  /// Reads one declared property from a document.
  ///
  /// This method is strict for present values: invalid values throw
  /// [ConfigException]. Missing values return the property default.
  ///
  /// @param document source document
  /// @param property property declaration
  /// @param codecRegistry registry used to decode the stored value
  /// @param <T> value type
  /// @return decoded value, or the property default when the path is missing
  public <T> T get(ConfigDocument document, ConfigProperty<T> property, ConfigCodecRegistry codecRegistry) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(property, "property");
    Objects.requireNonNull(codecRegistry, "codecRegistry");

    return document.find(property.path())
      .map(node -> decode(codecRegistry, node, property))
      .orElse(property.defaultValue());
  }

  /// Reads one property and reports whether the source should be rewritten.
  ///
  /// @param document source document
  /// @param property property declaration
  /// @param codecRegistry registry used to decode the stored value
  /// @param invalidValuePolicy invalid value behavior
  /// @param <T> value type
  /// @return read result
  public <T> StaticConfigValue<T> resolve(
    ConfigDocument document,
    ConfigProperty<T> property,
    ConfigCodecRegistry codecRegistry,
    StaticInvalidValuePolicy invalidValuePolicy
  ) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(property, "property");
    Objects.requireNonNull(codecRegistry, "codecRegistry");
    Objects.requireNonNull(invalidValuePolicy, "invalidValuePolicy");

    var node = document.find(property.path());
    if (node.isEmpty()) {
      return StaticConfigValue.missing(property);
    }

    try {
      return StaticConfigValue.valid(property, decode(codecRegistry, node.orElseThrow(), property));
    } catch (ConfigException exception) {
      if (invalidValuePolicy == StaticInvalidValuePolicy.STRICT) {
        throw exception;
      }
      return StaticConfigValue.invalid(property, exception.getMessage());
    } catch (RuntimeException exception) {
      if (invalidValuePolicy == StaticInvalidValuePolicy.STRICT) {
        throw exception;
      }
      return StaticConfigValue.invalid(property, exception.getMessage());
    }
  }

  /// Writes a property value to a document.
  ///
  /// @param document target document
  /// @param property property declaration
  /// @param value value to write
  /// @param codecRegistry registry used to encode the value
  /// @param <T> value type
  /// @return the same document for chaining
  public <T> ConfigDocument set(ConfigDocument document, ConfigProperty<T> property, T value, ConfigCodecRegistry codecRegistry) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(property, "property");
    Objects.requireNonNull(value, "value");
    Objects.requireNonNull(codecRegistry, "codecRegistry");

    var node = property.type().encode(value, codecRegistry);
    node.setComment(property.comment());
    document.setNode(property.path(), node);
    return document;
  }

  void rewriteInvalidValues(ConfigDocument document, ConfigCodecRegistry codecRegistry, StaticInvalidValuePolicy invalidValuePolicy) {
    for (ConfigProperty<?> property : properties) {
      rewriteInvalidValue(document, codecRegistry, invalidValuePolicy, property);
    }
  }

  private <T> void rewriteInvalidValue(
    ConfigDocument document,
    ConfigCodecRegistry codecRegistry,
    StaticInvalidValuePolicy invalidValuePolicy,
    ConfigProperty<T> property
  ) {
    var value = resolve(document, property, codecRegistry, invalidValuePolicy);
    if (value.requiresRewrite()) {
      setDefault(document, property, codecRegistry);
    }
  }

  private <T> void setDefault(ConfigDocument document, ConfigProperty<T> property, ConfigCodecRegistry codecRegistry) {
    set(document, property, property.defaultValue(), codecRegistry);
  }

  private static <T> T decode(ConfigCodecRegistry codecRegistry, ConfigNode node, ConfigProperty<T> property) {
    return property.type().decode(node, codecRegistry);
  }

  private static void collectHolder(Class<?> holderType, PropertyCollector collector, StaticConfigCommentRegistry comments) {
    for (Field field : fieldsToProcess(holderType)) {
      collectPropertyField(field, collector);
    }
    collectAnnotatedComments(holderType, collector);
    collectHolderComments(holderType, comments);
  }

  private static void collectPropertyField(Field field, PropertyCollector collector) {
    if (!Modifier.isStatic(field.getModifiers()) || !ConfigProperty.class.isAssignableFrom(field.getType())) {
      return;
    }

    try {
      field.setAccessible(true);
      var property = (ConfigProperty<?>) field.get(null);
      if (property == null) {
        throw new ConfigException("Static config property " + field.getName() + " is null.");
      }
      collector.add(property);
    } catch (IllegalAccessException exception) {
      throw new ConfigException("Could not read static config property " + field.getName() + ".", exception);
    }
  }

  private static void collectAnnotatedComments(Class<?> holderType, PropertyCollector collector) {
    for (Field field : fieldsToProcess(holderType)) {
      var annotation = field.getAnnotation(ConfigComment.class);
      if (annotation == null || !Modifier.isStatic(field.getModifiers()) || !ConfigProperty.class.isAssignableFrom(field.getType())) {
        continue;
      }

      try {
        field.setAccessible(true);
        var property = (ConfigProperty<?>) field.get(null);
        if (property != null && property.comment().isEmpty()) {
          collector.replace(property, property.withComment(annotation.value()));
        }
      } catch (IllegalAccessException exception) {
        throw new ConfigException("Could not read static config property " + field.getName() + ".", exception);
      }
    }
  }

  private static void collectHolderComments(Class<?> holderType, StaticConfigCommentRegistry comments) {
    if (!StaticConfigComments.class.isAssignableFrom(holderType)) {
      return;
    }

    var holder = (StaticConfigComments) instantiate(holderType);
    holder.registerComments(comments);
  }

  private static Object instantiate(Class<?> holderType) {
    try {
      Constructor<?> constructor = holderType.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (NoSuchMethodException exception) {
      throw new ConfigException("Expected no-args constructor for " + holderType.getName() + ".", exception);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
      throw new ConfigException("Could not instantiate " + holderType.getName() + ".", exception);
    }
  }

  private static List<Field> fieldsToProcess(Class<?> holderType) {
    var hierarchy = new ArrayList<Class<?>>();
    var current = holderType;
    while (current != null && current != Object.class) {
      hierarchy.add(current);
      current = current.getSuperclass();
    }
    Collections.reverse(hierarchy);

    var fields = new ArrayList<Field>();
    for (Class<?> type : hierarchy) {
      fields.addAll(Arrays.asList(type.getDeclaredFields()));
    }
    return fields;
  }

  /// Builder for [StaticConfigDefinition].
  public static final class Builder {
    private final PropertyCollector collector = new PropertyCollector();
    private final Map<ConfigPath, net.pistonmaster.pistonconfig.core.ConfigComment> comments = new LinkedHashMap<>();
    private net.pistonmaster.pistonconfig.core.ConfigComment rootComment = net.pistonmaster.pistonconfig.core.ConfigComment.none();

    private Builder() {
    }

    /// Adds a property declaration.
    ///
    /// @param property property declaration
    /// @return this builder
    public Builder addProperty(ConfigProperty<?> property) {
      collector.add(property);
      return this;
    }

    /// Adds generated comments for a property or section path.
    ///
    /// @param path config path
    /// @param comment generated comment
    /// @return this builder
    public Builder comment(ConfigPath path, net.pistonmaster.pistonconfig.core.ConfigComment comment) {
      comments.put(Objects.requireNonNull(path, "path"), Objects.requireNonNull(comment, "comment"));
      return this;
    }

    /// Sets root comments.
    ///
    /// @param comment root comments
    /// @return this builder
    public Builder rootComment(net.pistonmaster.pistonconfig.core.ConfigComment comment) {
      rootComment = Objects.requireNonNull(comment, "comment");
      return this;
    }

    /// Builds the definition.
    ///
    /// @return static config definition
    public StaticConfigDefinition build() {
      return new StaticConfigDefinition(collector.create(), comments, rootComment);
    }
  }

  private static final class PropertyCollector {
    private final Entry root = new Entry();
    private final Map<ConfigPath, ConfigProperty<?>> byPath = new LinkedHashMap<>();

    void add(ConfigProperty<?> property) {
      Objects.requireNonNull(property, "property");
      if (byPath.containsKey(property.path())) {
        throw new ConfigException("Configuration path '" + property.path() + "' is declared more than once.");
      }

      var current = root;
      if (property.path().isRoot()) {
        if (root.property != null || !root.children.isEmpty()) {
          throw new ConfigException("Root configuration path conflicts with existing declarations.");
        }
        root.property = property;
        byPath.put(property.path(), property);
        return;
      }

      for (String segment : property.path().segments()) {
        if (current.property != null) {
          throw new ConfigException("Configuration path '" + property.path() + "' conflicts with parent path.");
        }
        current = current.children.computeIfAbsent(segment, _ -> new Entry());
      }

      if (current.property != null) {
        throw new ConfigException("Configuration path '" + property.path() + "' is declared more than once.");
      }
      if (!current.children.isEmpty()) {
        throw new ConfigException("Configuration path '" + property.path() + "' conflicts with child paths.");
      }

      current.property = property;
      byPath.put(property.path(), property);
    }

    void replace(ConfigProperty<?> oldProperty, ConfigProperty<?> newProperty) {
      Objects.requireNonNull(oldProperty, "oldProperty");
      Objects.requireNonNull(newProperty, "newProperty");
      if (!oldProperty.path().equals(newProperty.path())) {
        throw new ConfigException("Cannot replace static property with a different path.");
      }

      var entry = entry(oldProperty.path());
      if (entry.property.equals(oldProperty)) {
        entry.property = newProperty;
        byPath.put(newProperty.path(), newProperty);
      }
    }

    List<ConfigProperty<?>> create() {
      var result = new ArrayList<ConfigProperty<?>>();
      collect(root, result);
      return result;
    }

    private Entry entry(ConfigPath path) {
      var current = root;
      for (String segment : path.segments()) {
        current = current.children.get(segment);
        if (current == null) {
          throw new ConfigException("Unknown static property path '" + path + "'.");
        }
      }
      return current;
    }

    private static void collect(Entry entry, List<ConfigProperty<?>> result) {
      if (entry.property != null) {
        result.add(entry.property);
      }
      for (Entry child : entry.children.values()) {
        collect(child, result);
      }
    }
  }

  private static final class Entry {
    private ConfigProperty<?> property;
    private final Map<String, Entry> children = new LinkedHashMap<>();
  }
}
