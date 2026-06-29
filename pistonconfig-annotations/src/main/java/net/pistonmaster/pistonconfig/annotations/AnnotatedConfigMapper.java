package net.pistonmaster.pistonconfig.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/**
 * Maps annotation based configuration objects to and from {@link ConfigDocument}.
 */
public final class AnnotatedConfigMapper {
  private final ConfigCodecRegistry codecRegistry;

  public AnnotatedConfigMapper() {
    this(new ConfigCodecRegistry());
  }

  public AnnotatedConfigMapper(ConfigCodecRegistry codecRegistry) {
    this.codecRegistry = Objects.requireNonNull(codecRegistry, "codecRegistry");
  }

  public ConfigDocument writeDefaults(Object config) {
    Objects.requireNonNull(config, "config");
    var document = ConfigDocument.empty();
    writeInto(document, config);
    return document;
  }

  public void writeInto(ConfigDocument document, Object config) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(config, "config");

    var prefix = prefix(config.getClass());
    for (Field field : mappedFields(config.getClass())) {
      var path = resolve(prefix, fieldPath(field));
      var node = codecRegistry.encode(readField(field, config));
      var comment = field.getAnnotation(ConfigComment.class);
      if (comment != null) {
        node.setComment(new net.pistonmaster.pistonconfig.core.ConfigComment(List.of(comment.value()), ""));
      }
      document.setNode(path, node);
    }
  }

  public <T> T read(ConfigDocument document, Class<T> type) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(type, "type");

    var instance = instantiate(type);
    readInto(document, instance);
    return instance;
  }

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
