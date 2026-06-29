package net.pistonmaster.pistonconfig.annotations;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;

final class ConfigTypeMapper {
  private static final Set<Class<?>> INTEGER_TYPES = Set.of(
    byte.class, Byte.class,
    short.class, Short.class,
    int.class, Integer.class,
    long.class, Long.class
  );
  private static final Set<Class<?>> FLOATING_TYPES = Set.of(float.class, Float.class, double.class, Double.class);
  private static final Set<Class<?>> STRING_VALUE_TYPES = Set.of(
    LocalDate.class,
    LocalTime.class,
    LocalDateTime.class,
    Instant.class,
    Duration.class,
    Period.class,
    UUID.class,
    File.class,
    Path.class,
    URL.class,
    URI.class
  );

  private final ConfigMapperOptions options;

  ConfigTypeMapper(ConfigMapperOptions options) {
    this.options = Objects.requireNonNull(options, "options");
  }

  ConfigNode encodeRoot(Object value) {
    Objects.requireNonNull(value, "value");
    return encodeObject(value, value.getClass(), true);
  }

  <T> T decodeRoot(ConfigNode node, Class<T> type) {
    return type.cast(decodeObject(node, type, true));
  }

  void decodeInto(ConfigNode node, Object target) {
    Objects.requireNonNull(target, "target");
    readIntoObject(node, target, target.getClass(), true);
  }

  <T> T newDefaultInstance(Class<T> type) {
    Objects.requireNonNull(type, "type");
    if (type.isRecord()) {
      return newRecordInstance(type);
    }
    return instantiateClass(type);
  }

  ConfigNode encodeValue(Object value, AnnotatedType type) {
    return encodeValue(value, type, null, 0);
  }

  Object decodeValue(ConfigNode node, AnnotatedType type) {
    return decodeValue(node, type, null, 0);
  }

  private ConfigNode encodeValue(Object value, AnnotatedType annotatedType, ConfigMember member, int nesting) {
    Objects.requireNonNull(annotatedType, "annotatedType");
    if (value == null) {
      return options.outputNulls() ? ConfigNode.nullValue() : null;
    }

    var serializer = selectSerializer(annotatedType, member, nesting);
    if (serializer != null) {
      return encodeWithSerializer(serializer, value, context(annotatedType, nesting));
    }

    var type = annotatedType.getType();
    var rawType = rawClass(type);
    var polymorphic = rawType.getAnnotation(ConfigPolymorphic.class);
    if (polymorphic != null) {
      return encodePolymorphic(value, rawType, polymorphic);
    }

    if (rawType == String.class) {
      return ConfigNode.scalar(value.toString());
    }
    if (rawType == boolean.class || rawType == Boolean.class) {
      return ConfigNode.scalar(requireType(value, Boolean.class, rawType));
    }
    if (rawType == char.class || rawType == Character.class) {
      return ConfigNode.scalar(String.valueOf(requireType(value, Character.class, rawType)));
    }
    if (INTEGER_TYPES.contains(rawType) || FLOATING_TYPES.contains(rawType) || rawType == BigInteger.class || rawType == BigDecimal.class) {
      return ConfigNode.scalar(value);
    }
    if (rawType.isEnum()) {
      return ConfigNode.scalar(((Enum<?>) value).name());
    }
    if (STRING_VALUE_TYPES.contains(rawType)) {
      return ConfigNode.scalar(value.toString());
    }
    if (rawType.isArray()) {
      return encodeArray(value, annotatedType, member, nesting);
    }
    if (List.class.isAssignableFrom(rawType)) {
      return encodeCollection((Iterable<?>) value, parameterizedArgument(annotatedType, 0), member, nesting);
    }
    if (Set.class.isAssignableFrom(rawType)) {
      return encodeCollection((Iterable<?>) value, parameterizedArgument(annotatedType, 0), member, nesting);
    }
    if (Map.class.isAssignableFrom(rawType)) {
      return encodeMap((Map<?, ?>) value, annotatedType, member, nesting);
    }
    if (isConfigObject(rawType)) {
      return encodeObject(value, value.getClass(), false);
    }

    throw new ConfigException("No configuration serializer for type " + type.getTypeName() + ".");
  }

  private Object decodeValue(ConfigNode node, AnnotatedType annotatedType, ConfigMember member, int nesting) {
    Objects.requireNonNull(node, "node");
    Objects.requireNonNull(annotatedType, "annotatedType");

    var type = annotatedType.getType();
    var rawType = rawClass(type);
    if (node.kind() == net.pistonmaster.pistonconfig.core.ConfigValueKind.NULL) {
      if (!options.inputNulls()) {
        throw new ConfigException("Null value is disabled for type " + type.getTypeName() + ".");
      }
      requireNullable(rawType);
      return null;
    }

    var serializer = selectSerializer(annotatedType, member, nesting);
    if (serializer != null) {
      return decodeWithSerializer(serializer, node, context(annotatedType, nesting));
    }

    var polymorphic = rawType.getAnnotation(ConfigPolymorphic.class);
    if (polymorphic != null) {
      return decodePolymorphic(node, rawType, polymorphic);
    }

    if (rawType == String.class) {
      return decodeString(node, rawType);
    }
    if (rawType == boolean.class || rawType == Boolean.class) {
      return decodeBoolean(node, rawType);
    }
    if (rawType == char.class || rawType == Character.class) {
      return decodeCharacter(node, rawType);
    }
    if (rawType == byte.class || rawType == Byte.class) {
      return decodeInteger(node, rawType).byteValueExact();
    }
    if (rawType == short.class || rawType == Short.class) {
      return decodeInteger(node, rawType).shortValueExact();
    }
    if (rawType == int.class || rawType == Integer.class) {
      return decodeInteger(node, rawType).intValueExact();
    }
    if (rawType == long.class || rawType == Long.class) {
      return decodeInteger(node, rawType).longValueExact();
    }
    if (rawType == float.class || rawType == Float.class) {
      return decodeDecimal(node, rawType).floatValue();
    }
    if (rawType == double.class || rawType == Double.class) {
      return decodeDecimal(node, rawType).doubleValue();
    }
    if (rawType == BigInteger.class) {
      return decodeInteger(node, rawType);
    }
    if (rawType == BigDecimal.class) {
      return decodeDecimal(node, rawType);
    }
    if (rawType.isEnum()) {
      return decodeEnum(node, rawType);
    }
    if (STRING_VALUE_TYPES.contains(rawType)) {
      return decodeStringValue(node, rawType);
    }
    if (rawType.isArray()) {
      return decodeArray(node, annotatedType, rawType, member, nesting);
    }
    if (List.class.isAssignableFrom(rawType)) {
      return decodeList(node, parameterizedArgument(annotatedType, 0), member, nesting);
    }
    if (Set.class.isAssignableFrom(rawType)) {
      return decodeSet(node, parameterizedArgument(annotatedType, 0), member, nesting);
    }
    if (Map.class.isAssignableFrom(rawType)) {
      return decodeMap(node, annotatedType, member, nesting);
    }
    if (isConfigObject(rawType)) {
      return decodeObject(node, rawType, false);
    }

    throw new ConfigException("No configuration serializer for type " + type.getTypeName() + ".");
  }

  private ConfigNode encodeObject(Object value, Class<?> type, boolean root) {
    if (!isConfigObject(type)) {
      throw new ConfigException("Type " + type.getName() + " is not a supported config object.");
    }

    var node = ConfigNode.object();
    var typeComment = type.getAnnotation(ConfigComment.class);
    if (typeComment != null) {
      node.setComment(comment(typeComment.value()));
    }

    for (ConfigMember member : members(type, root)) {
      var memberValue = member.get(value);
      var child = encodeValue(memberValue, member.annotatedType(), member, 0);
      if (child == null) {
        continue;
      }

      var comment = member.annotation(ConfigComment.class);
      if (comment != null) {
        child.setComment(comment(comment.value()));
      }
      node.setNode(member.path(options), child);
    }

    return node;
  }

  private Object decodeObject(ConfigNode node, Class<?> type, boolean root) {
    requireObject(node, type);
    if (type.isRecord()) {
      return decodeRecord(node, type, root);
    }

    var instance = instantiateClass(type);
    readIntoObject(node, instance, type, root);
    return instance;
  }

  private void readIntoObject(ConfigNode node, Object target, Class<?> type, boolean root) {
    requireObject(node, type);
    for (ConfigMember member : members(type, root)) {
      var child = node.find(member.path(options));
      if (child.isEmpty() || (child.get().kind() == net.pistonmaster.pistonconfig.core.ConfigValueKind.NULL && !options.inputNulls())) {
        continue;
      }

      var value = decodeValue(child.get(), member.annotatedType(), member, 0);
      if (value == null) {
        requireNullable(rawClass(member.annotatedType().getType()));
      }
      member.set(target, value);
    }
  }

  private Object decodeRecord(ConfigNode node, Class<?> type, boolean root) {
    var components = members(type, root);
    var defaultInstance = defaultRecordInstance(type);
    var args = new Object[components.size()];

    for (int index = 0; index < components.size(); index++) {
      var member = components.get(index);
      var child = node.find(member.path(options));
      if (child.isEmpty() || (child.get().kind() == net.pistonmaster.pistonconfig.core.ConfigValueKind.NULL && !options.inputNulls())) {
        args[index] = defaultRecordValue(defaultInstance, member);
        continue;
      }

      args[index] = decodeValue(child.get(), member.annotatedType(), member, 0);
      if (args[index] == null) {
        requireNullable(rawClass(member.annotatedType().getType()));
      }
    }

    return callCanonicalConstructor(type, args);
  }

  private ConfigNode encodeArray(Object value, AnnotatedType annotatedType, ConfigMember member, int nesting) {
    var arrayType = rawClass(annotatedType.getType());
    var elementType = arrayElementType(annotatedType, arrayType);
    var node = ConfigNode.list();
    int length = Array.getLength(value);
    for (int index = 0; index < length; index++) {
      var child = encodeValue(Array.get(value, index), elementType, member, nesting + 1);
      if (child != null) {
        node.addListNode(child);
      }
    }
    return node;
  }

  private ConfigNode encodeCollection(Iterable<?> values, AnnotatedType elementType, ConfigMember member, int nesting) {
    var node = ConfigNode.list();
    for (Object value : values) {
      var child = encodeValue(value, elementType, member, nesting + 1);
      if (child != null) {
        node.addListNode(child);
      }
    }
    return node;
  }

  private ConfigNode encodeMap(Map<?, ?> values, AnnotatedType annotatedType, ConfigMember member, int nesting) {
    var keyType = parameterizedArgument(annotatedType, 0);
    var valueType = parameterizedArgument(annotatedType, 1);
    requireMapKeyType(keyType);

    var node = ConfigNode.object();
    for (var entry : values.entrySet()) {
      if (entry.getKey() == null) {
        throw new ConfigException("Map keys cannot be null for type " + annotatedType.getType().getTypeName() + ".");
      }

      var child = encodeValue(entry.getValue(), valueType, member, nesting + 1);
      if (child != null) {
        node.setNode(ConfigPath.of(encodeMapKey(entry.getKey(), keyType)), child);
      }
    }
    return node;
  }

  private Object decodeArray(ConfigNode node, AnnotatedType annotatedType, Class<?> rawType, ConfigMember member, int nesting) {
    requireList(node, annotatedType.getType());
    var elementType = arrayElementType(annotatedType, rawType);
    var componentType = rawType.getComponentType();
    var children = node.listChildren();
    var array = Array.newInstance(componentType, children.size());

    for (int index = 0; index < children.size(); index++) {
      var child = children.get(index);
      if (child.kind() == net.pistonmaster.pistonconfig.core.ConfigValueKind.NULL && !options.inputNulls()) {
        continue;
      }
      var value = decodeValue(child, elementType, member, nesting + 1);
      Array.set(array, index, value);
    }
    return array;
  }

  private List<Object> decodeList(ConfigNode node, AnnotatedType elementType, ConfigMember member, int nesting) {
    requireList(node, elementType.getType());
    var result = new ArrayList<>();
    for (ConfigNode child : node.listChildren()) {
      if (child.kind() == net.pistonmaster.pistonconfig.core.ConfigValueKind.NULL && !options.inputNulls()) {
        continue;
      }
      result.add(decodeValue(child, elementType, member, nesting + 1));
    }
    return result;
  }

  private Set<Object> decodeSet(ConfigNode node, AnnotatedType elementType, ConfigMember member, int nesting) {
    requireList(node, elementType.getType());
    var result = new LinkedHashSet<>();
    for (ConfigNode child : node.listChildren()) {
      if (child.kind() == net.pistonmaster.pistonconfig.core.ConfigValueKind.NULL && !options.inputNulls()) {
        continue;
      }
      result.add(decodeValue(child, elementType, member, nesting + 1));
    }
    return result;
  }

  private Map<Object, Object> decodeMap(ConfigNode node, AnnotatedType annotatedType, ConfigMember member, int nesting) {
    requireObject(node, annotatedType.getType());
    var keyType = parameterizedArgument(annotatedType, 0);
    var valueType = parameterizedArgument(annotatedType, 1);
    requireMapKeyType(keyType);

    var result = new LinkedHashMap<>();
    for (var entry : node.objectChildren().entrySet()) {
      var child = entry.getValue();
      if (child.kind() == net.pistonmaster.pistonconfig.core.ConfigValueKind.NULL && !options.inputNulls()) {
        continue;
      }
      result.put(decodeMapKey(entry.getKey(), keyType), decodeValue(child, valueType, member, nesting + 1));
    }
    return result;
  }

  private ConfigNode encodePolymorphic(Object value, Class<?> declaredType, ConfigPolymorphic polymorphic) {
    var subtype = value.getClass();
    if (!declaredType.isAssignableFrom(subtype)) {
      throw new ConfigException("Type " + subtype.getName() + " is not assignable to polymorphic type " + declaredType.getName() + ".");
    }

    if (polymorphic.property().isBlank()) {
      throw new ConfigException("@ConfigPolymorphic property cannot be blank for " + declaredType.getName() + ".");
    }

    var payload = encodeObject(value, subtype, false);
    if (payload.objectChildren().containsKey(polymorphic.property())) {
      throw new ConfigException("Polymorphic property " + polymorphic.property() + " conflicts with a config member on " + subtype.getName() + ".");
    }

    var result = ConfigNode.object();
    result.setNode(ConfigPath.of(polymorphic.property()), ConfigNode.scalar(polymorphicIdentifier(declaredType, subtype)));
    payload.objectChildren().forEach((key, child) -> result.setNode(ConfigPath.of(key), child));
    return result;
  }

  private Object decodePolymorphic(ConfigNode node, Class<?> declaredType, ConfigPolymorphic polymorphic) {
    requireObject(node, declaredType);
    var typeNode = node.find(ConfigPath.of(polymorphic.property()))
      .orElseThrow(() -> new ConfigException("Missing polymorphic type property " + polymorphic.property() + " for " + declaredType.getName() + "."));
    var identifier = typeNode.asString()
      .orElseThrow(() -> new ConfigException("Polymorphic type property " + polymorphic.property() + " must be a string."));
    var subtype = polymorphicSubtype(declaredType, identifier);
    if (!declaredType.isAssignableFrom(subtype)) {
      throw new ConfigException("Polymorphic subtype " + subtype.getName() + " is not assignable to " + declaredType.getName() + ".");
    }
    return decodeObject(node, subtype, false);
  }

  @SuppressWarnings("unchecked")
  private ConfigSerializer<Object> selectSerializer(AnnotatedType annotatedType, ConfigMember member, int nesting) {
    var elementSerializer = member == null ? null : member.annotation(ConfigSerializeWith.class);
    if (elementSerializer != null && elementSerializer.nesting() == nesting) {
      return (ConfigSerializer<Object>) instantiateSerializer(elementSerializer.value(), annotatedType, nesting);
    }

    var rawType = rawClass(annotatedType.getType());
    var factory = options.serializerFactories().get(rawType);
    if (factory != null) {
      var serializer = factory.create(context(annotatedType, nesting));
      if (serializer == null) {
        throw new ConfigException("Serializer factories must not return null.");
      }
      return (ConfigSerializer<Object>) serializer;
    }

    var registered = options.serializers().get(rawType);
    if (registered != null) {
      return (ConfigSerializer<Object>) registered;
    }

    var typeSerializer = rawType.getAnnotation(ConfigSerializeWith.class);
    if (typeSerializer != null) {
      return (ConfigSerializer<Object>) instantiateSerializer(typeSerializer.value(), annotatedType, nesting);
    }

    for (Annotation annotation : rawType.getDeclaredAnnotations()) {
      var metaSerializer = annotation.annotationType().getAnnotation(ConfigSerializeWith.class);
      if (metaSerializer != null) {
        return (ConfigSerializer<Object>) instantiateSerializer(metaSerializer.value(), annotatedType, nesting);
      }
    }

    return null;
  }

  private ConfigSerializer<?> instantiateSerializer(Class<? extends ConfigSerializer<?>> serializerType, AnnotatedType annotatedType, int nesting) {
    try {
      Constructor<? extends ConfigSerializer<?>> contextConstructor = null;
      for (var constructor : serializerType.getDeclaredConstructors()) {
        if (constructor.getParameterCount() == 1 && constructor.getParameterTypes()[0] == ConfigSerializationContext.class) {
          @SuppressWarnings("unchecked")
          var cast = (Constructor<? extends ConfigSerializer<?>>) constructor;
          contextConstructor = cast;
          break;
        }
      }

      if (contextConstructor != null) {
        contextConstructor.setAccessible(true);
        return contextConstructor.newInstance(context(annotatedType, nesting));
      }

      var constructor = serializerType.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (ReflectiveOperationException exception) {
      throw new ConfigException("Could not instantiate serializer " + serializerType.getName() + ".", exception);
    }
  }

  private ConfigSerializationContext context(AnnotatedType annotatedType, int nesting) {
    return new Context(annotatedType, nesting);
  }

  @SuppressWarnings("unchecked")
  private static ConfigNode encodeWithSerializer(ConfigSerializer<Object> serializer, Object value, ConfigSerializationContext context) {
    return Objects.requireNonNull(serializer.encode(value, context), "Custom serializers must not return null nodes.");
  }

  private static Object decodeWithSerializer(ConfigSerializer<Object> serializer, ConfigNode node, ConfigSerializationContext context) {
    return serializer.decode(node, context);
  }

  private String decodeString(ConfigNode node, Class<?> targetType) {
    if (!node.isScalar()) {
      throw expectedScalar(targetType, node);
    }

    var raw = node.rawValue();
    if (raw instanceof String stringValue) {
      return stringValue;
    }
    if (options.scalarCoercion() == ConfigScalarCoercion.STRING) {
      return raw.toString();
    }
    throw new ConfigException("Expected string for " + targetType.getTypeName() + " but found " + raw.getClass().getName() + ".");
  }

  private Boolean decodeBoolean(ConfigNode node, Class<?> targetType) {
    if (!node.isScalar()) {
      throw expectedScalar(targetType, node);
    }
    if (node.rawValue() instanceof Boolean booleanValue) {
      return booleanValue;
    }
    if (options.scalarCoercion() == ConfigScalarCoercion.STRING && node.rawValue() instanceof String stringValue) {
      if ("true".equalsIgnoreCase(stringValue) || "false".equalsIgnoreCase(stringValue)) {
        return Boolean.parseBoolean(stringValue);
      }
    }
    throw new ConfigException("Expected boolean for " + targetType.getTypeName() + ".");
  }

  private Character decodeCharacter(ConfigNode node, Class<?> targetType) {
    var value = decodeStringForValue(node, targetType);
    if (value.length() != 1) {
      throw new ConfigException("Expected one character for " + targetType.getTypeName() + ".");
    }
    return value.charAt(0);
  }

  private BigInteger decodeInteger(ConfigNode node, Class<?> targetType) {
    if (!node.isScalar()) {
      throw expectedScalar(targetType, node);
    }

    var raw = node.rawValue();
    try {
      if (raw instanceof BigInteger integer) {
        return integer;
      }
      if (raw instanceof BigDecimal decimal) {
        return decimal.toBigIntegerExact();
      }
      if (raw instanceof Byte || raw instanceof Short || raw instanceof Integer || raw instanceof Long) {
        return BigInteger.valueOf(((Number) raw).longValue());
      }
      if (raw instanceof Float || raw instanceof Double) {
        return BigDecimal.valueOf(((Number) raw).doubleValue()).toBigIntegerExact();
      }
      if (options.scalarCoercion() == ConfigScalarCoercion.STRING && raw instanceof String stringValue) {
        return new BigInteger(stringValue);
      }
    } catch (ArithmeticException | NumberFormatException exception) {
      throw new ConfigException("Could not convert " + raw + " to " + targetType.getTypeName() + ".", exception);
    }

    throw new ConfigException("Expected integer for " + targetType.getTypeName() + ".");
  }

  private BigDecimal decodeDecimal(ConfigNode node, Class<?> targetType) {
    if (!node.isScalar()) {
      throw expectedScalar(targetType, node);
    }

    var raw = node.rawValue();
    try {
      if (raw instanceof BigDecimal decimal) {
        return decimal;
      }
      if (raw instanceof BigInteger integer) {
        return new BigDecimal(integer);
      }
      if (raw instanceof Number number) {
        return BigDecimal.valueOf(number.doubleValue());
      }
      if (options.scalarCoercion() == ConfigScalarCoercion.STRING && raw instanceof String stringValue) {
        return new BigDecimal(stringValue);
      }
    } catch (NumberFormatException exception) {
      throw new ConfigException("Could not convert " + raw + " to " + targetType.getTypeName() + ".", exception);
    }

    throw new ConfigException("Expected decimal number for " + targetType.getTypeName() + ".");
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object decodeEnum(ConfigNode node, Class<?> targetType) {
    var value = decodeStringForValue(node, targetType);
    try {
      return Enum.valueOf((Class<? extends Enum>) targetType, value);
    } catch (IllegalArgumentException exception) {
      throw new ConfigException("Unknown enum value " + value + " for " + targetType.getName() + ".", exception);
    }
  }

  private Object decodeStringValue(ConfigNode node, Class<?> targetType) {
    var value = decodeStringForValue(node, targetType);
    try {
      if (targetType == LocalDate.class) {
        return LocalDate.parse(value);
      }
      if (targetType == LocalTime.class) {
        return LocalTime.parse(value);
      }
      if (targetType == LocalDateTime.class) {
        return LocalDateTime.parse(value);
      }
      if (targetType == Instant.class) {
        return Instant.parse(value);
      }
      if (targetType == Duration.class) {
        return Duration.parse(value);
      }
      if (targetType == Period.class) {
        return Period.parse(value);
      }
      if (targetType == UUID.class) {
        return UUID.fromString(value);
      }
      if (targetType == File.class) {
        return new File(value);
      }
      if (targetType == Path.class) {
        return Path.of(value);
      }
      if (targetType == URL.class) {
        return URI.create(value).toURL();
      }
      if (targetType == URI.class) {
        return URI.create(value);
      }
    } catch (IllegalArgumentException | MalformedURLException exception) {
      throw new ConfigException("Could not convert " + value + " to " + targetType.getName() + ".", exception);
    }

    throw new ConfigException("Unsupported string value type " + targetType.getName() + ".");
  }

  private String decodeStringForValue(ConfigNode node, Class<?> targetType) {
    if (!node.isScalar()) {
      throw expectedScalar(targetType, node);
    }
    var raw = node.rawValue();
    if (raw instanceof String stringValue) {
      return stringValue;
    }
    if (options.scalarCoercion() == ConfigScalarCoercion.STRING) {
      return raw.toString();
    }
    throw new ConfigException("Expected string value for " + targetType.getTypeName() + ".");
  }

  private String encodeMapKey(Object key, AnnotatedType keyType) {
    var rawType = rawClass(keyType.getType());
    if (rawType.isEnum()) {
      return ((Enum<?>) key).name();
    }
    if (rawType == Character.class || rawType == char.class) {
      return String.valueOf(key);
    }
    return key.toString();
  }

  private Object decodeMapKey(String key, AnnotatedType keyType) {
    var rawType = rawClass(keyType.getType());
    try {
      if (rawType == String.class) {
        return key;
      }
      if (rawType == boolean.class || rawType == Boolean.class) {
        if ("true".equalsIgnoreCase(key) || "false".equalsIgnoreCase(key)) {
          return Boolean.parseBoolean(key);
        }
        throw new ConfigException("Invalid boolean map key " + key + ".");
      }
      if (rawType == char.class || rawType == Character.class) {
        if (key.length() == 1) {
          return key.charAt(0);
        }
        throw new ConfigException("Invalid character map key " + key + ".");
      }
      if (rawType == byte.class || rawType == Byte.class) {
        return Byte.valueOf(key);
      }
      if (rawType == short.class || rawType == Short.class) {
        return Short.valueOf(key);
      }
      if (rawType == int.class || rawType == Integer.class) {
        return Integer.valueOf(key);
      }
      if (rawType == long.class || rawType == Long.class) {
        return Long.valueOf(key);
      }
      if (rawType == float.class || rawType == Float.class) {
        return Float.valueOf(key);
      }
      if (rawType == double.class || rawType == Double.class) {
        return Double.valueOf(key);
      }
      if (rawType == BigInteger.class) {
        return new BigInteger(key);
      }
      if (rawType == BigDecimal.class) {
        return new BigDecimal(key);
      }
      if (rawType.isEnum()) {
        return decodeEnum(ConfigNode.scalar(key), rawType);
      }
      if (STRING_VALUE_TYPES.contains(rawType)) {
        return decodeStringValue(ConfigNode.scalar(key), rawType);
      }
    } catch (RuntimeException exception) {
      if (exception instanceof ConfigException configException) {
        throw configException;
      }
      throw new ConfigException("Could not decode map key " + key + " as " + rawType.getName() + ".", exception);
    }

    throw new ConfigException("Unsupported map key type " + rawType.getName() + ".");
  }

  private void requireMapKeyType(AnnotatedType keyType) {
    var rawType = rawClass(keyType.getType());
    if (rawType == String.class || rawType == boolean.class || rawType == Boolean.class ||
      rawType == char.class || rawType == Character.class || INTEGER_TYPES.contains(rawType) ||
      FLOATING_TYPES.contains(rawType) || rawType == BigInteger.class || rawType == BigDecimal.class ||
      rawType.isEnum() || STRING_VALUE_TYPES.contains(rawType)) {
      return;
    }
    throw new ConfigException("Map keys can only use scalar, string-value, or enum types. Found " + keyType.getType().getTypeName() + ".");
  }

  private AnnotatedType parameterizedArgument(AnnotatedType annotatedType, int index) {
    if (!(annotatedType instanceof AnnotatedParameterizedType parameterizedType)) {
      throw new ConfigException("Type " + annotatedType.getType().getTypeName() + " must declare generic arguments.");
    }

    var arguments = parameterizedType.getAnnotatedActualTypeArguments();
    if (index >= arguments.length) {
      throw new ConfigException("Type " + annotatedType.getType().getTypeName() + " does not declare argument " + index + ".");
    }
    rejectUnsupportedType(arguments[index].getType());
    return arguments[index];
  }

  private AnnotatedType arrayElementType(AnnotatedType annotatedType, Class<?> arrayType) {
    if (annotatedType instanceof AnnotatedArrayType annotatedArrayType) {
      return annotatedArrayType.getAnnotatedGenericComponentType();
    }
    return new SimpleAnnotatedType(arrayType.getComponentType());
  }

  private static List<ConfigMember> members(Class<?> type, boolean root) {
    if (type.isRecord()) {
      return Arrays.stream(type.getRecordComponents())
        .filter(component -> component.getAnnotation(ConfigIgnore.class) == null)
        .map(component -> ConfigMember.record(component, root))
        .toList();
    }

    var hierarchy = new ArrayList<Class<?>>();
    Class<?> current = type;
    while (current != null && current != Object.class) {
      hierarchy.addFirst(current);
      current = current.getSuperclass();
    }

    var members = new ArrayList<ConfigMember>();
    for (Class<?> currentType : hierarchy) {
      for (Field field : currentType.getDeclaredFields()) {
        int modifiers = field.getModifiers();
        if (field.isSynthetic() || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) ||
          Modifier.isFinal(modifiers) || field.isAnnotationPresent(ConfigIgnore.class)) {
          continue;
        }
        members.add(ConfigMember.field(field, root));
      }
    }
    return List.copyOf(members);
  }

  private static net.pistonmaster.pistonconfig.core.ConfigComment comment(String[] lines) {
    return net.pistonmaster.pistonconfig.core.ConfigComment.builder()
      .addAllLeading(Arrays.stream(lines)
        .map(line -> ConfigCommentLine.builder()
          .text(line)
          .type(line.isEmpty() ? ConfigCommentType.BLANK : ConfigCommentType.BLOCK)
          .marker(line.isEmpty() ? ConfigCommentMarker.NONE : ConfigCommentMarker.HASH)
          .build())
        .toList())
      .build();
  }

  private static boolean isConfigObject(Class<?> type) {
    return !type.isPrimitive() &&
      !type.isEnum() &&
      !type.isArray() &&
      type != Object.class &&
      !CharSequence.class.isAssignableFrom(type) &&
      !Number.class.isAssignableFrom(type) &&
      type != Boolean.class &&
      type != Character.class &&
      !Iterable.class.isAssignableFrom(type) &&
      !Map.class.isAssignableFrom(type) &&
      !STRING_VALUE_TYPES.contains(type);
  }

  private static Class<?> rawClass(Type type) {
    rejectUnsupportedType(type);
    if (type instanceof Class<?> typeClass) {
      return typeClass;
    }
    if (type instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() instanceof Class<?> rawType) {
      return rawType;
    }
    if (type instanceof GenericArrayType genericArrayType) {
      return Array.newInstance(rawClass(genericArrayType.getGenericComponentType()), 0).getClass();
    }
    throw new ConfigException("Unsupported type " + type.getTypeName() + ".");
  }

  private static void rejectUnsupportedType(Type type) {
    if (type instanceof WildcardType) {
      throw new ConfigException("Wildcard config types are not supported: " + type.getTypeName() + ".");
    }
    if (type instanceof TypeVariable<?>) {
      throw new ConfigException("Type variables are not supported in config types: " + type.getTypeName() + ".");
    }
  }

  private static <T> T instantiateClass(Class<T> type) {
    try {
      var constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (ReflectiveOperationException exception) {
      throw new ConfigException("Could not instantiate " + type.getName() + ". A no-args constructor is required.", exception);
    }
  }

  private static <T> T newRecordInstance(Class<T> type) {
    var defaultInstance = defaultRecordInstance(type);
    if (defaultInstance != null) {
      return type.cast(defaultInstance);
    }

    var components = type.getRecordComponents();
    var args = new Object[components.length];
    for (int index = 0; index < components.length; index++) {
      args[index] = defaultValue(components[index].getType());
    }
    return type.cast(callCanonicalConstructor(type, args));
  }

  private static Object defaultRecordInstance(Class<?> type) {
    try {
      var constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (NoSuchMethodException _) {
      return null;
    } catch (ReflectiveOperationException exception) {
      throw new ConfigException("Could not instantiate default record " + type.getName() + ".", exception);
    }
  }

  private static Object defaultRecordValue(Object defaultInstance, ConfigMember member) {
    if (defaultInstance != null) {
      return member.get(defaultInstance);
    }
    return defaultValue(rawClass(member.annotatedType().getType()));
  }

  private static Object callCanonicalConstructor(Class<?> type, Object[] args) {
    var componentTypes = Arrays.stream(type.getRecordComponents())
      .map(RecordComponent::getType)
      .toArray(Class<?>[]::new);
    try {
      var constructor = type.getDeclaredConstructor(componentTypes);
      constructor.setAccessible(true);
      return constructor.newInstance(args);
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException exception) {
      throw new ConfigException("Could not call canonical constructor for " + type.getName() + ".", exception);
    } catch (InvocationTargetException exception) {
      throw new ConfigException("Record constructor for " + type.getName() + " rejected configuration values.", exception.getCause());
    }
  }

  private static Object defaultValue(Class<?> type) {
    if (!type.isPrimitive()) {
      return null;
    }
    if (type == boolean.class) {
      return false;
    }
    if (type == char.class) {
      return '\0';
    }
    if (type == byte.class) {
      return (byte) 0;
    }
    if (type == short.class) {
      return (short) 0;
    }
    if (type == int.class) {
      return 0;
    }
    if (type == long.class) {
      return 0L;
    }
    if (type == float.class) {
      return 0F;
    }
    if (type == double.class) {
      return 0D;
    }
    throw new ConfigException("Unsupported primitive type " + type.getName() + ".");
  }

  private static void requireObject(ConfigNode node, Type type) {
    if (!node.isObject()) {
      throw new ConfigException("Expected object node for " + type.getTypeName() + " but found " + node.kind() + ".");
    }
  }

  private static void requireList(ConfigNode node, Type type) {
    if (!node.isList()) {
      throw new ConfigException("Expected list node for " + type.getTypeName() + " but found " + node.kind() + ".");
    }
  }

  private static void requireNullable(Class<?> type) {
    if (type.isPrimitive()) {
      throw new ConfigException("Primitive type " + type.getName() + " cannot be assigned null.");
    }
  }

  private static ConfigException expectedScalar(Class<?> targetType, ConfigNode node) {
    return new ConfigException("Expected scalar value for " + targetType.getTypeName() + " but found " + node.kind() + ".");
  }

  private static <T> T requireType(Object value, Class<T> expected, Class<?> declared) {
    if (!expected.isInstance(value)) {
      throw new ConfigException("Expected " + expected.getName() + " for " + declared.getName() + " but found " + value.getClass().getName() + ".");
    }
    return expected.cast(value);
  }

  private static String polymorphicIdentifier(Class<?> declaredType, Class<?> subtype) {
    var aliases = declaredType.getAnnotation(ConfigPolymorphicTypes.class);
    if (aliases == null) {
      return subtype.getName();
    }

    for (var alias : aliases.value()) {
      if (alias.type() == subtype) {
        return alias.alias().isBlank() ? subtype.getName() : alias.alias();
      }
    }
    return subtype.getName();
  }

  private static Class<?> polymorphicSubtype(Class<?> declaredType, String identifier) {
    var aliases = declaredType.getAnnotation(ConfigPolymorphicTypes.class);
    if (aliases != null) {
      for (var alias : aliases.value()) {
        var aliasValue = alias.alias().isBlank() ? alias.type().getName() : alias.alias();
        if (aliasValue.equals(identifier)) {
          return alias.type();
        }
      }
    }

    try {
      return Class.forName(identifier);
    } catch (ClassNotFoundException exception) {
      throw new ConfigException("Unknown polymorphic subtype " + identifier + " for " + declaredType.getName() + ".", exception);
    }
  }

  private final class Context implements ConfigSerializationContext {
    private final AnnotatedType annotatedType;
    private final int nesting;

    private Context(AnnotatedType annotatedType, int nesting) {
      this.annotatedType = annotatedType;
      this.nesting = nesting;
    }

    @Override
    public ConfigMapperOptions options() {
      return options;
    }

    @Override
    public AnnotatedType annotatedType() {
      return annotatedType;
    }

    @Override
    public int nesting() {
      return nesting;
    }

    @Override
    public ConfigNode encode(Object value, AnnotatedType type) {
      return encodeValue(value, type, null, nesting + 1);
    }

    @Override
    public Object decode(ConfigNode node, AnnotatedType type) {
      return decodeValue(node, type, null, nesting + 1);
    }
  }

  private sealed interface ConfigMember permits FieldMember, RecordMember {
    String name();

    AnnotatedType annotatedType();

    Annotation[] annotations();

    Class<?> declaringType();

    Object get(Object target);

    void set(Object target, Object value);

    default ConfigPath path(ConfigMapperOptions options) {
      var name = annotation(ConfigName.class);
      var path = name == null
        ? ConfigPath.parse(options.nameFormatter().format(name()))
        : ConfigPath.parse(name.value());

      if (!root()) {
        return path;
      }

      var prefix = declaringType().getAnnotation(ConfigPathPrefix.class);
      if (prefix == null || prefix.value().isBlank()) {
        return path;
      }

      var resolved = ConfigPath.parse(prefix.value());
      for (String segment : path.segments()) {
        resolved = resolved.isRoot() ? ConfigPath.of(segment) : resolved.child(segment);
      }
      return resolved;
    }

    boolean root();

    default <A extends Annotation> A annotation(Class<A> annotationType) {
      for (Annotation annotation : annotations()) {
        if (annotationType.isInstance(annotation)) {
          return annotationType.cast(annotation);
        }
      }
      return null;
    }

    static ConfigMember field(Field field, boolean root) {
      return new FieldMember(field, root);
    }

    static ConfigMember record(RecordComponent component, boolean root) {
      return new RecordMember(component, root);
    }
  }

  private record FieldMember(Field field, boolean root) implements ConfigMember {
    @Override
    public String name() {
      return field.getName();
    }

    @Override
    public AnnotatedType annotatedType() {
      return field.getAnnotatedType();
    }

    @Override
    public Annotation[] annotations() {
      return field.getAnnotations();
    }

    @Override
    public Class<?> declaringType() {
      return field.getDeclaringClass();
    }

    @Override
    public Object get(Object target) {
      try {
        field.setAccessible(true);
        return field.get(target);
      } catch (IllegalAccessException exception) {
        throw new ConfigException("Could not read field " + field + ".", exception);
      }
    }

    @Override
    public void set(Object target, Object value) {
      try {
        field.setAccessible(true);
        field.set(target, value);
      } catch (IllegalAccessException exception) {
        throw new ConfigException("Could not write field " + field + ".", exception);
      }
    }
  }

  private record RecordMember(RecordComponent component, boolean root) implements ConfigMember {
    @Override
    public String name() {
      return component.getName();
    }

    @Override
    public AnnotatedType annotatedType() {
      return component.getAnnotatedType();
    }

    @Override
    public Annotation[] annotations() {
      return component.getAnnotations();
    }

    @Override
    public Class<?> declaringType() {
      return component.getDeclaringRecord();
    }

    @Override
    public Object get(Object target) {
      try {
        Method accessor = component.getAccessor();
        accessor.setAccessible(true);
        return accessor.invoke(target);
      } catch (IllegalAccessException exception) {
        throw new ConfigException("Could not read record component " + component + ".", exception);
      } catch (InvocationTargetException exception) {
        throw new ConfigException("Record accessor for " + component + " failed.", exception.getCause());
      }
    }

    @Override
    public void set(Object target, Object value) {
      throw new ConfigException("Record components cannot be written into an existing instance.");
    }
  }
}
