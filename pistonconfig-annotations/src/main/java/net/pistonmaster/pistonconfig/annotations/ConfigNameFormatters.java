package net.pistonmaster.pistonconfig.annotations;

import java.util.Locale;

/// Built-in configuration name formatters.
public final class ConfigNameFormatters {
  /// Keeps Java names unchanged.
  public static final ConfigNameFormatter IDENTITY = javaName -> javaName;

  /// Converts camelCase and PascalCase names to kebab-case.
  public static final ConfigNameFormatter KEBAB_CASE = javaName -> separate(javaName, '-');

  /// Converts camelCase and PascalCase names to snake_case.
  public static final ConfigNameFormatter SNAKE_CASE = javaName -> separate(javaName, '_');

  private ConfigNameFormatters() {
  }

  private static String separate(String javaName, char separator) {
    var result = new StringBuilder(javaName.length() + 4);
    for (int index = 0; index < javaName.length(); index++) {
      char character = javaName.charAt(index);
      if (Character.isUpperCase(character) && index > 0 && needsSeparator(javaName, index)) {
        result.append(separator);
      }
      result.append(Character.toLowerCase(character));
    }
    return result.toString().toLowerCase(Locale.ROOT);
  }

  private static boolean needsSeparator(String value, int index) {
    char previous = value.charAt(index - 1);
    if (previous == '-' || previous == '_') {
      return false;
    }
    if (Character.isLowerCase(previous) || Character.isDigit(previous)) {
      return true;
    }
    return index + 1 < value.length() && Character.isLowerCase(value.charAt(index + 1));
  }
}
