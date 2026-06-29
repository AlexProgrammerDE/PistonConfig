package net.pistonmaster.pistonconfig.staticfields;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigException;

/// Test-oriented validator for static config holder classes and definitions.
public class StaticConfigDefinitionValidator {
  /// Creates a validator with the default checks.
  public StaticConfigDefinitionValidator() {
  }

  /// Runs the default holder validations.
  ///
  /// @param holderTypes holder classes
  public void validate(Class<?>... holderTypes) {
    validate(Arrays.asList(holderTypes));
  }

  /// Runs the default holder validations.
  ///
  /// @param holderTypes holder classes
  public void validate(Iterable<Class<?>> holderTypes) {
    validateAllPropertiesAreStaticFinal(holderTypes);
    validateHolderClassesFinal(holderTypes);
    validateHolderClassesHaveHiddenNoArgConstructor(holderTypes);

    var definition = StaticConfigDefinition.from(holderTypes);
    validateHasCommentOnEveryProperty(definition, _ -> true);
    validateCommentLengthsAreWithinBounds(definition, null, 90);
    validateHasAllEnumEntriesInComment(definition, _ -> true);
    validateDefaultsRoundTrip(definition, new ConfigCodecRegistry());
  }

  /// Throws when any [ConfigProperty] field is not static final.
  ///
  /// @param holderTypes holder classes
  public void validateAllPropertiesAreStaticFinal(Iterable<Class<?>> holderTypes) {
    var invalidFields = new ArrayList<String>();
    for (Class<?> holderType : holderTypes) {
      for (Field field : fields(holderType)) {
        if (ConfigProperty.class.isAssignableFrom(field.getType()) && !isStaticFinal(field)) {
          invalidFields.add(field.getDeclaringClass().getSimpleName() + "#" + field.getName());
        }
      }
    }

    if (!invalidFields.isEmpty()) {
      throw new ConfigException("The following config properties are not static final:\n- " + String.join("\n- ", invalidFields));
    }
  }

  /// Throws when any holder class is not final.
  ///
  /// @param holderTypes holder classes
  public void validateHolderClassesFinal(Iterable<Class<?>> holderTypes) {
    var invalidTypes = new ArrayList<String>();
    for (Class<?> holderType : holderTypes) {
      if (!Modifier.isFinal(holderType.getModifiers())) {
        invalidTypes.add(holderType.getName());
      }
    }

    if (!invalidTypes.isEmpty()) {
      throw new ConfigException("The following config holders are not final:\n- " + String.join("\n- ", invalidTypes));
    }
  }

  /// Throws when any holder class lacks a single private no-args constructor.
  ///
  /// @param holderTypes holder classes
  public void validateHolderClassesHaveHiddenNoArgConstructor(Iterable<Class<?>> holderTypes) {
    var invalidTypes = new ArrayList<String>();
    for (Class<?> holderType : holderTypes) {
      if (!hasHiddenNoArgConstructor(holderType)) {
        invalidTypes.add(holderType.getName());
      }
    }

    if (!invalidTypes.isEmpty()) {
      throw new ConfigException("The following config holders do not have a single private no-args constructor:\n- "
        + String.join("\n- ", invalidTypes));
    }
  }

  /// Throws when any selected property has no generated comment.
  ///
  /// @param definition static definition
  /// @param propertyFilter property filter
  public void validateHasCommentOnEveryProperty(StaticConfigDefinition definition, Predicate<ConfigProperty<?>> propertyFilter) {
    Objects.requireNonNull(definition, "definition");
    var invalidProperties = new ArrayList<String>();
    for (ConfigProperty<?> property : definition.properties()) {
      if (propertyFilter.test(property) && property.comment().isEmpty()) {
        invalidProperties.add(property.path().toString());
      }
    }

    if (!invalidProperties.isEmpty()) {
      throw new ConfigException("The following config properties do not have a comment:\n- " + String.join("\n- ", invalidProperties));
    }
  }

  /// Throws when generated comment lines are outside the supplied bounds.
  ///
  /// @param definition static definition
  /// @param minLength minimum length, or `null`
  /// @param maxLength maximum length, or `null`
  public void validateCommentLengthsAreWithinBounds(StaticConfigDefinition definition, Integer minLength, Integer maxLength) {
    Objects.requireNonNull(definition, "definition");
    if (minLength == null && maxLength == null) {
      throw new IllegalArgumentException("minLength or maxLength must be non-null.");
    }

    var invalidPaths = new ArrayList<String>();
    for (ConfigProperty<?> property : definition.properties()) {
      if (hasInvalidLength(property.comment().leadingText(), minLength, maxLength)) {
        invalidPaths.add(property.path().toString());
      }
    }
    for (var entry : definition.comments().entrySet()) {
      if (hasInvalidLength(entry.getValue().leadingText(), minLength, maxLength)) {
        invalidPaths.add(entry.getKey().toString());
      }
    }
    if (hasInvalidLength(definition.rootComment().leadingText(), minLength, maxLength)
      || hasInvalidLength(definition.rootComment().trailingText(), minLength, maxLength)) {
      invalidPaths.add("<root>");
    }

    if (!invalidPaths.isEmpty()) {
      throw new ConfigException("The comments for the following paths are outside the configured length bounds:\n- "
        + String.join("\n- ", invalidPaths));
    }
  }

  /// Throws when enum property comments do not mention every enum constant.
  ///
  /// @param definition static definition
  /// @param propertyFilter property filter
  public void validateHasAllEnumEntriesInComment(StaticConfigDefinition definition, Predicate<ConfigProperty<?>> propertyFilter) {
    Objects.requireNonNull(definition, "definition");
    var invalidProperties = new ArrayList<String>();
    for (ConfigProperty<?> property : definition.properties()) {
      if (!propertyFilter.test(property)) {
        continue;
      }

      var constants = property.type().enumConstantNames();
      if (constants.isEmpty()) {
        continue;
      }

      var comments = String.join("\n", property.comment().leadingText());
      var missing = constants.stream()
        .filter(constant -> !comments.contains(constant))
        .toList();
      if (!missing.isEmpty()) {
        invalidProperties.add(property.path() + ": missing " + String.join(", ", missing));
      }
    }

    if (!invalidProperties.isEmpty()) {
      throw new ConfigException("The following enum property comments do not list every enum value:\n- "
        + String.join("\n- ", invalidProperties));
    }
  }

  /// Throws when any default cannot be encoded and decoded.
  ///
  /// @param definition static definition
  /// @param codecRegistry codec registry
  public void validateDefaultsRoundTrip(StaticConfigDefinition definition, ConfigCodecRegistry codecRegistry) {
    var defaults = definition.defaults(codecRegistry);
    for (ConfigProperty<?> property : definition.properties()) {
      definition.resolve(defaults, property, codecRegistry, StaticInvalidValuePolicy.STRICT);
    }
  }

  private static boolean isStaticFinal(Field field) {
    var modifiers = field.getModifiers();
    return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
  }

  private static boolean hasHiddenNoArgConstructor(Class<?> type) {
    Constructor<?>[] constructors = type.getDeclaredConstructors();
    return constructors.length == 1
      && constructors[0].getParameterCount() == 0
      && Modifier.isPrivate(constructors[0].getModifiers());
  }

  private static List<Field> fields(Class<?> holderType) {
    var hierarchy = new ArrayList<Class<?>>();
    var current = holderType;
    while (current != null && current != Object.class) {
      hierarchy.add(current);
      current = current.getSuperclass();
    }

    var fields = new ArrayList<Field>();
    for (Class<?> type : hierarchy) {
      fields.addAll(Arrays.asList(type.getDeclaredFields()));
    }
    return fields;
  }

  private static boolean hasInvalidLength(List<String> lines, Integer minLength, Integer maxLength) {
    return lines.stream().anyMatch(line ->
      (minLength != null && line.length() < minLength)
        || (maxLength != null && line.length() > maxLength));
  }
}
