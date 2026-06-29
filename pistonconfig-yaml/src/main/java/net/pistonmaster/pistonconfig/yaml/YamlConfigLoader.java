package net.pistonmaster.pistonconfig.yaml;

import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import net.pistonmaster.pistonconfig.core.ConfigCollectionStyle;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigLoader;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.ConfigScalarStyle;
import net.pistonmaster.pistonconfig.core.ConfigSourceLocation;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.CollectionNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * YAML reader and writer backed by SnakeYAML's comment-aware node API.
 */
public final class YamlConfigLoader implements ConfigLoader {
  @Override
  public ConfigDocument load(Reader reader) {
    var loaderOptions = new LoaderOptions();
    loaderOptions.setProcessComments(true);
    loaderOptions.setMergeOnCompose(true);

    try {
      var node = new Yaml(loaderOptions).compose(reader);
      if (node == null) {
        return ConfigDocument.empty();
      }

      return ConfigDocument.of(fromYaml(node));
    } catch (RuntimeException exception) {
      throw new ConfigException("Could not parse YAML configuration.", exception);
    }
  }

  @Override
  public void save(ConfigDocument document, Writer writer) {
    var dumperOptions = new DumperOptions();
    dumperOptions.setProcessComments(true);
    dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    dumperOptions.setIndent(2);
    dumperOptions.setPrettyFlow(true);

    try {
      new Yaml(dumperOptions).serialize(toYaml(document.root()), writer);
    } catch (RuntimeException exception) {
      throw new UncheckedIOException(new java.io.IOException("Could not write YAML configuration.", exception));
    }
  }

  private static ConfigNode fromYaml(Node yamlNode) {
    ConfigNode node = switch (yamlNode.getNodeId()) {
      case mapping -> fromMapping((MappingNode) yamlNode);
      case sequence -> fromSequence((SequenceNode) yamlNode);
      case scalar -> fromScalar((ScalarNode) yamlNode);
      case anchor -> throw new ConfigException("Unexpected standalone YAML anchor node.");
    };

    applyValueDecorations(node, yamlNode);
    return node;
  }

  private static ConfigNode fromMapping(MappingNode mappingNode) {
    var node = ConfigNode.object();
    for (NodeTuple tuple : mappingNode.getValue()) {
      if (!(tuple.getKeyNode() instanceof ScalarNode keyNode)) {
        throw new ConfigException("Only scalar YAML mapping keys can be represented as config paths.");
      }

      var child = fromYaml(tuple.getValueNode());
      applyKeyDecorations(child, keyNode);
      node.setNode(ConfigPath.of(keyNode.getValue()), child);
    }
    return node;
  }

  private static ConfigNode fromSequence(SequenceNode sequenceNode) {
    var node = ConfigNode.list();
    for (Node child : sequenceNode.getValue()) {
      node.addListNode(fromYaml(child));
    }
    return node;
  }

  private static ConfigNode fromScalar(ScalarNode scalarNode) {
    var tag = scalarNode.getTag();
    var rawValue = scalarNode.getValue();
    var normalized = rawValue.replace("_", "");

    ConfigNode node;
    if (Tag.NULL.equals(tag)) {
      node = ConfigNode.nullValue();
    } else if (Tag.BOOL.equals(tag)) {
      node = ConfigNode.scalar(Boolean.parseBoolean(rawValue));
    } else if (Tag.INT.equals(tag)) {
      node = parseInteger(normalized);
    } else if (Tag.FLOAT.equals(tag)) {
      node = ConfigNode.scalar(new BigDecimal(normalized));
    } else {
      node = ConfigNode.scalar(rawValue);
    }

    node.decorate(decorations -> decorations.withScalarStyle(scalarStyle(scalarNode.getScalarStyle())));
    node.setMetadata(YamlMetadataKeys.SCALAR_RAW, rawValue);
    return node;
  }

  private static ConfigNode parseInteger(String normalized) {
    try {
      return ConfigNode.scalar(Long.decode(normalized));
    } catch (NumberFormatException exception) {
      return ConfigNode.scalar(new BigInteger(normalized));
    }
  }

  private static Node toYaml(ConfigNode node) {
    Node yamlNode;
    if (node.isObject()) {
      var tuples = new ArrayList<NodeTuple>();
      for (var entry : node.objectChildren().entrySet()) {
        tuples.add(new NodeTuple(toYamlKey(entry.getKey(), entry.getValue()), toYaml(entry.getValue())));
      }
      yamlNode = new MappingNode(tag(node, Tag.MAP), tuples, flowStyle(node.decorations().collectionStyle()));
    } else if (node.isList()) {
      var children = new ArrayList<Node>();
      for (ConfigNode child : node.listChildren()) {
        children.add(toYaml(child));
      }
      yamlNode = new SequenceNode(tag(node, Tag.SEQ), children, flowStyle(node.decorations().collectionStyle()));
    } else {
      yamlNode = toYamlScalar(node);
    }

    applyComments(yamlNode, node.comment());
    yamlNode.setAnchor(node.decorations().attributes().get(YamlMetadataKeys.ANCHOR));
    return yamlNode;
  }

  private static Node toYamlKey(String key, ConfigNode valueNode) {
    var keyNode = new ScalarNode(
      new Tag(valueNode.decorations().attributes().getOrDefault(YamlMetadataKeys.KEY_TAG, Tag.STR.getValue())),
      key,
      null,
      null,
      scalarStyle(valueNode.decorations().keyStyle())
    );

    applyComments(keyNode, valueNode.decorations().keyComment());
    return keyNode;
  }

  private static Node toYamlScalar(ConfigNode node) {
    var value = node.rawValue();
    if (value == null) {
      return new ScalarNode(tag(node, Tag.NULL), "null", null, null, scalarStyle(node.decorations().scalarStyle()));
    }
    if (value instanceof Boolean booleanValue) {
      return new ScalarNode(tag(node, Tag.BOOL), booleanValue.toString(), null, null, scalarStyle(node.decorations().scalarStyle()));
    }
    if (value instanceof Number numberValue) {
      var tag = numberValue instanceof Float || numberValue instanceof Double || numberValue instanceof BigDecimal
        ? Tag.FLOAT
        : Tag.INT;
      var rendered = node.metadata(YamlMetadataKeys.SCALAR_RAW).map(Object::toString).orElse(numberValue.toString());
      return new ScalarNode(tag(node, tag), rendered, null, null, scalarStyle(node.decorations().scalarStyle()));
    }

    return new ScalarNode(tag(node, Tag.STR), value.toString(), null, null, scalarStyle(node.decorations().scalarStyle()));
  }

  private static void applyValueDecorations(ConfigNode node, Node yamlNode) {
    node.setComment(comment(yamlNode));
    node.decorate(decorations -> decorations.withValueLocation(location(yamlNode)));

    if (yamlNode.getTag() != null) {
      node.decorate(decorations -> decorations.withAttribute(YamlMetadataKeys.TAG, yamlNode.getTag().getValue()));
    }
    if (yamlNode.getAnchor() != null) {
      node.decorate(decorations -> decorations.withAttribute(YamlMetadataKeys.ANCHOR, yamlNode.getAnchor()));
    }
    if (yamlNode instanceof CollectionNode<?> collectionNode) {
      node.decorate(decorations -> decorations.withCollectionStyle(collectionStyle(collectionNode.getFlowStyle())));
    }
  }

  private static void applyKeyDecorations(ConfigNode node, ScalarNode keyNode) {
    node.decorate(decorations -> decorations
      .withKeyComment(comment(keyNode))
      .withKeyStyle(scalarStyle(keyNode.getScalarStyle()))
      .withKeyLocation(location(keyNode))
      .withAttribute(YamlMetadataKeys.KEY_TAG, keyNode.getTag().getValue()));
  }

  private static void applyComments(Node yamlNode, ConfigComment comment) {
    yamlNode.setBlockComments(commentLines(comment.leading()));
    yamlNode.setInLineComments(commentLines(comment.inline()));
    yamlNode.setEndComments(commentLines(comment.trailing()));
  }

  private static ConfigComment comment(Node yamlNode) {
    return new ConfigComment(
      comments(yamlNode.getBlockComments(), ConfigCommentType.BLOCK),
      comments(yamlNode.getInLineComments(), ConfigCommentType.INLINE),
      comments(yamlNode.getEndComments(), ConfigCommentType.BLOCK)
    );
  }

  private static List<ConfigCommentLine> comments(List<CommentLine> comments, ConfigCommentType fallbackType) {
    if (comments == null || comments.isEmpty()) {
      return List.of();
    }

    return comments.stream()
      .map(comment -> newCommentLine(comment, fallbackType))
      .toList();
  }

  private static ConfigCommentLine newCommentLine(CommentLine comment, ConfigCommentType fallbackType) {
    return ConfigCommentLine.builder()
      .text(comment.getValue().stripLeading())
      .type(comment.getCommentType() == CommentType.BLANK_LINE ? ConfigCommentType.BLANK : fallbackType)
      .marker(ConfigCommentMarker.HASH)
      .build();
  }

  private static List<CommentLine> commentLines(List<ConfigCommentLine> comments) {
    if (comments.isEmpty()) {
      return List.of();
    }

    return comments.stream()
      .map(comment -> new CommentLine(null, null, comment.text(), yamlCommentType(comment)))
      .toList();
  }

  private static CommentType yamlCommentType(ConfigCommentLine comment) {
    return switch (comment.type()) {
      case BLANK -> CommentType.BLANK_LINE;
      case INLINE -> CommentType.IN_LINE;
      case BLOCK -> CommentType.BLOCK;
    };
  }

  private static Tag tag(ConfigNode node, Tag fallback) {
    return node.decorations().attributes().containsKey(YamlMetadataKeys.TAG)
      ? new Tag(node.decorations().attributes().get(YamlMetadataKeys.TAG))
      : fallback;
  }

  private static ConfigScalarStyle scalarStyle(DumperOptions.ScalarStyle style) {
    if (style == null) {
      return ConfigScalarStyle.UNSPECIFIED;
    }

    return switch (style) {
      case PLAIN -> ConfigScalarStyle.PLAIN;
      case SINGLE_QUOTED -> ConfigScalarStyle.SINGLE_QUOTED;
      case DOUBLE_QUOTED, JSON_SCALAR_STYLE -> ConfigScalarStyle.DOUBLE_QUOTED;
      case LITERAL -> ConfigScalarStyle.LITERAL;
      case FOLDED -> ConfigScalarStyle.FOLDED;
    };
  }

  private static DumperOptions.ScalarStyle scalarStyle(ConfigScalarStyle style) {
    return switch (style) {
      case SINGLE_QUOTED -> DumperOptions.ScalarStyle.SINGLE_QUOTED;
      case DOUBLE_QUOTED -> DumperOptions.ScalarStyle.DOUBLE_QUOTED;
      case LITERAL -> DumperOptions.ScalarStyle.LITERAL;
      case FOLDED -> DumperOptions.ScalarStyle.FOLDED;
      default -> DumperOptions.ScalarStyle.PLAIN;
    };
  }

  private static ConfigCollectionStyle collectionStyle(DumperOptions.FlowStyle flowStyle) {
    if (flowStyle == null) {
      return ConfigCollectionStyle.UNSPECIFIED;
    }

    return switch (flowStyle) {
      case BLOCK -> ConfigCollectionStyle.BLOCK;
      case FLOW -> ConfigCollectionStyle.FLOW;
      case AUTO -> ConfigCollectionStyle.UNSPECIFIED;
    };
  }

  private static DumperOptions.FlowStyle flowStyle(ConfigCollectionStyle collectionStyle) {
    return switch (collectionStyle) {
      case FLOW, INLINE -> DumperOptions.FlowStyle.FLOW;
      case BLOCK -> DumperOptions.FlowStyle.BLOCK;
      default -> DumperOptions.FlowStyle.BLOCK;
    };
  }

  private static ConfigSourceLocation location(Node node) {
    var mark = node.getStartMark();
    if (mark == null) {
      return ConfigSourceLocation.unknown();
    }

    return ConfigSourceLocation.of(mark.getName(), mark.getLine(), mark.getColumn());
  }
}
