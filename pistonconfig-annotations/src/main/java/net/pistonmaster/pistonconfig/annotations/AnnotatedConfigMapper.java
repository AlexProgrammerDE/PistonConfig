package net.pistonmaster.pistonconfig.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/// Maps annotation-based configuration objects to and from [ConfigDocument].
///
/// The mapper reads non-static, non-transient fields, honors field names and
/// comments from this package, and delegates value conversion to a
/// [ConfigCodecRegistry].
public final class AnnotatedConfigMapper {
  private final ConfigCodecRegistry codecRegistry;

  /// Creates a mapper with a fresh [ConfigCodecRegistry].
  public AnnotatedConfigMapper() {
    this(new ConfigCodecRegistry());
  }

  /// Creates a mapper with a caller-provided codec registry.
  ///
  /// @param codecRegistry registry used to encode and decode field values
  public AnnotatedConfigMapper(ConfigCodecRegistry codecRegistry) {
    this.codecRegistry = Objects.requireNonNull(codecRegistry, "codecRegistry");
  }

  /// Writes the current field values of a config object into a new document.
  ///
  /// @param config config object to inspect
  /// @return document containing encoded defaults
  public ConfigDocument writeDefaults(Object config) {
    Objects.requireNonNull(config, "config");
    var document = ConfigDocument.empty();
    writeInto(document, config);
    return document;
  }

  /// Writes the current field values of a config object into an existing document.
  ///
  /// @param document target document
  /// @param config config object to inspect
  public void writeInto(ConfigDocument document, Object config) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(config, "config");

    var prefix = prefix(config.getClass());
    for (Field field : mappedFields(config.getClass())) {
      var path = resolve(prefix, fieldPath(field));
      var node = codecRegistry.encode(readField(field, config));
      var comment = field.getAnnotation(ConfigComment.class);
      if (comment != null) {
        node.setComment(net.pistonmaster.pistonconfig.core.ConfigComment.builder()
          .addAllLeading(Arrays.stream(comment.value())
            .map(line -> ConfigCommentLine.builder()
              .text(line)
              .type(line.isEmpty() ? ConfigCommentType.BLANK : ConfigCommentType.BLOCK)
              .marker(line.isEmpty() ? ConfigCommentMarker.NONE : ConfigCommentMarker.HASH)
              .build())
            .toList())
          .build());
      }
      document.setNode(path, node);
    }
  }

  /// Instantiates a config type and reads matching values from a document.
  ///
  /// @param document source document
  /// @param type config class with a no-args constructor
  /// @param <T> config type
  /// @return populated config instance
  public <T> T read(ConfigDocument document, Class<T> type) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(type, "type");

    var instance = instantiate(type);
    readInto(document, instance);
    return instance;
  }

  /// Reads matching values from a document into an existing config object.
  ///
  /// @param document source document
  /// @param target config object to mutate
  public void readInto(ConfigDocument document, Object target) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(target, "target");

    var prefix = prefix(target.getClass());
    for (Field field : mappedFields(target.getClass())) {
      var path = resolve(prefix, fieldPath(field));
      document.find(path).ifPresent(node -> writeField(field, target, codecRegistry.decode(node, field.getType())));
    }
  }

  private static ConfigPath prefix(Class<?> type) {
    var prefix = type.getAnnotation(ConfigPathPrefix.class);
    return prefix == null || prefix.value().isBlank() ? ConfigPath.root() : ConfigPath.parse(prefix.value());
  }

  private static ConfigPath fieldPath(Field field) {
    var name = field.getAnnotation(ConfigName.class);
    return ConfigPath.parse(name == null ? field.getName() : name.value());
  }

  private static ConfigPath resolve(ConfigPath prefix, ConfigPath path) {
    var resolved = prefix;
    for (String segment : path.segments()) {
      resolved = resolved.isRoot() ? ConfigPath.of(segment) : resolved.child(segment);
    }
    return resolved;
  }

  private static List<Field> mappedFields(Class<?> type) {
    var fields = new ArrayList<Field>();
    Class<?> current = type;
    while (current != null && current != Object.class) {
      fields.addAll(Arrays.asList(current.getDeclaredFields()));
      current = current.getSuperclass();
    }

    return fields.stream()
      .filter(field -> !field.isAnnotationPresent(ConfigIgnore.class))
      .filter(field -> !Modifier.isStatic(field.getModifiers()))
      .filter(field -> !Modifier.isTransient(field.getModifiers()))
      .toList();
  }

  private static Object readField(Field field, Object target) {
    try {
      field.setAccessible(true);
      return field.get(target);
    } catch (IllegalAccessException exception) {
      throw new ConfigException("Could not read field " + field.getName() + ".", exception);
    }
  }

  private static void writeField(Field field, Object target, Object value) {
    try {
      field.setAccessible(true);
      field.set(target, value);
    } catch (IllegalAccessException exception) {
      throw new ConfigException("Could not write field " + field.getName() + ".", exception);
    }
  }

  private static <T> T instantiate(Class<T> type) {
    try {
      Constructor<T> constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (ReflectiveOperationException exception) {
      throw new ConfigException("Could not instantiate " + type.getName() + ". A no-args constructor is required.", exception);
    }
  }
}
