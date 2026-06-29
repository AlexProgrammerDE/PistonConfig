package net.pistonmaster.pistonconfig.json;

import de.marhali.json5.Json5;
import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import de.marhali.json5.Json5Primitive;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Instant;
import java.util.Map;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigScalarStyle;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigLoader;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ImmutableConfigNodeDecorations;

/// JSON, JSONC, and JSON5 reader and writer backed by json5-java.
public final class JsonConfigLoader implements ConfigLoader {
  private final Json5 json5 = Json5.builder(builder -> builder
    .parseComments()
    .writeComments()
    .allowNaN()
    .allowInfinity()
    .allowBinaryLiterals()
    .allowOctalLiterals()
    .allowHexFloatingLiterals()
    .allowLongUnicodeEscapes()
    .trailingComma()
    .prettyPrinting()
    .insertFinalNewline()
    .build());

  /// Creates a JSON-family loader.
  public JsonConfigLoader() {
  }

  /// Loads a JSON-family document from a reader.
  ///
  /// @param reader source reader
  /// @return loaded document
  @Override
  public ConfigDocument load(Reader reader) {
    try {
      return ConfigDocument.of(fromJson5(json5.parse(reader)));
    } catch (RuntimeException exception) {
      throw new ConfigException("Could not parse JSON/JSONC configuration.", exception);
    }
  }

  /// Saves a document as formatted JSON5.
  ///
  /// @param document document to save
  /// @param writer destination writer
  @Override
  public void save(ConfigDocument document, Writer writer) {
    try {
      json5.serialize(toJson5(document.root()), writer);
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not write JSON/JSONC configuration.", exception);
    }
  }

  private static ConfigNode fromJson5(Json5Element element) {
    ConfigNode node;
    if (element == null || element.isJson5Null()) {
      node = ConfigNode.nullValue();
    } else if (element.isJson5Object()) {
      node = ConfigNode.object();
      for (Map.Entry<String, Json5Element> entry : element.getAsJson5Object().entrySet()) {
        node.setNode(net.pistonmaster.pistonconfig.core.ConfigPath.of(entry.getKey()), fromJson5(entry.getValue()));
      }
    } else if (element.isJson5Array()) {
      node = ConfigNode.list();
      for (Json5Element child : element.getAsJson5Array()) {
        node.addListNode(fromJson5(child));
      }
    } else {
      var primitive = element.getAsJson5Primitive();
      if (primitive.isBoolean()) {
        node = ConfigNode.scalar(primitive.getAsBoolean());
      } else if (primitive.isNumber()) {
        node = ConfigNode.scalar(primitive.getAsNumber());
        node.decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withScalarStyle(numberStyle(primitive.getNumberRadix())));
        node.setMetadata(JsonMetadataKeys.NUMBER_RADIX, primitive.getNumberRadix());
      } else if (primitive.isInstant()) {
        node = ConfigNode.scalar(primitive.getAsInstant());
        node.decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withScalarStyle(ConfigScalarStyle.TIMESTAMP));
      } else {
        node = ConfigNode.scalar(primitive.getAsString());
      }
    }

    if (element != null && element.hasComment()) {
      node.setComment(ConfigComment.builder()
        .addAllLeading(element.getComment().lines()
          .map(line -> ConfigCommentLine.builder()
            .text(line)
            .type(line.isEmpty() ? ConfigCommentType.BLANK : ConfigCommentType.BLOCK)
            .marker(line.isEmpty() ? ConfigCommentMarker.NONE : ConfigCommentMarker.DOUBLE_SLASH)
            .build())
          .toList())
        .build());
    }
    return node;
  }

  private static Json5Element toJson5(ConfigNode node) {
    Json5Element element;
    if (node.isObject()) {
      var object = new Json5Object();
      for (Map.Entry<String, ConfigNode> entry : node.objectChildren().entrySet()) {
        object.add(entry.getKey(), toJson5(entry.getValue()));
      }
      element = object;
    } else if (node.isList()) {
      var array = new Json5Array();
      for (ConfigNode child : node.listChildren()) {
        array.add(toJson5(child));
      }
      element = array;
    } else {
      element = toJson5Primitive(node);
    }

    if (!node.comment().isEmpty()) {
      element.setComment(String.join(System.lineSeparator(), node.comment().all().stream().map(net.pistonmaster.pistonconfig.core.ConfigCommentLine::text).toList()));
    }
    return element;
  }

  private static Json5Element toJson5Primitive(ConfigNode node) {
    var value = node.rawValue();
    if (value == null) {
      return Json5Primitive.fromNull();
    }
    if (value instanceof Boolean booleanValue) {
      return Json5Primitive.fromBoolean(booleanValue);
    }
    if (value instanceof Number numberValue) {
      var radix = node.metadata(JsonMetadataKeys.NUMBER_RADIX)
        .filter(Number.class::isInstance)
        .map(Number.class::cast)
        .map(Number::intValue)
        .orElse(radix(node.decorations().scalarStyle()));
      return Json5Primitive.fromNumber(numberValue, radix);
    }
    if (value instanceof Instant instantValue) {
      return Json5Primitive.fromInstant(instantValue);
    }
    if (value instanceof Character characterValue) {
      return Json5Primitive.fromCharacter(characterValue);
    }
    return Json5Primitive.fromString(value.toString());
  }

  private static ConfigScalarStyle numberStyle(int radix) {
    return switch (radix) {
      case 2 -> ConfigScalarStyle.BINARY;
      case 8 -> ConfigScalarStyle.OCTAL;
      case 16 -> ConfigScalarStyle.HEX;
      default -> ConfigScalarStyle.DECIMAL;
    };
  }

  private static int radix(ConfigScalarStyle style) {
    return switch (style) {
      case BINARY -> 2;
      case OCTAL -> 8;
      case HEX -> 16;
      default -> 10;
    };
  }
}
