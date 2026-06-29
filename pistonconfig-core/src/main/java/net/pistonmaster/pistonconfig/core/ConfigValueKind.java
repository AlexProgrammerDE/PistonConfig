package net.pistonmaster.pistonconfig.core;

/// The structural kind stored by a [ConfigNode].
public enum ConfigValueKind {
  /// Object node containing named child nodes.
  OBJECT,
  /// List node containing ordered child nodes.
  LIST,
  /// Scalar node containing one raw Java value.
  SCALAR,
  /// Null node.
  NULL
}
